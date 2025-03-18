package com.apidynamics.test.client_demo.controller;

import com.apidynamics.test.client_demo.entity.ApiClient;
import com.apidynamics.test.client_demo.repository.ApiClientRepository;
import com.apidynamics.test.client_demo.service.TOTPService;
import com.apidynamics.test.client_demo.service.DemoServerApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Optional;

@Controller
public class ClientDemoController {

    private static final Logger LOG = LoggerFactory.getLogger(ClientDemoController.class);

    @Value("${spring.application.name}")
    private String appName;

    private final DemoServerApiService demoServerApiService;
    private final TOTPService totpService;
    private final ApiClientRepository apiClientRepository;

    @Autowired
    public ClientDemoController(DemoServerApiService demoServerApiService, TOTPService totpService, ApiClientRepository apiClientRepository) {
        this.demoServerApiService = demoServerApiService;
        this.totpService = totpService;
        this.apiClientRepository = apiClientRepository;
    }

    @RequestMapping("/")
    public String homePage(Model model) {
        Optional<ApiClient> apiClient = apiClientRepository.findById(1);
        model.addAttribute("appName", appName);
        apiClient.ifPresent(client -> model.addAttribute("clientId", client));
        return "home";
    }

    @PostMapping("/settings")
    public String save(@RequestParam(value = "client_id", defaultValue = "Unknown") String clientId) {
        apiClientRepository.save(ApiClient.builder().id(1).publicKey(clientId).build());
        return "redirect:/";
    }

    /**
     * Generates or Gets a TOTP token
     * Depending on the ApiClient token generation strategy
     * @param transactionId - Current Adaptive Auth Transaction Id
     * @param tokenType - Token generation strategy (self, server)
     * @return - A OTP token
     */
    private Pair<HttpStatusCode, Map<String, Object>> resolveTotpChallenge(String transactionId, String tokenType) {
        String totp;
        if (tokenType.equalsIgnoreCase("self")) {
            // gen top token flow
            totp = totpService.generateTOTP();
        } else {
            // get otp from server
            Pair<HttpStatusCode, Map<String, Object>> totpFromServerResult = totpService.getTotpToken(transactionId);
            HttpStatusCode totpStatusCode = totpFromServerResult.getFirst();
            Map<String, Object> totpResponseBody = totpFromServerResult.getSecond();

            LOG.info("Server TOTP generation call status : {}", totpStatusCode);
            LOG.debug("Server TOTP generation call response : {}", totpResponseBody);

            // if server TOTP generation call results in error, then return the error
            if (totpStatusCode.isError()) {
                return totpFromServerResult;
            }

            // if server TOTP token was returned successfully, then use it to validate it
            totp = totpFromServerResult.getSecond().get("totp").toString();
        }

        Pair<HttpStatusCode, Map<String, Object>> totpValidationResult = demoServerApiService.validateAdaptiveClientTotp(transactionId, totp);
        HttpStatusCode totpValidationStatusCode = totpValidationResult.getFirst();
        Map<String, Object> totpValidationResponseBody = totpValidationResult.getSecond();

        LOG.info("TOTP validation call status : {}", totpValidationStatusCode);
        LOG.debug("TOTP validation call response : {}", totpValidationResponseBody);

        return totpValidationResult;
    }

    @RequestMapping("/timestamp")
    public String timestampPage(Model model, @RequestParam(value = "token_type", defaultValue = "self") String tokenType) {
        LOG.info("Got to /timestamp");
        Pair<HttpStatusCode, Map<String, Object>> result = demoServerApiService.getTimestamp();
        HttpStatusCode httpStatusCode = result.getFirst();
        Map<String, Object> responseBody = result.getSecond();

        LOG.info("Timestamp call status : {}", httpStatusCode);
        LOG.debug("Timestamp call response : {}", responseBody);

        if (httpStatusCode.is2xxSuccessful()) {
            model.addAllAttributes(responseBody);
            return "timestamp";
        }

        // this scenario is when adaptive client needs to resolve a TOTP challenge
        if (httpStatusCode.isSameCodeAs(HttpStatusCode.valueOf(401))) {
            String tid = (String) responseBody.get("transaction_id");
            Pair<HttpStatusCode, Map<String, Object>> totpValidationResult = resolveTotpChallenge(tid, tokenType);
            HttpStatusCode totpValidationStatusCode = totpValidationResult.getFirst();
            Map<String, Object> totpValidationResponseBody = totpValidationResult.getSecond();
            if (totpValidationStatusCode.is2xxSuccessful()) {
                return "redirect:/timestamp";
            } else {
                model.addAttribute("status", totpValidationStatusCode);
                model.addAttribute("error", totpValidationResponseBody);
                return "error";
            }
        }

        // get error from response and return failed API call
        Map<String, Object> adaptiveAuthValidationResult = (Map<String, Object>) responseBody.get("validation_result");
        model.addAttribute("status", httpStatusCode);
        if (adaptiveAuthValidationResult != null) {
            String error = String.format("Adaptive Authentication Result. score=%s, decision=%s", adaptiveAuthValidationResult.get("score"), adaptiveAuthValidationResult.get("decision"));
            model.addAttribute("error", error);
        } else {
            model.addAttribute("error", responseBody);
        }
        return "error";
    }

    @RequestMapping("/httpbin")
    public String httpbinPage(Model model, @RequestParam(value = "test", defaultValue = "headers") String test) {
        LOG.info("Got to /httpbin");
        Pair<HttpStatusCode, Map<String, Object>> result = demoServerApiService.httpBin(test);
        HttpStatusCode httpStatusCode = result.getFirst();
        Map<String, Object> responseBody = result.getSecond();

        LOG.info("httpbin.org call status : {}", httpStatusCode);
        LOG.debug("httpbin.org call response : {}", responseBody);

        model.addAttribute("response", responseBody);
        return "httpbin";
    }

}
