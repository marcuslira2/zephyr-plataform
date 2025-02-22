package com.kafka.demo.producer.model;

public record Message(
        String identifier,
        String name,
        long amount,
        double value
) {
}
