package com.tcmp.tiapi.ti.route.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
