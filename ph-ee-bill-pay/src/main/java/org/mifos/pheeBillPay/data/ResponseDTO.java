package org.mifos.pheeBillPay.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO {
    private String ResponseCode;
    private String ResponseDescription;
    private String RequestID;

}
