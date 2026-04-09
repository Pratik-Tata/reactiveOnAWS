package com.reactive.demo.item;

import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ItemRepository {

	Flux<Item> findAll();

	Mono<Item> findById(String id);

	Mono<Item> save(Item item);

	Mono<Boolean> deleteById(String id);

	List<Item> snapshot();
}
