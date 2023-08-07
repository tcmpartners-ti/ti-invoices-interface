package com.tcmp.tiapi.customer;

import com.tcmp.tiapi.program.ProgramRepository;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {
  private final CustomerRepository customerRepository;
  private final ProgramRepository programRepository;

  public Page<Program> getCustomerPrograms(String customerMnemonic, PageParams pageParams) {
    if (!customerRepository.existsByIdMnemonic(customerMnemonic)) {
      throw new NotFoundHttpException(
        String.format("Could not find customer with mnemonic %s.", customerMnemonic));
    }

    return programRepository.findAllByCustomerMnemonic(
      customerMnemonic, PageRequest.of(pageParams.getPage(), pageParams.getSize()));
  }
}
