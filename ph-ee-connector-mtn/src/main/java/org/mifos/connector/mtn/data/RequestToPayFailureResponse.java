package org.mifos.connector.mtn.data;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestToPayFailureResponse {
    private String externalId;
    private String amount;
    private String currency;
    private Payer payer;
    private String status;
    private String reason;
}
