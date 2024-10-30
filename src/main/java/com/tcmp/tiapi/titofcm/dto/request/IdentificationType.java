package com.tcmp.tiapi.titofcm.dto.request;

public enum IdentificationType {
    CEDULA("C"),
    RUC("R"),
    PASAPORTE("P");

    private final String value;

    // Constructor
    IdentificationType(String value) {
        this.value = value;
    }

    // Getter
    public String getValue() {
        return value;
    }
}
