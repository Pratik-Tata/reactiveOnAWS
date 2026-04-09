package com.reactive.demo.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seed a couple of rows so the CRUD and stream endpoints are immediately useful.
 */
@Configuration
public class DataLoader {

	private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

	@Bean
	CommandLineRunner loadSampleData(ItemService itemService) {
		return args -> itemService.create(new Item(null, "Notebook", "Used to write reactive ideas"))
				.then(itemService.create(new Item(null, "Keyboard", "Used to call the API")))
				.doOnSuccess(ignored -> log.debug("Sample items loaded"))
				.block();
	}
}
