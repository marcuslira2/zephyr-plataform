package com.kafka.demo.spring.kafka.consumer.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PersistenceBatch<T> {

    private List<T> list = new ArrayList<>();

    private final EntityManagerFactory entityManagerFactory;

    public PersistenceBatch(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void add(T obj) {
        list.add(obj);
    }

    public void saveAll() {
        StatelessSession session = null;

        try {
            SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
            session = sessionFactory.openStatelessSession();
            EntityTransaction transaction = session.getTransaction();
            transaction.begin();
            for (T obj : list) {
                session.insert(obj);
            }
            transaction.commit();

        } catch (Exception e) {
            log.error("Erro ao salvar o arquivo: {}", e.getMessage());
        } finally {
            list.clear();
            if (session != null) {
                session.close();
            }
        }
    }
}
