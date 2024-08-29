package org.mifos.connector.mtn.service;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.mifos.connector.mtn.zeebe.ReceiveTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class SendCallbackService {

    @Autowired
    private ReceiveTask receiveTask;

    private static final Logger logger = LoggerFactory.getLogger(SendCallbackService.class);
    private static final int TIMEOUT = 5000;

    public void sendCallback(String body, String callbackURL) {
        logger.info("Sending callback to URL: {}", callbackURL);
        logger.info("Request body: {}", body);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(callbackURL);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(body));

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(TIMEOUT)
                    .setSocketTimeout(TIMEOUT)
                    .build();
            httpPost.setConfig(requestConfig);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int responseCode = response.getStatusLine().getStatusCode();

                if (HttpStatus.valueOf(responseCode).is2xxSuccessful()) {
                    logger.debug("Callback successfully received with response code: {}", responseCode);
                } else {
                    logger.warn("Callback failed with response code: {}", responseCode);
                    String responseBody = EntityUtils.toString(response.getEntity());
                    logger.warn("Response body: {}", responseBody);
                }

            }
        } catch (Exception e) {
            logger.error("Error sending callback", e);
        }
    }
}