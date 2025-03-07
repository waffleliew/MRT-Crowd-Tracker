package com.example.demo.repository;

import com.example.demo.model.CrowdDensity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CrowdDensityRepo extends MongoRepository<CrowdDensity, String> {
    List<CrowdDensity> findByStation(String station); // Query by station code
}
