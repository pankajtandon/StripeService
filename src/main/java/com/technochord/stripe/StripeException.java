package com.technochord.stripe;

public class StripeException extends RuntimeException {
    public StripeException(String message) {
        super(message);
    }

    public StripeException(String message, Throwable t) {
        super(message, t);
    }
}
