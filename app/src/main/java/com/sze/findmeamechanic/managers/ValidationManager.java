package com.sze.findmeamechanic.managers;

public interface ValidationManager {
    boolean isInputValidated();
    boolean isValidPassword(String password);
    boolean isValidFullName(String fullName);
    boolean isValidTaxNumber(String taxNumber);
}