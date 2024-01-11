package com.tcmp.tiapi.invoice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.*;

import com.tcmp.tiapi.AbstractIntegrationTest;
import com.tcmp.tiapi.invoice.service.InvoiceEventService;
import com.tcmp.tiapi.invoice.service.InvoiceService;
import com.tcmp.tiapi.shared.UUIDGenerator;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingClient;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayClient;
import io.restassured.RestAssured;
import jakarta.jms.*;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.activemq.command.ActiveMQQueue;
import org.awaitility.Durations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class InvoiceIntegrationTest extends AbstractIntegrationTest {
  @Autowired private InvoiceService invoiceService;

  @SpyBean private OperationalGatewayClient operationalGatewayClient;
  @SpyBean private BusinessBankingClient businessBankingClient;
  @SpyBean private InvoiceEventService invoiceEventService;
  @MockBean private UUIDGenerator uuidGenerator;

  private MessageConsumer ftiConsumer;
  private MessageProducer ftiReplyProducer;
  private MessageProducer ticcProducer;

  @BeforeEach
  void setUpProducersAndConsumers() throws JMSException {
    var ftiOutgoing = new ActiveMQQueue(ftiOutgoingQueue);
    var ftiIncomingReply = new ActiveMQQueue(ftiIncomingReplyQueue);
    var ticcIncoming = new ActiveMQQueue(ticcIncomingQueue);

    ftiConsumer = session.createConsumer(ftiOutgoing);
    ftiReplyProducer = session.createProducer(ftiIncomingReply);
    ticcProducer = session.createProducer(ticcIncoming);

    cleanQueue(ftiOutgoing);
    cleanQueue(ftiIncomingReply);
    cleanQueue(ticcIncoming);
  }

  @AfterEach
  void destroyProducersAndConsumers() throws JMSException {
    session.close();
    connection.close();

    ticcProducer.close();
    ftiReplyProducer.close();
    ftiConsumer.close();
  }

  @AfterEach
  void cleanUpMocks() {
    Mockito.reset(
        operationalGatewayClient, businessBankingClient, invoiceEventService, uuidGenerator);
  }

  @Test
  void contextLoads() {
    assertNotNull(invoiceService);
  }

  /**
   * Invoice creation flow is the following: Store information temporarily in redis. Wait for the
   * queue reply. Notify to third-party services on error or success. Send email notification if
   * creation ack is received in the ticc incoming queue.
   */
  @Test
  void createInvoice_itShouldHandleHappyPath() throws JMSException {
    var invoiceUuid = "000-001";

    mockServerClient
        .when(request().withPath("/operational-gateway/v2/notifications"))
        .respond(
            response()
                .withStatusCode(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(
                    "{\"data\":[{\"recipient\":{\"channel\":{\"description\":\"email\",\"value\":\"dareyesp@pichincha.com\"}}}]}"));
    Mockito.when(uuidGenerator.getNewId()).thenReturn(invoiceUuid);

    var body =
        "{\"context\":{\"customer\":\"0190123626001\",\"theirReference\":\"001-001-000000036\",\"behalfOfBranch\":\"BPEC\"},\"anchorParty\":\"0190123626001\",\"anchorAccount\":\"AH2100170032\",\"programme\":\"ASEGURADORASUR\",\"seller\":\"1722466420001\",\"buyer\":\"0190123626001\",\"invoiceNumber\":\"001-001-000000036\",\"issueDate\":\"12-12-2023\",\"faceValue\":{\"amount\":100,\"currency\":\"USD\"},\"outstandingAmount\":{\"amount\":100,\"currency\":\"USD\"},\"settlementDate\":\"30-03-2024\"}";
    var response = RestAssured.given().spec(requestSpecification).body(body).post("/invoices");

    var expectedMessage =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ServiceRequest xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"urn:control.services.tiplus2.misys.com\" xmlns:ns3=\"urn:common.service.ti.apps.tiplus2.misys.com\" xmlns:ns2=\"urn:messages.service.ti.apps.tiplus2.misys.com\" xmlns:ns4=\"urn:custom.service.ti.apps.tiplus2.misys.com\"><RequestHeader><Service>TI</Service><Operation>TFINVNEW</Operation><Credentials><Name>TI_INTERFACE</Name></Credentials><ReplyFormat>STATUS</ReplyFormat><NoOverride>N</NoOverride><CorrelationId>%s</CorrelationId></RequestHeader><ns2:TFINVNEW><ns2:Context><ns3:Branch>BPEC</ns3:Branch><ns3:Customer>0190123626001</ns3:Customer><ns3:TheirReference>001-001-000000036</ns3:TheirReference><ns3:BehalfOfBranch>BPEC</ns3:BehalfOfBranch></ns2:Context><ns2:Programme>ASEGURADORASUR</ns2:Programme><ns2:Seller>1722466420001</ns2:Seller><ns2:Buyer>0190123626001</ns2:Buyer><ns2:AnchorParty>0190123626001</ns2:AnchorParty><ns2:InvoiceNumber>001-001-000000036</ns2:InvoiceNumber><ns2:IssueDate>2023-12-12</ns2:IssueDate><ns2:FaceValue><ns3:Amount>100.00</ns3:Amount><ns3:Currency>USD</ns3:Currency></ns2:FaceValue><ns2:OutstandingAmount><ns3:Amount>100.00</ns3:Amount><ns3:Currency>USD</ns3:Currency></ns2:OutstandingAmount><ns2:SettlementDate>2024-03-30</ns2:SettlementDate><ns2:InvoiceApproved>Y</ns2:InvoiceApproved><ns2:ExtraData><ns4:FinanceAccount>AH2100170032</ns4:FinanceAccount></ns2:ExtraData></ns2:TFINVNEW></ServiceRequest>"
            .formatted(invoiceUuid);
    var expectedEmailRequestBody =
        "{\"data\":{\"flow\":{\"state\":\"notificacionesAdicionales\"},\"requester\":{\"documentNumber\":\"1722466420001\",\"documentType\":\"001\"},\"additionalRecipient\":[{\"email\":{\"address\":\"dareyesp@pichincha.com\"},\"cellphone\":{\"internationalPrefix\":\" \",\"number\":\" \"}}],\"template\":{\"templateId\":\"305003729\",\"sequentialId\":\"0\",\"fields\":[{\"key\":\"RucEnmascarado\",\"value\":\"xxxxxx6420001\"},{\"key\":\"NombreEmpresa\",\"value\":\"DAVID REYES\"},{\"key\":\"FechaIngreso\",\"value\":\"2024-03-15\"},{\"key\":\"HoraIngreso\",\"value\":\"12:00\"},{\"key\":\"action\",\"value\":\"Carga de la factura No. 001-001-000000036 por USD 100.00\"},{\"key\":\"urlBanca\",\"value\":\"https://bancaempresas.pichincha.com\"}]}}}";

    AtomicReference<String> actualMessage = new AtomicReference<>();

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    await()
        .atMost(Durations.ONE_SECOND)
        .pollDelay(Durations.ONE_HUNDRED_MILLISECONDS)
        .untilAsserted(
            () -> {
              var message = ftiConsumer.receiveNoWait();
              assertThat(message).isNotNull();
              actualMessage.set(message.getBody(String.class));

              message.acknowledge();
            });

    assertEquals(expectedMessage, actualMessage.get());
    mockSuccessfulTiResponses(invoiceUuid);

    await()
        .atMost(Durations.ONE_SECOND)
        .pollDelay(Durations.ONE_HUNDRED_MILLISECONDS)
        .untilAsserted(
            () -> {
              verify(invoiceEventService).deleteInvoiceByUuid(invoiceUuid);
              verify(operationalGatewayClient)
                  .sendEmailNotification(ArgumentMatchers.anyMap(), ArgumentMatchers.any());

              var requestExpectations = mockServerClient.retrieveRecordedRequests(request());
              String actualEmailRequestBody = requestExpectations[0].getBodyAsString();
              JSONAssert.assertEquals(expectedEmailRequestBody, actualEmailRequestBody, true);
            });
  }

  private void mockSuccessfulTiResponses(String invoiceUuid) throws JMSException {
    var ftiReplyMessage =
        session.createTextMessage(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ServiceResponse xmlns=\"urn:control.services.tiplus2.misys.com\" xmlns:ns2=\"urn:messages.service.ti.apps.tiplus2.misys.com\" xmlns:ns3=\"urn:common.service.ti.apps.tiplus2.misys.com\"><ResponseHeader><Service>TI</Service><Operation>TFINVNEW</Operation><Status>SUCCEEDED</Status><CorrelationId>%s</CorrelationId></ResponseHeader></ServiceResponse>"
                .formatted(invoiceUuid));
    var ticcMessage =
        session.createTextMessage(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:ServiceRequest xmlns:ns2=\"urn:control.services.tiplus2.misys.com\"><ns2:RequestHeader><ns2:Service>GATEWAY</ns2:Service><ns2:Operation>TFINVACK</ns2:Operation><ns2:Credentials/><ns2:ReplyFormat>FULL</ns2:ReplyFormat><ns2:TargetSystem>CorporateChannels</ns2:TargetSystem><ns2:SourceSystem>ZONE1</ns2:SourceSystem><ns2:NoRepair>Y</ns2:NoRepair><ns2:NoOverride>Y</ns2:NoOverride><ns2:CorrelationId>8678</ns2:CorrelationId><ns2:TransactionControl>NONE</ns2:TransactionControl><ns2:CreationDate>2024-03-15</ns2:CreationDate></ns2:RequestHeader><ns2:tfinvdet><ns2:MessageName>TFINVACK</ns2:MessageName><ns2:eBankMasterRef></ns2:eBankMasterRef><ns2:eBankEventRef></ns2:eBankEventRef><ns2:MasterRef>INV00002722BPCH</ns2:MasterRef><ns2:EventRef>CRE001</ns2:EventRef><ns2:BehalfOfBranch>BPEC</ns2:BehalfOfBranch><ns2:SBB>GPCH</ns2:SBB><ns2:MBE>BPCH</ns2:MBE><ns2:Programme>ASEGURADORASUR</ns2:Programme><ns2:ProgrammeTypeCode>B</ns2:ProgrammeTypeCode><ns2:BuyerIdentifier>0190123626001</ns2:BuyerIdentifier><ns2:SellerIdentifier>1722466420001</ns2:SellerIdentifier><ns2:AnchorPartyCustomerMnemonic>0190123626001</ns2:AnchorPartyCustomerMnemonic><ns2:CounterpartyCustomerMnemonic>1722466420001</ns2:CounterpartyCustomerMnemonic><ns2:BuyerBOB>BPEC</ns2:BuyerBOB><ns2:SCFBuyerRef></ns2:SCFBuyerRef><ns2:SellerBOB>BPEC</ns2:SellerBOB><ns2:SCFSellerRef></ns2:SCFSellerRef><ns2:BuyerName>ASEGURADORA DEL SUR C.A.</ns2:BuyerName><ns2:BuyerAddr1>Av Amazonas 111</ns2:BuyerAddr1><ns2:BuyerAddr2></ns2:BuyerAddr2><ns2:BuyerAddr3></ns2:BuyerAddr3><ns2:BuyerAddr4></ns2:BuyerAddr4><ns2:BuyerCountry>EC</ns2:BuyerCountry><ns2:SellerName>DAVID REYES</ns2:SellerName><ns2:SellerAddr1>Amazonas</ns2:SellerAddr1><ns2:SellerAddr2></ns2:SellerAddr2><ns2:SellerAddr3></ns2:SellerAddr3><ns2:SellerAddr4></ns2:SellerAddr4><ns2:SellerCountry>EC</ns2:SellerCountry><ns2:ReceivedOn>2024-03-15</ns2:ReceivedOn><ns2:IssueDate>2023-12-12</ns2:IssueDate><ns2:InvoiceNumber>001-001-000000036</ns2:InvoiceNumber><ns2:FaceValueAmount>10000</ns2:FaceValueAmount><ns2:FaceValueCurrency>USD</ns2:FaceValueCurrency><ns2:AdjustmentAmount></ns2:AdjustmentAmount><ns2:AdjustmentCurrency>USD</ns2:AdjustmentCurrency><ns2:AdjustmentDirection></ns2:AdjustmentDirection><ns2:OutstandingAmount>10000</ns2:OutstandingAmount><ns2:OutstandingCurrency>USD</ns2:OutstandingCurrency><ns2:RelatedGoodsOrServices></ns2:RelatedGoodsOrServices><ns2:SettlementDate>2024-03-30</ns2:SettlementDate><ns2:EligibleForFinancing>Y</ns2:EligibleForFinancing><ns2:IneligibilityReason>-</ns2:IneligibilityReason><ns2:SenderToReceiverInfo></ns2:SenderToReceiverInfo></ns2:tfinvdet></ns2:ServiceRequest>");

    ftiReplyProducer.send(ftiReplyMessage);
    ticcProducer.send(ticcMessage);
  }

  @Test
  void createInvoice_itShouldNotifyIfInvoiceCouldNotBeCreated() throws JMSException {
    var invoiceUuid = "000-001";

    mockServerClient
        .when(request().withPath("/business-banking/v1/operational-gateway"))
        .respond(
            response()
                .withStatusCode(HttpStatus.CREATED.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(""));
    Mockito.when(uuidGenerator.getNewId()).thenReturn(invoiceUuid);

    var body =
        "{\"context\":{\"customer\":\"0190123626001\",\"theirReference\":\"001-001-000000036\",\"behalfOfBranch\":\"BPEC\"},\"anchorParty\":\"0190123626001\",\"anchorAccount\":\"AH2100170032\",\"programme\":\"ASEGURADORASUR\",\"seller\":\"1722466420001\",\"buyer\":\"0190123626001\",\"invoiceNumber\":\"001-001-000000036\",\"issueDate\":\"12-12-2023\",\"faceValue\":{\"amount\":100,\"currency\":\"USD\"},\"outstandingAmount\":{\"amount\":100,\"currency\":\"USD\"},\"settlementDate\":\"30-03-2024\"}";
    var response = RestAssured.given().spec(requestSpecification).body(body).post("/invoices");

    var expectedCreationMessage =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ServiceRequest xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"urn:control.services.tiplus2.misys.com\" xmlns:ns3=\"urn:common.service.ti.apps.tiplus2.misys.com\" xmlns:ns2=\"urn:messages.service.ti.apps.tiplus2.misys.com\" xmlns:ns4=\"urn:custom.service.ti.apps.tiplus2.misys.com\"><RequestHeader><Service>TI</Service><Operation>TFINVNEW</Operation><Credentials><Name>TI_INTERFACE</Name></Credentials><ReplyFormat>STATUS</ReplyFormat><NoOverride>N</NoOverride><CorrelationId>%s</CorrelationId></RequestHeader><ns2:TFINVNEW><ns2:Context><ns3:Branch>BPEC</ns3:Branch><ns3:Customer>0190123626001</ns3:Customer><ns3:TheirReference>001-001-000000036</ns3:TheirReference><ns3:BehalfOfBranch>BPEC</ns3:BehalfOfBranch></ns2:Context><ns2:Programme>ASEGURADORASUR</ns2:Programme><ns2:Seller>1722466420001</ns2:Seller><ns2:Buyer>0190123626001</ns2:Buyer><ns2:AnchorParty>0190123626001</ns2:AnchorParty><ns2:InvoiceNumber>001-001-000000036</ns2:InvoiceNumber><ns2:IssueDate>2023-12-12</ns2:IssueDate><ns2:FaceValue><ns3:Amount>100.00</ns3:Amount><ns3:Currency>USD</ns3:Currency></ns2:FaceValue><ns2:OutstandingAmount><ns3:Amount>100.00</ns3:Amount><ns3:Currency>USD</ns3:Currency></ns2:OutstandingAmount><ns2:SettlementDate>2024-03-30</ns2:SettlementDate><ns2:InvoiceApproved>Y</ns2:InvoiceApproved><ns2:ExtraData><ns4:FinanceAccount>AH2100170032</ns4:FinanceAccount></ns2:ExtraData></ns2:TFINVNEW></ServiceRequest>"
            .formatted(invoiceUuid);

    AtomicReference<String> actualMessage = new AtomicReference<>();

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    await()
        .atMost(Durations.ONE_SECOND)
        .pollDelay(Durations.ONE_HUNDRED_MILLISECONDS)
        .untilAsserted(
            () -> {
              var message = ftiConsumer.receiveNoWait();
              assertThat(message).isNotNull();
              actualMessage.set(message.getBody(String.class));

              message.acknowledge();
            });

    assertEquals(expectedCreationMessage, actualMessage.get());
    mockErrorTiResponse(invoiceUuid);

    // Requests are encrypted
    var expectedNotificationRequestBody =
        "cZcYwUNfTQ4kWKr+P4y7DM+T0wbqcM3hrxMMwHJ1BKpOYOORXBLIyqkoxtPlDnuQsmWPwhLjrFEHpSBmc11PD7ZPZ2wPscsW7gIVymw9SYccdVD5brIEggvwc/2mplx7JzDeagROtxvZeMlweMc5c7f2L5LNhvFPOWPT0r2NrTU1dipKtGobbkYpYxNlFKDAKTPJz6u1QyXs1Pmf/pQvloLFCWqk7m0s2dVxtD33jgX9qSCftY6uMueW4AROXrV3QzeYwe2DPojEmX4tIaprVvzH1AEmQPy7WTk8bc+4ooKVu6dJ+P/rvDMP9hf9MOYZqN9AzigDGdMdrWh6XrSlGzMWCum/+dPyvOrW5W8d3MrABaO1/SNXM9PRfrdApktXikUqu4WjfNO4ueqN71ATvsIZDLBUjmZCC1sz8c7ATed1twionIYG+F9MxMUR7f7f";
    await()
        .atMost(Durations.FIVE_HUNDRED_MILLISECONDS)
        .pollDelay(Durations.ONE_HUNDRED_MILLISECONDS)
        .untilAsserted(
            () -> {
              verify(invoiceEventService).findInvoiceEventInfoByUuid(invoiceUuid);
              verify(businessBankingClient)
                  .notifyEvent(ArgumentMatchers.anyMap(), ArgumentMatchers.any());
              verify(invoiceEventService).deleteInvoiceByUuid(invoiceUuid);

              var requestExpectations = mockServerClient.retrieveRecordedRequests(request());
              var actualNotificationRequestBody = requestExpectations[0].getBodyAsString();
              assertEquals(expectedNotificationRequestBody, actualNotificationRequestBody);
            });
  }

  private void mockErrorTiResponse(String invoiceUuid) throws JMSException {
    var ftiReplyMessage =
        session.createTextMessage(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ServiceResponse xmlns=\"urn:control.services.tiplus2.misys.com\" xmlns:ns2=\"urn:messages.service.ti.apps.tiplus2.misys.com\" xmlns:ns3=\"urn:common.service.ti.apps.tiplus2.misys.com\"><ResponseHeader><Service>TI</Service><Operation>TFINVNEW</Operation><Status>FAILED</Status><Details><Error>Duplicate invoice number - 001-001-000000036</Error></Details><CorrelationId>%s</CorrelationId></ResponseHeader></ServiceResponse>"
                .formatted(invoiceUuid));

    ftiReplyProducer.send(ftiReplyMessage);
  }
}
