package org.mifos.connector.airtel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
@SuppressWarnings({ "HideUtilityClassConstructor" })
public class AirtelConnectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AirtelConnectorApplication.class, args);
    }

}
