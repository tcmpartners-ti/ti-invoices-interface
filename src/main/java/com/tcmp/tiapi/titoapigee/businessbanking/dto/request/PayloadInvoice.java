package com.tcmp.tiapi.titoapigee.businessbanking.dto.request;

import lombok.Builder;

/**
 * @param batchId The batch id.
 * @param reference The invoice number.
 * @param sellerMnemonic The seller's RUC.
 * @param operationId The credit creation operation id (generated in GAF).
 */
@Builder
public record PayloadInvoice(
    String batchId, String reference, String sellerMnemonic, String operationId) {}
