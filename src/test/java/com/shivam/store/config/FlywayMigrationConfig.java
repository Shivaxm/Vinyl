package com.shivam.store.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@TestConfiguration
public class FlywayMigrationConfig {

    @Bean
    public Flyway flyway(DataSource dataSource, Environment environment) {
        String locationsProp = environment.getProperty("spring.flyway.locations", "classpath:db/migration");
        String[] locations = Arrays.stream(locationsProp.split(","))
                .map(String::trim)
                .filter(location -> !location.isEmpty())
                .toArray(String[]::new);

        boolean cleanDisabled = environment.getProperty("spring.flyway.clean-disabled", Boolean.class, Boolean.FALSE);
        boolean baselineOnMigrate = environment.getProperty("spring.flyway.baseline-on-migrate", Boolean.class, Boolean.FALSE);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("uuid_fn", environment.getProperty("spring.flyway.placeholders.uuid_fn", "gen_random_uuid()"));

        return Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                .cleanDisabled(cleanDisabled)
                .baselineOnMigrate(baselineOnMigrate)
                .placeholders(placeholders)
                .load();
    }

    @Bean
    public InitializingBean flywayInitializer(Flyway flyway) {
        return () -> {
            flyway.clean();
            flyway.migrate();
        };
    }
}
