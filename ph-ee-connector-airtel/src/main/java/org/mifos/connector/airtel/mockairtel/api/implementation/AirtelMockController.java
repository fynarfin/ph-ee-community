package org.mifos.connector.airtel.mockairtel.api.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.mifos.connector.airtel.mockairtel.utils.TransferStatus;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelCallBackRequestDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelCallBackRequestTransactionDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelEnquiryResponseDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelEnquiryResponseDataDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelEnquiryResponseDataTransactionDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelPaymentRequestDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelPaymentResponseDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelPaymentResponseDataDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelPaymentResponseDataTransactionDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelResponseStatusDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AirtelMockController {

    @Autowired
    ObjectMapper objectMapper;

    @Value("${airtel.endpoints.contact-point}")
    public String airtelContactPoint;

    @Value("${server.port}")
    public String port;

    @Value("${airtel.endpoints.call-back}")
    public String callbackEndpoint;

    @Value("${mock-airtel.MSISDN_FAILED}")
    private String msisdnFailed;

    @Value("${mock-airtel.CLIENT_CORRELATION_ID_SUCCESSFUL}")
    private String transactionIdSuccessful;

    @Value("${mock-airtel.CLIENT_CORRELATION_ID_FAILED}")
    private String transactionIdFailed;

    @Value("${mock-airtel.SUCCESS_RESPONSE_CODE}")
    private String successResponseCode;

    @Value("${mock-airtel.FAILED_RESPONSE_CODE}")
    private String failedResponseCode;

    @Value("${mock-airtel.PENDING_RESPONSE_CODE}")
    private String pendingResponseCode;

    @Value("${mock-airtel.RESULT_CODE}")
    private String resultCode;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity<AirtelEnquiryResponseDTO> getTransactionStatus(String transactionId) {
        String airtelMoneyId = UUID.nameUUIDFromBytes(transactionId.getBytes(StandardCharsets.UTF_8)).toString().replace("-", "");
        String message;
        String status;
        String code;
        String responseCode;
        boolean success;
        HttpStatus httpStatus;

        if (transactionId.equals(transactionIdSuccessful)) {
            message = TransferStatus.SUCCESS.name();
            status = TransferStatus.TS.name();
            code = HttpStatus.OK.toString();
            responseCode = successResponseCode;
            success = true;
            httpStatus = HttpStatus.OK;
        }

        else if (transactionId.equals(transactionIdFailed)) {
            message = TransferStatus.FAILED.name();
            status = TransferStatus.TF.name();
            code = HttpStatus.BAD_REQUEST.toString();
            responseCode = failedResponseCode;
            success = false;
            httpStatus = HttpStatus.BAD_REQUEST;
        }

        else {
            message = TransferStatus.IN_PROGRESS.name();
            status = TransferStatus.TIP.name();
            code = HttpStatus.ACCEPTED.toString();
            responseCode = pendingResponseCode;
            success = true;
            httpStatus = HttpStatus.ACCEPTED;
        }

        return airtelEnquiryResponse(airtelMoneyId, transactionId, message, status, code, responseCode, success, httpStatus);
    }

    public ResponseEntity<AirtelPaymentResponseDTO> initiateTransaction(AirtelPaymentRequestDTO airtelPaymentRequestDTO) {
        String code = HttpStatus.OK.name();
        HttpStatus httpStatus = HttpStatus.OK;
        String msisdn = airtelPaymentRequestDTO.getSubscriber().getMsisdn();

        String message;
        String status;
        String responseCode;
        boolean success;

        if (msisdn.equals(msisdnFailed)) {
            message = TransferStatus.FAILED.name();
            responseCode = failedResponseCode;
            success = false;
            AirtelResponseStatusDTO airtelResponseStatusDTO = new AirtelResponseStatusDTO(code, message, resultCode, responseCode, success);
            AirtelPaymentResponseDTO responseEntity = new AirtelPaymentResponseDTO(null, airtelResponseStatusDTO);
            return ResponseEntity.status(httpStatus).body(responseEntity);
        }

        else {
            message = TransferStatus.IN_PROGRESS.name();
            status = TransferStatus.IN_PROGRESS.name();
            responseCode = pendingResponseCode;
            success = true;

            String transactionId = airtelPaymentRequestDTO.getTransaction().getId();
            sendCallback(transactionId);
        }

        boolean id = false;
        AirtelPaymentResponseDataTransactionDTO airtelPaymentResponseDataTransactionDTO = new AirtelPaymentResponseDataTransactionDTO(id,
                status);
        AirtelPaymentResponseDataDTO airtelResponseDataDTO = new AirtelPaymentResponseDataDTO(airtelPaymentResponseDataTransactionDTO);
        AirtelResponseStatusDTO airtelResponseStatusDTO = new AirtelResponseStatusDTO(code, message, resultCode, responseCode, success);
        AirtelPaymentResponseDTO responseEntity = new AirtelPaymentResponseDTO(airtelResponseDataDTO, airtelResponseStatusDTO);
        return ResponseEntity.status(httpStatus).body(responseEntity);
    }

    private ResponseEntity<AirtelEnquiryResponseDTO> airtelEnquiryResponse(String airtelMoneyId, String transactionId, String message,
            String status, String code, String responseCode, Boolean success, HttpStatus httpStatus) {
        String id = transactionId;

        AirtelEnquiryResponseDataTransactionDTO airtelEnquiryResponseDataTransactionDTO = new AirtelEnquiryResponseDataTransactionDTO(
                airtelMoneyId, id, message, status);
        AirtelEnquiryResponseDataDTO airtelResponseDataDTO = new AirtelEnquiryResponseDataDTO(airtelEnquiryResponseDataTransactionDTO);

        AirtelResponseStatusDTO airtelResponseStatusDTO = new AirtelResponseStatusDTO(code, message, resultCode, responseCode, success);

        AirtelEnquiryResponseDTO responseEntity = new AirtelEnquiryResponseDTO(airtelResponseDataDTO, airtelResponseStatusDTO);
        return ResponseEntity.status(httpStatus).body(responseEntity);
    }

    @Async
    public void sendCallback(String transactionId) {
        String url = airtelContactPoint + ":" + port + callbackEndpoint;
        HttpHeaders headers = new HttpHeaders();
        String airtelMoneyId = UUID.nameUUIDFromBytes(transactionId.getBytes(StandardCharsets.UTF_8)).toString().replace("-", "");
        String statusCode = TransferStatus.TF.name();

        if (transactionId.equals(transactionIdSuccessful)) {
            statusCode = TransferStatus.TS.name();
        }

        AirtelCallBackRequestTransactionDTO transactionDTO = new AirtelCallBackRequestTransactionDTO();
        transactionDTO.setId(transactionId);
        transactionDTO.setMessage("Paid amount x");
        transactionDTO.setStatusCode(statusCode);
        transactionDTO.setAirtelMoneyId(airtelMoneyId);

        AirtelCallBackRequestDTO callbackRequestDTO = new AirtelCallBackRequestDTO();
        callbackRequestDTO.setTransaction(transactionDTO);
        try {
            // Sleep for 1 second before sending callback
            // Thread.sleep(1000);
            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(callbackRequestDTO, headers), AirtelCallBackRequestDTO.class);
        } catch (Exception ex) {
            throw new RuntimeException("Invalid response!", ex);
        }
    }
}
