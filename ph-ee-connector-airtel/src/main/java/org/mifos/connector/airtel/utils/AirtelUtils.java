package org.mifos.connector.airtel.utils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelPaymentRequestDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelPaymentRequestSubscriberDTO;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelPaymentRequestTransactionDTO;
import org.springframework.stereotype.Component;

@Component
public class AirtelUtils {

    private int parseAmount(String amount) {
        try {
            int parsedAmount = Integer.parseInt(amount);
            if (parsedAmount <= 0) {
                throw new IllegalArgumentException("Invalid amount. Amount should be a positive integer");
            }
            return parsedAmount;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format. Amount should be a valid integer", e);
        }
    }

    public AirtelPaymentRequestDTO convertToAirtelPaymentRequestDTO(String channelRequest, String clientCorrelationId, String msisdn,
            String country) {
        JSONObject channelRequestJson = new JSONObject(channelRequest);

        String currency = channelRequestJson.getJSONObject("amount").getString("currency");
        String amount = channelRequestJson.getJSONObject("amount").getString("amount");
        String reference = channelRequestJson.getString("note");

        AirtelPaymentRequestSubscriberDTO subscriberDTO = new AirtelPaymentRequestSubscriberDTO(country, currency, msisdn);
        AirtelPaymentRequestTransactionDTO transactionDTO = new AirtelPaymentRequestTransactionDTO(parseAmount(amount), country, currency,
                clientCorrelationId);
        return new AirtelPaymentRequestDTO(reference, subscriberDTO, transactionDTO);
    }

    public String channelRequestToGSMAConvertor(String channelRequest) {
        JSONObject gsmaChannelRequestJson = new JSONObject();
        JSONObject channelRequestJson = new JSONObject(channelRequest);

        String requestingOrganisationTransactionReference = channelRequestJson.getString("note");
        String amount = channelRequestJson.getJSONObject("amount").getString("amount");
        String currency = channelRequestJson.getJSONObject("amount").getString("currency");

        gsmaChannelRequestJson.put("requestingOrganisationTransactionReference", requestingOrganisationTransactionReference);
        gsmaChannelRequestJson.put("subType", "inbound");
        gsmaChannelRequestJson.put("type", "transfer");
        gsmaChannelRequestJson.put("amount", amount);
        gsmaChannelRequestJson.put("currency", currency);
        gsmaChannelRequestJson.put("descriptionText", requestingOrganisationTransactionReference);

        ZonedDateTime now = Instant.now().atZone(ZoneOffset.UTC);
        String formattedDate = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(now);
        gsmaChannelRequestJson.put("requestDate", formattedDate);

        // Handling customData
        JSONArray customData = channelRequestJson.getJSONArray("customData");
        gsmaChannelRequestJson.put("customData", customData);

        // Handling payer and payee
        JSONArray payerArray = channelRequestJson.getJSONArray("payer");
        JSONArray gsmaPayerArray = new JSONArray();
        JSONArray gsmaPayeeArray = new JSONArray();

        for (int i = 0; i < payerArray.length(); i++) {
            JSONObject payerObject = payerArray.getJSONObject(i);
            String key = payerObject.getString("key");
            String value = payerObject.getString("value");

            if (key.equalsIgnoreCase("MSISDN")) {
                JSONObject gsmaPayerObject = new JSONObject();
                gsmaPayerObject.put("partyIdType", "MSISDN");
                gsmaPayerObject.put("partyIdIdentifier", value);
                gsmaPayerArray.put(gsmaPayerObject);
            } else if (key.equalsIgnoreCase("ACCOUNTID")) {
                JSONObject gsmaPayeeObject = new JSONObject();
                gsmaPayeeObject.put("partyIdType", "accountId");
                gsmaPayeeObject.put("partyIdIdentifier", value);
                gsmaPayeeArray.put(gsmaPayeeObject);
            }
        }

        gsmaChannelRequestJson.put("payer", gsmaPayerArray);
        gsmaChannelRequestJson.put("payee", gsmaPayeeArray);

        return gsmaChannelRequestJson.toString();
    }

}
