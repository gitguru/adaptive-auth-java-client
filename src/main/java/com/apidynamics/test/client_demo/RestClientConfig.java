package com.apidynamics.test.client_demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.io.IOException;

@Configuration
public class RestClientConfig {

    @Value("${ADAPTIVE_AUTHENTICATION_BASE_URL:http://localhost:3000}")
    private String adaptiveAuthenticationBaseURL;
    @Value("${DEMO_SERVER_BASE_URL:http://localhost:8080}")
    private String demoServerBaseURL;

    private static final Logger LOG = LoggerFactory.getLogger(RestClientConfig.class);

    @Bean(name = "adaptiveRestTemplate")
    @Primary
    public RestTemplate adaptiveRestTemplate() {
        LOG.info("Creating adaptiveRestClient with base URL : {}", adaptiveAuthenticationBaseURL);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(adaptiveAuthenticationBaseURL));
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {}
        });
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        return restTemplate;
    }

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
