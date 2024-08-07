package org.mifos.connector.crm.api.definition;

import static org.mifos.connector.crm.zeebe.ZeebeVariables.CLIENTCORRELATIONID;
import static org.mifos.connector.crm.zeebe.ZeebeVariables.PLATFORM_TENANT;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.concurrent.ExecutionException;
import org.mifos.connector.crm.data.BillRTPReqDTO;
import org.mifos.connector.crm.data.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "GOV")
public interface BillRtpRespApi {

    @Operation(summary = "Bill RTP Resp API from PBB to Bill Agg")
    @PostMapping("/billTransferRequests")
    ResponseEntity<ResponseDTO> billRTPResp(@RequestHeader(value = PLATFORM_TENANT) String tenantId,
            @RequestHeader(value = CLIENTCORRELATIONID) String correlationId, @RequestParam(value = "X-Biller-Id") String billerId,
            @RequestBody BillRTPReqDTO billRTPReqDTO) throws ExecutionException, InterruptedException;
}
