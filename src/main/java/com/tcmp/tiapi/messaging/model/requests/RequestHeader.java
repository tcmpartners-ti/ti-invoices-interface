package com.tcmp.tiapi.messaging.model.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "RequestHeader")
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestHeader {

    @XmlElement(name = "Service")
    private String service;

    @XmlElement(name = "Operation")
    private String operation;

    @XmlElement(name = "Credentials")
    private Credentials credentials;

    @XmlElement(name = "ReplyFormat")
    private String replyFormat;

    @XmlElement(name = "ReplyTarget")
    private String replyTarget;

    @XmlElement(name = "TargetSystem")
    private String targetSystem;

    @XmlElement(name = "SourceSystem")
    private String sourceSystem;

    @XmlElement(name = "NoRepair")
    private String noRepair;

    @XmlElement(name = "NoOverride")
    private String noOverride;

    @XmlElement(name = "CorrelationId")
    private String correlationId;

    @XmlElement(name = "TransactionControl")
    private String transactionControl;

    @XmlElement(name = "CreationDate")
    private String creationDate;

    @XmlElement(name = "GroupingId")
    private String groupingId;
}
