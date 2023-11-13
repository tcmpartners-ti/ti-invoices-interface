package com.tcmp.tiapi.messaging.router.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XmlNamespaceFixerTest {
  private XmlNamespaceFixer namespaceFixer;

  @BeforeEach
  void setUp() {
    namespaceFixer = new XmlNamespaceFixer();
  }

  @Test
  void itShouldRemoveIncorrectPrefixFromXml() {
    var incorrectXml = "<xml:_>hello</xml> ";
    var expectedXml = "<xml>hello</xml> ";

    var actualXml = namespaceFixer.fixNamespaces(incorrectXml);

    assertEquals(expectedXml, actualXml);
  }
}
