package org.mifos.connector.ams.paygops.paygopsDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PaygopsResponseDto {

    /*"transaction_id": "simon_nationalID_test2",
    "amount": "123",
    "sender_name": "Simon Test2 National ID",
    "sender_phone_number": "+25512345676",
    "sent_datetime": "2022-04-25T12:07:58.048847Z",
    "memo": "abd1",
    "wallet_operator": "",
    "country": "",
    "currency": "",
    "warnings": [],
    "reconciled": false
    "destination_type": "contract_repayment",
    "destination": "C391270014"
    */
    @JsonProperty("transaction_id")
    private String transaction_id;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("sender_name")
    private String sender_name;

    @JsonProperty("sender_phone_number")
    private String sender_phone_number;

    @JsonProperty("sent_datetime")
    private String sent_datetime;

    @JsonProperty("memo")
    private String memo;

    @JsonProperty("wallet_operator")
    private String wallet_operator;

    @JsonProperty("wallet_name")
    private String wallet_name;

    @JsonProperty("wallet_msisdn")
    private String wallet_msisdn;

    @JsonProperty("reception_datetime")
    private String reception_datetime;

    @JsonProperty("country")
    private String country;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("warnings")
    private String [] warnings = new String[1];

    @JsonProperty("reconciled")
    private Boolean reconciled;

    @JsonProperty("destination_type")
    private String destination_type;

    @JsonProperty("destination")
    private String destination;

    @JsonProperty("destinations")
    private Object[] destinations = new Object[1];
}
