package com.example.demo.kafka.Consumer;

import com.example.demo.model.CrowdDensity;
import com.example.demo.repository.CrowdDensityRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
        // try {
        //     // Log received message
        //     logger.info("Received MRT Crowd Density Data: " + message);

        //     // Send data to Next.js via WebSockets
        //     messagingTemplate.convertAndSend("/topic/crowdDensity", message);

        //     // Convert JSON String to CrowdDensity object
        //     CrowdDensity crowdDensity = objectMapper.readValue(message, CrowdDensity.class);

        //     // Save to MongoDB
        //     crowdDensityRepository.save(crowdDensity);
        //     logger.info("Saved to MongoDB: " + crowdDensity);

        // } catch (Exception e) {
        //     logger.error("Error processing Kafka message: " + e.getMessage());
        // }
                // String message = record.value();

                
     try {
            logger.info("✅ Received Kafka Message: {}", message);

            // Deserialize JSON array into a List<CrowdDensity>
            List<CrowdDensity> crowdDensityList = objectMapper.readValue(
                message,
                new TypeReference<List<CrowdDensity>>() {}
            );

            // Send data to Next.js WebSocket clients
            messagingTemplate.convertAndSend("/topic/crowdDensity", message);

            // Save each CrowdDensity object to MongoDB
            for (CrowdDensity crowdDensity : crowdDensityList) {
                int retries = 1;
                int delay = 2000; // 2 seconds

                for (int i = 0; i < retries; i++) {
                    try {
                        crowdDensityRepository.save(crowdDensity);
                        logger.info("✅ Successfully saved to MongoDB: {}", crowdDensity.toString());
                        
                    } catch (DataAccessException e) {
                        logger.warn("❌ MongoDB write failed (attempt {}/{}). Retrying in {} ms...", (i + 1), retries, delay);
                        TimeUnit.MILLISECONDS.sleep(delay);
                    }
                }
            }



         } catch (WakeupException e) {
            logger.error("❌ Kafka Consumer Interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupt status
        } catch (Exception e) {
            logger.error("❌ Error processing Kafka message: {}", e.getMessage(), e);
        }
    }
}