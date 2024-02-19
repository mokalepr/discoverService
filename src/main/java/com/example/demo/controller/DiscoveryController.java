package com.example.demo.controller;


import com.example.demo.model.Service;
import com.example.demo.service.DiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class DiscoveryController {

    @Autowired
    private DiscoveryService discoveryService;


    //Controller for 1. DiscoverServices(List<String> services)
    @PostMapping("/discoverEc2")
    public ResponseEntity<String> discoverServices(@RequestBody List<Service> service)
    {
        // Validate input
        if (service.isEmpty()) {
            return ResponseEntity.badRequest().body("Services list cannot be empty");
        }
        // Start asynchronous discovery
        discoveryService.discoverEc2Instances();
        discoveryService.discoverS3Buckets();

        // Return a job ID or status
        String jobId = UUID.randomUUID().toString();
        return ResponseEntity.ok(jobId);
    }

    //Controller for 2. GetJobResult(Jobid)
    @GetMapping("/job/{jobId}")
    public ResponseEntity<String> getJobResult(@PathVariable String jobId) {
        // Retrieve job status from the service
        String status = discoveryService.getJobStatus(jobId);

        if (status != null) {
            return ResponseEntity.ok(status);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    //Code for 3. GetDiscoveryResult(String Service)
    @GetMapping("/discovery/{service}")
    public ResponseEntity<?> getDiscoveryResult(@PathVariable String service) {
        List<String> discoveryResult;

        if (service.equalsIgnoreCase("S3")) {
            discoveryResult = discoveryService.getS3Buckets();
        } else if (service.equalsIgnoreCase("EC2")) {
            discoveryResult = discoveryService.getEC2Instances();
        } else {
            return ResponseEntity.badRequest().body("Invalid service name");
        }

        if (discoveryResult.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(discoveryResult);
        }
    }


    //Controller for 4. GetS3BucketObjects(String BucketName)
    @GetMapping("/s3/{bucketName}/objects")
    public ResponseEntity<String> getS3BucketObjects(@PathVariable String bucketName) {
        String jobId = discoveryService.getS3BucketObjects(bucketName);
        return ResponseEntity.ok(jobId);
    }


    //Controller for 5. GetS3BucketObjectCount(String BucketName)
    @GetMapping("/s3/{bucketName}/objectCount")
    public ResponseEntity<Integer> getS3BucketObjectCount(@PathVariable String bucketName) {
        int objectCount = discoveryService.getS3BucketObjectCount(bucketName);
        return ResponseEntity.ok(objectCount);
    }


    @GetMapping("/s3/{bucketName}/objectsLike/{pattern}")
    public ResponseEntity<List<String>> getS3BucketObjectsLike(@PathVariable String bucketName, @PathVariable String pattern) {
        List<String> matchedObjects = discoveryService.getS3BucketObjectsLike(bucketName, pattern);
        return ResponseEntity.ok(matchedObjects);
    }

}
