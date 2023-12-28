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

/*
 * Sample response { “code”: “00”, “reason”: “Bill Inquiry Successful” “clientCorrelationId”: “915251236706”, “billId”:
 * “123456789101112”, “billDetails”: [ “billerId”: “1232211”, “billerName”: “Govt. Entity”, “billStatus”: “Unpaid”,
 * “dueDate”: “04/17/23” “amountonDueDate”: “2550”, “amountAfterDueDate”: “2570”,} }
 */
@Component
public class BillInquiryResponseDTO implements Serializable {

    private String code;
    private String reason;
    private String clientCorrelationId;
    private String billId;
    private Bill billDetails;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getClientCorrelationId() {
        return clientCorrelationId;
    }

    public void setClientCorrelationId(String clientCorrelationId) {
        this.clientCorrelationId = clientCorrelationId;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public Bill getBillDetails() {
        return billDetails;
    }

    public void setBillDetails(Bill billDetails) {
        this.billDetails = billDetails;
    }

    @Override
    public String toString() {
        return "BillInquiryResponseDTO{" + "code='" + code + '\'' + ", reason='" + reason + '\'' + ", clientCorrelationId='"
                + clientCorrelationId + '\'' + ", billId='" + billId + '\'' + ", billDetails=" + billDetails + '}';
    }
}
