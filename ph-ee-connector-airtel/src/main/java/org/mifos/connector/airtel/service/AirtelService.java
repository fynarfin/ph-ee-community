package org.mifos.connector.airtel.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelCallBackRequestDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelCallBackRequestTransactionDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelEnquiryResponseDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelPaymentRequestDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelPaymentResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class AirtelService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${airtel.authorization}")
    private String authorization;

    @Value("${airtel.signature}")
    private String signature;

    @Value("${airtel.key}")
    private String key;

    @Value("${airtel.endpoints.contact-point}")
    public String airtelContactPoint;

    @Value("${server.port}")
    public String port;

    @Value("${airtel.endpoints.airtel-ussd-push}")
    public String airtelUssdPushEndpoint;

    @Value("${airtel.endpoints.airtel-transaction-enquiry}")
    public String airtelTransactionEnquiryEndpoint;

    public AirtelPaymentResponseDTO initiateTransaction(AirtelPaymentRequestDTO airtelPaymentRequestDTO, String country, String currency) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("X-Country", country);
        headers.add("X-Currency", currency);
        headers.add("Authorization", authorization);
        headers.add("x-signature", signature);
        headers.add("x-key", key);

        String url = airtelContactPoint + ":" + port + airtelUssdPushEndpoint;
        try {
            ResponseEntity<AirtelPaymentResponseDTO> exchange = restTemplate.exchange(url, HttpMethod.POST,
                    new HttpEntity<>(airtelPaymentRequestDTO, headers), AirtelPaymentResponseDTO.class);

            return exchange.getBody();
        } catch (Exception ex) {
            throw new RuntimeException("Invalid response!", ex);
        }
    }

    public AirtelEnquiryResponseDTO getTransactionStatus(String transactionId, String country, String currency) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("X-Country", country);
        headers.add("X-Currency", currency);
        headers.add("Authorization", authorization);

        String url = airtelContactPoint + ":" + port + airtelTransactionEnquiryEndpoint + transactionId;
        ResponseEntity<AirtelEnquiryResponseDTO> exchange;
        try {
            exchange = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), AirtelEnquiryResponseDTO.class);
            return exchange.getBody();
        } catch (HttpClientErrorException.BadRequest ex) {
            try {
                String responseBody = ex.getResponseBodyAsString();
                AirtelEnquiryResponseDTO errorResponse = objectMapper.readValue(responseBody, AirtelEnquiryResponseDTO.class);
                return errorResponse;

            } catch (JsonProcessingException e) {
                throw new RuntimeException("Invalid response!", e);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Invalid response!", ex);
        }
    }

    public void forwardCallbackRequest(String callbackURL, String clientCorrelationId, String message, String statusCode,
            String externalTransactionId) {
        AirtelCallBackRequestTransactionDTO transaction = new AirtelCallBackRequestTransactionDTO(clientCorrelationId, message, statusCode,
                externalTransactionId);
        AirtelCallBackRequestDTO requestBody = new AirtelCallBackRequestDTO(transaction);
        try {
            ResponseEntity responseEntity = restTemplate.postForEntity(callbackURL, requestBody, null);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                logger.info("Callback sent");
            } else {
                logger.info("Callback failed!!!");
            }
        } catch (Exception exception) {
            logger.info("Callback failed!!!");
            logger.info(exception.getMessage());
        }
    }
}
