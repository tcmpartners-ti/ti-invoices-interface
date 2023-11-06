package com.tcmp.tiapi.messaging.config;

import com.tcmp.tiapi.messaging.model.TINamespace;
import com.tcmp.tiapi.messaging.model.requests.AckServiceRequest;
import com.tcmp.tiapi.messaging.model.requests.ServiceRequest;
import com.tcmp.tiapi.messaging.model.response.ServiceResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class TIMessagingConfiguration {

  public static final String SCHEMA_PREFIX = "xsi";
  // This is used just to be deleted in the camel route processor.
  public static final String CONTROL_PREFIX = "_";
  public static final String MESSAGES_PREFIX = "ns2";
  public static final String COMMON_PREFIX = "ns3";
  public static final String CUSTOM_PREFIX = "ns4";

  public static final Map<String, String> namespacesPrefixes = Map.of(
    TINamespace.CONTROL, CONTROL_PREFIX,
    TINamespace.SCHEMA, SCHEMA_PREFIX,
    TINamespace.MESSAGES, MESSAGES_PREFIX,
    TINamespace.COMMON, COMMON_PREFIX,
    TINamespace.CUSTOM, CUSTOM_PREFIX
  );

  @Bean
  @Qualifier("jaxbDataFormatServiceRequest")
  JaxbDataFormat jaxbDataFormatServiceRequest() throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(ServiceRequest.class);

    JaxbDataFormat jaxbDataFormat = new JaxbDataFormat(jaxbContext);
    jaxbDataFormat.setNamespacePrefix(namespacesPrefixes);

    return jaxbDataFormat;
  }

  @Bean
  @Qualifier("jaxbDataFormatServiceResponse")
  JaxbDataFormat jaxbDataFormatServiceResponse() throws JAXBException {
    JAXBContext jaxbServiceResponseContext = JAXBContext.newInstance(ServiceResponse.class);
    return new JaxbDataFormat(jaxbServiceResponseContext);
  }

  @Bean
  @Qualifier("jaxbDataFormatAckEventRequest")
  JaxbDataFormat jaxbDataFormatAckEventRequest() throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(AckServiceRequest.class);
    return new JaxbDataFormat(jaxbContext);
  }
}
