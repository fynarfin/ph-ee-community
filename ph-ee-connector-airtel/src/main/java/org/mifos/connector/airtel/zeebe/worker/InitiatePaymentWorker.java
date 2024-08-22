package org.mifos.connector.airtel.zeebe.worker;

import static org.mifos.connector.airtel.zeebe.ZeebeVariables.CHANNEL_REQUEST;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.CLIENT_CORRELATION_ID;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.COUNTRY;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.CURRENCY;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.PHONE_NUMBER;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.REQUEST_BODY;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.RESPONSE_CODE;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.RESULT_CODE;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.RETRY_COUNT;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.STATUS_MESSAGE;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.STATUS_SUCCESS;
import static org.mifos.connector.airtel.zeebe.ZeebeVariables.TRANSACTION_STATUS_FAILED;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.mifos.connector.airtel.service.AirtelService;
import org.mifos.connector.airtel.utils.AirtelUtils;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelPaymentRequestDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelPaymentResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InitiatePaymentWorker {

    @Value("${airtel.endpoints.call-back}")
    private String callBackEndpoint;

    @Value("${mock-airtel.SUCCESS_RESPONSE_CODE}")
    private String successResponseCode;

    @Value("${mock-airtel.FAILED_RESPONSE_CODE}")
    private String failedResponseCode;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private AirtelService airtelService;

    @Autowired
    private AirtelUtils airtelUtils;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void setup() {
        zeebeClient.newWorker().jobType("initiate-payment").handler((client, job) -> {
            logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
            Map<String, Object> variables = job.getVariablesAsMap();
            variables.put(RETRY_COUNT, 0);
            String requestBody = variables.get(REQUEST_BODY).toString();
            String clientCorrelationId = variables.get(CLIENT_CORRELATION_ID).toString();
            String msisdn = variables.get(PHONE_NUMBER).toString();
            String country = variables.get(COUNTRY).toString();
            String currency = variables.get(CURRENCY).toString();
            AirtelPaymentRequestDTO airtelPaymentRequestDTO = airtelUtils.convertToAirtelPaymentRequestDTO(requestBody, clientCorrelationId,
                    msisdn, country);

            AirtelPaymentResponseDTO airtelPaymentResponseDTO = airtelService.initiateTransaction(airtelPaymentRequestDTO, country,
                    currency);

            variables.put(STATUS_MESSAGE, airtelPaymentResponseDTO.getStatus().getMessage());
            variables.put(RESULT_CODE, airtelPaymentResponseDTO.getStatus().getResultCode());
            variables.put(STATUS_SUCCESS, airtelPaymentResponseDTO.getStatus().getSuccess());
            variables.put(RESPONSE_CODE, airtelPaymentResponseDTO.getStatus().getResponseCode());

            String gsmaChannelRequest = airtelUtils.channelRequestToGSMAConvertor(requestBody);
            variables.put(CHANNEL_REQUEST, gsmaChannelRequest);

            if (airtelPaymentResponseDTO.getData() != null) {
                variables.put(TRANSACTION_STATUS_FAILED, false);
            } else {
                variables.put(TRANSACTION_STATUS_FAILED, true);
            }
            client.newCompleteCommand(job.getKey()).variables(variables).send().join();
        }).name("initiate-payment").open();
    }

}
