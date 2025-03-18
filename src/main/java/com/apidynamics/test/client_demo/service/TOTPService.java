package com.apidynamics.test.client_demo.service;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.util.Pair;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

/**
 * This service is used to generate self generated TOTP tokens.
 * This token is validated by Adaptive Authentication Server.
 */
@Service
public class TOTPService {

    private static final Logger LOG = LoggerFactory.getLogger(TOTPService.class);

    private final ClientIdService clientIdService;
    private final RestTemplate restTemplate;

    @Autowired
    public TOTPService(@Qualifier("adaptiveRestTemplate") RestTemplate restTemplate, ClientIdService clientIdService) {
        this.restTemplate = restTemplate;
        this.clientIdService = clientIdService;
    }

    /**
     * @param key - secret credential key (HEX)
     * @return the OTP
     */
    public static String getOTP(String key) {
        return getOTP(getStep(), key);
    }

    /**
     * @param key - secret credential key (HEX)
     * @param otp - OTP to validate
     * @return valid?
     */
    public static boolean validate(final String key, final String otp) {
        return validate(getStep(), key, otp);
    }

    private static boolean validate(final long step, final String key, final String otp) {
        return getOTP(step, key).equals(otp) || (step > 0 && getOTP(step - 1, key).equals(otp));
    }

    private static long getStep() {
        // 30 seconds StepSize (ID TOTP)
        return System.currentTimeMillis() / 30000;
    }

    private static String getOTP(final long step, final String key) {
        if (step < 0) {
            throw new IllegalArgumentException("Step must be greater than or equal to zero.");
        }
        String steps = Long.toHexString(step).toUpperCase();
        while (steps.length() < 16) {
            steps = "0" + steps;
        }

        // Get the HEX in a Byte[]
        final byte[] msg = hexStr2Bytes(steps);
        final byte[] k = hexStr2Bytes(key);

        final byte[] hash = hmac_sha1(k, msg);

        // put selected bytes into result int
        final int offset = hash[hash.length - 1] & 0xf;
        final int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16) | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);
        final int otp = binary % 1000000;

        String result = Integer.toString(otp);
        while (result.length() < 6) {
            result = "0" + result;
        }
        return result;
    }

    /**
     * This method converts HEX string to Byte[]
     *
     * @param hex the HEX string
     *
     * @return A byte array
     */
    private static byte[] hexStr2Bytes(final String hex) {
        // Adding one byte to get the right conversion
        // values starting with "0" can be converted
        final byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();
        final byte[] ret = new byte[bArray.length - 1];

        // Copy all the REAL bytes, not the "first"
        System.arraycopy(bArray, 1, ret, 0, ret.length);
        return ret;
    }

    /**
     * This method uses the JCE to provide the crypto algorithm. HMAC computes a Hashed Message Authentication Code with the crypto hash
     * algorithm as a parameter.
     *
     * @param keyBytes the bytes to use for the HMAC key
     * @param text the message or text to be authenticated.
     */
    private static byte[] hmac_sha1(final byte[] keyBytes, final byte[] text) {
        try {
            final Mac hmac = Mac.getInstance("HmacSHA1");
            final SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (final GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }

    /**
     * Generate a self generated OTP token that can be validated by Adaptive Authentication Server
     *
     * @return A TOTP token that last 30 seconds
     */
    public String generateTOTP() {
        Base32 base32 = new Base32();
        // we must make sure that used key can be encoded into Base32
        byte[] encodedBytes = base32.encode(clientIdService.getClientId().getBytes(StandardCharsets.UTF_8));
        String token = getOTP(Hex.encodeHexString(base32.decode(encodedBytes)));
        LOG.info("Generated TOTP token : {}", token);

        return token;
    }

    /**
     * Get a OTP token from Adaptive Authentication server
     * @param transactionId - Current adaptive transaction id
     * @return - Adaptive Authentication Server response
     */
    public Pair<HttpStatusCode, Map<String, Object>> getTotpToken(String transactionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("X-API-Dynamics-Client-Id", clientIdService.getClientId());
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange("/totp/client/generate?tid={transactionId}", HttpMethod.GET, entity, responseType, transactionId);
        return Pair.of(response.getStatusCode(), response.getBody());
    }
}
