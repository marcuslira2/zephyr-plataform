package com.kafka.demo.spring.kafka.consumer.repository;

import com.kafka.demo.spring.kafka.consumer.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
}
