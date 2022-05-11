package com.udnahc.locationapp.database;


public enum ExpenseColumn {
    id("TEXT"),
    timeStamp("TEXT"),
    reason("TEXT"),
    extra1("TEXT"),
    extra2("TEXT"),
    extra3("TEXT"),
    endTime("TEXT");
    private final String value;

    ExpenseColumn(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name();
    }
}

