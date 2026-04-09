package com.reactive.demo.item;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.springframework.test.web.reactive.server.WebTestClient;

class ItemControllerTest {

	private final WebTestClient webTestClient =
			WebTestClient.bindToController(new ItemController(new ItemService(new InMemoryItemRepository()),""))
					.build();

	@Test
	void shouldCreateAndFetchItem() {
		Item created = webTestClient.post()
				.uri("/api/items")
				.bodyValue(new Item(null, "Mouse", "Reactive test item"))
				.exchange()
				.expectStatus().isCreated()
				.expectBody(Item.class)
				.returnResult()
				.getResponseBody();

		assertNotNull(created);

		webTestClient.get()
				.uri("/api/items/{id}", created.id())
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.name").isEqualTo("Mouse")
				.jsonPath("$.description").isEqualTo("Reactive test item");
	}
}
