package com.tcmp.tiapi.messaging;

import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.TIService;
import com.tcmp.tiapi.messaging.model.requests.Credentials;
import com.tcmp.tiapi.messaging.model.requests.RequestHeader;
import com.tcmp.tiapi.messaging.model.requests.ServiceRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TIServiceRequestWrapper {
    // TODO: Define the credentials origin
    public static <T> ServiceRequest<T> wrapRequest(TIService service, TIOperation operation, T request) {
        return ServiceRequest.<T>builder()
                .header(RequestHeader.builder()
                        .service(service.getValue())
                        .operation(operation.getValue())
                        .credentials(Credentials.builder()
                                .name("David")
                                .password("david123")
                                .certificate("cert123")
                                .digest("digest")
                                .build())
                        .build())
                .body(request)
                .build();
    }
}
