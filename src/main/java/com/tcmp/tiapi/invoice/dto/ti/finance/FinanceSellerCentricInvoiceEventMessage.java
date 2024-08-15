package com.tcmp.tiapi.invoice.dto.ti.finance;


import com.tcmp.tiapi.ti.dto.TINamespace;
import jakarta.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "TFSELFIN", namespace = TINamespace.MESSAGES)
public class FinanceSellerCentricInvoiceEventMessage extends FinanceInvoiceEventMessage {

}

