package org.mifos.connector.mtn.data.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mifos.connector.mtn.Util.GenerateUUID;
import org.mifos.connector.mtn.data.Payer;
import org.mifos.connector.mtn.data.RequestToPaySuccessResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChannelToRtpCallback {

    @Value("${payerIdentifier.reject}")
    private String rejectPayerId;

    public RequestToPaySuccessResponse convertToRtpCallbackResponseDTO(String channelDto) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dto1Node = objectMapper.readTree(channelDto);

        JsonNode payerNode = dto1Node.path("payer").path("partyIdInfo");
        Payer payer = Payer.builder()
                .partyIdType(payerNode.path("partyIdType").asText())
                .partyId(payerNode.path("partyIdentifier").asText())
                .build();

        JsonNode amountNode = dto1Node.path("amount");
        String amount = amountNode.path("amount").asText();
        String currency = amountNode.path("currency").asText();

        JsonNode payeeNode = dto1Node.path("payee").path("partyIdInfo");
        String externalId = payeeNode.path("partyIdentifier").asText();

        String financialTransactionId = GenerateUUID.generateUUID().replace("-", "");

        RequestToPaySuccessResponse dto =  RequestToPaySuccessResponse.builder()
                .financialTransactionId(financialTransactionId)
                .externalId(externalId)
                .amount(amount)
                .currency(currency)
                .payer(payer)
                .status("SUCCESSFUL")
                .build();

        if(payer.getPartyId().equals(rejectPayerId)) {
            return prepareFailureDto(dto);
        }

        return dto;
    }

    public RequestToPaySuccessResponse prepareFailureDto(RequestToPaySuccessResponse dto) {
        dto.setFinancialTransactionId(null);
        dto.setStatus("FAILED");
        return dto;
    }



}
