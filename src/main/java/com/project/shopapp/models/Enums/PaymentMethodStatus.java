package com.project.shopapp.models.Enums;

public enum PaymentMethodStatus {
    COD("cod", "Thanh toán khi nhận hàng"),
    VNPAY("vnpay", "VNPay");

    private final String code;
    private final String description;

    PaymentMethodStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PaymentMethodStatus fromCode(String code) {
        for (PaymentMethodStatus method : PaymentMethodStatus.values()) {
            if (method.code.equalsIgnoreCase(code)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown payment method: " + code);
    }
}
