package org.mifos.pheeBillPay.camel.routes;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.LoggingLevel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mifos.connector.common.camel.ErrorHandlerRouteBuilder;
import org.mifos.pheeBillPay.data.Bill;
import org.mifos.pheeBillPay.data.BillInquiryResponseDTO;
import org.mifos.pheeBillPay.properties.BillerDetails;
import org.mifos.pheeBillPay.utils.BillPayEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.mifos.pheeBillPay.utils.BillPayEnum.SUCCESS_RESPONSE_CODE;
import static org.mifos.pheeBillPay.utils.BillPayEnum.SUCCESS_RESPONSE_MESSAGE;
import static org.mifos.pheeBillPay.zeebe.ZeebeVariables.*;

@Component
public class BillInquiryRouteBuilder extends ErrorHandlerRouteBuilder {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    BillInquiryResponseDTO billInquiryResponseDTO;

    @Autowired
    private Bill billDetails;

    @Override
    public void configure() {

        from("direct:bill-inquiry-response")
                .routeId("bill-inquiry-response")
                        .log("Triggering callback for bill inquiry response")
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                        .process(exchange -> {
                            String billInquiryResponseDTO =
                                    exchange.getProperty(BILL_INQUIRY_RESPONSE, String.class);
                            exchange.getIn().setBody(billInquiryResponseDTO);
                            logger.info("Bill Inquiry Response: " + billInquiryResponseDTO);
                        })
                .log(LoggingLevel.DEBUG, "Sending bill inquiry response to callback URL: ${exchangeProperty.X-CallbackURL}")
                .toD("https://webhook.site/b44174ab-04b4-4b0d-8426-a3c54bc2f794" + "?bridgeEndpoint=true&throwExceptionOnFailure=false");
    }


}