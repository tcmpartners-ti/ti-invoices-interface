package com.tcmp.tiapi.titoapigee.operationalgateway.dto.request;

import lombok.Builder;

import java.util.List;

@Builder
public record Template(
  String templateId,
  String sequentialId,
  List<TemplateField> fields
) {
}
