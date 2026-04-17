package com.payment.gateway.config;

import com.payment.gateway.dto.TransactionEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig; // Add this
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.StringDeserializer; // Add this
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka; // Add this
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory; // Add this
import org.springframework.kafka.core.*; // This covers ProducerFactory and ConsumerFactory
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {
	
	@Bean
	public ConsumerFactory<String, TransactionEvent> consumerFactory() {
	    Map<String, Object> props = new HashMap<>();
	    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
	    // Change this ID to a new one (like v2) to force Kafka to read from the start
	    props.put(ConsumerConfig.GROUP_ID_CONFIG, "gateway-test-consumer-3");
	    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
	    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
	    
	    // Crucial for JSON: tells the consumer how to handle the TransactionEvent class
	    JsonDeserializer<TransactionEvent> jsonDeserializer = new JsonDeserializer<>(TransactionEvent.class);
	    jsonDeserializer.addTrustedPackages("*");
	    jsonDeserializer.setUseTypeHeaders(false); // Matches your producer setting

	    return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, TransactionEvent> kafkaListenerContainerFactory() {
		System.out.println("DEBUG: Building the Kafka Listener Factory!");
	    ConcurrentKafkaListenerContainerFactory<String, TransactionEvent> factory = 
	        new ConcurrentKafkaListenerContainerFactory<>();
	    factory.setConsumerFactory(consumerFactory());
	    factory.setAutoStartup(true);
	    return factory;
	}
	@Bean
    public ProducerFactory<String, TransactionEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        
        
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        
        configProps.put(JsonSerializer.TYPE_MAPPINGS, "transaction:com.payment.gateway.dto.TransactionEvent");
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, TransactionEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}