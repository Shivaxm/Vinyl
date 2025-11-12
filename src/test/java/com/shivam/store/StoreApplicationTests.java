package com.shivam.store;

import com.shivam.store.config.FlywayMigrationConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = FlywayMigrationConfig.class)
class StoreApplicationTests {

	@Test
	void contextLoads() {
	}

}
