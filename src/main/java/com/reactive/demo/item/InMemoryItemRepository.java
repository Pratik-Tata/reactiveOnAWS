package com.reactive.demo.item;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * In-memory storage keeps the sample minimal.
 * We still return Mono/Flux because the service/controller should speak
 * in reactive types even when the backing store is simple.
 */
@Repository
public class InMemoryItemRepository implements ItemRepository {

	private static final Logger log = LoggerFactory.getLogger(InMemoryItemRepository.class);

	private final Map<String, Item> store = new ConcurrentHashMap<>();

	@Override
	public Flux<Item> findAll() {
		return Flux.defer(() -> {
			log.debug("Repository findAll called. Current size={}", store.size());
			return Flux.fromIterable(store.values());
		});
	}

	@Override
	public Mono<Item> findById(String id) {
		return Mono.defer(() -> {
			log.debug("Repository findById called for id={}", id);
			return Mono.justOrEmpty(store.get(id));
		});
	}

	@Override
	public Mono<Item> save(Item item) {
		return Mono.fromSupplier(() -> {
			log.debug("Repository save called for id={}", item.id());
			store.put(item.id(), item);
			return item;
		});
	}

	@Override
	public Mono<Boolean> deleteById(String id) {
		return Mono.fromSupplier(() -> {
			log.debug("Repository deleteById called for id={}", id);
			return store.remove(id) != null;
		});
	}

	@Override
	public List<Item> snapshot() {
		return List.copyOf(store.values());
	}
}
