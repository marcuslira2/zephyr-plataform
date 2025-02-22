package com.kafka.demo.producer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    private final KafkaProperties kafkaProperties;

    @Value("${topic.send.csv.consumer}")
    private String sendCSVConsumerTopic;

    @Value("${topic.send.csv.kafka}")
    private String sendCSVKafkaTopic;

    public KafkaProducerConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ProducerFactory<String, String> producerFactoryString() {
        Map<String, Object> properties = new HashMap<>(kafkaProperties.buildProducerProperties());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public ProducerFactory<String, byte[]> producerFactoryByte() {
        Map<String, Object> properties = new HashMap<>(kafkaProperties.buildProducerProperties());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public ProducerFactory<String, Map<String, Object>> producerFactoryStruct() {
        Map<String, Object> properties = new HashMap<>(kafkaProperties.buildProducerProperties());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplateString() {
        return new KafkaTemplate<>(producerFactoryString());
    }

    @Bean
    public KafkaTemplate<String, byte[]> kafkaTemplateByte() {
        return new KafkaTemplate<>(producerFactoryByte());
    }

    @Bean
    public KafkaTemplate<String, Map<String, Object>> kafkaTemplateStruct() {
        return new KafkaTemplate<>(producerFactoryStruct());
    }

    @Bean
    public NewTopic sendCSVTopicBuilder() {
        return TopicBuilder.name(sendCSVConsumerTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic sendKafkaTopicBuilder() {
        return TopicBuilder.name(sendCSVKafkaTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

}
