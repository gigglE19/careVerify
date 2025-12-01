package com.careverify.adapter.npi.model;

import java.util.List;

public record NpiNormalized(String npi, String name, String state, List<String> specialties, String status) {}

