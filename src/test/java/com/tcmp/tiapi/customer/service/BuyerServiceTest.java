package com.tcmp.tiapi.customer.service;

import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.program.ProgramMapper;
import com.tcmp.tiapi.program.ProgramRepository;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuyerServiceTest {
  @Mock
  private CustomerRepository customerRepository;
  @Mock
  private ProgramRepository programRepository;
  @Mock
  private ProgramMapper programMapper;

  private BuyerService testedBuyerService;

  @BeforeEach
  void setUp() {
    testedBuyerService = new BuyerService(
      customerRepository,
      programRepository,
      programMapper
    );
  }

  @Test
  void itShouldThrowNotFoundExceptionWhenCustomerNotFound() {
    String expectedBuyerMnemonic = "1722466421001";
    PageParams pageParams = new PageParams();

    when(customerRepository.existsByIdMnemonic(anyString()))
      .thenReturn(false);

    assertThrows(NotFoundHttpException.class, () ->
        testedBuyerService.getBuyerProgramsByMnemonic(
          expectedBuyerMnemonic,
          pageParams
        ),
      String.format("Could not find customer with mnemonic %s.", expectedBuyerMnemonic)
    );
  }

  @Test
  void itShouldRequestPageFromReceivedParams() {
    String expectedBuyerMnemonic = "1722466421001";
    PageParams expectedPageParams = new PageParams();

    List<Program> mockPrograms = List.of(
      Program.builder()
        .pk(1L)
        .build(),
      Program.builder()
        .pk(2L)
        .build()
    );

    ArgumentCaptor<String> buyerMnemonicArgumentCaptor =
      ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<PageRequest> pageRequestArgumentCaptor =
      ArgumentCaptor.forClass(PageRequest.class);

    when(customerRepository.existsByIdMnemonic(anyString()))
      .thenReturn(true);
    when(programRepository.findAllByCustomerMnemonic(
      buyerMnemonicArgumentCaptor.capture(),
      pageRequestArgumentCaptor.capture()
    ))
      .thenReturn(new PageImpl<>(mockPrograms));

    testedBuyerService.getBuyerProgramsByMnemonic(
      expectedBuyerMnemonic,
      expectedPageParams
    );

    assertEquals(expectedBuyerMnemonic, buyerMnemonicArgumentCaptor.getValue());
    assertEquals(expectedPageParams.getPage(), pageRequestArgumentCaptor.getValue().getPageNumber());
    assertEquals(expectedPageParams.getSize(), pageRequestArgumentCaptor.getValue().getPageSize());
  }
}
