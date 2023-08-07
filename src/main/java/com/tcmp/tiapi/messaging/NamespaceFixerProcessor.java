package com.tcmp.tiapi.messaging;

import com.tcmp.tiapi.messaging.config.TIMessagingConfig;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.Optional;

/**
 * This class is used to fix a problem produced when adding the default namespace in the ti ServiceRequest
 * XML.
 */
public class NamespaceFixerProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        Optional<String> body = Optional.ofNullable(exchange.getIn().getBody(String.class));

        body.ifPresent(originalXml -> {
            String cleanedUpXml = removePrefixFromDefaultNamespace(originalXml);
            exchange.getIn().setBody(cleanedUpXml);
        });
    }

    private String removePrefixFromDefaultNamespace(String originalXml) {
        String incorrectPrefix = ":" + TIMessagingConfig.CONTROL_PREFIX;
        return originalXml.replaceFirst(incorrectPrefix, "");
    }
}
