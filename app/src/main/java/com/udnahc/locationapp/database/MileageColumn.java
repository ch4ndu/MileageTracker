package com.udnahc.locationapp.database;


public enum MileageColumn {
    startTime("TEXT"),
    miles("TEXT"),
    endTime("TEXT"),
    expenseName("TEXT"),
    startPoint("TEXT"),
    endPoint("TEXT"),
    path("TEXT");
    private final String value;

    MileageColumn(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
