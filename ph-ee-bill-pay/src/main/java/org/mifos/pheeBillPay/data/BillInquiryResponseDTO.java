package org.mifos.pheeBillPay.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
public class BillInquiryResponseDTO implements Serializable {
    private String transactionId;

    public void setTransactionId(String s) {
    }
    //private List<PaymentModalityDTO> paymentModalityList;
}
