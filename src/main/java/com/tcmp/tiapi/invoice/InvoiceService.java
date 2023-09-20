package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.customer.model.CounterParty;
import com.tcmp.tiapi.customer.model.CounterPartyRole;
import com.tcmp.tiapi.customer.repository.CounterPartyRepository;
import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceFinancingDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceSearchParams;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.dto.ti.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.dto.ti.FinanceBuyerCentricInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.program.ProgramRepository;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.exception.BadRequestHttpException;
import com.tcmp.tiapi.shared.exception.InvalidFileHttpException;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {
  private static final int MAX_BATCH_ID_LENGTH = 20;

  private final ProducerTemplate producerTemplate;

  private final InvoiceConfiguration invoiceConfiguration;
  private final InvoiceRepository invoiceRepository;
  private final CounterPartyRepository counterPartyRepository;
  private final ProgramRepository programRepository;
  private final InvoiceMapper invoiceMapper;

  public InvoiceDTO getInvoiceById(Long invoiceId) {
    InvoiceMaster invoice = invoiceRepository.findById(invoiceId)
      .orElseThrow(() -> new NotFoundHttpException(
        String.format("Could not find invoice with id %s.", invoiceId)));

    Long buyerId = invoice.getBuyerId();
    Long sellerId = invoice.getSellerId();
    Map<Long, CounterParty> idToCounterparties = getCounterPartiesByIds(List.of(buyerId, sellerId));

    Program program = getProgramByPk(invoice.getProgrammeId());

    return invoiceMapper.mapEntityToDTO(
      invoice,
      idToCounterparties.get(buyerId),
      idToCounterparties.get(sellerId),
      program
    );
  }

  private Map<Long, CounterParty> getCounterPartiesByIds(List<Long> counterPartyIds) {
    List<CounterParty> counterParties = counterPartyRepository.findByIdIn(counterPartyIds);
    return counterParties.stream().collect(Collectors.toMap(CounterParty::getId, c -> c));
  }

  public InvoiceDTO searchInvoice(InvoiceSearchParams searchParams) {
    Program program = getProgramById(searchParams.programme());
    CounterParty seller = getProgramCounterParty(program.getPk(), searchParams.seller(), CounterPartyRole.SELLER);
    CounterParty buyer = getProgramCounterParty(program.getPk(), program.getCustomerMnemonic(), CounterPartyRole.BUYER);
    InvoiceMaster invoice = getProgramAndSellerInvoice(program.getPk(), seller.getId(), searchParams.invoice());

    return invoiceMapper.mapEntityToDTO(invoice, buyer, seller, program);
  }

  private CounterParty getProgramCounterParty(Long programPk, String counterPartyMnemonic, CounterPartyRole role) {
    return counterPartyRepository.findByProgrammePkAndMnemonicAndRole(programPk, counterPartyMnemonic, role.getValue())
      .orElseThrow(() -> new NotFoundHttpException(
        String.format("Could not find a counter party with mnemonic %s related to the given program.", counterPartyMnemonic)));
  }

  private InvoiceMaster getProgramAndSellerInvoice(Long programmePk, Long sellerId, String invoiceReference) {
    return invoiceRepository.findFirstByProgrammeIdAndSellerIdAndReference(programmePk, sellerId, invoiceReference)
      .orElseThrow(() -> new NotFoundHttpException(
        String.format("Could not find the invoice %s for the given program and seller.", invoiceReference)));
  }

  private Program getProgramByPk(Long programPk) {
    return programRepository.findByPk(programPk)
      .orElseThrow(() -> new NotFoundHttpException(
        String.format("Could not find a program with id %s.", programPk)));
  }

  private Program getProgramById(String programId) {
    return programRepository.findById(programId)
      .orElseThrow(() -> new NotFoundHttpException(
        String.format("Could not find a program with id %s.", programId)));
  }

  public void createSingleInvoiceInTi(InvoiceCreationDTO invoiceDTO) {
    CreateInvoiceEventMessage createInvoiceEventMessage = invoiceMapper.mapDTOToFTIMessage(invoiceDTO);

    log.info("[Invoice: Create] {}", createInvoiceEventMessage);

    producerTemplate.sendBodyAndHeaders(
      invoiceConfiguration.getUriCreateFrom(),
      createInvoiceEventMessage,
      Map.ofEntries(
        Map.entry("JMSCorrelationID", createInvoiceEventMessage.getInvoiceNumber())
      )
    );
  }

  public void createMultipleInvoicesInTi(MultipartFile invoicesFile, String batchId) {
    if (invoicesFile.isEmpty()) throw new InvalidFileHttpException("File is empty.");

    if (batchId.length() > MAX_BATCH_ID_LENGTH) {
      throw new BadRequestHttpException("Batch id must be up to 20 characters.");
    }

    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(invoicesFile.getInputStream()))) {
      log.info("[Invoice: bulk create] Sending invoices to TI.");

      producerTemplate.sendBodyAndHeaders(
        invoiceConfiguration.getUriBulkCreateFrom(),
        bufferedReader,
        Map.ofEntries(
          Map.entry("batchId", batchId)
        )
      );
    } catch (IOException e) {
      log.error("[Invoice: bulk create] Invalid file uploaded");
      throw new InvalidFileHttpException("Could not read the uploaded file");
    }
  }

  public void financeInvoice(InvoiceFinancingDTO invoiceFinancingDTO) {
    FinanceBuyerCentricInvoiceEventMessage financeInvoiceMessage = invoiceMapper.mapFinancingDTOToFTIMessage(invoiceFinancingDTO);

    producerTemplate.sendBody(
      invoiceConfiguration.getUriFinanceFrom(),
      financeInvoiceMessage
    );
  }
}
