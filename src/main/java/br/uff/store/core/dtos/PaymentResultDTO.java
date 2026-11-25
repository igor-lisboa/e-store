package br.uff.store.core.dtos;

/** Vendor-neutral outcome of a payment attempt. Every adapter fills the same fields. */
public class PaymentResultDTO extends BaseDTO {
    public static final String NOT_CHARGED = "NOT_CHARGED";
    public static final String AUTHORIZED = "AUTHORIZED";
    public static final String CAPTURED = "CAPTURED";

    private final String provider;
    private final String externalReference;
    private final String status;
    private final String rawResponseRef;

    public PaymentResultDTO(String provider, String externalReference, String status, String rawResponseRef) {
        this.provider = provider;
        this.externalReference = externalReference;
        this.status = status;
        this.rawResponseRef = rawResponseRef;
    }

    public String getProvider() {
        return provider;
    }

    /** The vendor identifier for the payment; null when no vendor is involved. */
    public String getExternalReference() {
        return externalReference;
    }

    public String getStatus() {
        return status;
    }

    /** Where the untouched vendor response is stored; null when there is none. */
    public String getRawResponseRef() {
        return rawResponseRef;
    }
}
