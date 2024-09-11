package org.mifos.connector.airtel.api.implementation;

import static org.mifos.connector.airtel.zeebe.ZeebeVariables.CALLBACK_MESSAGE;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.CALL_BACK_RESPONSE;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.EXTERNAL_TRANSACTION_ID;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.TRANSACTION_STATE;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.TRANSACTION_STATUS;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.mifos.connector.airtel.mockairtel.utils.TransferStatus;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelCallBackRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CallBackController {

    @Autowired
    ZeebeClient zeebeClient;

    @Autowired
    ObjectMapper objectMapper;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity<AirtelCallBackRequestDTO> handleCallBackRequest(AirtelCallBackRequestDTO requestBody) {
        Map<String, Object> variables = new HashMap<>();

        String transactionStatus = requestBody.getTransaction().getStatusCode();
        variables.put(TRANSACTION_STATUS, transactionStatus);

        String transactionState = "failed";

        if (transactionStatus.equals(TransferStatus.TS.name())) {
            transactionState = "successful";
        }

        String clientCorrelationId = requestBody.getTransaction().getId();
        variables.put(TRANSACTION_STATE, transactionState);
        variables.put(CALLBACK_MESSAGE, requestBody.getTransaction().getMessage());
        variables.put(EXTERNAL_TRANSACTION_ID, requestBody.getTransaction().getAirtelMoneyId());

        if (zeebeClient != null) {
            zeebeClient.newPublishMessageCommand().messageName(CALL_BACK_RESPONSE).correlationKey(clientCorrelationId)
                    .timeToLive(Duration.ofSeconds(30)).variables(variables).send().join();
            logger.info("Published zeebe message event {}", CALL_BACK_RESPONSE);
        }

        return ResponseEntity.status(HttpStatus.OK).body(requestBody);
    }
}
