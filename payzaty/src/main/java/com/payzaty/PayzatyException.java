package com.payzaty;

public class PayzatyException extends Exception {

    public PayzatyException(String msg, Exception exception) {
        super(msg, exception);
    }

    public PayzatyException(String msg, Throwable throwable) {
        super(msg, throwable);
    }


    public PayzatyException(String msg) {
        super(msg);
    }


}
