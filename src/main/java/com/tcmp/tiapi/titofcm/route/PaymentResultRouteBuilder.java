package com.tcmp.tiapi.titofcm.route;

import com.tcmp.tiapi.titofcm.dto.response.PaymentResultResponse;
import com.tcmp.tiapi.titofcm.model.InvoicePaymentCorrelationInfo;
import com.tcmp.tiapi.titofcm.repository.InvoicePaymentCorrelationInfoRepository;
import com.tcmp.tiapi.titofcm.strategy.PaymentResultHandlerContext;
import com.tcmp.tiapi.titofcm.strategy.PaymentResultStrategy;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;

@RequiredArgsConstructor
public class PaymentResultRouteBuilder extends RouteBuilder {
  private final String uriFrom;
  private final JacksonDataFormat jacksonDataFormat;

  private final InvoicePaymentCorrelationInfoRepository invoicePaymentCorrelationInfoRepository;
  private final PaymentResultHandlerContext handler;

  @Override
  public void configure() {
    from(uriFrom)
        .routeId("paymentResultNotification")
        .unmarshal(jacksonDataFormat)
        .process()
        .body(PaymentResultResponse.class, this::handlePaymentResult)
        .end();
  }

  private void handlePaymentResult(PaymentResultResponse paymentResultResponse) {
    try {
      InvoicePaymentCorrelationInfo invoicePaymentCorrelationInfo =
          invoicePaymentCorrelationInfoRepository
              .findByPaymentReference(paymentResultResponse.paymentReference())
              .orElseThrow(EntityNotFoundException::new);
      invoicePaymentCorrelationInfoRepository.delete(invoicePaymentCorrelationInfo);

      PaymentResultStrategy strategy =
          handler.getStrategy(invoicePaymentCorrelationInfo.getInitialEvent());
      strategy.handleResult(invoicePaymentCorrelationInfo, paymentResultResponse);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }
}
