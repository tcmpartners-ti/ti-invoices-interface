package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.service.InvoiceFinancingService;
import com.tcmp.tiapi.titoapigee.corporateloan.CorporateLoanService;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Data;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Error;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import com.tcmp.tiapi.titoapigee.paymentexecution.PaymentExecutionService;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.BusinessAccountTransfersResponse;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.TransferResponseData;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceFinanceResultFlowRouteBuilderTest extends CamelTestSupport {
  private static final String URI_FROM = "direct:startFinanceFlow";

  @Mock private InvoiceFinancingService invoiceFinancingService;
  @Mock private CorporateLoanService corporateLoanService;
  @Mock private PaymentExecutionService paymentExecutionService;
  @Mock private OperationalGatewayService operationalGatewayService;

  @EndpointInject(URI_FROM) ProducerTemplate from;

  @Override
  protected RoutesBuilder createRouteBuilder() {
    return new InvoiceFinanceResultFlowRouteBuilder(
      invoiceFinancingService,
      corporateLoanService,
      paymentExecutionService,
      operationalGatewayService,

      URI_FROM
    );
  }

  @Test
  void financeEvent_itShouldSendBothNotificationsToSellerIfSuccessful() {
    String invoiceFinanceMessage = buildMockFinanceAck();

    when(invoiceFinancingService.findCustomerByMnemonic(anyString()))
      .thenReturn(Customer.builder().build()) // Buyer
      .thenReturn(Customer.builder().build()); // Seller
    when(invoiceFinancingService.findProductMasterExtensionByMasterReference(anyString()))
      .thenReturn(ProductMasterExtension.builder().financeAccount("CC0974631820").build());
    when(invoiceFinancingService.findAccountByCustomerMnemonic(anyString()))
      .thenReturn(Account.builder().externalAccountNumber("AH0974631821").build());
    when(corporateLoanService.createCredit(any()))
      .thenReturn(new DistributorCreditResponse(Data.builder()
        .disbursementAmount(100)
        .error(new Error("", "", "INFO"))
        .build()));
    when(invoiceFinancingService.buildInvoiceFinancingEmailInfo(any(), any(), any(), any()))
      .thenReturn(InvoiceEmailInfo.builder().build());
    when(paymentExecutionService.makeTransactionRequest(any()))
      .thenReturn(new BusinessAccountTransfersResponse(
        new TransferResponseData("OK", "", "")))
      .thenReturn(new BusinessAccountTransfersResponse(
        new TransferResponseData("OK", "", "")));

    from.sendBody(invoiceFinanceMessage);

    verify(operationalGatewayService, times(2))
      .sendNotificationRequest(any(InvoiceEmailInfo.class));
  }

  @Test
  void financeEvent_itShouldSendOneNotificationIfTransactionFailed() {
    String invoiceFinanceMessage = buildMockFinanceAck();

    when(invoiceFinancingService.findCustomerByMnemonic(anyString()))
      .thenReturn(Customer.builder().build()) // Buyer
      .thenReturn(Customer.builder().build()); // Seller
    when(invoiceFinancingService.findProductMasterExtensionByMasterReference(anyString()))
      .thenReturn(ProductMasterExtension.builder().financeAccount("CC0974631820").build());
    when(invoiceFinancingService.findAccountByCustomerMnemonic(anyString()))
      .thenReturn(Account.builder().externalAccountNumber("AH0974631821").build());
    when(corporateLoanService.createCredit(any()))
      .thenReturn(new DistributorCreditResponse(Data.builder()
        .disbursementAmount(100)
        .error(new Error("", "", "INFO"))
        .build()));
    when(invoiceFinancingService.buildInvoiceFinancingEmailInfo(any(), any(), any(), any()))
      .thenReturn(InvoiceEmailInfo.builder().build());
    when(paymentExecutionService.makeTransactionRequest(any()))
      .thenReturn(new BusinessAccountTransfersResponse(
        new TransferResponseData("OK", "", "")))
      .thenReturn(new BusinessAccountTransfersResponse(
        new TransferResponseData("FAILED", "", "")));

    from.sendBody(invoiceFinanceMessage);

    verify(operationalGatewayService, times(1))
      .sendNotificationRequest(any(InvoiceEmailInfo.class));
  }

  private String buildMockFinanceAck() {
    return """
      <?xml version="1.0" encoding="UTF-8"?><ServiceRequest xmlns="urn:control.services.tiplus2.misys.com" xmlns:ns2="urn:messages.service.ti.apps.tiplus2.misys.com" xmlns:ns4="urn:custom.service.ti.apps.tiplus2.misys.com" xmlns:ns3="urn:common.service.ti.apps.tiplus2.misys.com"><RequestHeader><Service>GATEWAY</Service><Operation>TFBCFCRE</Operation><Credentials><Name>SUPERVISOR</Name></Credentials><ReplyFormat>FULL</ReplyFormat><TargetSystem>CorporateChannels</TargetSystem><SourceSystem>ZONE1</SourceSystem><NoRepair>Y</NoRepair><NoOverride>Y</NoOverride><CorrelationId>1883</CorrelationId><TransactionControl>NONE</TransactionControl><CreationDate>2023-12-07</CreationDate></RequestHeader><tfinvfindet xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      <MessageName>TFBCFCRE</MessageName>
      <eBankMasterRef xsi:nil="true"/>
      <eBankEventRef xsi:nil="true"/>
      <MasterRef>IRF00000126BPCH</MasterRef>
      <TheirRef>002-001-0000004</TheirRef>
      <EventRef>CRE001</EventRef>
      <BehalfOfBranch>BPEC</BehalfOfBranch>
      <SBB>GPCH</SBB>
      <MBE>BPCH</MBE>
      <Programme>TESTBUYER</Programme>
      <ProgrammeTypeCode>B</ProgrammeTypeCode>
      <BuyerIdentifier>1722466420003</BuyerIdentifier>
      <SellerIdentifier>1722466420002</SellerIdentifier>
      <AnchorPartyCustomerMnemonic>1722466420003</AnchorPartyCustomerMnemonic>
      <CounterpartyCustomerMnemonic>1722466420002</CounterpartyCustomerMnemonic>
      <SellerName>NASLY BARRERA</SellerName>
      <SellerAddr1>cuero y caicedo</SellerAddr1>
      <SellerAddr2 xsi:nil="true"/>
      <SellerAddr3 xsi:nil="true"/>
      <SellerAddr4 xsi:nil="true"/>
      <SellerCountry xsi:nil="true"/>
      <BuyerName>TESTBUYER</BuyerName>
      <BuyerAddr1>Av Amazonas 111</BuyerAddr1>
      <BuyerAddr2 xsi:nil="true"/>
      <BuyerAddr3 xsi:nil="true"/>
      <BuyerAddr4 xsi:nil="true"/>
      <BuyerCountry>EC</BuyerCountry>
      <FinancePercent><![CDATA[45.0 %]]></FinancePercent>
      <EventCode>IRCR</EventCode>
      <BuyerBOB>BPEC</BuyerBOB>
      <SCFBuyerRef xsi:nil="true"/>
      <SellerBOB>BPEC</SellerBOB>
      <SCFSellerRef xsi:nil="true"/>
      <Product>IRF</Product>
      <ProductSubType xsi:nil="true"/>
      <StartDate>2023-12-06</StartDate>
      <DueDate>2023-12-07</DueDate>
      <FinancingRef>002-001-0000004</FinancingRef>
      <FinanceDealAmount>45000</FinanceDealAmount>
      <FinanceDealCurrency>USD</FinanceDealCurrency>
      <OutstandingFinanceAmount>55000</OutstandingFinanceAmount>
      <OutstandingFinanceCurrency>USD</OutstandingFinanceCurrency>
      <OutstandingAmount>45000</OutstandingAmount>
      <OutstandingCurrency>USD</OutstandingCurrency>
      <ReceivedOn>2023-12-07</ReceivedOn>
      <MaturityDate>2023-12-07</MaturityDate>
      <FinancePaymentDetails><FinancePaymentDetails>No</FinancePaymentDetails><Amount>449.95</Amount><Currency>USD</Currency><ValueDate>2023-12-06</ValueDate><AccountDetails>Ourselves</AccountDetails><SettlementParty>NASLY BARRERA</SettlementParty></FinancePaymentDetails>
      <SenderToReceiverInfo xsi:nil="true"/>
      <InvoiceArray><InvoiceReference>INV00000627BPCH</InvoiceReference><InvoiceNumber>002-001-0000004</InvoiceNumber><InvoiceIssueDate>2023-11-30</InvoiceIssueDate><InvoiceSettlementDate >2023-12-07</InvoiceSettlementDate ><InvoiceOutstandingAmount>1,000.00</InvoiceOutstandingAmount><InvoiceOutstandingAmountCurrency>USD</InvoiceOutstandingAmountCurrency><InvoiceAdvanceAmount>450.00</InvoiceAdvanceAmount><InvoiceAdvanceAmountCurrency >USD</InvoiceAdvanceAmountCurrency ></InvoiceArray>
      </tfinvfindet></ServiceRequest>
       """;
  }
}
