package org.mifos.connector.mtn.zeebe;

import io.camunda.zeebe.client.ZeebeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ReceiveTask {
    @Autowired
    private ZeebeClient zeebeClient;

    public void publishTransactionCallback(String transactionId, String callbackResponse) {

        Map<String, Object> newVariables = new HashMap<>();
        newVariables.put("mtnResponseBody", callbackResponse);


        zeebeClient.newPublishMessageCommand()
                .messageName("mtn-transaction-request")
                .correlationKey(transactionId)
                .variables(newVariables)
                .timeToLive(Duration.ofMillis(3000))
                .send()
                .join();

        log.info("Published callback message with transactionId: {}", transactionId);
    }

}
