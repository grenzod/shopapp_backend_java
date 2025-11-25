package com.project.shopapp.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String host;

    @Value("${spring.kafka.listener.concurrency}")
    private int listenerConcurrency;

    @Bean
    public KafkaAdmin admin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, host);
        return new KafkaAdmin(configs);
    }

    @Bean
    public KafkaAdmin.NewTopics topic1() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name("order-requests")
                        .partitions(listenerConcurrency)
                        .replicas(1)
                        .compact()
                        .build(),
                TopicBuilder.name("order-results")
                        .partitions(listenerConcurrency)
                        .replicas(1)
                        .compact()
                        .build()
        );
    }
}
