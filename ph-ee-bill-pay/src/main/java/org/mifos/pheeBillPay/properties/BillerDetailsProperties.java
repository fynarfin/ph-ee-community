package org.mifos.pheeBillPay.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "billers")
public class BillerDetailsProperties {

    public List<BillerDetails> getDetails() {
        return details;
    }

    public void setDetails(List<BillerDetails> details) {
        this.details = details;
    }

    List<BillerDetails> details;

}
