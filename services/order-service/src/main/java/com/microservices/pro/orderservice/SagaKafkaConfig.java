package com.microservices.pro.orderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

/**
 * SagaKafkaConfig — Session 12.
 *
 * Provides a dedicated KafkaListenerContainerFactory for the "saga-results"
 * topic. The three @KafkaListener methods on OrderSagaOrchestrator all
 * reference containerFactory = "sagaResultsListenerFactory" — without this
 * bean, Spring cannot construct their listeners and the context fails to start.
 *
 * Why a separate factory? The default factory uses String-typed values (or
 * the global JsonDeserializer). The Orchestrator needs to receive different
 * result types on the same topic ("saga-results") and dispatch them by type.
 * Using a dedicated factory with trusted packages set explicitly is the
 * safest way to do this without polluting the global consumer configuration.
 *
 * Common Issues note (from Session 12 docx): if you see
 * "No bean named 'sagaResultsListenerFactory'" at startup, this class is
 * missing from the classpath or not being picked up as a @Configuration.
 */
@Configuration
public class SagaKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, Object> sagaResultsConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
                JsonDeserializer.TRUSTED_PACKAGES, "com.microservices.pro.*",
                JsonDeserializer.USE_TYPE_INFO_HEADERS, false,
                JsonDeserializer.VALUE_DEFAULT_TYPE, Object.class.getName()
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> sagaResultsListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(sagaResultsConsumerFactory());
        return factory;
    }
}
