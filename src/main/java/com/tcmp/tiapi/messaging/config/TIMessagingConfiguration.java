package com.tcmp.tiapi.messaging.config;

import com.tcmp.tiapi.messaging.model.TINamespace;
import com.tcmp.tiapi.messaging.model.requests.ServiceRequest;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class TIMessagingConfiguration {

  public static final String SCHEMA_PREFIX = "xsi";
  // This is used just to be deleted in the camel route processor.
  public static final String CONTROL_PREFIX = "_";
  public static final String MESSAGES_PREFIX = "m";
  public static final String COMMON_PREFIX = "c";

  public static final Map<String, String> namespacesPrefixes = Map.of(
    TINamespace.CONTROL, CONTROL_PREFIX,
    TINamespace.SCHEMA_NAMESPACE, SCHEMA_PREFIX,
    TINamespace.MESSAGES, MESSAGES_PREFIX,
    TINamespace.COMMON, COMMON_PREFIX
  );

  @Bean
  JaxbDataFormat jaxbDataFormat() throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(ServiceRequest.class);

    JaxbDataFormat jaxbDataFormat = new JaxbDataFormat(jaxbContext);
    jaxbDataFormat.setNamespacePrefix(namespacesPrefixes);

    return jaxbDataFormat;
  }
}