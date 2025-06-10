package africa.payaza.routing.core.config;

import africa.payaza.routing.core.service.RoutingDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;


@Configuration
public class JpaConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        return new RoutingDataSource();
    }
}