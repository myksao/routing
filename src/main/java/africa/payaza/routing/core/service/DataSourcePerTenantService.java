package africa.payaza.routing.core.service;


import africa.payaza.routing.core.config.JDBCDriverConfig;
import africa.payaza.routing.core.domain.PlatformTenant;
import africa.payaza.routing.core.domain.PlatformTenantConnection;
import africa.payaza.routing.security.service.TenantDetailImpl;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("dataSourcePerTenantService")
public class DataSourcePerTenantService implements RoutingDataSourceService, ApplicationListener<ContextRefreshedEvent> {

    private final Map<String, DataSource> TENANT_TO_DATA_SOURCE_MAP = new HashMap<>(1);
    private final DataSource tenantDataSource;
    private final TenantDetailImpl detail;


    @Autowired
    private JDBCDriverConfig driverConfig;

    @Autowired
    public DataSourcePerTenantService(final  @Qualifier("hikariTenantDataSource") DataSource tenantDataSource,
                                      final TenantDetailImpl tenantDetail) {
        this.tenantDataSource = tenantDataSource;
        this.detail = tenantDetail;
    }

    @Override
    public DataSource retrieveDataSource() {
        DataSource tenantDataSource = this.tenantDataSource;

        final PlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        if (tenant != null) {
            final PlatformTenantConnection tenantConnection = tenant.getConnection();

            synchronized (this.TENANT_TO_DATA_SOURCE_MAP) {
                // if tenantConnection information available switch to the
                // appropriate datasource for that routing.
                DataSource dataSource = this.TENANT_TO_DATA_SOURCE_MAP.get(tenantConnection.getId());
                if (dataSource != null) {
                    tenantDataSource = dataSource;
                } else {
                    tenantDataSource = createNewDataSourceFor(tenantConnection);
                    this.TENANT_TO_DATA_SOURCE_MAP.put(tenantConnection.getId(), tenantDataSource);
                }
            }
        }

        return tenantDataSource;
    }

    private DataSource createNewDataSourceFor(final PlatformTenantConnection connection) {
        String jdbcUrl = this.driverConfig.constructProtocol(connection.getSchemaServer(), connection.getSchemaPort(), connection.getSchemaName());

        HikariConfig config = getConfig(connection, jdbcUrl);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("preparedStatementCacheQueries", "250");
        config.addDataSourceProperty("preparedStatementCacheSizeMiB", "2");
        config.addDataSourceProperty("tcpKeepAlive", "true");
        config.addDataSourceProperty("logServerErrorDetail", "true");

        config.addDataSourceProperty("useSSL", "false");

        return new HikariDataSource(config);
    }

    private HikariConfig getConfig(PlatformTenantConnection connection, String jdbcUrl) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(this.driverConfig.getDriverClassName());
        config.setPoolName(connection.getSchemaName() + "_pool");
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(connection.getSchemaUsername());
        config.setPassword(connection.getSchemaPassword());
        config.setMinimumIdle(connection.getPoolMinimumIdle());
        config.setMaximumPoolSize(connection.getPoolMaximumPoolSize());
        config.setConnectionTestQuery("SELECT 1");
        config.setAutoCommit(true);

        // https://github.com/brettwooldridge/HikariCP/wiki/MBean-(JMX)-Monitoring-and-Management
        config.setRegisterMbeans(true);
        return config;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        final List<PlatformTenant> allTenants = detail.fetchAllTenants();
        for (final PlatformTenant tenant : allTenants) {
            initializeDataSourceConnection(tenant);
        }
    }

    private void initializeDataSourceConnection(PlatformTenant tenant) {
        log.debug("Initializing database connection for {}", tenant.getName());
        final PlatformTenantConnection tenantConnection = tenant.getConnection();
        String tenantConnectionKey = tenantConnection.getId();
        TENANT_TO_DATA_SOURCE_MAP.computeIfAbsent(tenantConnectionKey, (key) -> {
            DataSource tenantSpecificDataSource =createNewDataSourceFor(tenantConnection);
            try (Connection connection = tenantSpecificDataSource.getConnection()) {
                String url = connection.getMetaData().getURL();
                log.debug("Established database connection with URL {}", url);
            } catch (SQLException e) {
                log.error("Error while initializing database connection for {}", tenant.getName(), e);
            }
            return tenantSpecificDataSource;
        });
        log.debug("Database connection for {} initialized", tenant.getName());

    }
}
