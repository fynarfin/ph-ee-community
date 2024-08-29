package org.mifos.connector.mtn.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import lombok.extern.slf4j.Slf4j;
import org.mifos.connector.mtn.data.RequestToPaySuccessResponse;
import org.mifos.connector.mtn.service.SendCallbackService;
import org.mifos.connector.mtn.zeebe.ReceiveTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class CallbackController {

    @Autowired
    ZeebeClient zeebeClient;

    @Autowired
    ReceiveTask receiveTask;

    @Autowired
    SendCallbackService callbackService;

    @Value("${payerIdentifier.callback.failure}")
    private String callbackFailure;

    @Value("${payerIdentifier.callback.delay}")
    private String callbackDelay;

    @PostMapping("/callback")
    public void receiveCallback(@RequestBody String callbackData) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            RequestToPaySuccessResponse response = objectMapper.readValue(callbackData, RequestToPaySuccessResponse.class);

            String payerId = response.getPayer().getPartyId();
            log.debug("Transaction Id is : {}", response.getExternalId());
            log.debug("Payer Id is : {}", payerId);

            if (!payerId.equals(callbackFailure) && !payerId.equals(callbackDelay)) {
                receiveTask.publishTransactionCallback(response.getExternalId(), callbackData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}