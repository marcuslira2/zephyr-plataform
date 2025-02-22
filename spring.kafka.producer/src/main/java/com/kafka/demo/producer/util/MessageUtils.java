package com.kafka.demo.producer.util;

import com.kafka.demo.producer.model.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class MessageUtils {

    private MessageUtils() {
    }

    public static byte[] compressGzip(String data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
             gzip.write(data.getBytes());
             gzip.finish();
             return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao comprimir JSON", e);
        }
    }

    public static Map<String, Object> toStructuredMessage(Message product) {
        Map<String, Object> message = new HashMap<>();

        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "struct");
        schema.put("fields", new Object[]{
                Map.of("field", "identifier", "type", "string"),
                Map.of("field", "name", "type", "string"),
                Map.of("field", "amount", "type", "int64"),
                Map.of("field", "value", "type", "double")
        });

        Map<String, Object> payload = new HashMap<>();
        payload.put("identifier", product.identifier());
        payload.put("name", product.name());
        payload.put("amount", product.amount());
        payload.put("value", product.value());

        message.put("schema", schema);
        message.put("payload", payload);
        return message;
    }

    public static MessageUtils createMessageUtils() {
        return new MessageUtils();
    }
}
