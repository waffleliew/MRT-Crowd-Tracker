package com.example.demo.controller;

import com.example.demo.kafka.Consumer.KafkaConsumerService;
import com.example.demo.model.CrowdDensity;
import com.example.demo.repository.CrowdDensityRepo;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestController
@RequestMapping("/api/mrt-density") // API Endpoint: http://backend:8080/api/mrt-density
@CrossOrigin(origins = "*") // Allow frontend to access
public class CrowdDensityController {
    private final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private final CrowdDensityRepo crowdDensityRepository;

    public CrowdDensityController(CrowdDensityRepo crowdDensityRepository) {
        this.crowdDensityRepository = crowdDensityRepository;
    }

    // Fetch All Records from MongoDB
    @GetMapping
    public List<CrowdDensity> getAllCrowdData() {
        List<CrowdDensity> message = crowdDensityRepository.findAll();
        // logger.info("📩 latest feed from mongodb: " + message);
        
        return message;
    }

    // Fetch by MRT Station Code
    @GetMapping("/{station}")
    public List<CrowdDensity> getCrowdDataByStation(@PathVariable String station) {
        return crowdDensityRepository.findByStation(station);
    }
}
