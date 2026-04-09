package com.reactive.demo.item;

import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

class ItemServiceTest {

	@Test
	void streamShouldRespectRequestedDemand() {
		ItemService itemService = new ItemService(new InMemoryItemRepository());
		itemService.create(new Item(null, "Book", "Backpressure demo")).block();

		StepVerifier.create(itemService.streamWithBackpressure(), 0)
				.thenRequest(1)
				.expectNextCount(1)
				.thenRequest(2)
				.expectNextCount(2)
				.thenCancel()
				.verify();
	}
}
