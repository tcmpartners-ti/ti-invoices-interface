package com.tcmp.tiapi.messaging.model.response;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.util.List;


@XmlType
@XmlRootElement(name = "Details")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Details {
  @XmlElement(name = "Error")
  private List<Error> error;
}
