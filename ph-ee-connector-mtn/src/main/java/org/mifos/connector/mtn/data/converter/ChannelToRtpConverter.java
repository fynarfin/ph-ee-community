package org.mifos.connector.mtn.data.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mifos.connector.mtn.data.Payer;
import org.mifos.connector.mtn.data.RequestToPayDTO;
import org.springframework.stereotype.Component;

@Getter
@Setter
@NoArgsConstructor
public class ChannelToRtpConverter {

    public RequestToPayDTO convertToRtpDto(String transactionId, String channelDTO) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rtpNode = objectMapper.readTree(channelDTO);

        JsonNode payerNode = rtpNode.path("payer").path("partyIdInfo");
        String partyIdType = payerNode.path("partyIdType").asText();
        String partyId = payerNode.path("partyIdentifier").asText();

        JsonNode amountNode = rtpNode.path("amount");
        String amount = amountNode.path("amount").asText();
        String currency = amountNode.path("currency").asText();

        JsonNode payeeNode = rtpNode.path("payee").path("partyIdInfo");

        Payer payer = Payer.builder()
                .partyIdType(partyIdType)
                .partyId(partyId)
                .build();

        RequestToPayDTO dto = RequestToPayDTO.builder()
                .amount(amount)
                .currency(currency)
                .externalId(transactionId)
                .payer(payer)
                .build();

        return dto;
    }
}
