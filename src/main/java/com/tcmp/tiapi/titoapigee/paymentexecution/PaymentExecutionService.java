package com.tcmp.tiapi.titoapigee.paymentexecution;

import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.request.TransactionRequest;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.request.TransactionType;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.BusinessAccountTransfersResponse;
import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentExecutionService {
  private final HeaderSigner plainBodyRequestHeaderSigner;
  private final PaymentExecutionClient paymentExecutionClient;

  public void processInvoiceWithPrePayment(
    String bglAccount,
    String anchorAccount,
    String sellerAccount,
    String invoiceReference,
    BigDecimal amount
  ) {
    BusinessAccountTransfersResponse anchorToBgl = transferInvoiceTotalFromAnchorToBgl(anchorAccount, bglAccount, invoiceReference, amount);
    BusinessAccountTransfersResponse bglToSeller = transferInvoiceTotalFromBglToSeller(bglAccount, sellerAccount, invoiceReference, amount);
    var invoiceInterest = amount.multiply(BigDecimal.valueOf(0.1)); // Temporal
    BusinessAccountTransfersResponse sellerToBgl = transferInvoiceInterestFromSellerToBgl(sellerAccount, bglAccount, invoiceReference, invoiceInterest);

    log.info("Transferred $ {} (Interest $ {}) from {} (anchor) to {} (seller).", amount, invoiceInterest, anchorAccount, sellerAccount);
    log.info("[Anchor - BGL] {}", anchorToBgl.data().creationDateTime());
    log.info("[BGL - Seller] {}", bglToSeller.data().creationDateTime());
    log.info("[Seller - BGL] {}", sellerToBgl.data().creationDateTime());
  }

  public void processInvoiceWithoutPrePayment(
    String bglAccount,
    String anchorAccount,
    String sellerAccount,
    String invoiceReference,
    BigDecimal amount
  ) {
    BusinessAccountTransfersResponse anchorToBgl = transferInvoiceTotalFromAnchorToBgl(anchorAccount, bglAccount, invoiceReference, amount);
    BusinessAccountTransfersResponse bglToSeller = transferInvoiceTotalFromBglToSeller(bglAccount, sellerAccount, invoiceReference, amount);

    log.info("Transferred $ {} from {} (anchor) to {} (seller).", amount, anchorAccount, sellerAccount);
    log.info("[Anchor - BGL] {}", anchorToBgl.data().creationDateTime());
    log.info("[BGL - Seller] {}", bglToSeller.data().creationDateTime());
  }

  private BusinessAccountTransfersResponse transferInvoiceTotalFromAnchorToBgl(
    String anchorAccount,
    String bglAccount,
    String invoiceReference,
    BigDecimal amount
  ) {
    TransactionRequest requestData = TransactionRequest.from(
      TransactionType.CLIENT_TO_BGL,
      anchorAccount,
      bglAccount,
      "Descuento de factura %s".formatted(invoiceReference),
      amount.toString()
    );

    ApiGeeBaseRequest<TransactionRequest> request = new ApiGeeBaseRequest<>(requestData);
    Map<String, String> headers = plainBodyRequestHeaderSigner.buildRequestHeaders(request);

    return paymentExecutionClient.postPayment(headers, request);
  }

  private BusinessAccountTransfersResponse transferInvoiceTotalFromBglToSeller(
    String bglAccount,
    String sellerAccount,
    String invoiceReference,
    BigDecimal amount
  ) {
    TransactionRequest requestData = TransactionRequest.from(
      TransactionType.BGL_TO_CLIENT,
      bglAccount,
      sellerAccount,
      "Descuento de factura %s".formatted(invoiceReference),
      amount.toString()
    );

    ApiGeeBaseRequest<TransactionRequest> request = new ApiGeeBaseRequest<>(requestData);
    Map<String, String> headers = plainBodyRequestHeaderSigner.buildRequestHeaders(request);

    return paymentExecutionClient.postPayment(headers, request);
  }

  private BusinessAccountTransfersResponse transferInvoiceInterestFromSellerToBgl(
    String sellerAccount,
    String bglAccount,
    String invoiceReference,
    BigDecimal amount
  ) {
    TransactionRequest requestData = TransactionRequest.from(
      TransactionType.BGL_TO_CLIENT,
      bglAccount,
      sellerAccount,
      "Tasa descuento factura %s".formatted(invoiceReference),
      amount.toString()
    );

    ApiGeeBaseRequest<TransactionRequest> request = new ApiGeeBaseRequest<>(requestData);
    Map<String, String> headers = plainBodyRequestHeaderSigner.buildRequestHeaders(request);

    return paymentExecutionClient.postPayment(headers, request);
  }
}
