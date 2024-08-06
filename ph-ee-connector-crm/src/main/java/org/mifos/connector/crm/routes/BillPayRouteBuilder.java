package org.mifos.connector.crm.routes;

import static org.mifos.connector.crm.utils.BillPayEnum.*;
import static org.mifos.connector.crm.zeebe.ZeebeVariables.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mifos.connector.common.camel.ErrorHandlerRouteBuilder;
import org.mifos.connector.crm.data.BillPaymentsReqDTO;
import org.mifos.connector.crm.data.BillPaymentsResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BillPayRouteBuilder extends ErrorHandlerRouteBuilder {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${billPay.billAlreadyPaidId}")
    private String billAlreadyPaidId;

    @Value("${billPay.billPayTimeoutId}")
    private String billPayTimeoutId;
    @Autowired
    BillPaymentsResponseDTO billPaymentsResponseDTO;

    @Override
    public void configure() {

        from("direct:bill-payments").routeId("bill-payments").log("Received request for bill payments").unmarshal()
                .json(JsonLibrary.Jackson, BillPaymentsReqDTO.class).setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .process(exchange -> {
                    BillPaymentsResponseDTO response;
                    logger.debug("Bill Payments Request: {}" ,exchange.getIn().getBody(BillPaymentsReqDTO.class));
                    if(exchange.getIn().getBody(BillPaymentsReqDTO.class).getBillId().equals(billAlreadyPaidId)){
                        response =
                                setResponseBodyForBillPaid(exchange.getIn().getBody(BillPaymentsReqDTO.class));
                    }
                    else if(exchange.getIn().getBody(BillPaymentsReqDTO.class).getBillId().equals(billPayTimeoutId)){
                        Thread.sleep(15000);
                        response =
                                setResponseBodyForBillPayTimeout(exchange.getIn().getBody(BillPaymentsReqDTO.class));
                    }
                    else {
                       response =
                               setResponseBodyForSuccess(exchange.getIn().getBody(BillPaymentsReqDTO.class));
                    }
                    exchange.setProperty("billPayFailed", false);
                    exchange.setProperty(BILL_PAY_RESPONSE, response);
                    exchange.setProperty("reason", billPaymentsResponseDTO.getReason());
                    exchange.setProperty("code", billPaymentsResponseDTO.getCode());
                    exchange.setProperty("status", billPaymentsResponseDTO.getStatus());
                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonString = objectMapper.writeValueAsString(response);
                    exchange.getIn().setBody(jsonString);
                    logger.debug("Bill Payments Response: {}", response);
                });

    }

    private BillPaymentsResponseDTO setResponseBodyForSuccess(BillPaymentsReqDTO billPaymentsReqDTO) {

        billPaymentsResponseDTO.setBillId(billPaymentsReqDTO.getBillId());
        billPaymentsResponseDTO.setCode(SUCCESS_RESPONSE_CODE.getValue());
        billPaymentsResponseDTO.setReason(SUCCESS_RESPONSE_MESSAGE.getValue());
        billPaymentsResponseDTO.setStatus(SUCCESS_STATUS.getValue());
        billPaymentsResponseDTO.setRequestID(billPaymentsReqDTO.getBillInquiryRequestId());
        billPaymentsResponseDTO.setPaymentReferenceID(billPaymentsReqDTO.getPaymentReferenceID());
        return billPaymentsResponseDTO;
    }
    private BillPaymentsResponseDTO setResponseBodyForBillPaid(BillPaymentsReqDTO billPaymentsReqDTO) {

        billPaymentsResponseDTO.setBillId(billPaymentsReqDTO.getBillId());
        billPaymentsResponseDTO.setCode(FAILED_RESPONSE_CODE.getValue());
        billPaymentsResponseDTO.setReason(FAILED_DUPLICATE_PAYMENT_MESSAGE.getValue());
        billPaymentsResponseDTO.setStatus(FAILED_STATUS.getValue());
        billPaymentsResponseDTO.setRequestID(billPaymentsReqDTO.getBillInquiryRequestId());
        billPaymentsResponseDTO.setPaymentReferenceID(billPaymentsReqDTO.getPaymentReferenceID());
        return billPaymentsResponseDTO;
    }
    private BillPaymentsResponseDTO setResponseBodyForBillPayTimeout(BillPaymentsReqDTO billPaymentsReqDTO) {

        billPaymentsResponseDTO.setBillId(billPaymentsReqDTO.getBillId());
        billPaymentsResponseDTO.setCode(FAILED_RESPONSE_CODE.getValue());
        billPaymentsResponseDTO.setReason(FAILED_TIMEOUT_MESSAGE.getValue());
        billPaymentsResponseDTO.setStatus(FAILED_STATUS.getValue());
        billPaymentsResponseDTO.setRequestID(billPaymentsReqDTO.getBillInquiryRequestId());
        billPaymentsResponseDTO.setPaymentReferenceID(billPaymentsReqDTO.getPaymentReferenceID());
        return billPaymentsResponseDTO;
    }

}
