package org.mifos.connector.mtn.api.mock;


import org.mifos.connector.mtn.data.RequestToPayDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/collection/v1_0")
public class CollectionApiController {

    @PostMapping("/requesttopay")
    public void requestToPay(@RequestBody RequestToPayDTO request) {

    }
}
