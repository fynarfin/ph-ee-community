package org.mifos.connector.airtel.zeebe.worker;

import static org.mifos.connector.airtel.zeebe.ZeebeVariables.CALL_BACK_RESPONSE;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.CLIENT_CORRELATION_ID;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.COUNTRY;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.CURRENCY;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.EXTERNAL_TRANSACTION_ID;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.RESPONSE_CODE;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.RESULT_CODE;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.RETRY_COUNT;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.STATUS_CODE;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.STATUS_MESSAGE;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.STATUS_SUCCESS;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.TRANSACTION_MESSAGE;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.TRANSACTION_STATE;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.TRANSACTION_STATUS;

import io.camunda.zeebe.client.ZeebeClient;
import java.time.Duration;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.mifos.connector.airtel.mockairtel.utils.TransferStatus;
import org.mifos.connector.airtel.service.AirtelService;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelEnquiryResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GetTransactionStatusWorker {

    @Value("${airtel.MAX_RETRY_COUNT}")
    private int maxRetryCount;

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private AirtelService airtelService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void setup() {
        zeebeClient.newWorker().jobType("fetch-transaction-status").handler((client, job) -> {
            logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
            Map<String, Object> variables = job.getVariablesAsMap();

            int retryCount = 1 + (Integer) variables.get(RETRY_COUNT);

            variables.put(RETRY_COUNT, retryCount);

            if (retryCount > maxRetryCount) {
                logger.error("Retry count exhausted for job '{}' from process '{}' with key {}", job.getType(), job.getBpmnProcessId(),
                        job.getKey());
                variables.put(TRANSACTION_STATUS, TransferStatus.TF.name());
                variables.put(TRANSACTION_STATE, "failed");
                String clientCorrelationId = (String) variables.get(CLIENT_CORRELATION_ID);
                zeebeClient.newPublishMessageCommand().messageName(CALL_BACK_RESPONSE).correlationKey(clientCorrelationId)
                        .timeToLive(Duration.ofSeconds(30)).variables(variables).send().join();
                logger.error("Published zeebe message event {} {}", CALL_BACK_RESPONSE, clientCorrelationId);
                logger.error("Failed to fail the job. Retry count exhausted");
            }

            else {
                String transactionId = variables.get(CLIENT_CORRELATION_ID).toString();
                String country = variables.get(COUNTRY).toString();
                String currency = variables.get(CURRENCY).toString();

                AirtelEnquiryResponseDTO airtelEnquiryResponseDTO = airtelService.getTransactionStatus(transactionId, country, currency);

                variables.put(EXTERNAL_TRANSACTION_ID, airtelEnquiryResponseDTO.getData().getTransaction().getAirtelMoneyId());
                variables.put(TRANSACTION_MESSAGE, airtelEnquiryResponseDTO.getData().getTransaction().getMessage());
                variables.put(STATUS_CODE, airtelEnquiryResponseDTO.getStatus().getCode());
                variables.put(STATUS_MESSAGE, airtelEnquiryResponseDTO.getStatus().getMessage());
                variables.put(RESULT_CODE, airtelEnquiryResponseDTO.getStatus().getResultCode());
                variables.put(RESPONSE_CODE, airtelEnquiryResponseDTO.getStatus().getResponseCode());
                variables.put(STATUS_SUCCESS, airtelEnquiryResponseDTO.getStatus().getSuccess());

                String transactionStatus = airtelEnquiryResponseDTO.getData().getTransaction().getStatus();

                if (transactionStatus.equalsIgnoreCase(TransferStatus.TF.name())
                        || transactionStatus.equalsIgnoreCase(TransferStatus.TS.name())) {
                    variables.put(TRANSACTION_STATUS, transactionStatus);
                    String clientCorrelationId = (String) variables.get(CLIENT_CORRELATION_ID);
                    zeebeClient.newPublishMessageCommand().messageName(CALL_BACK_RESPONSE).correlationKey(clientCorrelationId)
                            .timeToLive(Duration.ofSeconds(30)).variables(variables).send().join();
                    logger.error("Published zeebe message event {} {}", CALL_BACK_RESPONSE, clientCorrelationId);

                }

                if (transactionStatus.equalsIgnoreCase(TransferStatus.TS.name())) {
                    variables.put(TRANSACTION_STATE, "successful");
                    logger.info("Transaction successful, transaction status: {}", variables.get(TRANSACTION_STATUS));
                } else if (transactionStatus.equalsIgnoreCase(TransferStatus.TF.name())) {
                    variables.put(TRANSACTION_STATE, "failed");
                    logger.info("Transaction failed, transaction status: {}", variables.get(TRANSACTION_STATUS));
                } else {
                    variables.put(TRANSACTION_STATE, "pending");
                    logger.info("Transaction pending, transaction status: {}", variables.get(TRANSACTION_STATUS));
                }
            }
            client.newCompleteCommand(job.getKey()).variables(variables).send().join();
        }).name("fetch-transaction-status").open();
    }
}
