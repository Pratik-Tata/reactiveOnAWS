package com.reactive.demo.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/items")
public class ItemController {

	private static final Logger log = LoggerFactory.getLogger(ItemController.class);
	private String message;

	private final ItemService itemService;

	public ItemController(ItemService itemService, @Value("${app.message}") String message) {
		this.itemService = itemService;
		this.message = message;
	}

	@GetMapping
	public Flux<Item> getAll() {
		log.debug("HTTP GET /api/items");
		return itemService.getAll();
	}

	@GetMapping("/{id}")
	public Mono<Item> getById(@PathVariable String id) {
		log.debug("HTTP GET /api/items/{}", id);
		return itemService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Item> create(@RequestBody Item item) {
		log.debug("HTTP POST /api/items body={}", item);
		return itemService.create(item);
	}

	@PutMapping("/{id}")
	public Mono<Item> update(@PathVariable String id, @RequestBody Item item) {
		log.debug("HTTP PUT /api/items/{} body={}", id, item);
		return itemService.update(id, item);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public Mono<Void> delete(@PathVariable String id) {
		log.debug("HTTP DELETE /api/items/{}", id);
		return itemService.delete(id);
	}

	@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ItemEvent> stream() {
		log.debug("HTTP GET /api/items/stream");
		return itemService.streamWithBackpressure();
	}

	@GetMapping("/message")
	public Mono<String> message(){
		return Mono.just(this.message);
	}
}
