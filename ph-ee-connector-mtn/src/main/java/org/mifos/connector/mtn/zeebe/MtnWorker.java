package org.mifos.connector.mtn.zeebe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import lombok.extern.slf4j.Slf4j;
import org.mifos.connector.mtn.data.RequestToPayDTO;
import org.mifos.connector.mtn.data.RequestToPaySuccessResponse;
import org.mifos.connector.mtn.data.converter.ChannelToRtpCallback;
import org.mifos.connector.mtn.data.converter.ChannelToRtpConverter;
import org.mifos.connector.mtn.service.ApiService;
import org.mifos.connector.mtn.service.SendCallbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MtnWorker {
    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private SendCallbackService callbackService;

    @Autowired
    private ReceiveTask receiveTask;

    @Autowired
    private ChannelToRtpCallback rtpConverter;

    @Autowired
    private ApiService service;

    @Value("${payerIdentifier.callback.failure}")
    private String callbackFailure;

    @Value("${payerIdentifier.callback.delay}")
    private String callbackDelay;

    @Value("${zeebe.client.evenly-allocated-max-jobs}")
    private int workerMaxJobs;

    @Value("${mock-payment-schema.contactpoint}")
    private String mockPaymentSchemaContactPoint;

    @Value("mock-payment-schema.endpoints.mtn-collection")
    private String mockPaymentSchemaCollectionContactPoint;

    @Value("mock-payment-schema.endpoints.request-to-pay")
    private String mockPaymentSchemaRequestToPayContactPoint;


    @PostConstruct
    public void setupWorkers() throws UnsupportedEncodingException, JsonProcessingException {
        workerExecuteRtpexecuteRtpTransfer();
        workerExecuteGetRtpStatus();
        sendCallbackWorker();
    }

    public void workerExecuteRtpexecuteRtpTransfer() {
        zeebeClient.newWorker().jobType("mtn-init-transfer").handler((client, job) -> {
            log.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());

            Map<String, Object> variables = job.getVariablesAsMap();
            String requestBody = (String) variables.get("channelRequest");
            String transactionId = (String) variables.get("transactionId");

            ChannelToRtpConverter converter = new ChannelToRtpConverter();

            RequestToPayDTO dto = converter.convertToRtpDto(transactionId, requestBody);
            executeRtpTransfer(transactionId, dto);
            client.newCompleteCommand(job.getKey()).variables(variables).send();

        }).name("mtn-init-transfer").maxJobsActive(workerMaxJobs).open();
    }

    public void sendCallbackWorker() {
        zeebeClient.newWorker().jobType("send-mtn-callback").handler((client, job) -> {
            log.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());

            Map<String, Object> variables = job.getVariablesAsMap();
            String requestBody = (String) variables.get("channelRequest");
            String payerId = extractPayerIdentifier(requestBody);
            String jsonResponse = (String) variables.get("mtnResponseBody");

            String callbackURL = (String) variables.get("X-CallbackURL");

            if (!payerId.equals(callbackFailure)) {
                callbackService.sendCallback(jsonResponse, callbackURL);
            }


        }).name("send-mtn-callback").maxJobsActive(workerMaxJobs).open();
    }



    public void workerExecuteGetRtpStatus() {
        zeebeClient.newWorker().jobType("get-mtn-transaction-status").handler((client, job) -> {

            log.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());

            Map<String, Object> variables = job.getVariablesAsMap();
            String requestBody = (String) variables.get("channelRequest");
            String transactionId = (String) variables.get("transactionId");

            Object response = executeGetStatus(transactionId);
            String jsonResponse = null;
            if(response != null) {
                //Preparing full response body
                jsonResponse = prepareJsonResponse(requestBody);
            }

            receiveTask.publishTransactionCallback(transactionId, jsonResponse);
            client.newCompleteCommand(job.getKey()).send();

        }).name("get-mtn-transaction-status").maxJobsActive(workerMaxJobs).open();
    }

    private String extractPayerIdentifier(String requestBody) {

        ObjectMapper mapper = new ObjectMapper();
        String payerId = "";
        try {
            JsonNode root = mapper.readTree(requestBody);

            String payerPartyIdentifier = root.path("payer").path("partyIdInfo").path("partyIdentifier").asText();
            payerId = payerPartyIdentifier;

        } catch (Exception e) {
            log.error("Failed to parse channelRequest JSON", e);
        }

        return payerId;
    }

    private void executeRtpTransfer(String referenceId, RequestToPayDTO dto) throws UnsupportedEncodingException, JsonProcessingException {
        service.requestToPay(null, referenceId, dto);
    }

    private Object executeGetStatus(String referenceId) {
        return service.getTxnStatus(null, referenceId);
    }

    private String prepareJsonResponse(String requestBody) throws Exception {
        RequestToPaySuccessResponse dto = rtpConverter.convertToRtpCallbackResponseDTO(requestBody);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(dto);

        return jsonResponse;
    }

}

