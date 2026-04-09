package com.reactive.demo.item;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

@Service
public class ItemService {

	private static final Logger log = LoggerFactory.getLogger(ItemService.class);

	private final ItemRepository itemRepository;

	public ItemService(ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}

	public Flux<Item> getAll() {
		return itemRepository.findAll()
				.delayElements(Duration.ofMillis(150))
				.doOnSubscribe(subscription -> log.debug("Subscribed to getAll stream"))
				.doOnNext(item -> log.debug("Emitting item from getAll: {}", item))
				.doOnComplete(() -> log.debug("getAll stream completed"));
	}

	public Mono<Item> getById(String id) {
		return itemRepository.findById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found: " + id)))
				.doOnNext(item -> log.debug("Found item: {}", item));
	}

	public Mono<Item> create(Item item) {
		Item itemToSave = new Item(UUID.randomUUID().toString(), item.name(), item.description());
		return itemRepository.save(itemToSave)
				.doOnNext(saved -> log.debug("Created item: {}", saved));
	}

	public Mono<Item> update(String id, Item item) {
		return getById(id)
				.flatMap(existing -> itemRepository.save(new Item(existing.id(), item.name(), item.description())))
				.doOnNext(updated -> log.debug("Updated item: {}", updated));
	}

	public Mono<Void> delete(String id) {
		return itemRepository.deleteById(id)
				.flatMap(deleted -> deleted
						? Mono.<Void>empty()
						: Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found: " + id)))
				.doOnSuccess(ignored -> log.debug("Deleted item with id={}", id));
	}

	public Flux<ItemEvent> streamWithBackpressure() {
		AtomicLong sequence = new AtomicLong();

		return Flux.<ItemEvent>create(sink -> sink.onRequest(requested -> emitRequestedItems(sink, sequence, requested)),
						FluxSink.OverflowStrategy.ERROR)
				.doOnSubscribe(subscription -> log.debug("Subscriber connected to backpressure stream"))
				.doOnRequest(requested -> log.debug("Client requested {} item(s) from stream", requested))
				.doOnNext(event -> log.debug("Streaming event {}", event))
				.doOnCancel(() -> log.debug("Client cancelled backpressure stream"));
	}

	private void emitRequestedItems(FluxSink<ItemEvent> sink, AtomicLong sequence, long requested) {
		if (requested <= 0) {
			return;
		}

		// We read a snapshot of the current items and emit only as many as the
		// downstream subscriber asked for. That is the core backpressure idea.
		List<Item> snapshot = itemRepository.snapshot();

		if (snapshot.isEmpty()) {
			sink.next(new ItemEvent(sequence.incrementAndGet(), null, "No items yet. Create one and request again."));
			return;
		}

		for (long index = 0; index < requested && !sink.isCancelled(); index++) {
			Item item = snapshot.get((int) (index % snapshot.size()));
			sink.next(new ItemEvent(sequence.incrementAndGet(), item, "Sent only because the client requested data."));
		}
	}
}
