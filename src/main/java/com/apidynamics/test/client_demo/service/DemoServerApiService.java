package com.apidynamics.test.client_demo.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.util.Pair;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DemoServerApiService {

    private static final Logger LOG = LoggerFactory.getLogger(DemoServerApiService.class);

    @Value("${DEMO_SERVER_BASE_URL:http://localhost:8080}")
    private String demoServerBaseURL;

    private final ClientIdService clientIdService;
    private final RestTemplate demoServerRestTemplate;

    @Autowired
    public DemoServerApiService(@Qualifier("demoServerRestTemplate") RestTemplate demoServerRestTemplate, ClientIdService clientIdService) {
        this.demoServerRestTemplate = demoServerRestTemplate;
        this.clientIdService = clientIdService;
    }

    /**
     * Get request headers from current request context
     * @return - HttpHeaders object
     */
    private HttpHeaders getRequestHeaders() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        request.getHeaderNames().asIterator().forEachRemaining(header -> LOG.debug("request.getHeader({}) ======> {}", header, request.getHeader(header)));

        HttpHeaders httpHeaders = Collections.list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        h -> Collections.list(request.getHeaders(h)),
                        (oldValue, newValue) -> newValue,
                        HttpHeaders::new
                ));

        // add remote address header since it is not coming explicitly within list of headers
        httpHeaders.add("Remote_Addr", request.getRemoteAddr());
        return httpHeaders;
    }

    /**
     * API call to validate client OTP token. It could be self or server generated
     * @param transactionId - Current adaptive transaction id
     * @param totp - Self or Server generated TOTP token
     * @return - Demo server response
     */
    public Pair<HttpStatusCode, Map<String, Object>> validateAdaptiveClientTotp(String transactionId, String totp) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("X-API-Dynamics-Client-Id", clientIdService.getClientId());
        HttpEntity<?> entity = new HttpEntity<>(headers);
        Map<String, String> uriVariables = Map.of("transactionId", transactionId, "totp", totp);
        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<Map<String, Object>> response = demoServerRestTemplate.exchange(demoServerBaseURL + "/validateAdaptiveClientTotp?tid={transactionId}&totp={totp}", HttpMethod.GET, entity, responseType, uriVariables);
        return Pair.of(response.getStatusCode(), response.getBody());
    }

    /**
     * Sample API call to get current UTC timestamp from demo server
     * @return - Demo server response
     */
    public Pair<HttpStatusCode, Map<String, Object>> getTimestamp() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("X-API-Dynamics-Client-Id", clientIdService.getClientId());
        headers.addAll(getRequestHeaders());
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<Map<String, Object>> response = demoServerRestTemplate.exchange(demoServerBaseURL + "/api/timestamp", HttpMethod.GET, entity, responseType);
        return Pair.of(response.getStatusCode(), response.getBody());
    }

    /**
     * Just another way to use RestTemplate
     * @param test - which endpoint of httpbin you want to hit?
     * @return - httpbin raw response
     */
    public Pair<HttpStatusCode, Map<String, Object>> httpBin(String test) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }
        });

        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange("https://httpbin.org/{test}", HttpMethod.GET, entity, responseType, test);
        return Pair.of(response.getStatusCode(), response.getBody());
    }

}
