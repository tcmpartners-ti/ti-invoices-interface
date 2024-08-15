package com.tcmp.tiapi.invoice.dto.ti.finance;

import com.tcmp.tiapi.ti.dto.TINamespace;
import jakarta.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "TFBUYFIN", namespace = TINamespace.MESSAGES)
public class FinanceBuyerCentricInvoiceEventMessage extends FinanceInvoiceEventMessage {}
