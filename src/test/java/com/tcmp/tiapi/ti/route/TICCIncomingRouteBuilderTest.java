package com.tcmp.tiapi.ti.route;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.tcmp.tiapi.ti.dto.request.AckServiceRequest;
import com.tcmp.tiapi.ti.handler.TICCIncomingHandlerContext;
import com.tcmp.tiapi.ti.route.ticc.TICCIncomingRouteBuilder;
import com.tcmp.tiapi.ti.route.ticc.TICCIncomingStrategy;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.InputStream;
import java.time.Duration;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.awaitility.Durations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TICCIncomingRouteBuilderTest extends CamelTestSupport {
  private static final String URI_FROM = "direct:mockUriSource";
  private static final String MOCK_TICC_RESPONSE =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:ServiceRequest xmlns:ns2=\"urn:control.services.tiplus2.misys.com\"><ns2:RequestHeader><ns2:Service>GATEWAY</ns2:Service><ns2:Operation>TFINVACK</ns2:Operation><ns2:Credentials/><ns2:ReplyFormat>FULL</ns2:ReplyFormat><ns2:TargetSystem>CorporateChannels</ns2:TargetSystem><ns2:SourceSystem>ZONE1</ns2:SourceSystem><ns2:NoRepair>Y</ns2:NoRepair><ns2:NoOverride>Y</ns2:NoOverride><ns2:CorrelationId>8678</ns2:CorrelationId><ns2:TransactionControl>NONE</ns2:TransactionControl><ns2:CreationDate>2024-03-15</ns2:CreationDate></ns2:RequestHeader><ns2:tfinvdet><ns2:MessageName>TFINVACK</ns2:MessageName><ns2:eBankMasterRef></ns2:eBankMasterRef><ns2:eBankEventRef></ns2:eBankEventRef><ns2:MasterRef>INV00002722BPCH</ns2:MasterRef><ns2:EventRef>CRE001</ns2:EventRef><ns2:BehalfOfBranch>BPEC</ns2:BehalfOfBranch><ns2:SBB>GPCH</ns2:SBB><ns2:MBE>BPCH</ns2:MBE><ns2:Programme>ASEGURADORASUR</ns2:Programme><ns2:ProgrammeTypeCode>B</ns2:ProgrammeTypeCode><ns2:BuyerIdentifier>0190123626001</ns2:BuyerIdentifier><ns2:SellerIdentifier>1722466420001</ns2:SellerIdentifier><ns2:AnchorPartyCustomerMnemonic>0190123626001</ns2:AnchorPartyCustomerMnemonic><ns2:CounterpartyCustomerMnemonic>1722466420001</ns2:CounterpartyCustomerMnemonic><ns2:BuyerBOB>BPEC</ns2:BuyerBOB><ns2:SCFBuyerRef></ns2:SCFBuyerRef><ns2:SellerBOB>BPEC</ns2:SellerBOB><ns2:SCFSellerRef></ns2:SCFSellerRef><ns2:BuyerName>ASEGURADORA DEL SUR C.A.</ns2:BuyerName><ns2:BuyerAddr1>Av Amazonas 111</ns2:BuyerAddr1><ns2:BuyerAddr2></ns2:BuyerAddr2><ns2:BuyerAddr3></ns2:BuyerAddr3><ns2:BuyerAddr4></ns2:BuyerAddr4><ns2:BuyerCountry>EC</ns2:BuyerCountry><ns2:SellerName>DAVID REYES</ns2:SellerName><ns2:SellerAddr1>Amazonas</ns2:SellerAddr1><ns2:SellerAddr2></ns2:SellerAddr2><ns2:SellerAddr3></ns2:SellerAddr3><ns2:SellerAddr4></ns2:SellerAddr4><ns2:SellerCountry>EC</ns2:SellerCountry><ns2:ReceivedOn>2024-03-15</ns2:ReceivedOn><ns2:IssueDate>2023-12-12</ns2:IssueDate><ns2:InvoiceNumber>001-001-000000036</ns2:InvoiceNumber><ns2:FaceValueAmount>10000</ns2:FaceValueAmount><ns2:FaceValueCurrency>USD</ns2:FaceValueCurrency><ns2:AdjustmentAmount></ns2:AdjustmentAmount><ns2:AdjustmentCurrency>USD</ns2:AdjustmentCurrency><ns2:AdjustmentDirection></ns2:AdjustmentDirection><ns2:OutstandingAmount>10000</ns2:OutstandingAmount><ns2:OutstandingCurrency>USD</ns2:OutstandingCurrency><ns2:RelatedGoodsOrServices></ns2:RelatedGoodsOrServices><ns2:SettlementDate>2024-03-30</ns2:SettlementDate><ns2:EligibleForFinancing>Y</ns2:EligibleForFinancing><ns2:IneligibilityReason>-</ns2:IneligibilityReason><ns2:SenderToReceiverInfo></ns2:SenderToReceiverInfo></ns2:tfinvdet></ns2:ServiceRequest>";

  @EndpointInject(URI_FROM)
  private ProducerTemplate from;

  private JaxbDataFormat jaxbDataFormatAckEventRequest;
  @Mock private TICCIncomingHandlerContext handlerContextMock;
  @Mock private TICCIncomingStrategy strategyMock;

  @Captor private ArgumentCaptor<String> operationArgumentCaptor;

  @Override
  protected RoutesBuilder createRouteBuilder() throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(AckServiceRequest.class);
    JaxbDataFormat jaxbDataFormat = new JaxbDataFormat(jaxbContext);
    jaxbDataFormatAckEventRequest = spy(jaxbDataFormat);

    return new TICCIncomingRouteBuilder(
        jaxbDataFormatAckEventRequest, handlerContextMock, URI_FROM);
  }

  @Test
  void itShouldUseHandlerStrategy() {
    when(handlerContextMock.strategy(anyString())).thenReturn(strategyMock);

    from.sendBody(MOCK_TICC_RESPONSE);
    from.sendBody(MOCK_TICC_RESPONSE);
    from.sendBody(MOCK_TICC_RESPONSE);

    var expectedOperation = "TFINVACK";

    await()
        .atMost(Durations.ONE_HUNDRED_MILLISECONDS)
        .pollDelay(Duration.ofMillis(10L))
        .untilAsserted(
            () -> {
              verify(jaxbDataFormatAckEventRequest, times(3))
                  .unmarshal(any(Exchange.class), any(InputStream.class));
              verify(handlerContextMock, times(3)).strategy(operationArgumentCaptor.capture());
            });

    assertTrue(operationArgumentCaptor.getAllValues().stream().allMatch(expectedOperation::equals));
  }

  @Test
  void itShouldCatchUnhandledOperations() {
    var mockException = mock(IllegalArgumentException.class);
    when(handlerContextMock.strategy(anyString())).thenThrow(mockException);

    from.sendBody(MOCK_TICC_RESPONSE);

    verify(handlerContextMock).strategy(anyString());
    verify(mockException).getMessage();
  }
}
