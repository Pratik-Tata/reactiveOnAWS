package com.reactive.demo.item;

/**
 * Extra wrapper used by the streaming endpoint so we can see
 * which element number was pushed to the client.
 */
public record ItemEvent(
		long sequence,
		Item item,
		String note
) {
}
