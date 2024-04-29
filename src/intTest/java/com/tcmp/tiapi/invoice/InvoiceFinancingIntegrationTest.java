package com.tcmp.tiapi.invoice;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import com.tcmp.tiapi.AbstractIntegrationTest;
import com.tcmp.tiapi.customer.model.CounterParty;
import com.tcmp.tiapi.invoice.model.EventExtension;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.repository.EventExtensionRepository;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.ProductMasterExtensionRepository;
import com.tcmp.tiapi.invoice.service.InvoiceEventService;
import com.tcmp.tiapi.shared.UUIDGenerator;
import com.tcmp.tiapi.ti.dto.TIOperation;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingClient;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingMapper;
import com.tcmp.tiapi.titoapigee.corporateloan.CorporateLoanClient;
import com.tcmp.tiapi.titofcm.client.SingleElectronicPaymentClient;
import com.tcmp.tiapi.titofcm.dto.request.SinglePaymentRequest;
import com.tcmp.tiapi.titofcm.model.InvoicePaymentCorrelationInfo;
import com.tcmp.tiapi.titofcm.repository.InvoicePaymentCorrelationInfoRepository;
import io.restassured.RestAssured;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.activemq.command.ActiveMQQueue;
import org.awaitility.Durations;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class InvoiceFinancingIntegrationTest extends AbstractIntegrationTest {
  private static final int EXPECTED_PAYMENT_REQUESTS = 2;

  @SpyBean private BusinessBankingClient businessBankingClient;
  @SpyBean private CorporateLoanClient corporateLoanClient;
  @SpyBean private SingleElectronicPaymentClient singlePaymentClient;
  @SpyBean private BusinessBankingMapper businessBankingMapper;
  @SpyBean private InvoiceEventService invoiceEventService;
  @SpyBean private ProductMasterExtensionRepository productMasterExtensionRepository;
  @SpyBean private InvoiceRepository invoiceRepository;

  @MockBean private InvoicePaymentCorrelationInfoRepository invoicePaymentCorrelationInfoRepository;
  @MockBean private EventExtensionRepository eventExtensionRepository;
  @MockBean private UUIDGenerator uuidGenerator;

  private MessageConsumer ftiConsumer;
  private MessageProducer ftiReplyProducer;
  private MessageProducer ticcProducer;
  private MessageProducer fcmProducer;

  @BeforeEach
  void setUpProducersAndConsumers() throws JMSException {
    var ftiOutgoing = new ActiveMQQueue(ftiOutgoingQueue);
    var ftiIncomingReply = new ActiveMQQueue(ftiIncomingReplyQueue);
    var ticcIncoming = new ActiveMQQueue(ticcIncomingQueue);
    var fcmIncoming = new ActiveMQQueue(fcmIncomingQueue);

    ftiConsumer = session.createConsumer(ftiOutgoing);
    ftiReplyProducer = session.createProducer(ftiIncomingReply);
    ticcProducer = session.createProducer(ticcIncoming);
    fcmProducer = session.createProducer(fcmIncoming);

    cleanQueue(ftiOutgoing);
    cleanQueue(ftiIncomingReply);
    cleanQueue(ticcIncoming);
    cleanQueue(fcmIncoming);
  }

  @Test
  void financeInvoice_itShouldHandleHappyPath()
      throws JMSException, JSONException, InterruptedException {
    var latch = new CountDownLatch(EXPECTED_PAYMENT_REQUESTS);

    var invoiceCorrelationUuid = "6c84fb90-12c4-11ea-8d71-362b9e155667";
    var dbInvoice =
        InvoiceMaster.builder()
            .batchId("16880")
            .reference("001-040-0087565")
            .seller(CounterParty.builder().mnemonic("1722466420001").build())
            .build();

    mockServerClient
        .when(
            request()
                .withMethod(HttpMethod.POST.name())
                .withPath("/operational-gateway/v2/notifications"))
        .respond(
            response()
                .withStatusCode(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(
                    "{\"data\":[{\"recipient\":{\"channel\":{\"description\":\"email\",\"value\":\"dareyesp@pichincha.com\"}}}]}"));

    mockServerClient
        .when(
            request()
                .withMethod(HttpMethod.POST.name())
                .withPath("/domain/corporate-loan/v1/cfs-loans/distributor-credits"))
        .respond(
            response()
                .withStatusCode(HttpStatus.CREATED.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                .withBody(
                    "a+XUPylZgZ3t/ozfCheG4Hu/U8vgDvy96K9dDTw+Cqza2l4+zcaZ+FetwJ4vbE8z9LXORd6bGVCvaBcOKa0FUx+8leFYjjmc0FKmZnfwTI/glk0cCNaZMPEjtp2Pha9gJCkgKZU+65QytHUmQ1LAu6PCSXUedWCIaXHvr1bxFcALCS/51e2cby79EVzMtwn+luXK10QbvTPuMR5Vt6cXq2Zr3vUNRIS/y1llgTe7wyv7cZvsAujZbtJYiTft36OHYlIDrvXVJV0d3Tate2YazDmw07glLsQlhF8OOw9g+ESpb64vSRpxFl054BxSo/5AhYxdcpi+fb4PlZI/MsQ+aaWZiESTfUaa2piyaOL7wmD7BbyKR0lo+8t+n9Gy8UbLuP5EsUiADEh9jOthnXDoohIPKr4BTClY5yVbzrKZyGm92oMDT6btLBGYlRDNOhtMtQj/S+P6nXStv5V69es0cJOcE+ohzfepxCTBJv8EPL9/1qdV1rHPE8OG2b7H9RVwZooXHA/zITGW2T6ocC4qJN2U+D94ClBPUW7NXN1OGd5tDc2OGrJ4H17gqPG3SmFw19NnaXaMCvBCQE1eGXAQtDfvvriXLjgPARwDVzYaWa5XyGSbEVQQHpujumVpzWsd4G20HvCsXqpKCar/Dbtb3dTreIZ3LxJpCz/CpchTpGHJDZce6YyzfG4cYOTTfJYuhJKJBLUyo/KIEiYCC23mIkklF3eFlCwpi21GVyMHIAbjvuuTrsBebHSlVx7QXDACJJvco3x+Cr+T4PGuDVgolLMO8P32H3QPP0c2ch4oOCmWL4afO1N88U4CcbM5L3Sh98l5VPG1pokdoOoTf82CViGBxC8/9B1v7J+TgPVH35zi6WAqPLC1jEzq2p8SbJ33zJlQfxYlxI0szZePDj6//sR2ijt62OuuWoqHOu70mliTKpVGIQA897gaR8zJSpKvTqRK80FItvpi+q2l47eo4yhK5WTGWvWj2uTSEU54jkpG9N4mrjLrBz4pmB01E1SvOWVFDW7t659K2M7gRzM5eg=="));

    // FCM transactions
    // Credit amount from buyer to seller
    var creditPaymentReference = "2404290095H7";
    mockServerClient
        .when(
            request()
                .withMethod(HttpMethod.POST.name())
                .withPath("/fcmclient/services/openapi/electronic/single-payment"))
        .respond(
            response()
                .withStatusCode(HttpStatus.CREATED.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(
                    "{\"data\":{\"paymentReferenceNumber\":\"%s\"}}"
                        .formatted(creditPaymentReference)));
    // Taxes amount from seller to buyer
    var taxesPaymentReference = "3404290095H7";
    mockServerClient
        .when(
            request()
                .withMethod(HttpMethod.POST.name())
                .withPath("/fcmclient/services/openapi/electronic/single-payment"))
        .respond(
            response()
                .withStatusCode(HttpStatus.CREATED.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(
                    "{\"data\":{\"paymentReferenceNumber\":\"%s\"}}"
                        .formatted(taxesPaymentReference)));

    mockServerClient
        .when(
            request()
                .withMethod(HttpMethod.POST.name())
                .withPath("/business-banking/v1/operational-gateway"))
        .respond(
            response()
                .withStatusCode(HttpStatus.CREATED.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(""));

    when(uuidGenerator.getNewId()).thenReturn(invoiceCorrelationUuid);
    when(invoiceRepository.findByProgramIdAndSellerMnemonicAndReference(
            anyString(), anyString(), anyString()))
        .thenReturn(Optional.of(dbInvoice));
    // Find invoice is mocked due to some weird error while casting a long.
    when(invoiceRepository.findByProductMasterMasterReference(anyString()))
        .thenReturn(Optional.of(dbInvoice));
    when(productMasterExtensionRepository.findByMasterReference(anyString()))
        .thenReturn(
            Optional.of(ProductMasterExtension.builder().financeAccount("AH1234567890").build()));
    when(eventExtensionRepository.findByMasterReference(anyString()))
        .thenReturn(
            Optional.of(
                EventExtension.builder().id(1L).financeSellerAccount("AH1234567891").build()));
    doAnswer(
            invocation -> {
              latch.countDown();
              return null;
            })
        .when(invoicePaymentCorrelationInfoRepository)
        .save(any(InvoicePaymentCorrelationInfo.class));
    // Redis breaks here :c
    when(invoicePaymentCorrelationInfoRepository.findByPaymentReference(anyString()))
        .thenReturn(
            Optional.of(
                InvoicePaymentCorrelationInfo.builder()
                    .id(invoiceCorrelationUuid)
                    .paymentReference(creditPaymentReference)
                    .initialEvent(
                        InvoicePaymentCorrelationInfo.InitialEvent.BUYER_CENTRIC_FINANCE_0)
                    .eventPayload(
                        "{\"messageName\":\"TFBCFCRE\",\"masterRef\":\"IRF00000218BPCH\",\"theirRef\":\"001-040-0087565\",\"eventRef\":\"CRE001\",\"behalfOfBranch\":\"BPEC\",\"sbb\":\"GPCH\",\"mbe\":\"BPCH\",\"programme\":\"ASEGURADORASUR\",\"programmeTypeCode\":\"B\",\"buyerIdentifier\":\"0190123626001\",\"sellerIdentifier\":\"1722466420001\",\"anchorPartyCustomerMnemonic\":\"0190123626001\",\"counterpartyCustomerMnemonic\":\"1722466420001\",\"sellerName\":\"DAVID REYES\",\"sellerAddr1\":\"Amazonas\",\"sellerAddr2\":\"\",\"sellerAddr3\":\"\",\"sellerAddr4\":\"\",\"sellerCountry\":\"EC\",\"buyerName\":\"ASEGURADORA DEL SUR C.A.\",\"buyerAddr1\":\"Av Amazonas 111\",\"buyerAddr2\":\"\",\"buyerAddr3\":\"\",\"buyerAddr4\":\"\",\"buyerCountry\":\"EC\",\"financePercent\":\"32.22\",\"eventCode\":\"IRCR\",\"buyerBOB\":\"BPEC\",\"scfBuyerRef\":\"\",\"sellerBOB\":\"BPEC\",\"scfSellerRef\":\"\",\"product\":\"IRF\",\"productSubType\":\"\",\"startDate\":\"2024-01-19\",\"dueDate\":\"2024-03-21\",\"financingRef\":\"001-040-0087565\",\"financeDealAmount\":\"3222\",\"financeDealCurrency\":\"USD\",\"outstandingFinanceAmount\":\"2184\",\"outstandingFinanceCurrency\":\"USD\",\"outstandingAmount\":\"1038\",\"outstandingCurrency\":\"USD\",\"receivedOn\":\"2024-03-20\",\"maturityDate\":\"2024-03-21\",\"paymentDetails\":{\"details\":\"No\",\"amount\":\"10.30\",\"currency\":\"USD\",\"valueDate\":\"2024-01-19\",\"accountDetails\":\"Ourselves\",\"settlementParty\":\"DAVID REYES\"},\"senderToReceiverInfo\":\"\",\"invoiceArray\":[{\"invoiceReference\":\"INV00001110BPCH\",\"invoiceNumber\":\"001-040-0087565\",\"invoiceIssueDate\":\"2023-12-19\",\"invoiceSettlementDate\":\"2024-03-21\",\"invoiceOutstandingAmount\":\"32.22\",\"invoiceOutstandingAmountCurrency\":\"USD\",\"invoiceAdvanceAmount\":\"10.38\",\"invoiceAdvanceAmountCurrency\":\"USD\"}],\"ebankMasterRef\":\"\",\"ebankEventRef\":\"\"}")
                    .build()))
        .thenReturn(
            Optional.of(
                InvoicePaymentCorrelationInfo.builder()
                    .id(invoiceCorrelationUuid)
                    .paymentReference(taxesPaymentReference)
                    .initialEvent(
                        InvoicePaymentCorrelationInfo.InitialEvent.BUYER_CENTRIC_FINANCE_1)
                    .eventPayload(
                        "{\"messageName\":\"TFBCFCRE\",\"masterRef\":\"IRF00000218BPCH\",\"theirRef\":\"001-040-0087565\",\"eventRef\":\"CRE001\",\"behalfOfBranch\":\"BPEC\",\"sbb\":\"GPCH\",\"mbe\":\"BPCH\",\"programme\":\"ASEGURADORASUR\",\"programmeTypeCode\":\"B\",\"buyerIdentifier\":\"0190123626001\",\"sellerIdentifier\":\"1722466420001\",\"anchorPartyCustomerMnemonic\":\"0190123626001\",\"counterpartyCustomerMnemonic\":\"1722466420001\",\"sellerName\":\"DAVID REYES\",\"sellerAddr1\":\"Amazonas\",\"sellerAddr2\":\"\",\"sellerAddr3\":\"\",\"sellerAddr4\":\"\",\"sellerCountry\":\"EC\",\"buyerName\":\"ASEGURADORA DEL SUR C.A.\",\"buyerAddr1\":\"Av Amazonas 111\",\"buyerAddr2\":\"\",\"buyerAddr3\":\"\",\"buyerAddr4\":\"\",\"buyerCountry\":\"EC\",\"financePercent\":\"32.22\",\"eventCode\":\"IRCR\",\"buyerBOB\":\"BPEC\",\"scfBuyerRef\":\"\",\"sellerBOB\":\"BPEC\",\"scfSellerRef\":\"\",\"product\":\"IRF\",\"productSubType\":\"\",\"startDate\":\"2024-01-19\",\"dueDate\":\"2024-03-21\",\"financingRef\":\"001-040-0087565\",\"financeDealAmount\":\"3222\",\"financeDealCurrency\":\"USD\",\"outstandingFinanceAmount\":\"2184\",\"outstandingFinanceCurrency\":\"USD\",\"outstandingAmount\":\"1038\",\"outstandingCurrency\":\"USD\",\"receivedOn\":\"2024-03-20\",\"maturityDate\":\"2024-03-21\",\"paymentDetails\":{\"details\":\"No\",\"amount\":\"10.30\",\"currency\":\"USD\",\"valueDate\":\"2024-01-19\",\"accountDetails\":\"Ourselves\",\"settlementParty\":\"DAVID REYES\"},\"senderToReceiverInfo\":\"\",\"invoiceArray\":[{\"invoiceReference\":\"INV00001110BPCH\",\"invoiceNumber\":\"001-040-0087565\",\"invoiceIssueDate\":\"2023-12-19\",\"invoiceSettlementDate\":\"2024-03-21\",\"invoiceOutstandingAmount\":\"32.22\",\"invoiceOutstandingAmountCurrency\":\"USD\",\"invoiceAdvanceAmount\":\"10.38\",\"invoiceAdvanceAmountCurrency\":\"USD\"}],\"ebankMasterRef\":\"\",\"ebankEventRef\":\"\"}")
                    .build()));

    var body =
        "{\"context\":{\"customer\":\"0190123626001\",\"theirReference\":\"001-040-0087565\",\"behalfOfBranch\":\"BPEC\"},\"anchorParty\":\"0190123626001\",\"programme\":\"ASEGURADORASUR\",\"seller\":\"1722466420001\",\"sellerAccount\":\"AH2100086419\",\"buyer\":\"0190123626001\",\"maturityDate\":\"21-03-2024\",\"financeCurrency\":\"USD\",\"financePercent\":32.22,\"financeDate\":\"19-01-2024\",\"invoice\":{\"number\":\"001-040-0087565\",\"issueDate\":\"19-12-2023\",\"outstanding\":{\"amount\":32.22,\"currency\":\"USD\"}}}";
    var response =
        RestAssured.given().spec(requestSpecification).body(body).post("/invoices/finance");

    var expectedOutgoingMessage =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ServiceRequest xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"urn:control.services.tiplus2.misys.com\" xmlns:ns3=\"urn:common.service.ti.apps.tiplus2.misys.com\" xmlns:ns2=\"urn:messages.service.ti.apps.tiplus2.misys.com\" xmlns:ns4=\"urn:custom.service.ti.apps.tiplus2.misys.com\"><RequestHeader><Service>TI</Service><Operation>TFBUYFIN</Operation><Credentials><Name>TI_INTERFACE</Name></Credentials><ReplyFormat>STATUS</ReplyFormat><NoOverride>N</NoOverride><CorrelationId>%s</CorrelationId></RequestHeader><ns2:TFBUYFIN><ns2:Context><ns3:Branch>BPEC</ns3:Branch><ns3:Customer>0190123626001</ns3:Customer><ns3:TheirReference>001-040-0087565</ns3:TheirReference><ns3:BehalfOfBranch>BPEC</ns3:BehalfOfBranch></ns2:Context><ns2:TheirRef>001-040-0087565</ns2:TheirRef><ns2:Programme>ASEGURADORASUR</ns2:Programme><ns2:Seller>1722466420001</ns2:Seller><ns2:Buyer>0190123626001</ns2:Buyer><ns2:AnchorParty>0190123626001</ns2:AnchorParty><ns2:MaturityDate>2024-03-21</ns2:MaturityDate><ns2:FinanceCurrency>USD</ns2:FinanceCurrency><ns2:FinancePercent>32.22</ns2:FinancePercent><ns2:FinanceDate>2024-01-19</ns2:FinanceDate><ns2:InvoiceNumberss><ns2:InvoiceNumbers><ns2:InvoiceNumber>001-040-0087565</ns2:InvoiceNumber><ns2:IssueDate>2023-12-19</ns2:IssueDate><ns2:OutstandingAmount>32.22</ns2:OutstandingAmount><ns2:OutstandingAmountCurrency>USD</ns2:OutstandingAmountCurrency></ns2:InvoiceNumbers></ns2:InvoiceNumberss><ns2:ExtraData><ns4:FinanceSellerAccount>AH2100086419</ns4:FinanceSellerAccount></ns2:ExtraData></ns2:TFBUYFIN></ServiceRequest>"
            .formatted(invoiceCorrelationUuid);

    var actualOutgoingMessage = new AtomicReference<String>();

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertAndSetActualMessage(actualOutgoingMessage);
    assertEquals(expectedOutgoingMessage, actualOutgoingMessage.get());

    mockSuccessfulInvoiceFinancingTiResponses(invoiceCorrelationUuid);

    // FTI Reply flow
    await()
        .atMost(Durations.ONE_SECOND)
        .pollDelay(Durations.ONE_HUNDRED_MILLISECONDS)
        .untilAsserted(
            () -> {
              verify(invoiceEventService).deleteInvoiceByUuid(invoiceCorrelationUuid);
              verify(invoiceEventService, times(0)).findInvoiceEventInfoByUuid(anyString());

              verifyNoInteractions(businessBankingMapper, businessBankingClient);
            });

    // TICC incoming flow
    // ---
    // Start Date: 2024-01-19
    // Maturity Date: 2024-03-21
    // Buyer term: 112
    // Seller term: 62
    var expectedAmountOfTotalRequests = 7;
    var expectedFinancedEmailRequestBody =
        "{\"data\":{\"flow\":{\"state\":\"notificacionesAdicionales\"},\"requester\":{\"documentNumber\":\"1722466420001\",\"documentType\":\"001\"},\"additionalRecipient\":[{\"email\":{\"address\":\"dareyesp@pichincha.com\"},\"cellphone\":{\"internationalPrefix\":\" \",\"number\":\" \"}}],\"template\":{\"templateId\":\"305003729\",\"sequentialId\":\"0\",\"fields\":[{\"key\":\"RucEnmascarado\",\"value\":\"xxxxxx6420001\"},{\"key\":\"NombreEmpresa\",\"value\":\"DAVID REYES\"},{\"key\":\"FechaIngreso\",\"value\":\"2024-01-19\"},{\"key\":\"HoraIngreso\",\"value\":\"12:00\"},{\"key\":\"action\",\"value\":\"Anticipo de la factura No. 001-040-0087565 por USD 32.22\"},{\"key\":\"urlBanca\",\"value\":\"https://bancaempresas.pichincha.com\"}]}}}";
    var expectedProcessedEmailRequestBody =
        "{\"data\":{\"flow\":{\"state\":\"notificacionesAdicionales\"},\"requester\":{\"documentNumber\":\"1722466420001\",\"documentType\":\"001\"},\"additionalRecipient\":[{\"email\":{\"address\":\"dareyesp@pichincha.com\"},\"cellphone\":{\"internationalPrefix\":\" \",\"number\":\" \"}}],\"template\":{\"templateId\":\"305003729\",\"sequentialId\":\"0\",\"fields\":[{\"key\":\"RucEnmascarado\",\"value\":\"xxxxxx6420001\"},{\"key\":\"NombreEmpresa\",\"value\":\"DAVID REYES\"},{\"key\":\"FechaIngreso\",\"value\":\"2024-01-19\"},{\"key\":\"HoraIngreso\",\"value\":\"12:00\"},{\"key\":\"action\",\"value\":\"Procesamiento de la factura No. 001-040-0087565 por USD 32.22\"},{\"key\":\"urlBanca\",\"value\":\"https://bancaempresas.pichincha.com\"}]}}}";
    var expectedCreditCreationRequestBody =
        "xsejW/Pkljz4wWE9VaKXqbqg6Fx93teAX2i0APcIuKy73kp6RG7vWDfODgQZNP4QNFxSmcV+tqS32tcj07+ktlL+fRabyBOTx2lB7ZZT4LoLA5Veq+fpXF3fR3rs540EKTgsX1ZE4Tvk9ciojBVz3UEk0M7eCCffHyGKjELQKe//hkEAkdb2e7M+gBb6Qlr5IYuLjKBuz7L49WWs7CWS8BPCMu577PaOKzM2sUFY8xwWwPO2oHKy7HEY41+mjMTmdR2yj2X4aCQRVmGfwrNXSdBfuFyqozDlir79oD5CxfDDCbb+19QKEGt723HVHFumwmjcfd3Y/C8crHBzpxSlsMaK9BEb6UOeecFy/HWvzgoeDK63xS03Ea6VDIhMQhd+Aqi04kGogosozn32IjDmmmD8eArMk1FvEoYBCLSP6I/InVKoaK6MSdksiuLbiGLYALJDLjnn+ehzoIR/yCR2xPsC39EsbKQmba1gEpJP4wf9mLzXc14urpwRqFlud7nEUsBnKl7Gaa3R5iTmCkuAebxa7WE26GvslOn1SGO6W+YZfjfFXDi1yHvh5aWHoXg/+ynvqjQlgd/vZgJY8oxmMwfcrfx5jFnqJnp5QcMjNwJJZ2ck3xbfDXfjrRnyvb5re+K3L3o0cAZ6gT30lcNTkQrPGUzPcGdYmbqIhQ8fKbgH+5EcST4c+lW52DtixTsa/u2Vy6g3SVb9YjQOQXt4Z+E5QhvtoYCVmZB5ODugL9H3Cg0ukna68gY/J2w+NIY75xsyGTNm/GHxlN5LXCwoDE/JbHuVucZiksFiZXiiY4AxY8o8bUBMWMbN62Eb3srb";
    var expectedCreditSimulationRequestBody =
        "xsejW/Pkljz4wWE9VaKXqbqg6Fx93teAX2i0APcIuKy73kp6RG7vWDfODgQZNP4QNFxSmcV+tqS32tcj07+ktlL+fRabyBOTx2lB7ZZT4LoLA5Veq+fpXF3fR3rs540EKTgsX1ZE4Tvk9ciojBVz3UEk0M7eCCffHyGKjELQKe//hkEAkdb2e7M+gBb6Qlr5IYuLjKBuz7L49WWs7CWS8BPCMu577PaOKzM2sUFY8xwWwPO2oHKy7HEY41+mjMTmdR2yj2X4aCQRVmGfwrNXSdBfuFyqozDlir79oD5CxfDDCbb+19QKEGt723HVHFumwmjcfd3Y/C8crHBzpxSlsMaK9BEb6UOeecFy/HWvzgoeDK63xS03Ea6VDIhMQhd+Aqi04kGogosozn32IjDmmgRL8NwcddAEJLUIsT3Z91oo1mHHFJg/KTCykk0pzUoreo3+KUdQdReO35d4l3U6YPF5btDObGSRe0FN1qzf25zZovI0v/trPTl5/3ngiZ9lyt43H3S19m+qkP1/wbfhPKp2EIJImbTOyEDOwmwgrW0agFuM5FnfHYQ8g2orU7r5D5oZw7XBYsmT6UtuTQJ3ziJhgHgS4NAIoWGp6GX1aq5sc/N48BW5K0SgKwy0syLJcm1J6EPISlc6apyZgqfGunn0X+Bye8qc2lnMx58mkUhJq6dm77khT6LH/8ntwiXnQAqNGJ31sz/Eq2kPJ0R7KdLJkSqIeT1vCUrJsaJhIfxhhqndgrVr9pgccR0IwhDsngCEPBbAR+9LxO8ur5QAEFA4EIuTggjGlshN1IBfLJ13OoidkfE3EwhqPTPCUaTq";
    var expectedOperationalGwRequestBody =
        "cZcYwUNfTQ4kWKr+P4y7DM+T0wbqcM3hrxMMwHJ1BKpOYOORXBLIyqkoxtPlDnuQsmWPwhLjrFEHpSBmc11PDwEmIJNbTqJEC5KJhndzPJ7OBxDfWFgsXEUBa+HiwGpPetLyY4uIeVbpGkR5NKq6rhv+1KhdxeF5Wbyu4kEXIicEyuE6MFk6r24qZ/ONJ11Jq1YXpK2Ojq5XZRxBHxuvGvHVZhEybmMc4IonNfFWWo6RwrGjnS6cTmKnU/Eh2Cw2BVb1AgUNJMRkCBOWWh633U9INAG1S84gFyNiAzsW1Bb02bAWHQuTvgf6I57tENoB4G/mSTnwneNssJq0mjaoXUWbcEODcWLXnsIXARcBfinX2GbuAHFOFq/MBDBd4D29cmBNZSZYW1DyUHAIa3TYQgO8aBBsdC76oD5CVemOvUg=";

    await()
        .atMost(Durations.FIVE_SECONDS)
        .pollDelay(Durations.ONE_HUNDRED_MILLISECONDS)
        .untilAsserted(() -> assertEquals(1L, latch.getCount()));
    mockSuccessfulPaymentResponse(creditPaymentReference);

    // Wait until invoice taxes correlation payment is stored in redis
    latch.await(5, TimeUnit.SECONDS);
    mockSuccessfulPaymentResponse(creditPaymentReference);

    // Lo demás que debería funcionar igual.
    var requestExpectationsRef = new AtomicReference<HttpRequest[]>();
    await()
        .atMost(Durations.ONE_SECOND)
        .pollDelay(Durations.ONE_HUNDRED_MILLISECONDS)
        .untilAsserted(
            () -> {
              var tempExpectations = mockServerClient.retrieveRecordedRequests(request());
              assertEquals(expectedAmountOfTotalRequests, tempExpectations.length);

              requestExpectationsRef.set(tempExpectations);
            });

    var requestExpectations = requestExpectationsRef.get();
    var actualFinancedEmailRequestBody = requestExpectations[0].getBodyAsString();
    var actualCreditCreationRequest = requestExpectations[1];
    var actualCreditSimulationRequest = requestExpectations[3];
    var actualProcessedEmailRequestBody = requestExpectations[5].getBodyAsString();
    var actualOperationalGwRequestBody = requestExpectations[6].getBodyAsString();

    // Credit creation (1 request) and credit simulation (1 request)
    verify(corporateLoanClient, times(2)).createCredit(anyMap(), any());
    // Credit amount from buyer to bgl and from bgl to seller (1 request)
    // Credit simulation from seller to bgl and from bgl to buyer (1 request)
    verify(singlePaymentClient, times(2))
        .createSinglePayment(anyMap(), any(SinglePaymentRequest.class));
    verify(businessBankingClient).notifyEvent(anyMap(), any());

    JSONAssert.assertEquals(expectedFinancedEmailRequestBody, actualFinancedEmailRequestBody, true);
    JSONAssert.assertEquals(
        expectedProcessedEmailRequestBody, actualProcessedEmailRequestBody, true);
    assertEquals(expectedCreditCreationRequestBody, actualCreditCreationRequest.getBodyAsString());
    assertEquals("C/D", actualCreditCreationRequest.getFirstHeader("X-Operation-Id"));
    assertEquals(
        expectedCreditSimulationRequestBody, actualCreditSimulationRequest.getBodyAsString());
    assertEquals("V/X", actualCreditSimulationRequest.getFirstHeader("X-Operation-Id"));
    assertEquals(expectedOperationalGwRequestBody, actualOperationalGwRequestBody);
  }

  private void mockSuccessfulInvoiceFinancingTiResponses(String invoiceUuid) throws JMSException {
    mockSuccessfulFtiReply(invoiceUuid);

    var ticcMessage =
        session.createTextMessage(
            String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <ns2:ServiceRequest xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ns2=\"urn:control.services.tiplus2.misys.com\"> <ns2:RequestHeader> <ns2:Service>GATEWAY</ns2:Service> <ns2:Operation>TFBCFCRE</ns2:Operation> <ns2:Credentials /> <ns2:ReplyFormat>FULL</ns2:ReplyFormat> <ns2:TargetSystem>CorporateChannels</ns2:TargetSystem> <ns2:SourceSystem>ZONE1</ns2:SourceSystem> <ns2:NoRepair>Y</ns2:NoRepair> <ns2:NoOverride>Y</ns2:NoOverride> <ns2:CorrelationId>%s</ns2:CorrelationId> <ns2:TransactionControl>NONE</ns2:TransactionControl> <ns2:CreationDate>2024-03-15</ns2:CreationDate> </ns2:RequestHeader> <ns2:tfinvfindet> <ns2:MessageName>TFBCFCRE</ns2:MessageName> <ns2:eBankMasterRef xsi:nil=\"true\" /> <ns2:eBankEventRef xsi:nil=\"true\" /> <ns2:MasterRef>IRF00000218BPCH</ns2:MasterRef> <ns2:TheirRef>001-040-0087565</ns2:TheirRef> <ns2:EventRef>CRE001</ns2:EventRef> <ns2:BehalfOfBranch>BPEC</ns2:BehalfOfBranch> <ns2:SBB>GPCH</ns2:SBB> <ns2:MBE>BPCH</ns2:MBE> <ns2:Programme>ASEGURADORASUR</ns2:Programme> <ns2:ProgrammeTypeCode>B</ns2:ProgrammeTypeCode> <ns2:BuyerIdentifier>0190123626001</ns2:BuyerIdentifier> <ns2:SellerIdentifier>1722466420001</ns2:SellerIdentifier> <ns2:AnchorPartyCustomerMnemonic>0190123626001</ns2:AnchorPartyCustomerMnemonic> <ns2:CounterpartyCustomerMnemonic>1722466420001</ns2:CounterpartyCustomerMnemonic> <ns2:SellerName>DAVID REYES</ns2:SellerName> <ns2:SellerAddr1>Amazonas</ns2:SellerAddr1> <ns2:SellerAddr2 xsi:nil=\"true\" /> <ns2:SellerAddr3 xsi:nil=\"true\" /> <ns2:SellerAddr4 xsi:nil=\"true\" /> <ns2:SellerCountry>EC</ns2:SellerCountry> <ns2:BuyerName>ASEGURADORA DEL SUR C.A.</ns2:BuyerName> <ns2:BuyerAddr1>Av Amazonas 111</ns2:BuyerAddr1> <ns2:BuyerAddr2 xsi:nil=\"true\" /> <ns2:BuyerAddr3 xsi:nil=\"true\" /> <ns2:BuyerAddr4 xsi:nil=\"true\" /> <ns2:BuyerCountry>EC</ns2:BuyerCountry> <ns2:FinancePercent><![CDATA[32.22]]></ns2:FinancePercent> <ns2:EventCode>IRCR</ns2:EventCode> <ns2:BuyerBOB>BPEC</ns2:BuyerBOB> <ns2:SellerBOB>BPEC</ns2:SellerBOB> <ns2:Product>IRF</ns2:Product> <ns2:SCFBuyerRef xsi:nil=\"true\" /> <ns2:SCFSellerRef xsi:nil=\"true\" /> <ns2:ProductSubType xsi:nil=\"true\" /> <ns2:StartDate>2024-01-19</ns2:StartDate> <ns2:DueDate>2024-03-21</ns2:DueDate> <ns2:FinancingRef>001-040-0087565</ns2:FinancingRef> <ns2:FinanceDealAmount>3222</ns2:FinanceDealAmount> <ns2:FinanceDealCurrency>USD</ns2:FinanceDealCurrency> <ns2:OutstandingFinanceAmount>2184</ns2:OutstandingFinanceAmount> <ns2:OutstandingFinanceCurrency>USD</ns2:OutstandingFinanceCurrency> <ns2:OutstandingAmount>1038</ns2:OutstandingAmount> <ns2:OutstandingCurrency>USD</ns2:OutstandingCurrency> <ns2:ReceivedOn>2024-03-20</ns2:ReceivedOn> <ns2:MaturityDate>2024-03-21</ns2:MaturityDate> <ns2:FinancePaymentDetails> <ns2:FinancePaymentDetails>No</ns2:FinancePaymentDetails> <ns2:Amount>10.30</ns2:Amount> <ns2:Currency>USD</ns2:Currency> <ns2:ValueDate>2024-01-19</ns2:ValueDate> <ns2:AccountDetails>Ourselves</ns2:AccountDetails> <ns2:SettlementParty>DAVID REYES</ns2:SettlementParty> </ns2:FinancePaymentDetails> <ns2:SenderToReceiverInfo xsi:nil=\"true\" /> <ns2:InvoiceArray> <ns2:InvoiceReference>INV00001110BPCH</ns2:InvoiceReference> <ns2:InvoiceNumber>001-040-0087565</ns2:InvoiceNumber> <ns2:InvoiceIssueDate>2023-12-19</ns2:InvoiceIssueDate> <ns2:InvoiceSettlementDate>2024-03-21</ns2:InvoiceSettlementDate> <ns2:InvoiceOutstandingAmount>32.22</ns2:InvoiceOutstandingAmount> <ns2:InvoiceOutstandingAmountCurrency>USD</ns2:InvoiceOutstandingAmountCurrency> <ns2:InvoiceAdvanceAmount>10.38</ns2:InvoiceAdvanceAmount> <ns2:InvoiceAdvanceAmountCurrency>USD</ns2:InvoiceAdvanceAmountCurrency> </ns2:InvoiceArray> </ns2:tfinvfindet> </ns2:ServiceRequest>"
                    .formatted(invoiceUuid)));
    ticcProducer.send(ticcMessage);
  }

  private void mockSuccessfulFtiReply(String invoiceUuid) throws JMSException {
    var ftiReplyMessage =
        session.createTextMessage(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ServiceResponse xmlns=\"urn:control.services.tiplus2.misys.com\" xmlns:ns2=\"urn:messages.service.ti.apps.tiplus2.misys.com\" xmlns:ns3=\"urn:common.service.ti.apps.tiplus2.misys.com\"><ResponseHeader><Service>TI</Service><Operation>%s</Operation><Status>SUCCEEDED</Status><CorrelationId>%s</CorrelationId></ResponseHeader></ServiceResponse>"
                .formatted(TIOperation.FINANCE_INVOICE.getValue(), invoiceUuid));

    ftiReplyProducer.send(ftiReplyMessage);
  }

  private void mockSuccessfulPaymentResponse(String paymentReference) throws JMSException {
    var paymentMessage =
        session.createTextMessage(
            "{\"status\":\"SUCCEEDED\",\"paymentReference\":\"%s\",\"type\":\"bgl-client-transfer\"}"
                .formatted(paymentReference));

    fcmProducer.send(paymentMessage);
  }

  private void assertAndSetActualMessage(AtomicReference<String> actualOutgoingMessage) {
    await()
        .atMost(Durations.ONE_SECOND)
        .pollDelay(Durations.ONE_HUNDRED_MILLISECONDS)
        .untilAsserted(
            () -> {
              var message = ftiConsumer.receiveNoWait();
              assertNotNull(message);
              actualOutgoingMessage.set(message.getBody(String.class));

              message.acknowledge();
            });
  }
}
