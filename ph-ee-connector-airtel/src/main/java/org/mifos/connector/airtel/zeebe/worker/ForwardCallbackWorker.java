package org.mifos.connector.airtel.zeebe.worker;

import static org.mifos.connector.airtel.zeebe.ZeebeVariables.CALLBACK_MESSAGE;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.CLIENT_CORRELATION_ID;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.EXTERNAL_TRANSACTION_ID;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.TRANSACTION_STATUS;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.X_CALLBACKURL;

import io.camunda.zeebe.client.ZeebeClient;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.mifos.connector.airtel.service.AirtelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ForwardCallbackWorker {

    @Value("${airtel.MAX_RETRY_COUNT}")
    private int maxRetryCount;

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private AirtelService airtelService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void setup() {
        zeebeClient.newWorker().jobType("forward-callback").handler((client, job) -> {
            logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
            Map<String, Object> variables = job.getVariablesAsMap();

            String clientCorrelationId = variables.get(CLIENT_CORRELATION_ID).toString();
            String message = variables.get(CALLBACK_MESSAGE).toString();
            String transactionStatus = variables.get(TRANSACTION_STATUS).toString();
            String externalTransactionId = variables.get(EXTERNAL_TRANSACTION_ID).toString();

            String callbackUrl = variables.get(X_CALLBACKURL).toString();

            airtelService.forwardCallbackRequest(callbackUrl, clientCorrelationId, message, transactionStatus, externalTransactionId);

            client.newCompleteCommand(job.getKey()).variables(variables).send().join();
        }).name("forward-callback").open();
    }
}
