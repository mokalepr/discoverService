package com.example.demo.model;


import jakarta.persistence.*;

@Entity
@Table(name = "Discover")
public class DiscoveryResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service")
    private Service service;


    @Column(name = "instance_id")
    private Long instanceId;

    @Column(name = "bucket_name")
    private String bucketName;


    @Column(name = "object_name")
    private String objectName;

    public DiscoveryResult(){}

    public DiscoveryResult(Long id, Service service, Long instanceId, String bucketName, String objectName) {
        this.id = id;
        this.service = service;
        this.instanceId = instanceId;
        this.bucketName = bucketName;
        this.objectName = objectName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }


    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    @Override
    public String toString() {
        return "DiscoveryResult{" +
                "id=" + id +
                ", service=" + service +
                ", instanceId='" + instanceId + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", objectName='" + objectName + '\'' +
                '}';
    }
}
