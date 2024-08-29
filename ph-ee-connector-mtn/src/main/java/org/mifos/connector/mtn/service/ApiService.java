package org.mifos.connector.mtn.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.mifos.connector.mtn.Util.GenerateUUID;
import org.mifos.connector.mtn.data.Payer;
import org.mifos.connector.mtn.data.RequestToPayDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

@Slf4j
@Service
public class ApiService {

    @Value("${mock-payment-schema.contactpoint}")
    private String mockPaymentSchemaContactPoint;

    @Value("${mock-payment-schema.endpoints.mtn-collection}")
    private String mockPaymentSchemaCollectionContactPoint;

    @Value("${mock-payment-schema.endpoints.request-to-pay}")
    private String mockPaymentSchemaRequestToPayContactPoint;

    String subscriptionKey = "subscriptionKey";
    String username = "username";
    String api_key = "api_key";

    private static String getBasicAuthHeader(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        return "Basic " + new String(encodedAuth);
    }

    public String generateAccessToken() {
        return getBasicAuthHeader(username, api_key);
    }

    public void requestToPay(String callbackUrl, String referenceId, RequestToPayDTO requestToPayDTO) throws
            JsonProcessingException, UnsupportedEncodingException {

        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {

            StringBuilder sb = new StringBuilder();

            String rtp_endpoint = sb.append(mockPaymentSchemaContactPoint).append(mockPaymentSchemaCollectionContactPoint)
                    .append(mockPaymentSchemaRequestToPayContactPoint).toString();
            String accessToken = generateAccessToken();

            HttpPost postRequest = new HttpPost(rtp_endpoint);
            postRequest.setHeader(new BasicHeader("Ocp-Apim-Subscription-Key", subscriptionKey));
            postRequest.setHeader(new BasicHeader("X-Reference-Id", referenceId));
            postRequest.setHeader(new BasicHeader("X-Target-Environment", "sandbox"));
            postRequest.setHeader(new BasicHeader("X-Callback-Url", callbackUrl));
            postRequest.setHeader(new BasicHeader("Content-Type", "application/json"));
            postRequest.setHeader(new BasicHeader("Authorization", "Bearer " + accessToken));

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonPayload = objectMapper.writeValueAsString(requestToPayDTO);

            StringEntity entity = new StringEntity(jsonPayload);
            postRequest.setEntity(entity);

            CloseableHttpResponse response = httpClient.execute(postRequest);
        } catch (Exception e) {
            log.error("An exception occurred : {}", e.getMessage());
            e.printStackTrace();
        }

    }


    public Object getTxnStatus(String callbackUrl,  String referenceId) {
        StringBuilder sb = new StringBuilder();
        String txnStatusEndpoint = sb.append(mockPaymentSchemaContactPoint)
                .append(mockPaymentSchemaCollectionContactPoint)
                .append(mockPaymentSchemaRequestToPayContactPoint)
                .append("/")
                .append(referenceId).toString();

        String accessToken = generateAccessToken();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet getRequest = new HttpGet(txnStatusEndpoint);
            getRequest.setHeader(new BasicHeader("Ocp-Apim-Subscription-Key", subscriptionKey));
            getRequest.setHeader(new BasicHeader("X-Target-Environment", "sandbox"));
            getRequest.setHeader(new BasicHeader("Authorization", "Bearer " + accessToken));

            try (CloseableHttpResponse response = httpClient.execute(getRequest)) {
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    String responseBody = EntityUtils.toString(responseEntity);
                    log.debug("Get RTP Response Body: {}", responseBody);
                    return response;
                }

            }
        } catch (Exception e) {
            log.error("An exception occurred : {}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
