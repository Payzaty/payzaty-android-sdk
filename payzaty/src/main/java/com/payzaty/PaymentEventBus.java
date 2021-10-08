package com.payzaty;

import java.util.ArrayList;
import java.util.List;

class PaymentEventBus {

    private final static List<PaymentEvent> listeners = new ArrayList<>();

    private PaymentEventBus() {

    }

    public static void publishEvent(String event) {
        for (PaymentEvent listener : listeners) {
            ((PaymentEvent) listener).onWebViewPaymentFinish(event);
        }
    }

    public static void register(PaymentEvent event) {
        if (!listeners.contains(event)) {
            listeners.add(event);
        }
    }

    public static void remove(PaymentEvent event) {
        if (listeners.contains(event)) {
            listeners.remove(event);
        }
    }
}
