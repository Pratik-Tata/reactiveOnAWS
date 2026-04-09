package com.reactive.demo.item;

/**
 * Simple DTO used for request/response payloads.
 * We keep it small so the reactive flow is easy to focus on.
 */
public record Item(
		String id,
		String name,
		String description
) {
}
