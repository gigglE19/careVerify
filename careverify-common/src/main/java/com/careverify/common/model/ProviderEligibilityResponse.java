package com.careverify.common.model;

public class ProviderEligibilityResponse {
    private final boolean verified;
    private final ProviderInfo provider;
    private final ValidationDetails validationDetails;
    private final String source;
    private final String reason;

    private ProviderEligibilityResponse(boolean verified, ProviderInfo provider, ValidationDetails validationDetails, String source, String reason) {
        this.verified = verified;
        this.provider = provider;
        this.validationDetails = validationDetails;
        this.source = source;
        this.reason = reason;
    }

    public static ProviderEligibilityResponse success(ProviderInfo provider, ValidationDetails details) {
        return new ProviderEligibilityResponse(true, provider, details, "NPI Registry", null);
    }

    public static ProviderEligibilityResponse failed(String reason) {
        return new ProviderEligibilityResponse(false, null, null, null, reason);
    }

    public static ProviderEligibilityResponse failed(String reason, ProviderInfo provider, ValidationDetails details) {
        return new ProviderEligibilityResponse(false, provider, details, null, reason);
    }

    public boolean isVerified() { return verified; }
    public ProviderInfo getProvider() { return provider; }
    public ValidationDetails getValidationDetails() { return validationDetails; }
    public String getSource() { return source; }
    public String getReason() { return reason; }
}

