package org.mifos.connector.ams.utils;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateUtil {

    private static final Logger logger = LoggerFactory.getLogger(RestTemplateUtil.class);

    private String keystorePath = "keystore.jks";

    private String keystorePassword = "openmf";

    private RestTemplate restTemplate;

    public RestTemplateUtil() {
        this.restTemplate = createCustomRestTemplate();
    }

    private RestTemplate createCustomRestTemplate() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(new FileInputStream(new File(keystorePath)), keystorePassword.toCharArray());

            SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(keyStore, new TrustSelfSignedStrategy()).build();

            CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();

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
