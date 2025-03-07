package com.example.demo.kafka.Producer;

import com.example.demo.model.CrowdDensity;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.Unirest;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@EnableScheduling
public class KafkaProducerService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${datamall.apikey}")
    private String apiKey;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // @EventListener(ContextRefreshedEvent.class)
    // @Scheduled(cron = "0 */5 * * * * ") // Runs every 10 minutes
    // @Scheduled(cron = "*/60 * * * * * ") // Runs every 1 minutes
    @Scheduled(initialDelay = 10_000, fixedRate = 60_000) // Runs every 10 minutes after a brief delay

    public void getMrtDensity() {
        for (String trainline : new String[]{"NSL", "EWL", "CCL", "NEL", "DTL", "TEL"}) { // Loop through each trainline to fetch data from LTA 

            String url = "https://datamall2.mytransport.sg/ltaodataservice/PCDRealTime";

            try {
                // logger.info("Fetching latest MRT crowd density data for trainline: " + trainline);

                Unirest.get(url)
                        .header("AccountKey", apiKey)
                        .queryString("TrainLine", trainline)
                        .asJson()
                        .ifSuccess(response -> {
                            try {
                                JsonNode body = response.getBody();
                                JSONArray stationsArray = body.getObject().getJSONArray("value"); // Get the array of stations

                                List<CrowdDensity> crowdDensityList = new ArrayList<>();

                                for (int i = 0; i < stationsArray.length(); i++) {
                                    JSONObject stationData = stationsArray.getJSONObject(i); // Get each station data

                                    String station = stationData.getString("Station");
                                    String startTime = stationData.getString("StartTime");
                                    String endTime = stationData.getString("EndTime");
                                    String crowdLevel = stationData.getString("CrowdLevel");

                                    // Create a CrowdDensity object
                                    CrowdDensity crowdDensity = new CrowdDensity(station, crowdLevel, startTime, endTime);
                                    crowdDensityList.add(crowdDensity);
                                }

                                // Serialize the list of CrowdDensity objects to JSON string
                                String crowdDensityJson = objectMapper.writeValueAsString(crowdDensityList);

                                // Publish the list as a single Kafka message
                                Message<String> message = MessageBuilder
                                        .withPayload(crowdDensityJson)
                                        .setHeader(KafkaHeaders.TOPIC, "lta-mrt-density")
                                        .build();

                                kafkaTemplate.send(message);
                                logger.info("📩Published data for trainline: " + trainline);

                            } catch (Exception e) {
                                logger.error("Error processing API response: " + e.getMessage());
                            }
                        })
                        .ifFailure(response -> {
                            logger.error("Failed to fetch MRT density data: " + response.getStatusText());
                        });

            } catch (Exception e) {
                logger.error("Error fetching data from LTA API: " + e.getMessage());
            }
        }
    }
}
