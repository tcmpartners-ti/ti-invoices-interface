package com.tcmp.tiapi.messaging.config;

import com.tcmp.tiapi.messaging.TINamespacePrefixMapper;
import com.tcmp.tiapi.messaging.model.requests.ServiceRequest;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

@Configuration
public class TIMessagingConfig {
    @Bean
    JAXBContext jaxbContext() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ServiceRequest.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new TINamespacePrefixMapper());

        return jaxbContext;
    }

    /**
     * Do not change this method, the combination of both `marshaller.setProperty()` and
     * `jaxbDataFormat.setNamespacePrefix()` provides a correct namespace mapping.
     */
    @Bean
    JaxbDataFormat jaxbDataFormat(JAXBContext jaxbContext) {
        JaxbDataFormat jaxbDataFormat = new JaxbDataFormat(jaxbContext);
        jaxbDataFormat.setNamespacePrefix(TINamespacePrefixMapper.namespacesPrefixes);

        return jaxbDataFormat;
    }
}
