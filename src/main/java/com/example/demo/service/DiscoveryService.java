package com.example.demo.service;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2instanceconnect.AWSEC2InstanceConnect;
import com.amazonaws.services.ec2instanceconnect.AWSEC2InstanceConnectClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.demo.model.DiscoveryResult;
import com.example.demo.respository.DiscoveryResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DiscoveryService {

    //Autowiring instance of DiscoveryResultRepository to communicate with MySQL DB
    @Autowired
    private DiscoveryResultRepository discoveryResultRepository;

    int count = 0;
    //Setting the credentials
    private final String accessKey = "AKIAX5XSI5UTUOMGM5F3";
    private final String secretKey = "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";




    //Service method to execute 1. DiscoverServices(List<String> services) EC2 and S3
    @Async
    public void discoverEc2Instances() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        //using BasicAWSCredentials that defines access and secret keys required for accessing AWS services.


        // Using AWSEC2InstanceConnect to connect to EC2 instances in the specified region (AP_SOUTH_1) which is mumbai,
        // retrieves information about those instances, and saves the relevant data (such as instance ID) to the database.
        AWSEC2InstanceConnect client = AWSEC2InstanceConnectClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.AP_SOUTH_1).build();


        // requesting information about EC2 instances from the AWS EC2 (Elastic Compute Cloud) service.
        DescribeInstancesRequest request = new DescribeInstancesRequest();

        // used to store the instance information about EC2 instances from the AWS EC2 (Elastic Compute Cloud) service fetch by request.
        DescribeInstancesResult result = new DescribeInstancesResult();

        // using Java Stream API to process the result obtained from DescribeInstancesResult and extract a list of EC2 instances.
        List<Instance> instances = result.getReservations().stream()
                .flatMap(reservation -> reservation.getInstances().stream()).toList();


        DiscoveryResult ec2Result = new DiscoveryResult();

        // iterating over a list of EC2 instances (instances) and saving information about each instance to a DiscoveryResult object
        instances.forEach(instance -> {
            ec2Result.setService(com.example.demo.model.Service.Ec2);
            ec2Result.setInstanceId(UUID.randomUUID().timestamp());
            // Set other properties of the discovery result from the EC2 instance
            discoveryResultRepository.save(ec2Result);
        });
    }
    @Async
    public void discoverS3Buckets() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);

        //This line constructs an Amazon S3 client using the AmazonS3ClientBuilder.
        //  and configures the client with the provided credentials and sets the region to Regions.AP_SOUTH_1, indicating the Asia Pacific (Mumbai) region.
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.AP_SOUTH_1)
                .build();

        //  retrieving a list of S3 buckets in the specified AWS region.
        List<Bucket> buckets = s3Client.listBuckets();

        // Persist S3 buckets to the database
        buckets.forEach(bucket -> {
            count+=1;
            DiscoveryResult s3Result = new DiscoveryResult();
            s3Result.setService(com.example.demo.model.Service.S3);
            s3Result.setBucketName("Bucket:" + count);
            // Set other properties of the discovery result from the S3 bucket
            discoveryResultRepository.save(s3Result);

        });
    }


    //Service Method to execute 2. GetJobResult(Jobid)
    public String getJobStatus(String jobId) {
        Optional<DiscoveryResult> resultOptional = discoveryResultRepository.findById(Long.parseLong(jobId));
        if (resultOptional.isPresent()) {
            return "Success"; // Assuming the job is considered successful if the result is found in the database
        } else {
            return "In Progress"; // You might have a more sophisticated logic here to determine the job status
        }
    }


    // Service Method to handle 3. GetDiscoveryResult(String Service)
    public List<String> getS3Buckets()
    {
        List<DiscoveryResult> s3result = discoveryResultRepository.findByService(com.example.demo.model.Service.S3);
        List<String> s3Buckets = new ArrayList<>();
        for (DiscoveryResult result : s3result) {
            // bucket name is stored as a property of DiscoveryResult
            s3Buckets.add(result.getBucketName());
        }
        return s3Buckets;
    }


    public List<String> getEC2Instances() {
        List<DiscoveryResult> ec2Results = discoveryResultRepository.findByService(com.example.demo.model.Service.Ec2);
        List<String> ec2Instances = new ArrayList<>();
        for (DiscoveryResult result : ec2Results) {
            // instance ID is stored as a property of DiscoveryResult
            ec2Instances.add((result.getInstanceId().toString())); // Replace getProperty() with the actual getter method
        }
        return ec2Instances;
    }


    //Service Method to handle  4. GetS3BucketObjects(String BucketName)
    @Async
    public String getS3BucketObjects(String bucketName) {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.AP_SOUTH_1)
                .build();

        ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(bucketName);
        ListObjectsV2Result result;
        String jobId = UUID.randomUUID().toString();

        do {
            result = s3Client.listObjectsV2(request);

            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                DiscoveryResult s3Object = new DiscoveryResult();
                s3Object.setService(com.example.demo.model.Service.S3);
                s3Object.setBucketName(bucketName);
                s3Object.setObjectName(objectSummary.getKey());
                // Set other properties of the discovery result from the S3 object
                discoveryResultRepository.save(s3Object);
            }

            request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());

        return jobId;
    }


  // Service Method to handle  5. GetS3BucketObjectCount(String BucketName)
    public int getS3BucketObjectCount(String bucketName) {
        List<DiscoveryResult> results = discoveryResultRepository.findByBucketName(bucketName);
        return results.size();
    }


    // Service Method to handle  6. GetS3BucketObjectlike(String BucketName, String Pattern)
    @Async
    public List<String> getS3BucketObjectsLike(String bucketName, String pattern) {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.AP_SOUTH_1)
                .build();

        ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(pattern);
        ListObjectsV2Result result = s3Client.listObjectsV2(request);

        List<String> matchedObjects = new ArrayList<>();
        for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
            matchedObjects.add(objectSummary.getKey());
        }

        return matchedObjects;
    }

}