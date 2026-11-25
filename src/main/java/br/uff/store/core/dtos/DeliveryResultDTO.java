package br.uff.store.core.dtos;

/** Vendor-neutral outcome of arranging a delivery. Every adapter fills the same fields. */
public class DeliveryResultDTO extends BaseDTO {
    public static final String NOT_ARRANGED = "NOT_ARRANGED";
    public static final String PENDING = "PENDING";

    private final String provider;
    private final String method;
    private final String trackingCode;
    private final String status;
    private final String rawResponseRef;

    public DeliveryResultDTO(String provider, String method, String trackingCode, String status,
            String rawResponseRef) {
        this.provider = provider;
        this.method = method;
        this.trackingCode = trackingCode;
        this.status = status;
        this.rawResponseRef = rawResponseRef;
    }

    public String getProvider() {
        return provider;
    }

    public String getMethod() {
        return method;
    }

    public String getTrackingCode() {
        return trackingCode;
    }

    public String getStatus() {
        return status;
    }

    public String getRawResponseRef() {
        return rawResponseRef;
    }
}
