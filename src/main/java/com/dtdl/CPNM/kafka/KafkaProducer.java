package com.dtdl.CPNM.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
@Component
public class KafkaProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducer.class);
    @Value("${spring.kafka.template.default-topic}")
    private String TOPIC_NAME;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public void send(String payload) {
        try {
            LOGGER.info("sending payload = '{}' to topic = '{}'", payload, TOPIC_NAME);
            kafkaTemplate.sendDefault(payload);
            kafkaTemplate.flush();
        } catch (Exception e) {
            LOGGER.info("Failed to publish message to kafka");
            e.printStackTrace();
        }
    }
}
