package com.payzaty;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.payzaty.payment.R;

public class PaymentActivity extends AppCompatActivity {

    private WebView webView;

    public static String CHECKOUT_URL_KEY = "checkout_url";
    public static String CALLBACK_URL_KEY = "callback_url";

    public static Intent newIntent(Context from, final String checkoutUrl, final String callbackUrl) {
        Intent intent = new Intent(from, PaymentActivity.class);
        intent.putExtra(CHECKOUT_URL_KEY, checkoutUrl);
        intent.putExtra(CALLBACK_URL_KEY, callbackUrl);
        return intent;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        webView = findViewById(R.id.webView);

        String checkoutUrl = getIntent().getStringExtra(CHECKOUT_URL_KEY);
        String callbackUrl = getIntent().getStringExtra(CALLBACK_URL_KEY);
        if (checkoutUrl == null) {
            finish();
        }


        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);


        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    String currentUrl = request.getUrl().toString();

                    if (currentUrl.startsWith(callbackUrl)) {

                        String checkoutId = request.getUrl().getQueryParameter("checkoutId");
                        PaymentEventBus.publishEvent(checkoutId);
                        finish();
                        return true;
                    }
                }
                return false;
            }
        });

        webView.loadUrl(checkoutUrl);
    }
}