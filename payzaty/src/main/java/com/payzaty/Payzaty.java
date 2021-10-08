package com.payzaty;

import android.content.Context;
import android.os.Build;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Payzaty implements PaymentEvent {

    private String merchantNo;
    private String secretKey;
    private String language;
    private boolean sandbox;

    private final static Object LOCK = new Object();
    private static Payzaty instance;

    private CheckoutHandler handler;

    public static Payzaty init(final String merchantNo, final String secretKey, final String language, final boolean sandbox) {
        if (merchantNo == null) {
            throw new NullPointerException("MerchantNo can't be null");
        }
        if (secretKey == null) {
            throw new NullPointerException("SecretKey can't be null");
        }

        synchronized (LOCK) {
            if (instance == null) {
                instance = new Payzaty(merchantNo, secretKey, language, sandbox);
                PaymentEventBus.register(instance);
            } else {
                instance.merchantNo = merchantNo;
                instance.secretKey = secretKey;
                instance.language = language;
                instance.sandbox = sandbox;
            }
            return instance;
        }
    }


    private static Payzaty getInstance() {
        synchronized (LOCK) {
            if (instance == null) {
                throw new IllegalStateException("Call Payzaty.init(merchantNo, secretKey) before on Main Application");
            }

            return instance;
        }
    }

    private static void destory() {
        synchronized (LOCK) {
            if (instance != null) {
                PaymentEventBus.remove(instance);
                instance = null;
            }
        }
    }

    private Payzaty(final String merchantNo, final String secretKey, final String language, final boolean sandbox) {
        this.merchantNo = merchantNo;
        this.secretKey = secretKey;
        this.language = language;
        this.sandbox = sandbox;
    }


    public void checkout(Context context, CheckoutRequest request, CheckoutHandler handler) {

        this.handler = handler;
        SdkRetrofit.getInstance(sandbox)
                .getSdkService()
                .checkout(
                        merchantNo,
                        secretKey,
                        language,
                        request.getName(),
                        request.getEmail(),
                        request.getPhoneCode(),
                        request.getPhoneNumber(),
                        request.getAmount() + "",
                        request.getCurrencyID(),
                        request.getResponseUrl(),
                        request.getUdf1(),
                        request.getUdf2(),
                        request.getUdf3()

                )
                .enqueue(new Callback<CheckoutLocalResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<CheckoutLocalResponse> call, @NotNull Response<CheckoutLocalResponse> response) {

                        if (response.isSuccessful()) {

                            if (response.body() != null) {
                                if (response.body().success) {
                                    context.startActivity(PaymentActivity.newIntent(context, response.body().checkoutUrl, request.getResponseUrl()));
                                } else {
                                    CheckoutResponse res = new CheckoutResponse();
                                    res.success = response.body().success;
                                    res.error = response.body().error;
                                    res.errorText = response.body().errorText;
                                    handler.onSuccess(res);
                                }
                            } else {
                                try {
                                    handler.onFailure(new PayzatyException("CheckoutUrl == null, Please Call Support " + response.errorBody().string()));
                                } catch (Exception e) {
                                    handler.onFailure(new PayzatyException("CheckoutUrl == null, Please Call Support " + e.getLocalizedMessage(), e.getCause()));

                                }
                            }
                        } else {

                            String errorBody = "Some Error";
                            try {
                                errorBody = response.errorBody() == null ? " Some Error  " : response.errorBody().string();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            handler.onFailure(new PayzatyException(errorBody));
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<CheckoutLocalResponse> call, @NotNull Throwable t) {
                        handler.onFailure(new PayzatyException(t.getLocalizedMessage()));
                    }
                });
    }

    @Override
    public void onWebViewPaymentFinish(String checkoutId) {
        // TODO Parse Response Here
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (checkoutId != null) {
                getStatus(checkoutId, handler);
            } else {
                handler.onFailure(new PayzatyException("Payment Failed "));
            }
        } else {
            handler.onFailure(new PayzatyException("your android sdk version  less than 21 :)"));
        }
    }

    public void getStatus(String checkoutId, CheckoutHandler handler) {
        if (handler == null) {
            return;
        }

        if (checkoutId == null || checkoutId.isEmpty()) {
            handler.onFailure(new PayzatyException("CheckoutId can't be null"));
            return;
        }

        SdkRetrofit.getInstance(sandbox)
                .getSdkService()
                .status(
                        merchantNo,
                        secretKey,
                        language,
                        checkoutId
                )
                .enqueue(new Callback<CheckoutResponse>() {
                    @Override
                    public void onResponse(Call<CheckoutResponse> call, Response<CheckoutResponse> response) {

                        if (response.isSuccessful()) {
                            handler.onSuccess(response.body());
                        } else {
                            try {
                                handler.onFailure(new PayzatyException(response.errorBody().string()));
                            } catch (IOException e) {
                                handler.onFailure(new PayzatyException(e.getLocalizedMessage(), e.getCause()));
                            }

                        }
                    }

                    @Override
                    public void onFailure(Call<CheckoutResponse> call, Throwable t) {
                        handler.onFailure(new PayzatyException(t.getLocalizedMessage(), t.getCause()));

                    }
                });

    }
}
