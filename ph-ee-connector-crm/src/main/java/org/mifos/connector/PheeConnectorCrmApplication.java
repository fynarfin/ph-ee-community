package org.mifos.connector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("org.mifos.connector.crm")
public class PheeConnectorCrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(PheeConnectorCrmApplication.class, args);
    }

}
