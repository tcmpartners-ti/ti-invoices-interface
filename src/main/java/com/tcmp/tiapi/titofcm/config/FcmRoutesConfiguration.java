package com.tcmp.tiapi.titofcm.config;

import com.tcmp.tiapi.titofcm.dto.response.PaymentResultResponse;
import com.tcmp.tiapi.titofcm.repository.InvoicePaymentCorrelationInfoRepository;
import com.tcmp.tiapi.titofcm.route.PaymentResultRouteBuilder;
import com.tcmp.tiapi.titofcm.strategy.PaymentResultHandlerContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.jms.JmsComponent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FcmRoutesConfiguration {
  @Value("${fcm.route.payment-result.from}")
  private String uriFromPaymentResult;

  /**
   * This bean creates the jms component required to consume the FCM active mq server, this
   * component is consumed in the camel routes that require it.
   *
   * @param configuration fcm configuration with the mq credentials.
   * @return jms component with the active mq connection factory.
   */
  @Bean
  public JmsComponent activemqFcm(FcmConfiguration configuration) {
    FcmConfiguration.ActiveMqConfiguration mq = configuration.getActivemq();
    String brokerUrl = String.format("tcp://%s:%s", mq.host(), mq.port());
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
    connectionFactory.setTrustedPackages(List.of(""));
    connectionFactory.setUserName(mq.user());
    connectionFactory.setPassword(mq.password());

    JmsComponent component = new JmsComponent();
    component.setConnectionFactory(connectionFactory);

    return component;
  }

  @Bean
  public PaymentResultRouteBuilder paymentResultRouteBuilder(
      InvoicePaymentCorrelationInfoRepository invoicePaymentCorrelationInfoRepository,
      PaymentResultHandlerContext paymentResultHandlerContext) {
    return new PaymentResultRouteBuilder(
        uriFromPaymentResult,
        new JacksonDataFormat(PaymentResultResponse.class),
        invoicePaymentCorrelationInfoRepository,
        paymentResultHandlerContext);
  }
}
