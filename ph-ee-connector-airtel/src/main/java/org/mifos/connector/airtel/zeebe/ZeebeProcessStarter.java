package org.mifos.connector.airtel.zeebe;

import static org.mifos.connector.airtel.zeebe.ZeebeVariables.CHANNEL_REQUEST;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.ORIGIN_DATE;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.TRANSACTION_ID;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.ClientStatusException;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.mifos.connector.airtel.exception.ZeebeClientStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ZeebeProcessStarter {

    private static final Logger logger = LoggerFactory.getLogger(ZeebeProcessStarter.class);

    @Autowired
    private ZeebeClient zeebeClient;

    public String startZeebeWorkflow(String workflowId, String request, Map<String, Object> extraVariables) {
        String transactionId = generateTransactionId();

        Map<String, Object> variables = new HashMap<>();
        variables.put(TRANSACTION_ID, transactionId);
        variables.put(CHANNEL_REQUEST, request);
        variables.put(ORIGIN_DATE, Instant.now().toEpochMilli());
        if (extraVariables != null) {
            variables.putAll(extraVariables);
        }
        try {
            ProcessInstanceEvent instance = zeebeClient.newCreateInstanceCommand().bpmnProcessId(workflowId).latestVersion()
                    .variables(variables).send().join();
            logger.info("zeebee workflow instance from process {} started with transactionId {}, instance key: {}", workflowId,
                    transactionId, instance.getProcessInstanceKey());
        } catch (ClientStatusException ex) {
            throw new ZeebeClientStatusException(ex.getMessage(), extraVariables.get("requestId").toString(), ex);
        }
        return transactionId;
    }

    private String generateTransactionId() {
        return UUID.randomUUID().toString();
    }
}
