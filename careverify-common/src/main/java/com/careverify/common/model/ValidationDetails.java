package com.careverify.common.model;

public class ValidationDetails {
    private final boolean npiValid;
    private final boolean stateMatch;
    private final boolean specialtyMatch;

    public ValidationDetails(boolean npiValid, boolean stateMatch, boolean specialtyMatch) {
        this.npiValid = npiValid;
        this.stateMatch = stateMatch;
        this.specialtyMatch = specialtyMatch;
    }

    public boolean isNpiValid() { return npiValid; }
    public boolean isStateMatch() { return stateMatch; }
    public boolean isSpecialtyMatch() { return specialtyMatch; }
}

