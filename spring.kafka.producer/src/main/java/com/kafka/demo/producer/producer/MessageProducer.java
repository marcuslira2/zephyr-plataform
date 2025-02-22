package com.kafka.demo.producer.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.demo.producer.model.Message;
import com.kafka.demo.producer.util.MessageUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class MessageProducer {

    @Value("${topic.send.csv.consumer}")
    private String sendConsumerTopic;

    @Value("${topic.send.csv.kafka}")
    private String sendKafkaTopic;

    @Value("${batch.size}")
    private static final Integer BATCH_SIZE = 15000;

    private final ObjectMapper objectMapper;

    private final KafkaTemplate<String, byte[]> kafkaTemplateByte;
    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplateStruct;


    public MessageProducer(ObjectMapper objectMapper, KafkaTemplate<String, byte[]> kafkaTemplate, KafkaTemplate<String, Map<String, Object>> kafkaTemplateStruct) {
        this.objectMapper = objectMapper;
        this.kafkaTemplateByte = kafkaTemplate;
        this.kafkaTemplateStruct = kafkaTemplateStruct;
    }

    public void sendCsvToKafka(Path csvPath) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                sendProduct(convertCsvLineToStruct(line));
            }
        }
    }

    public void sendCsvToConsumer(Path csvPath) {
        List<Map<String, String>> batch = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                batch.add(convertCsvLineToJson(line));

                if (batch.size() >= BATCH_SIZE) {
                    sendBatch(sendConsumerTopic, batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                sendBatch(sendConsumerTopic, batch);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Message convertCsvLineToStruct(String line) {
        String[] fields = line.split(";");
        return new Message(
                fields[0].trim(),
                fields[1].trim(),
                Long.parseLong(fields[2].trim()),
                Double.parseDouble(fields[3].trim()));
    }

    private Map<String, String> convertCsvLineToJson(String line) {
        String[] fields = line.split(";");

        if (fields.length < 4) {
            return Collections.emptyMap();
        }

        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("identifier", fields[0].trim());
        jsonMap.put("name", fields[1].trim());
        jsonMap.put("amount", fields[2].trim());
        jsonMap.put("value", fields[3].trim());

        return jsonMap;
    }

    public void sendProduct(Message product) {
        kafkaTemplateStruct.send(sendKafkaTopic, MessageUtils.toStructuredMessage(product));
    }

    private void sendBatch(String topic, List<Map<String, String>> batch) {
        try {
            String jsonBatch = objectMapper.writeValueAsString(batch);
            byte[] compressedData = MessageUtils.compressGzip(jsonBatch);
            kafkaTemplateByte.send(topic, compressedData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


}

