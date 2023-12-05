package org.mifos.pheeBillPay.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillInquiryResponseDTO implements Serializable {
    private String transactionId;
    //private List<PaymentModalityDTO> paymentModalityList;
}
