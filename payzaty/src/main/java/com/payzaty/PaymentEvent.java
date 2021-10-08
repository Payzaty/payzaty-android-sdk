package com.payzaty;

interface PaymentEvent {

    void onWebViewPaymentFinish(String checkoutId);
}
