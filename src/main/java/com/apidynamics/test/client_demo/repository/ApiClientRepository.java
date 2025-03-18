package com.apidynamics.test.client_demo.repository;


import com.apidynamics.test.client_demo.entity.ApiClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiClientRepository extends JpaRepository<ApiClient, Integer> {
}
