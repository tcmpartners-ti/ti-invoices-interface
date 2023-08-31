package com.tcmp.tiapi.customer.service;

import com.tcmp.tiapi.customer.model.CounterParty;
import com.tcmp.tiapi.customer.model.CounterPartyRole;
import com.tcmp.tiapi.customer.repository.CounterPartyRepository;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.InvoiceRepository;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.program.ProgramRepository;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {
  private final CustomerRepository customerRepository;
  private final ProgramRepository programRepository;
  private final CounterPartyRepository counterPartyRepository;
  private final InvoiceRepository invoiceRepository;

  public Page<Program> getCustomerPrograms(String customerMnemonic, PageParams pageParams) {
    if (!customerRepository.existsByIdMnemonic(customerMnemonic)) {
      throw new NotFoundHttpException(
        String.format("Could not find customer with mnemonic %s.", customerMnemonic));
    }

    return programRepository.findAllByCustomerMnemonic(
      customerMnemonic, PageRequest.of(pageParams.getPage(), pageParams.getSize()));
  }

  public Page<InvoiceMaster> getCustomerInvoices(String customerMnemonic, PageParams pageParams) {
    if (!customerRepository.existsByIdMnemonic(customerMnemonic)) {
      throw new NotFoundHttpException(
        String.format("Could not find customer with mnemonic %s.", customerMnemonic));
    }

    // Buyer is the anchor client.
    List<Long> counterPartyIds = counterPartyRepository.findByCustomerMnemonicAndRole(customerMnemonic, CounterPartyRole.BUYER.getValue())
      .stream().map(CounterParty::getId)
      .toList();

    return invoiceRepository.findByBuyerIdIn(
      counterPartyIds, PageRequest.of(pageParams.getPage(), pageParams.getSize()));
  }
}
