package com.example.demo.kafka.Consumer;

import com.example.demo.model.CrowdDensity;
import com.example.demo.repository.CrowdDensityRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class KafkaConsumerService {

    private final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CrowdDensityRepo crowdDensityRepository; // Injected properly

    public KafkaConsumerService(SimpMessagingTemplate messagingTemplate, CrowdDensityRepo crowdDensityRepository) {
        this.crowdDensityRepository = crowdDensityRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "lta-mrt-density", groupId = "mrt_id")
    public void consume(String message) {
        try {
            // Log received message
            logger.info("Received MRT Crowd Density Data: " + message);

            // Send data to Next.js via WebSockets
            messagingTemplate.convertAndSend("/topic/crowdDensity", message);

            // Convert JSON String to CrowdDensity object
            CrowdDensity crowdDensity = objectMapper.readValue(message, CrowdDensity.class);

            // Save to MongoDB
            crowdDensityRepository.save(crowdDensity);
            logger.info("Saved to MongoDB: " + crowdDensity);

        } catch (Exception e) {
            logger.error("Error processing Kafka message: " + e.getMessage());
        }
    }
}