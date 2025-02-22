package com.kafka.demo.spring.kafka.consumer.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.demo.spring.kafka.consumer.model.Product;
import com.kafka.demo.spring.kafka.consumer.util.PersistenceBatch;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

@Service
public class CSVRequestConsumer {

    private static final Logger log = LoggerFactory.getLogger(CSVRequestConsumer.class);

    private final ObjectMapper objectMapper;

    private final PersistenceBatch<Product> persistenceBatch;

    public CSVRequestConsumer(ObjectMapper objectMapper, PersistenceBatch<Product> persistenceBatch) {
        this.objectMapper = objectMapper;
        this.persistenceBatch = persistenceBatch;
    }

    @KafkaListener(topics = "${topic.send.csv.consumer}", groupId = "csv-request-consumer-1")
    @Transactional
    public void consume(ConsumerRecord<String, byte[]> data) {
        try {
            log.info("Request recebida no tópico: {}, Partition: {}, Offset: {}",
                    data.topic(),
                    data.partition(),
                    data.offset());

            byte[] compressedData = data.value();
            String json = decompressGzip(compressedData);

            List<Map<String, String>> batch = objectMapper.readValue(json, new TypeReference<>() {
            });

            persistInBatches(batch);

        } catch (IOException e) {
            log.error("Erro ao processar a mensagem: {}", e.getMessage());
        }
    }

    private void persistInBatches(List<Map<String, String>> batch) {

        for (Map<String, String> dataMap : batch) {
            Product product = new Product();
            product.setIdentifier(dataMap.get("identifier"));
            product.setName(dataMap.get("name"));
            product.setAmount(Long.parseLong(dataMap.get("amount")));
            product.setValue(new BigDecimal(dataMap.get("value")));
            persistenceBatch.add(product);
        }
        persistenceBatch.saveAll();

        log.info("Lote salvo com sucesso!");

    }


    private String decompressGzip(byte[] compressedData) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
             GZIPInputStream gzip = new GZIPInputStream(byteArrayInputStream);
             InputStreamReader reader = new InputStreamReader(gzip, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                output.append(line);
            }
            return output.toString();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao descomprimir JSON: " + e.getMessage(), e);
        }
    }
}
