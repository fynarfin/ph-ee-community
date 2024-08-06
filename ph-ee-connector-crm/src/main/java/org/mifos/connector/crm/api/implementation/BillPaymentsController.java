package org.mifos.connector.crm.api.implementation;

import static org.mifos.connector.crm.zeebe.ZeebeVariables.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.ExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.mifos.connector.crm.api.definition.BillPaymentsApi;
import org.mifos.connector.crm.data.BillPaymentsReqDTO;
import org.mifos.connector.crm.data.BillPaymentsResponseDTO;
import org.mifos.connector.crm.utils.Headers;
import org.mifos.connector.crm.utils.SpringWrapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BillPaymentsController implements BillPaymentsApi {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public BillPaymentsResponseDTO billPayments(String tenantId, String correlationId, String payerFspId, BillPaymentsReqDTO requestBody)
            throws ExecutionException, InterruptedException, JsonProcessingException {
        Headers headers = new Headers.HeaderBuilder().addHeader(PLATFORM_TENANT, tenantId).addHeader(CLIENTCORRELATIONID, correlationId)
                .addHeader(PAYER_FSP, payerFspId).build();
        Exchange exchange = SpringWrapperUtil.getDefaultWrappedExchange(producerTemplate.getCamelContext(), headers,
                objectMapper.writeValueAsString(requestBody));
        producerTemplate.send("direct:bill-payments", exchange);
        return exchange.getIn().getBody(BillPaymentsResponseDTO.class);
    }
}
