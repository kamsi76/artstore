package com.uni4989.artstore;

import android.content.Intent;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class PopupWebActivity extends AbstractPopWebActive {

    public PopupWebActivity() {
        super(R.layout.activity_popup_web);
    }

    @Override
    public void openWeb(String url) {

        Intent intent = new Intent(PopupWebActivity.this, ChildPopupWebActivity.class);

        intent.putExtra("wepUrl", url);
        //startActivity(intent);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivityForResult(intent, POPUP_REQUEST_CODE);

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    @Override
    public void openViewImage(String images) {
        Intent intent = new Intent(PopupWebActivity.this, ImageViewActivity.class);
        intent.putExtra("images", images);
        startActivityForResult(intent, VIEWIMG_REQUEST_CODE);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    @Override
    public void createWeb() {

        mPopWebView = (WebView) findViewById(R.id.activity_popup_webview);

        WebSettings webSettings = mPopWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);

        mPopWebView.addJavascriptInterface(new AndroidBridge(), "BRIDGE");
    }

}