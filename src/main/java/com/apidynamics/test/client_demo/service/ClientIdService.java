package com.apidynamics.test.client_demo.service;

import com.apidynamics.test.client_demo.entity.ApiClient;
import com.apidynamics.test.client_demo.repository.ApiClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * This service is used to provide current client id "public key" to the RestClient
 * Since this is a demo client, this is a way to provide a convenient client id
 * depending on the testing the user is performing
 */
@Service
public class ClientIdService {
    private static final Logger LOG = LoggerFactory.getLogger(ClientIdService.class);

    @Value("${API_DYNAMICS_CLIENT_ID}")
    private String apiDynamicsClientId;

    private final ApiClientRepository apiClientRepository;

    @Autowired
    public ClientIdService(ApiClientRepository apiClientRepository) {
        this.apiClientRepository = apiClientRepository;
    }

    public String getClientId() {
        Optional<ApiClient> apiClient = apiClientRepository.findById(1);
        String clientId = null;
        if (apiClient.isPresent()) {
            clientId = apiClient.get().getPublicKey();
        } else {
            clientId = apiDynamicsClientId;
        }
        LOG.info("Client id: {}", clientId);
        return clientId;
    }
}
