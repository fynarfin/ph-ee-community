package org.mifos.connector.mtn.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestToPaySuccessResponse {
    private String financialTransactionId;
    private String externalId;
    private String amount;
    private String currency;
    private Payer payer;
    private String status;
}
