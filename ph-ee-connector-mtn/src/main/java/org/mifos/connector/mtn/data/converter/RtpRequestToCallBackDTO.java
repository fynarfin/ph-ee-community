package org.mifos.connector.mtn.data.converter;

import org.mifos.connector.mtn.Util.GenerateUUID;
import org.mifos.connector.mtn.data.RequestToPayDTO;
import org.mifos.connector.mtn.data.RequestToPayFailureResponse;
import org.mifos.connector.mtn.data.RequestToPaySuccessResponse;

public class RtpRequestToCallBackDTO {
    public RequestToPayFailureResponse converterForFailureResponse(RequestToPayDTO requestToPayDTO) {

        return RequestToPayFailureResponse.builder()
                .externalId(requestToPayDTO.getExternalId())
                .amount(requestToPayDTO.getAmount())
                .currency(requestToPayDTO.getCurrency())
                .payer(requestToPayDTO.getPayer())
                .status("FAILED")
                .reason("APPROVAL_REJECTED")
                .build();
    }

    public RequestToPaySuccessResponse converterForSuccessfulResponse(RequestToPayDTO requestToPayDTO) {

        String financialTransactionId = GenerateUUID.generateUUID().replace("-", "");
        return RequestToPaySuccessResponse.builder()
                .financialTransactionId(financialTransactionId)
                .externalId(requestToPayDTO.getExternalId())
                .amount(requestToPayDTO.getAmount())
                .currency(requestToPayDTO.getCurrency())
                .payer(requestToPayDTO.getPayer())
                .status("FAILED")
                .build();
    }

}
