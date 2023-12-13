package com.tcmp.tiapi.titoapigee.businessbanking;


import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessBankingServiceTest {
  @Mock private HeaderSigner businessBankingHeaderSigner;
  @Mock private BusinessBankingClient businessBankingClient;

  private BusinessBankingService testedService;

  @BeforeEach
  void setUp() {
    testedService = new BusinessBankingService(businessBankingHeaderSigner, businessBankingClient);
  }
}
