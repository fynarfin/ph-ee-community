package org.mifos.connector.ams.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.File;
import java.security.KeyStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Component
public class RestTemplateUtil {

    private static final Logger logger = LoggerFactory.getLogger(RestTemplateUtil.class);

    @Value("${ams.local.keystore-path}")
    private String keystorePath;

    //    @Value("${ams.local.keystore-password}")
    private String keystorePassword = "openmf";

    private RestTemplate restTemplate;

    public RestTemplateUtil() {
        this.restTemplate = createCustomRestTemplate();
    }

    private RestTemplate createCustomRestTemplate() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            logger.info("keystore path is "+ keystorePath);
            logger.info("keystore password is "+keystorePassword);
            keyStore.load(new FileInputStream(new File("keystore.jks")), keystorePassword.toCharArray());

            SSLContext sslContext = SSLContextBuilder
                    .create()
                    .loadTrustMaterial(keyStore, new TrustSelfSignedStrategy())
                    .build();

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
            return new RestTemplate(factory);
        } catch (Exception e) {

            throw new RuntimeException("Error creating RestTemplate with custom SSL context", e);
        }
    }

    public ResponseEntity<String> exchange(String url, HttpMethod method, HttpHeaders headers, Object body) {
        return restTemplate.exchange(url, method, new HttpEntity<>(body, headers), String.class);
    }
}

