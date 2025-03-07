package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "crowd_density") // Match Kafka Connect collection name
public class CrowdDensity {

    @Id
    private String id;
    private String station;      // Example: "TE7"
    private String crowdLevel;   // Example: "h", "m", "l"
    private String startTime;    // Example: "2025-03-06T23:30:00+08:00"
    private String endTime;      // Example: "2025-03-06T23:40:00+08:00"

    // Default Constructor
    public CrowdDensity() {}

    // Parameterized Constructor
    public CrowdDensity(String station, String crowdLevel, String startTime, String endTime) {
        this.station = station;
        this.crowdLevel = crowdLevel;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters & Setters
    public String getId() { return id; }
    public String getStation() { return station; }
    public void setStation(String station) { this.station = station; }
    public String getCrowdLevel() { return crowdLevel; }
    public void setCrowdLevel(String crowdLevel) { this.crowdLevel = crowdLevel; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
