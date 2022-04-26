package org.mifos.connector.ams.paygops;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class HealthCheck extends RouteBuilder {


    @Override
    public void configure() throws Exception {
        from("rest:GET:/")
                //.to("direct:transfer-settlement-base")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setBody(constant("GET Good"));

        from("rest:POST:/")
                //.to("direct:transfer-settlement-base")
                .log(LoggingLevel.INFO, "POST Body: ${body}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setBody(constant("All Post Good"));

    }
}
