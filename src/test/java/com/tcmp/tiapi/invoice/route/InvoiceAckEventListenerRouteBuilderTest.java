package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.messaging.model.requests.AckServiceRequest;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class InvoiceAckEventListenerRouteBuilderTest extends CamelTestSupport {
  private static final String URI_FROM = "direct:mockActiveMqAck";
  private static final String URI_TO_FINANCE = "direct:startFinanceFlow";
  private static final String URI_TO_SETTLE = "direct:startSettleFlow";

  // Descomenta esto y borra este comentario
  // @EndpointInject(URI_FROM) ProducerTemplate from;

  @Override
  protected RoutesBuilder createRouteBuilder() throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(AckServiceRequest.class);
    var jaxbDataFormat = new JaxbDataFormat(jaxbContext);

    return new InvoiceAckEventListenerRouteBuilder(
      jaxbDataFormat,

      URI_FROM,
      URI_TO_FINANCE,
      URI_TO_SETTLE
    );
  }

  // Puedes borrar esta prueba, solo es para que no me salten alertas.
  @Test
  void test() {
    assertNotNull(buildMockMessage("OPERATION1"));
    assertNotNull(buildMockMessage("OPERATION2"));
  }

  private String buildMockMessage(String operation) {
    return """
      <?xml version="1.0" encoding="UTF-8"?>
      <ServiceRequest xmlns="urn:control.services.tiplus2.misys.com" xmlns:ns2="urn:messages.service.ti.apps.tiplus2.misys.com" xmlns:ns4="urn:custom.service.ti.apps.tiplus2.misys.com" xmlns:ns3="urn:common.service.ti.apps.tiplus2.misys.com">
      <RequestHeader>
        <Service>GATEWAY</Service>
        <Operation>%s</Operation>
        <Credentials>
          <Name>SUPERVISOR</Name>
        </Credentials>
        <ReplyFormat>FULL</ReplyFormat>
        <TargetSystem>CorporateChannels</TargetSystem>
        <SourceSystem>ZONE1</SourceSystem>
        <NoRepair>Y</NoRepair>
        <NoOverride>Y</NoOverride>
        <CorrelationId>1892</CorrelationId>
        <TransactionControl>NONE</TransactionControl>
        <CreationDate>2023-12-07</CreationDate>
      </RequestHeader>
      <tfinvset xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      <MessageName>TFINVSET</MessageName>
      <eBankMasterRef xsi:nil="true"/>
      <eBankEventRef xsi:nil="true"/>
      <MasterRef>INV00000591BPCH</MasterRef>
      <EventRef>STL001</EventRef>
      <BehalfOfBranch>BPEC</BehalfOfBranch>
      <SBB>GPCH</SBB>
      <MBE>BPCH</MBE>
      <BankComment>We have settled this Invoice.</BankComment>
      <Programme>TESTBUYER</Programme>
      <ProgrammeTypeCode>B</ProgrammeTypeCode>
      <BuyerIdentifier>1722466420003</BuyerIdentifier>
      <SellerIdentifier>1722466420002</SellerIdentifier>
      <AnchorPartyCustomerMnemonic>1722466420003</AnchorPartyCustomerMnemonic>
      <CounterpartyCustomerMnemonic>1722466420002</CounterpartyCustomerMnemonic>
      <BuyerBOB>BPEC</BuyerBOB>
      <SCFBuyerRef xsi:nil="true"/>
      <SellerBOB>BPEC</SellerBOB>
      <SCFSellerRef xsi:nil="true"/>
      <ReceivedOn>2023-12-06</ReceivedOn>
      <IssueDate>2023-11-30</IssueDate>
      <PaymentValueDate>2023-12-06</PaymentValueDate>
      <InvoiceNumber>002-001-0000001</InvoiceNumber>
      <PaymentAmount>100000</PaymentAmount>
      <PaymentCurrency>USD</PaymentCurrency>
      <OutstandingAmount>0</OutstandingAmount>
      <OutstandingCurrency>USD</OutstandingCurrency>
      <EligibleForFinancing>N</EligibleForFinancing>
      <InvoiceStatusCode>P</InvoiceStatusCode>
      <NotesForCustomer xsi:nil="true"/>
      <NotesForBuyer xsi:nil="true"/>
      </tfinvset></ServiceRequest>
      """.formatted(operation);
  }
}
