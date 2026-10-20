package com.vsbp.domain;

public enum ServiceType {
    GENERAL_SERVICE("General service"),
    OIL_CHANGE("Oil change"),
    BRAKE_INSPECTION("Brake inspection"),
    TYRE_ROTATION("Tyre rotation"),
    AC_SERVICE("AC service");

    private final String label;

    ServiceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
