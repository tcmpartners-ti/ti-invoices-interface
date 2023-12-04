package com.tcmp.tiapi.customer.mapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tcmp.tiapi.customer.dto.request.CounterPartyDTO;
import com.tcmp.tiapi.customer.model.CounterParty;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CounterPartyMapperTest {
  @Autowired CounterPartyMapper counterPartyMapper;

  @Test
  void mapEntityToDTO() {
    CounterParty counterParty = buildMockCounterParty();

    CounterPartyDTO counterPartyDto = counterPartyMapper.mapEntityToDTO(counterParty);

    assertNotNull(counterPartyDto);
    assertNotNull(counterPartyDto.getMnemonic());
    assertNotNull(counterPartyDto.getName());
    assertNotNull(counterPartyDto.getAddress());
    assertNotNull(counterPartyDto.getBranch());
    assertNotNull(counterPartyDto.getStatus());
  }

  private CounterParty buildMockCounterParty() {
    return CounterParty.builder()
      .id(1L)
      .programmePk(2L)
      .mnemonic("SampleMnemonic")
      .role('A')
      .customerSourceBankingBusinessCode("12345678")
      .customerMnemonic("CustomerMnemonic")
      .name("Sample Name")
      .salutation("Mr.")
      .nameAndAddressLine1("Address Line 1")
      .nameAndAddressLine2("Address Line 2")
      .nameAndAddressLine3("Address Line 3")
      .nameAndAddressLine4("Address Line 4")
      .nameAndAddressLine5("Address Line 5")
      .nameAndAddressFreeFormat("Free Format")
      .zip("12345")
      .phone("123-456-7890")
      .fax("123-456-7890")
      .telex("Telex")
      .telexAnswerBack("AnswerBack")
      .email("sample@example.com")
      .branchCode("BRANCH")
      .swiftAddressSWBank("SWBANK")
      .cpartycnas("CNAS")
      .cpartyswl("SWL")
      .cpartyswbr("SWBR")
      .cpartysna1("SNA1")
      .cpartysna2("SNA2")
      .cpartysna3("SNA3")
      .cpartysna4("SNA4")
      .cpartysna5("SNA5")
      .cpartysnaf("Free Format 2")
      .country("US")
      .cpartyxm("XM")
      .language("EN")
      .status('A')
      .limitAmt(new BigDecimal("100000.00"))
      .limitCcy("USD")
      .translit('Y')
      .lastMaint(LocalDate.of(2023, 1, 15))
      .obsolete('N')
      .autokey("AutoKey")
      .mntInBo('Y')
      .typeflag(1)
      .tstamp(12345)
      .build();
  }
}
