package com.udnahc.locationapp.database;


public enum UserColumn {
    email("TEXT"),
    password("TEXT"),
    address("TEXT"),
    secondaryAddress("TEXT"),
    city("TEXT"),
    state("TEXT"),
    zipcode("INTEGER");
    private final String value;

    UserColumn(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
