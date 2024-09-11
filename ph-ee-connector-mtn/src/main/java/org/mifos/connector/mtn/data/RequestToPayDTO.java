package org.mifos.connector.mtn.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestToPayDTO {
    private String amount;
    private String currency;
    private String externalId;
    private Payer payer;
}
