package africa.payaza.routing.security.service;

import africa.payaza.routing.core.domain.PlatformOrganization;
import africa.payaza.routing.core.domain.PlatformTenant;
import africa.payaza.routing.core.domain.PlatformTenantConnection;
import africa.payaza.routing.security.exception.InvalidTenantIdentifierException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class TenantDetailImpl implements TenantDetail{

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TenantDetailImpl(@Qualifier("hikariTenantDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    private static final class TenantMapper implements RowMapper<PlatformTenant> {

        private final StringBuilder sqlBuilder;

        public TenantMapper() {
            this.sqlBuilder = buildSql();
        }

        public String schema() {
            return this.sqlBuilder.toString();
        }

        private StringBuilder buildSql(){
            StringBuilder sql = new StringBuilder();
            sql.append("tnt.id, tnt.name, tnt.identifier, tnt.name, tnt.timezone, ");
            sql.append("tnc.id as connection_id, tnc.schema_server, tnc.schema_port, tnc.schema_name, tnc.schema_username, tnc.schema_password, tnc.pool_size, tnc.pool_minimum_idle, tnc.pool_maximum_pool_size, ");
            sql.append("torg.id as organization_id, torg.bank_code, torg.name, torg.type, torg.country_code, torg.logo_url ");
            sql.append("from tenant tnt ");
            sql.append("inner join tenant_connection tnc on tnt.connection_id = tnc.id ");
            sql.append("inner join tenant_organization torg on tnt.id = torg.tenant_id ");
            return sql;
        }

        @Override
        public PlatformTenant mapRow(ResultSet rs, int rowNum) throws SQLException {

            return PlatformTenant.builder()
                    .id(rs.getString("id"))
                    .tenantIdentifier(rs.getString("identifier"))
                    .name(rs.getString("name"))
                    .timezone(rs.getString("timezone"))
                    .connection(PlatformTenantConnection.builder()
                            .id(rs.getString("connection_id"))
                            .schemaServer(rs.getString("schema_server"))
                            .schemaPort(rs.getString("schema_port"))
                            .schemaName(rs.getString("schema_name"))
                            .schemaUsername(rs.getString("schema_username"))
                            .schemaPassword(rs.getString("schema_password"))
                            .poolSize(rs.getInt("pool_size"))
                            .poolMinimumIdle(rs.getInt("pool_minimum_idle"))
                            .poolMaximumPoolSize(rs.getInt("pool_maximum_pool_size"))
                            .build())
                    .organization(
                        PlatformOrganization.builder()
                            .id(rs.getString("organization_id"))
                            .bankCode(rs.getString("bank_code"))
                            .name(rs.getString("name"))
                            .type(rs.getString("type"))
                            .countryCode(rs.getString("country_code"))
                            .logoUrl(rs.getString("logo_url"))
                            .build())
                    .build();
        }

    }

    @Override
    @Cacheable(value = "fetchTenant", key = "#identifier")
    public PlatformTenant fetchTenant(String identifier) {
        try {
            final TenantMapper rm = new TenantMapper();
            final String sql = "select " + rm.schema() + " where tnt.identifier = ?";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { identifier });
        } catch (final EmptyResultDataAccessException e) {
            throw new InvalidTenantIdentifierException("The tenant identifier: " + identifier + " is not valid.", e);
        }
    }

    @Override
    public List<PlatformTenant> fetchAllTenants() {
        final TenantMapper rm = new TenantMapper();
        final String sql = "select " + rm.schema() + " order by tnt.name";

        try {
            return this.jdbcTemplate.query(sql, rm);
        } catch (final EmptyResultDataAccessException e) {
            return List.of();
        }
    }


}
