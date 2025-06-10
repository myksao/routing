package africa.payaza.routing.core.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Value("${payaza.tenants_driver}")
    private String driver;

    @Value("${payaza.tenants_url}")
    private String jdbcUrl;

    @Value("${payaza.tenants_username}")
    private String username;

    @Value("${payaza.tenants_password}")
    private String password;

    @Bean("hikariTenantDataSource")
    public DataSource datasource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(this.driver);
        config.setJdbcUrl(this.jdbcUrl);
        config.setUsername(this.username);
        config.setPassword(this.password);
        config.setMinimumIdle(3);
        config.setMaximumPoolSize(10);
        config.setConnectionTestQuery("SELECT 1");
        config.setIdleTimeout(600000);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("preparedStatementCacheQueries", "250");
        config.addDataSourceProperty("preparedStatementCacheSizeMiB", "2");
        config.addDataSourceProperty("tcpKeepAlive", "true");
        config.addDataSourceProperty("logServerErrorDetail", "true");

        return new HikariDataSource(config);
    }
}
