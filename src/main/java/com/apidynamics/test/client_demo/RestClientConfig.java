package com.apidynamics.test.client_demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Configuration
public class RestClientConfig {

    @Value("${DEMO_SERVER_BASE_URL:http://localhost:8080}")
    private String demoServerBaseURL;

    private static final Logger LOG = LoggerFactory.getLogger(RestClientConfig.class);

    @Bean(name = "demoServerRestTemplate")
    public RestTemplate demoServerRestTemplate() {
        LOG.info("Creating demoServerRestTemplate with base URL : {}", demoServerBaseURL);
        RestTemplate restClient = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        restClient.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {}
        });

        return restClient;
    }
}
