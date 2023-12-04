package com.tcmp.tiapi.titoapigee.operationalgateway.dto.request;

import java.util.List;
import lombok.Builder;

@Builder
public record Template(
  String templateId,
  String sequentialId,
  List<TemplateField> fields
) {
}
