package com.tcmp.tiapi.messaging.router.processor;

import com.tcmp.tiapi.messaging.config.TIMessagingConfiguration;

/**
 * This class is used to fix a problem produced when adding the default namespace in the ti ServiceRequest
 * XML.
 */
public class XmlNamespaceFixer {
  public String fixNamespaces(String xmlBody) {
    var removedPrefixes = removePrefixFromDefaultNamespace(xmlBody);
    return removePrefixFromServiceRequest(removedPrefixes);
  }

  private String removePrefixFromDefaultNamespace(String originalXml) {
    String incorrectPrefix = ":" + TIMessagingConfiguration.CONTROL_PREFIX;
    return originalXml.replaceFirst(incorrectPrefix, "");
  }

  private String removePrefixFromServiceRequest(String modifiedXml) {
    return modifiedXml.replaceAll("_:\\S", "S");
  }
}
