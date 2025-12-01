package com.careverify.common.model;

import java.util.List;

public class ProviderInfo {
    private final String npi;
    private final String name;
    private final String state;
    private final List<String> specialties;
    private final String status;

    public ProviderInfo(String npi, String name, String state, List<String> specialties, String status) {
        this.npi = npi;
        this.name = name;
        this.state = state;
        this.specialties = specialties;
        this.status = status;
    }

    public String getNpi() { return npi; }
    public String getName() { return name; }
    public String getState() { return state; }
    public List<String> getSpecialties() { return specialties; }
    public String getStatus() { return status; }
}

