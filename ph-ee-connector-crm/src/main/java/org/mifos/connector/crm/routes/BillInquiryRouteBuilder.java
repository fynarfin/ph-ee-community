package org.mifos.connector.crm.routes;

import static org.mifos.connector.crm.utils.BillPayEnum.*;
import static org.mifos.connector.crm.zeebe.ZeebeVariables.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.mifos.connector.common.camel.ErrorHandlerRouteBuilder;
import org.mifos.connector.crm.data.Bill;
import org.mifos.connector.crm.data.BillInquiryResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BillInquiryRouteBuilder extends ErrorHandlerRouteBuilder {

    @Value("${billPay.billIdInvalidId}")
    private String billIdInvalidId;

    @Value("${billPay.billIdEmptyId}")
    private String billIdEmptyId;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    BillInquiryResponseDTO billInquiryResponseDTO;

    @Autowired
    private Bill billDetails;

    @Override
    public void configure() {

        from("direct:bill-inquiry").routeId("bill-inquiry").log("Received request for bill inquiry")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200)).process(exchange -> {
                    logger.debug("Bill Inquiry Id: {}" , exchange.getIn().getHeader(BILL_ID).toString());
                    BillInquiryResponseDTO billInquiryResponseDTO;
                    String billId = exchange.getIn().getHeader(BILL_ID).toString();
                    String billerID = exchange.getProperty(BILLER_ID).toString();
                    String billerName = exchange.getProperty(BILLER_NAME).toString();
                    if(billId.equals(billIdInvalidId)){
                        logger.info("Bill Id is Invalid");
                        billInquiryResponseDTO = setResponseBodyForInvalidBill(
                                exchange.getIn().getHeader(CLIENTCORRELATIONID).toString());
                        exchange.setProperty(BILL_FETCH_FAILED, true);
                        exchange.setProperty(ERROR_INFORMATION,"Bill Fetch failed: Invalid Bill Id");
                    }
                    else if(billId.equals(billIdEmptyId)){
                        logger.info("Bill Id is Empty");
                        billInquiryResponseDTO = setResponseBodyForEmptyBill(
                                exchange.getIn().getHeader(CLIENTCORRELATIONID).toString());
                        exchange.setProperty(BILL_FETCH_FAILED, true);
                        exchange.setProperty(ERROR_INFORMATION,"Bill Fetch failed: Empty bill ID");
                    }
                    else {
                        billInquiryResponseDTO = setResponseBody(
                                exchange.getIn().getHeader(CLIENTCORRELATIONID).toString(), billId, billerID, billerName);
                        exchange.setProperty(BILL_FETCH_FAILED, false);
                    }
                    exchange.setProperty(BILL_INQUIRY_RESPONSE, billInquiryResponseDTO);
                    exchange.setProperty(AMOUNT,billInquiryResponseDTO.getBillDetails().getAmountonDueDate());
                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonString = objectMapper.writeValueAsString(billInquiryResponseDTO);
                    exchange.getIn().setBody(jsonString);
                    logger.debug("Bill Inquiry Response: {}", jsonString);
                });
    }

    private BillInquiryResponseDTO setResponseBody(String clientCorrelationId, String billId, String billerID, String billerName) {

        billDetails.setBillerId(billerID);
        billDetails.setBillerName(billerName);
        billDetails.setAmountonDueDate("1000");
        billDetails.setAmountAfterDueDate("1100");
        billDetails.setDueDate("2021-07-01");
        billDetails.setBillStatus("PAID");
        billInquiryResponseDTO.setBillDetails(billDetails);
        billInquiryResponseDTO.setBillId(billId);
        billInquiryResponseDTO.setCode(SUCCESS_RESPONSE_CODE.getValue());
        billInquiryResponseDTO.setReason(SUCCESS_RESPONSE_MESSAGE.getValue());
        billInquiryResponseDTO.setClientCorrelationId(clientCorrelationId);
        return billInquiryResponseDTO;
    }
    private BillInquiryResponseDTO setResponseBodyForInvalidBill(String clientCorrelationId) {
        billInquiryResponseDTO.setCode(FAILED_RESPONSE_CODE.getValue());
        billInquiryResponseDTO.setReason("Invalid Bill ID");
        billInquiryResponseDTO.setClientCorrelationId(clientCorrelationId);
        return billInquiryResponseDTO;
    }
    private BillInquiryResponseDTO setResponseBodyForEmptyBill(String clientCorrelationId) {
        billInquiryResponseDTO.setCode(FAILED_RESPONSE_CODE.getValue());
        billInquiryResponseDTO.setReason("Empty Bill ID");
        billInquiryResponseDTO.setClientCorrelationId(clientCorrelationId);
        return billInquiryResponseDTO;
    }


}
