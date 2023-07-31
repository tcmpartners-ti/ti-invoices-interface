package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.dto.InvoiceCreationRowCSV;
import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.messaging.CreateInvoiceEventMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface InvoiceMapper {
    @Mapping(source = "context.customer", target = "context.customer")
    @Mapping(source = "context.theirReference", target = "context.theirReference")
    @Mapping(source = "anchorParty", target = "anchorParty")
    @Mapping(source = "programme", target = "programme")
    @Mapping(source = "seller", target = "seller")
    @Mapping(source = "buyer", target = "buyer")
    @Mapping(source = "receivedOn", target = "receivedOn")
    @Mapping(source = "invoiceNumber", target = "invoiceNumber")
    @Mapping(source = "issueDate", target = "issueDate")
    @Mapping(source = "faceValue.amount", target = "faceValue.amount")
    @Mapping(source = "faceValue.currency", target = "faceValue.currency")
    @Mapping(source = "outstandingAmount.amount", target = "outstandingAmount.amount")
    @Mapping(source = "outstandingAmount", target = "outstandingAmount")
    @Mapping(source = "settlementDate", target = "settlementDate")
    CreateInvoiceEventMessage mapDTOToFTIMessage(InvoiceCreationDTO invoiceCreationDTO);

    @Mapping(source = "customerMnemonic", target = "context.customer")
    @Mapping(source = "theirReference", target = "context.theirReference")
    @Mapping(source = "anchorPartyMnemonic", target = "anchorParty")
    @Mapping(source = "programmeId", target = "programme")
    @Mapping(source = "sellerId", target = "seller")
    @Mapping(source = "buyerId", target = "buyer")
    @Mapping(source = "receivedOn", target = "receivedOn")
    @Mapping(source = "invoiceNumber", target = "invoiceNumber")
    @Mapping(source = "issueDate", target = "issueDate")
    @Mapping(source = "faceValueAmount", target = "faceValue.amount")
    @Mapping(source = "faceValueCurrency", target = "faceValue.currency")
    @Mapping(source = "outstandingAmount", target = "outstandingAmount.amount")
    @Mapping(source = "outstandingCurrency", target = "outstandingAmount.currency")
    @Mapping(source = "settlementDate", target = "settlementDate")
    CreateInvoiceEventMessage mapCSVRowToFTIMessage(InvoiceCreationRowCSV invoiceCreationRowCSV);
}
