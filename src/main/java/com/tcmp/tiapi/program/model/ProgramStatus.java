package com.tcmp.tiapi.program.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProgramStatus {
    BLOCKED("B", "BLOCKED"),
    REFERRED("R", "REFERRED"),
    INACTIVE("I","INACTIVE" ),
    ACTIVE("A", "ACTIVE");

    @Getter
    private final String value;

    @Getter private final String display;

    public static ProgramStatus from(String value) {
        for (var s : ProgramStatus.values()) {
            if (s.value.equals(value)) {
                return s;
            }
        }

        throw new IllegalArgumentException("Invalid value for Program Status enum");
    }
}
