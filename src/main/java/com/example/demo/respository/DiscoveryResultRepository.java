package com.example.demo.respository;


import com.example.demo.model.DiscoveryResult;
import com.example.demo.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscoveryResultRepository extends JpaRepository<DiscoveryResult, Long> {
    List<DiscoveryResult> findByService(Service service);

   List <DiscoveryResult> findByBucketName(String serviceName);

}
