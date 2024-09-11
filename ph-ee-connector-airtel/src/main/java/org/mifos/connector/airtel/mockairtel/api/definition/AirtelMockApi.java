package org.mifos.connector.airtel.mockairtel.api.definition;

import org.mifos.connector.airtel.mockairtel.api.implementation.AirtelMockController;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelEnquiryResponseDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelPaymentRequestDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelPaymentResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AirtelMockApi {

    @Autowired
    private AirtelMockController airtelMockController;

    @GetMapping(value = "/standard/v1/payments/{transactionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AirtelEnquiryResponseDTO> airtelTransactionEnquiry(@PathVariable String transactionId) {
        return airtelMockController.getTransactionStatus(transactionId);
    }

    @PostMapping("/merchant/v2/payments")
    public ResponseEntity<AirtelPaymentResponseDTO> getAuthorization(@RequestHeader(value = "X-Country") String country,
            @RequestHeader(value = "X-Currency") String currency, @RequestHeader(value = "Authorization") String authorization,
            @RequestHeader(value = "x-signature") String signature, @RequestHeader(value = "x-key") String key,
            @RequestBody AirtelPaymentRequestDTO airtelPaymentRequestDTO) {
        return airtelMockController.initiateTransaction(airtelPaymentRequestDTO);
    }
}
