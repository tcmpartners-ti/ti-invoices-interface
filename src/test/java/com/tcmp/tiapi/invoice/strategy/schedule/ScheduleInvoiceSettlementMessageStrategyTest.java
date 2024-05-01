package com.tcmp.tiapi.invoice.strategy.schedule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.strategy.ticc.InvoiceSettlementFlowStrategy;
import com.tcmp.tiapi.schedule.ScheduledMessageRepository;
import com.tcmp.tiapi.schedule.model.ScheduledMessage;
import com.tcmp.tiapi.shared.UUIDGenerator;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ScheduleInvoiceSettlementMessageStrategyTest {
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private ScheduledMessageRepository scheduledMessageRepository;
  @Mock private InvoiceSettlementFlowStrategy invoiceSettlementFlowStrategy;
  @Mock private UUIDGenerator uuidGenerator;
  @Mock private Clock clock;

  @InjectMocks
  private ScheduleInvoiceSettlementMessageStrategy scheduleInvoiceSettlementMessageStrategy;

  @Test
  void handleIncomingMessage_itShouldProcessMessageOnConfiguration() {
    ReflectionTestUtils.setField(
        scheduleInvoiceSettlementMessageStrategy, "shouldScheduleMessages", false);

    scheduleInvoiceSettlementMessageStrategy.handleIncomingMessage(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ServiceRequest xmlns=\"urn:control.services.tiplus2.misys.com\" xmlns:ns2=\"urn:messages.service.ti.apps.tiplus2.misys.com\" xmlns:ns4=\"urn:custom.service.ti.apps.tiplus2.misys.com\" xmlns:ns3=\"urn:common.service.ti.apps.tiplus2.misys.com\"><RequestHeader><Service>GATEWAY</Service><Operation>TFINVSETCU</Operation><Credentials><Name>SUPERVISOR</Name></Credentials><ReplyFormat>FULL</ReplyFormat><TargetSystem>CorporateChannels</TargetSystem><SourceSystem>ZONE1</SourceSystem><NoRepair>Y</NoRepair><NoOverride>Y</NoOverride><CorrelationId>31378</CorrelationId><TransactionControl>NONE</TransactionControl><CreationDate>2024-02-07</CreationDate></RequestHeader><tfinvset xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><MessageName>TFINVSETCU</MessageName><eBankMasterRef xsi:nil=\"true\"/><eBankEventRef xsi:nil=\"true\"/><MasterRef>INV00002241BPCH</MasterRef><EventRef>STL001</EventRef><BehalfOfBranch>BPEC</BehalfOfBranch><SBB>GPCH</SBB><MBE>BPCH</MBE><BankComment>We have settled this Invoice.</BankComment><Programme>ASEGURADORA</Programme><ProgrammeTypeCode>B</ProgrammeTypeCode><BuyerIdentifier>0190123626001</BuyerIdentifier><SellerIdentifier>0992500913001</SellerIdentifier><AnchorPartyCustomerMnemonic>0190123626001</AnchorPartyCustomerMnemonic><CounterpartyCustomerMnemonic>0992500913001</CounterpartyCustomerMnemonic><BuyerBOB>BPEC</BuyerBOB><SCFBuyerRef xsi:nil=\"true\"/><SellerBOB>BPEC</SellerBOB><SCFSellerRef xsi:nil=\"true\"/><ReceivedOn>2024-02-07</ReceivedOn><IssueDate>2024-02-05</IssueDate><PaymentValueDate>2024-02-07</PaymentValueDate><InvoiceNumber>643-376-702447166</InvoiceNumber><PaymentAmount>50000</PaymentAmount><PaymentCurrency>USD</PaymentCurrency><OutstandingAmount>0</OutstandingAmount><OutstandingCurrency>USD</OutstandingCurrency><EligibleForFinancing>Y</EligibleForFinancing><InvoiceStatusCode>P</InvoiceStatusCode><NotesForCustomer xsi:nil=\"true\"/><NotesForBuyer xsi:nil=\"true\"/></tfinvset></ServiceRequest>\n");

    verify(invoiceSettlementFlowStrategy).handleServiceRequest(any());
  }

  @Test
  void handleIncomingMessage_itShouldProcessIfSettlementDateIsToday() {
    ReflectionTestUtils.setField(
        scheduleInvoiceSettlementMessageStrategy, "shouldScheduleMessages", true);

    var mockedToday = LocalDate.of(2024, 2, 8);
    var mockedClock =
        Clock.fixed(
            mockedToday.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    when(clock.instant()).thenReturn(mockedClock.instant());
    when(clock.getZone()).thenReturn(mockedClock.getZone());
    when(invoiceRepository.findByProductMasterMasterReference(anyString()))
        .thenReturn(Optional.of(InvoiceMaster.builder().settlementDate(mockedToday).build()));

    scheduleInvoiceSettlementMessageStrategy.handleIncomingMessage(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ServiceRequest xmlns=\"urn:control.services.tiplus2.misys.com\" xmlns:ns2=\"urn:messages.service.ti.apps.tiplus2.misys.com\" xmlns:ns4=\"urn:custom.service.ti.apps.tiplus2.misys.com\" xmlns:ns3=\"urn:common.service.ti.apps.tiplus2.misys.com\"><RequestHeader><Service>GATEWAY</Service><Operation>TFINVSETCU</Operation><Credentials><Name>SUPERVISOR</Name></Credentials><ReplyFormat>FULL</ReplyFormat><TargetSystem>CorporateChannels</TargetSystem><SourceSystem>ZONE1</SourceSystem><NoRepair>Y</NoRepair><NoOverride>Y</NoOverride><CorrelationId>31378</CorrelationId><TransactionControl>NONE</TransactionControl><CreationDate>2024-02-07</CreationDate></RequestHeader><tfinvset xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><MessageName>TFINVSETCU</MessageName><eBankMasterRef xsi:nil=\"true\"/><eBankEventRef xsi:nil=\"true\"/><MasterRef>INV00002241BPCH</MasterRef><EventRef>STL001</EventRef><BehalfOfBranch>BPEC</BehalfOfBranch><SBB>GPCH</SBB><MBE>BPCH</MBE><BankComment>We have settled this Invoice.</BankComment><Programme>ASEGURADORA</Programme><ProgrammeTypeCode>B</ProgrammeTypeCode><BuyerIdentifier>0190123626001</BuyerIdentifier><SellerIdentifier>0992500913001</SellerIdentifier><AnchorPartyCustomerMnemonic>0190123626001</AnchorPartyCustomerMnemonic><CounterpartyCustomerMnemonic>0992500913001</CounterpartyCustomerMnemonic><BuyerBOB>BPEC</BuyerBOB><SCFBuyerRef xsi:nil=\"true\"/><SellerBOB>BPEC</SellerBOB><SCFSellerRef xsi:nil=\"true\"/><ReceivedOn>2024-02-07</ReceivedOn><IssueDate>2024-02-05</IssueDate><PaymentValueDate>2024-02-07</PaymentValueDate><InvoiceNumber>643-376-702447166</InvoiceNumber><PaymentAmount>50000</PaymentAmount><PaymentCurrency>USD</PaymentCurrency><OutstandingAmount>0</OutstandingAmount><OutstandingCurrency>USD</OutstandingCurrency><EligibleForFinancing>Y</EligibleForFinancing><InvoiceStatusCode>P</InvoiceStatusCode><NotesForCustomer xsi:nil=\"true\"/><NotesForBuyer xsi:nil=\"true\"/></tfinvset></ServiceRequest>\n");

    verify(invoiceSettlementFlowStrategy).handleServiceRequest(any());
  }

  @Test
  void handleIncomingMessage_itShouldScheduleMessage() {
    ReflectionTestUtils.setField(
        scheduleInvoiceSettlementMessageStrategy, "shouldScheduleMessages", true);

    LocalDate mockedToday = LocalDate.of(2024, 2, 8);
    var mockedClock =
        Clock.fixed(
            mockedToday.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    when(clock.instant()).thenReturn(mockedClock.instant());
    when(clock.getZone()).thenReturn(mockedClock.getZone());
    when(invoiceRepository.findByProductMasterMasterReference(anyString()))
        .thenReturn(
            Optional.of(InvoiceMaster.builder().settlementDate(mockedToday.plusDays(1L)).build()));
    when(uuidGenerator.getNewId()).thenReturn("000-001");

    scheduleInvoiceSettlementMessageStrategy.handleIncomingMessage(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ServiceRequest xmlns=\"urn:control.services.tiplus2.misys.com\" xmlns:ns2=\"urn:messages.service.ti.apps.tiplus2.misys.com\" xmlns:ns4=\"urn:custom.service.ti.apps.tiplus2.misys.com\" xmlns:ns3=\"urn:common.service.ti.apps.tiplus2.misys.com\"><RequestHeader><Service>GATEWAY</Service><Operation>TFINVSETCU</Operation><Credentials><Name>SUPERVISOR</Name></Credentials><ReplyFormat>FULL</ReplyFormat><TargetSystem>CorporateChannels</TargetSystem><SourceSystem>ZONE1</SourceSystem><NoRepair>Y</NoRepair><NoOverride>Y</NoOverride><CorrelationId>31378</CorrelationId><TransactionControl>NONE</TransactionControl><CreationDate>2024-02-07</CreationDate></RequestHeader><tfinvset xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><MessageName>TFINVSETCU</MessageName><eBankMasterRef xsi:nil=\"true\"/><eBankEventRef xsi:nil=\"true\"/><MasterRef>INV00002241BPCH</MasterRef><EventRef>STL001</EventRef><BehalfOfBranch>BPEC</BehalfOfBranch><SBB>GPCH</SBB><MBE>BPCH</MBE><BankComment>We have settled this Invoice.</BankComment><Programme>ASEGURADORA</Programme><ProgrammeTypeCode>B</ProgrammeTypeCode><BuyerIdentifier>0190123626001</BuyerIdentifier><SellerIdentifier>0992500913001</SellerIdentifier><AnchorPartyCustomerMnemonic>0190123626001</AnchorPartyCustomerMnemonic><CounterpartyCustomerMnemonic>0992500913001</CounterpartyCustomerMnemonic><BuyerBOB>BPEC</BuyerBOB><SCFBuyerRef xsi:nil=\"true\"/><SellerBOB>BPEC</SellerBOB><SCFSellerRef xsi:nil=\"true\"/><ReceivedOn>2024-02-07</ReceivedOn><IssueDate>2024-02-05</IssueDate><PaymentValueDate>2024-02-07</PaymentValueDate><InvoiceNumber>643-376-702447166</InvoiceNumber><PaymentAmount>50000</PaymentAmount><PaymentCurrency>USD</PaymentCurrency><OutstandingAmount>0</OutstandingAmount><OutstandingCurrency>USD</OutstandingCurrency><EligibleForFinancing>Y</EligibleForFinancing><InvoiceStatusCode>P</InvoiceStatusCode><NotesForCustomer xsi:nil=\"true\"/><NotesForBuyer xsi:nil=\"true\"/></tfinvset></ServiceRequest>\n");

    verifyNoInteractions(invoiceSettlementFlowStrategy);
    verify(scheduledMessageRepository).save(any(ScheduledMessage.class));
  }
}
