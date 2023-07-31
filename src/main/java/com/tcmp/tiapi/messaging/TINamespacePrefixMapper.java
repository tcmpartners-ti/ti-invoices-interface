package com.tcmp.tiapi.messaging;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import com.tcmp.tiapi.messaging.model.TINamespace;

import java.util.Map;

public class TINamespacePrefixMapper extends NamespacePrefixMapper {
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

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        return namespacesPrefixes.getOrDefault(namespaceUri, null);
    }
}
