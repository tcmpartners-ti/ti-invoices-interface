package com.tcmp.tiapi.ti.route.processor;

import com.tcmp.tiapi.ti.config.MessagingConfiguration;

/**
 * This class is used to fix a problem produced when adding the default namespace in the ti
 * ServiceRequest XML.
 */
public class XmlNamespaceFixer {
  public String fixNamespaces(String xmlBody) {
    return removePrefixFromDefaultNamespace(xmlBody);
  }

  private String removePrefixFromDefaultNamespace(String originalXml) {
    String incorrectPrefix = ":" + MessagingConfiguration.CONTROL_PREFIX;
    return originalXml.replaceFirst(incorrectPrefix, "");
  }
}
