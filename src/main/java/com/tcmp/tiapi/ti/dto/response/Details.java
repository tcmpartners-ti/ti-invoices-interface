package com.tcmp.tiapi.ti.dto.response;

import com.tcmp.tiapi.ti.dto.TINamespace;
import jakarta.xml.bind.annotation.*;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
