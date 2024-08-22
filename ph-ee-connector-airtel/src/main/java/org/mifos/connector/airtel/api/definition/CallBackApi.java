package org.mifos.connector.airtel.api.definition;

import org.mifos.connector.airtel.api.implementation.CallBackController;
import org.mifos.connector.common.mobilemoney.airtel.dto.AirtelCallBackRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CallBackApi {

    @Autowired
    CallBackController callBackController;

    @PostMapping("/callback")
    public ResponseEntity<AirtelCallBackRequestDTO> getCallBack(@RequestBody AirtelCallBackRequestDTO requestBody) {
        return callBackController.handleCallBackRequest(requestBody);
    }
}
