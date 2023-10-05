package com.tcmp.tiapi.titoapigee.paymentexecution;

import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.request.*;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.BusinessAccountTransfersResponse;
import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentExecutionService {
  private final HeaderSigner paymentExecutionHeaderSigner;
  private final PaymentExecutionClient paymentExecutionClient;

  public BusinessAccountTransfersResponse executeClientToBglTransaction(
    String clientAccount,
    String invoiceReference,
    BigDecimal amount
  ) {
    String transitoryAccountId = "259090337001";

    TransactionRequest requestData = TransactionRequest.builder()
      .transactionType(TransactionType.BGL_TO_CLIENT.getValue())
      .debtor(new BancsCustomer(new BancsAccount(transitoryAccountId)))
      .creditor(new BancsCustomer(new BancsAccount(clientAccount)))
      .transaction(BancsTransaction.builder()
        .concept(String.format("Invoice %s financing", invoiceReference))
        .amount(amount.toString())
        .currency(new TransactionCurrency("usd"))
        .build())
      .build();
    ApiGeeBaseRequest<TransactionRequest> request = new ApiGeeBaseRequest<>(requestData);

    Map<String, String> headers = paymentExecutionHeaderSigner.buildRequestHeaders(request);

    return paymentExecutionClient.postPayment(
      headers,
      request
    );
  }
}
