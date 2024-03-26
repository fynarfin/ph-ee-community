package org.mifos.connector.crm.zeebe;

import static org.mifos.connector.crm.zeebe.ZeebeVariables.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.json.JSONObject;
import org.mifos.connector.crm.data.BillPaymentsReqDTO;
import org.mifos.connector.crm.utils.Headers;
import org.mifos.connector.crm.utils.SpringWrapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ZeebeWorkers {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${zeebe.client.evenly-allocated-max-jobs}")
    private int workerMaxJobs;

    @PostConstruct
    public void setupWorkers() {

        // fetch bill details worker for sync mock api with response
        zeebeClient.newWorker().jobType("fetch-bill").handler((client, job) -> {
            logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
            logWorkerDetails(job);
            Map<String, Object> variables = job.getVariablesAsMap();
            Headers headers = new Headers.HeaderBuilder().addHeader(PLATFORM_TENANT, variables.get(TENANT_ID).toString())
                    .addHeader(CLIENTCORRELATIONID, variables.get(CLIENTCORRELATIONID).toString())
                    .addHeader(PAYER_FSP, variables.get("payerFspId").toString()).addHeader(BILL_ID, variables.get(BILL_ID).toString())
                    .build();
            Exchange exchange = SpringWrapperUtil.getDefaultWrappedExchange(producerTemplate.getCamelContext(), headers, null);
            exchange.setProperty(BILLER_ID, variables.get(BILLER_ID).toString());
            exchange.setProperty(BILLER_NAME, variables.get(BILLER_NAME).toString());
            producerTemplate.send("direct:bill-inquiry", exchange);
            variables.put(BILL_INQUIRY_RESPONSE, exchange.getProperty(BILL_INQUIRY_RESPONSE));
            variables.put(BILL_FETCH_FAILED, exchange.getProperty(BILL_FETCH_FAILED));
            variables.put(AMOUNT,exchange.getProperty(AMOUNT));
            zeebeClient.newCompleteCommand(job.getKey()).variables(variables).send();
            logger.debug("Zeebe variable {}", job.getVariablesAsMap());
        }).name("fetch-bill").maxJobsActive(workerMaxJobs).open();

        // pay bill status worker for sync mock api with response
        zeebeClient.newWorker().jobType("billPay").handler((client, job) -> {
            logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
            logWorkerDetails(job);
            Map<String, Object> variables = job.getVariablesAsMap();
            Headers headers = new Headers.HeaderBuilder().addHeader(PLATFORM_TENANT, variables.get(TENANT_ID).toString())
                    .addHeader(CLIENTCORRELATIONID, variables.get(CLIENTCORRELATIONID).toString())
                    .addHeader(PAYER_FSP, variables.get("payerFspId").toString()).build();
            Gson gson = new Gson();
            BillPaymentsReqDTO requestBody = gson.fromJson((variables.get(BILL_PAYMENTS_REQ).toString()), BillPaymentsReqDTO.class);
            Exchange exchange = SpringWrapperUtil.getDefaultWrappedExchange(producerTemplate.getCamelContext(), headers,
                    objectMapper.writeValueAsString(requestBody));
            producerTemplate.send("direct:bill-payments", exchange);
            variables.put(BILL_PAY_RESPONSE, exchange.getProperty(BILL_PAY_RESPONSE));
            variables.put("code", exchange.getProperty("code"));
            variables.put("status", exchange.getProperty("status"));
            variables.put("reason", exchange.getProperty("reason"));
            variables.put("state", "ACCEPTED");
            variables.put(BILL_PAY_FAILED, exchange.getProperty(BILL_PAY_FAILED));
            zeebeClient.newCompleteCommand(job.getKey()).variables(variables).send();
            logger.debug("Zeebe variable {}", job.getVariablesAsMap());
        }).name("billPay").maxJobsActive(workerMaxJobs).open();

        zeebeClient.newWorker().jobType("billRtpAck").handler((client, job) -> {
            logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
            logWorkerDetails(job);
            Map<String, Object> variables = job.getVariablesAsMap();
            Headers headers = new Headers.HeaderBuilder().addHeader("X-Platform-TenantId", variables.get(TENANT_ID).toString())
                    .addHeader(CLIENTCORRELATIONID, variables.get(CLIENTCORRELATIONID).toString()).build();
            Exchange exchange = SpringWrapperUtil.getDefaultWrappedExchange(producerTemplate.getCamelContext(), headers, null);
            // check before implementing
            producerTemplate.send("direct:send-ack", exchange);
            zeebeClient.newCompleteCommand(job.getKey()).variables(variables).send();
            logger.debug("Zeebe variable {}", job.getVariablesAsMap());
        }).name("billRtpAck").maxJobsActive(workerMaxJobs).open();

        // billRTPResp
        zeebeClient.newWorker().jobType("billRTPResp").handler((client, job) -> {
            logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
            logWorkerDetails(job);
            Map<String, Object> variables = job.getVariablesAsMap();
            Headers headers = new Headers.HeaderBuilder().addHeader("X-Platform-TenantId", variables.get(TENANT_ID).toString())
                    .addHeader(CLIENTCORRELATIONID, variables.get(CLIENTCORRELATIONID).toString()).build();
            Exchange exchange = SpringWrapperUtil.getDefaultWrappedExchange(producerTemplate.getCamelContext(), headers, null);
            producerTemplate.send("direct:bill-rtp-resp", exchange);
            variables.put("billRTPResponse", exchange.getIn().getBody(String.class));
            zeebeClient.newCompleteCommand(job.getKey()).variables(variables).send();
            logger.debug("Zeebe variable {}", job.getVariablesAsMap());
        }).name("billRTPResp").maxJobsActive(workerMaxJobs).open();

    }

    private void logWorkerDetails(ActivatedJob job) {
        JSONObject jsonJob = new JSONObject();
        jsonJob.put("bpmnProcessId", job.getBpmnProcessId());
        jsonJob.put("elementInstanceKey", job.getElementInstanceKey());
        jsonJob.put("jobKey", job.getKey());
        jsonJob.put("jobType", job.getType());
        jsonJob.put("workflowElementId", job.getElementId());
        jsonJob.put("workflowDefinitionVersion", job.getProcessDefinitionVersion());
        jsonJob.put("workflowKey", job.getProcessDefinitionKey());
        jsonJob.put("workflowInstanceKey", job.getProcessInstanceKey());
        logger.info("Job started: {}", jsonJob.toString(4));
    }
}
