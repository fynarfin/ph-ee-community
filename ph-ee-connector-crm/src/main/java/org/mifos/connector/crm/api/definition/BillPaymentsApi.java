package org.mifos.connector.crm.api.definition;

import static org.mifos.connector.crm.zeebe.ZeebeVariables.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.concurrent.ExecutionException;
import org.mifos.connector.crm.data.BillPaymentsReqDTO;
import org.mifos.connector.crm.data.BillPaymentsResponseDTO;
import org.springframework.web.bind.annotation.*;

@Tag(name = "GOV")
public interface BillPaymentsApi {

    @Operation(summary = "Bill Payments API from Payer FSP to PBB")
    @PostMapping("/paymentNotifications")
    BillPaymentsResponseDTO billPayments(@RequestHeader(value = PLATFORM_TENANT) String tenantId,
            @RequestHeader(value = CLIENTCORRELATIONID) String correlationId, @RequestHeader(value = PAYER_FSP) String payerFspId,
            @RequestBody BillPaymentsReqDTO body) throws ExecutionException, InterruptedException, JsonProcessingException;
}
