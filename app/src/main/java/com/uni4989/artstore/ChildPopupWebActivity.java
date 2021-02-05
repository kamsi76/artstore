package com.uni4989.artstore;

import android.webkit.WebSettings;
import android.webkit.WebView;

public class ChildPopupWebActivity extends AbstractPopWebActive {

    public ChildPopupWebActivity() {
        super(R.layout.activity_child_popup_web);
    }

    @Override
    public void openWeb(String url) { }

    @Override
    public void createWeb() {

        mPopWebView = (WebView) findViewById(R.id.activity_child_popup_webview);

        WebSettings webSettings = mPopWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);

        mPopWebView.addJavascriptInterface(new AbstractPopWebActive.AndroidBridge(), "BRIDGE");
    }
}