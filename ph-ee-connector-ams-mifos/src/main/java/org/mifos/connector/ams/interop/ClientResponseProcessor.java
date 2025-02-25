package org.mifos.connector.ams.interop;

import static org.mifos.connector.ams.camel.config.CamelProperties.ZEEBE_JOB_KEY;
import static org.mifos.connector.ams.zeebe.ZeebeVariables.PARTY_ID;
import static org.mifos.connector.ams.zeebe.ZeebeVariables.PARTY_ID_TYPE;
import static org.mifos.connector.ams.zeebe.ZeebeVariables.PARTY_VALIDATION;
import static org.mifos.connector.ams.zeebe.ZeebeVariables.PAYEE_PARTY_RESPONSE;
import static org.mifos.connector.ams.zeebe.ZeebeVariables.TENANT_ID;
import static org.mifos.connector.common.ams.dto.LegalForm.PERSON;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.mifos.connector.ams.errorhandler.ErrorTranslator;
import org.mifos.connector.ams.properties.TenantProperties;
import org.mifos.connector.ams.utils.Utils;
import org.mifos.connector.common.ams.dto.ClientData;
import org.mifos.connector.common.ams.dto.Customer;
import org.mifos.connector.common.ams.dto.EnumOptionData;
import org.mifos.connector.common.ams.dto.LegalForm;
import org.mifos.connector.common.mojaloop.dto.ComplexName;
import org.mifos.connector.common.mojaloop.dto.Party;
import org.mifos.connector.common.mojaloop.dto.PartyIdInfo;
import org.mifos.connector.common.mojaloop.dto.PersonalInfo;
import org.mifos.connector.common.mojaloop.type.IdentifierType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
// @ConditionalOnExpression("${ams.local.enabled}")
public class ClientResponseProcessor implements Processor {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${ams.local.version}")
    private String amsVersion;

    @Autowired
    private TenantProperties tenantProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private ZeebeClient zeebeClient;

    @Autowired
    private ErrorTranslator errorTranslator;

    Boolean partyValidation;

    @Override
    public void process(Exchange exchange) throws Exception {
        Integer responseCode = exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
        String partyIdType = exchange.getProperty(PARTY_ID_TYPE, String.class);
        String partyId = exchange.getProperty(PARTY_ID, String.class);

        if (responseCode > 202) {
            partyValidation = false;
            Map<String, Object> variables = Utils.getDefaultZeebeErrorVariable(exchange, errorTranslator);
            logger.info("Setting error info: {}", variables);
            variables.put(PARTY_VALIDATION, partyValidation);
            zeebeClient.newCompleteCommand(exchange.getProperty(ZEEBE_JOB_KEY, Long.class)).variables(variables).send();
        } else {
            partyValidation = false;
            if (responseCode == 200) {
                partyValidation = true;
            }
            Party mojaloopParty = new Party(new PartyIdInfo(IdentifierType.valueOf(partyIdType), partyId, null,
                    tenantProperties.getTenant(exchange.getProperty(TENANT_ID, String.class)).getFspId()), null, null, null);

            if ("1.2".equals(amsVersion)) {
                ClientData client = exchange.getIn().getBody(ClientData.class);
                EnumOptionData legalForm = client.getLegalForm();
                if (legalForm == null
                        || (legalForm.getValue() != null && PERSON.equals(LegalForm.valueOf(legalForm.getValue().toUpperCase())))) {
                    PersonalInfo pi = new PersonalInfo();
                    pi.setDateOfBirth(client.getDateOfBirth() != null ? client.getDateOfBirth().toString() : null);
                    ComplexName cn = new ComplexName();
                    cn.setFirstName(client.getFirstname());
                    cn.setLastName(client.getLastname());
                    if (client.getMiddlename() != null && client.getMiddlename().length() > 1) {
                        cn.setMiddleName(client.getMiddlename());
                    }
                    pi.setComplexName(cn);
                    mojaloopParty.setPersonalInfo(pi);
                } else { // entity
                    mojaloopParty.setName(client.getFullname());
                }
            } else { // cn
                Customer client = exchange.getIn().getBody(Customer.class);
                if (PERSON.equals(client.getType())) {
                    PersonalInfo pi = new PersonalInfo();
                    pi.setDateOfBirth(client.getDateOfBirth() != null ? client.getDateOfBirth().toString() : null);
                    ComplexName cn = new ComplexName();
                    cn.setFirstName(client.getGivenName());
                    cn.setLastName(client.getSurname());
                    cn.setMiddleName(client.getMiddleName());
                    pi.setComplexName(cn);
                    mojaloopParty.setPersonalInfo(pi);
                } else {
                    mojaloopParty.setName(client.getGivenName());
                }
            }

            Map<String, Object> variables = new HashMap<>();
            variables.put(PAYEE_PARTY_RESPONSE, objectMapper.writeValueAsString(mojaloopParty));
            variables.put(PARTY_VALIDATION, partyValidation);
            zeebeClient.newCompleteCommand(exchange.getProperty(ZEEBE_JOB_KEY, Long.class)).variables(variables).send();
        }
    }
}
