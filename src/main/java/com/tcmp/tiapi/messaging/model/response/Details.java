package com.tcmp.tiapi.messaging.model.response;

import com.tcmp.tiapi.messaging.model.TINamespace;
import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@XmlType
@XmlRootElement(name = "Details")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Details {
  @XmlElement(name = "Error", namespace = TINamespace.CONTROL)
  private List<String> errors;

  @XmlElement(name = "Warning", namespace = TINamespace.CONTROL)
  private List<String> warnings;

  @XmlElement(name = "Info", namespace = TINamespace.CONTROL)
  private List<String> infos;
}
