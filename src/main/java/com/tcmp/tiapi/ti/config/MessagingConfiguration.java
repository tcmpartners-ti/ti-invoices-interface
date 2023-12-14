package com.tcmp.tiapi.ti.config;

import com.tcmp.tiapi.ti.TIServiceRequestWrapper;
import com.tcmp.tiapi.ti.dto.TINamespace;
import com.tcmp.tiapi.ti.dto.request.AckServiceRequest;
import com.tcmp.tiapi.ti.dto.request.ServiceRequest;
import com.tcmp.tiapi.ti.dto.response.ServiceResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.util.Map;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfiguration {

  public static final String SCHEMA_PREFIX = "xsi";
  // This is used just to be deleted in the camel route processor.
  public static final String CONTROL_PREFIX = "_";
  public static final String MESSAGES_PREFIX = "ns2";
  public static final String COMMON_PREFIX = "ns3";
  public static final String CUSTOM_PREFIX = "ns4";

  public static final Map<String, String> namespacesPrefixes =
      Map.of(
          TINamespace.CONTROL, CONTROL_PREFIX,
          TINamespace.SCHEMA, SCHEMA_PREFIX,
          TINamespace.MESSAGES, MESSAGES_PREFIX,
          TINamespace.COMMON, COMMON_PREFIX,
          TINamespace.CUSTOM, CUSTOM_PREFIX);

  @Bean
  public TIServiceRequestWrapper serviceRequestWrapper() {
    return new TIServiceRequestWrapper();
  }

  @Bean
  public JaxbDataFormat jaxbDataFormatServiceRequest() throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(ServiceRequest.class);

    JaxbDataFormat jaxbDataFormat = new JaxbDataFormat(jaxbContext);
    jaxbDataFormat.setNamespacePrefix(namespacesPrefixes);
    jaxbDataFormat.setJaxbProviderProperties(Map.of(Marshaller.JAXB_FORMATTED_OUTPUT, false));

    return jaxbDataFormat;
  }

  @Bean
  public JaxbDataFormat jaxbDataFormatServiceResponse() throws JAXBException {
    JAXBContext jaxbServiceResponseContext = JAXBContext.newInstance(ServiceResponse.class);
    return new JaxbDataFormat(jaxbServiceResponseContext);
  }

  @Bean
  public JaxbDataFormat jaxbDataFormatAckEventRequest() throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(AckServiceRequest.class);
    return new JaxbDataFormat(jaxbContext);
  }
}
