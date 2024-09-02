package org.mifos.connector.mtn.zeebe;

import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ZeebeClientConfiguration {

    @Value("${zeebe.broker.contactpoint}")
    private String zeebeBrokerContactpoint;

    @Value("${zeebe.client.max-execution-threads}")
    private int zeebeClientMaxThreads;

    @Value("${zeebe.client.poll-interval}")
    private int zeebeClientPollInterval;

    @Bean
    public ZeebeClient setup() {
        return ZeebeClient.newClientBuilder().gatewayAddress(zeebeBrokerContactpoint).usePlaintext()
                .defaultJobPollInterval(Duration.ofMillis(zeebeClientPollInterval)).defaultJobWorkerMaxJobsActive(2000)
                .numJobWorkerExecutionThreads(zeebeClientMaxThreads).build();
    }
}
