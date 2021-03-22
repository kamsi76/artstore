package com.uni4989.artstore;

import android.webkit.WebSettings;
import android.webkit.WebView;

public class ChildPopupWebActivity extends AbstractPopWebActive {

    public ChildPopupWebActivity() {
        super(R.layout.activity_child_popup_web);
    }

    @Override
    public void createWeb() {

        mWebView = (WebView) findViewById(R.id.activity_child_popup_webview);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);

        mWebView.addJavascriptInterface(new AbstractPopWebActive.AndroidBridge(), "BRIDGE");
    }
}