package org.mifos.connector.crm.data;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
public class BillRTPReqDTO implements Serializable {

    private String billRequestId;
    private String billId;
    private String status;
    private String rejectionReason;

}
