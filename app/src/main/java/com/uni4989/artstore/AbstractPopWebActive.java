package com.uni4989.artstore;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractPopWebActive extends CommonActivity {

    private int layoutId;

    protected WebView mPopWebView;
    private ProgressBar progressBar;

    protected String mainUrl;

    protected final static int POPUP_REQUEST_CODE = 100;
    protected final static int POPUP_RESULT_CODE = 1;

    protected final static int VIEWIMG_REQUEST_CODE = 200;

    public AbstractPopWebActive(int layoutId) {
        this.layoutId = layoutId;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId);

        Bundle extras = getIntent().getExtras();
        String url =  getString(R.string.default_url) + extras.getString("wepUrl");
        mainUrl = url;

        /*
         * WEB Active 생성
         */
        createWeb();

        WebSettings webSettings = mPopWebView.getSettings();
        webSettings.setTextZoom(100);

        mPopWebView.addJavascriptInterface(new AndroidBridge(), "BRIDGE");

        mPopWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.startsWith("tel:"))
                {
                    view.stopLoading();
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
                    return false;
                }

                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);

                Log.d("", "케스트url" + mPopWebView.getUrl());

                if (mPopWebView.getUrl().contains("jscall://")) {
                    mPopWebView.stopLoading();
                    progressBar.setVisibility(View.GONE);

                    String tempUrl = mPopWebView.getUrl().substring(mPopWebView.getUrl().lastIndexOf("jscall://")+1);

                    Log.d("", "케스트 jscall 호출" + tempUrl);
                }
            }

            //웹페이지 로딩 종료시 호출
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }
        });

        mPopWebView.setWebChromeClient(new WebChromeClientClass());

        //메인 주소는 이쪽
        mPopWebView.loadUrl(url);

        //progressbar
        progressBar = (ProgressBar) findViewById(R.id.web_progress);
        progressBar.setVisibility(View.GONE);
    }

    public abstract void createWeb();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d("onActivityResult() ", "resultCode = " + requestCode);

        switch (requestCode) {
            case POPUP_REQUEST_CODE:
                String getString = data.getStringExtra("string");
                mPopWebView.loadUrl("javascript:callParntByChild('" + getString + "')");

                break;
            case FILECHOOSER_NORMAL_REQ_CODE:
                if (resultCode == RESULT_OK) {
                    if (filePathCallbackNormal == null) return;
                    Uri result = (data == null || resultCode != RESULT_OK) ? null : data.getData();
                    //  onReceiveValue 로 파일을 전송한다.
                    filePathCallbackNormal.onReceiveValue(result);
                    filePathCallbackNormal = null;
                }
                break;
            case FILECHOOSER_LOLLIPOP_REQ_CODE:
                Log.d("onActivityResult() ", "FILECHOOSER_LOLLIPOP_REQ_CODE = " + FILECHOOSER_LOLLIPOP_REQ_CODE);

                if (resultCode == RESULT_OK) {
                    Log.d("onActivityResult() ", "FILECHOOSER_LOLLIPOP_REQ_CODE 의 if문  RESULT_OK 안에 들어옴");

                    if (filePathCallbackLollipop == null) return;
                    if (data == null)
                        data = new Intent();
                    if (data.getData() == null)
                        data.setData(cameraImageUri);

                    filePathCallbackLollipop.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                    filePathCallbackLollipop = null;
                } else {
                    Log.d("onActivityResult() ", "FILECHOOSER_LOLLIPOP_REQ_CODE 의 if문의 else문 안으로~");
                    if (filePathCallbackLollipop != null) {   //  resultCode에 RESULT_OK가 들어오지 않으면 null 처리하지 한다.(이렇게 하지 않으면 다음부터 input 태그를 클릭해도 반응하지 않음)

                        Log.d("onActivityResult() ", "FILECHOOSER_LOLLIPOP_REQ_CODE 의 if문의 filePathCallbackLollipop이 null이 아니면");
                        filePathCallbackLollipop.onReceiveValue(null);
                        filePathCallbackLollipop = null;
                    }

                    if (filePathCallbackNormal != null) {
                        filePathCallbackNormal.onReceiveValue(null);
                        filePathCallbackNormal = null;
                    }
                }
                break;
            default:
                break;

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(!mPopWebView.getUrl().equals(mainUrl)) {
                if (mPopWebView.canGoBack()) {
                    mPopWebView.goBack();
                    return false;
                } else {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("string", "");
                    setResult(POPUP_RESULT_CODE, resultIntent);
                    finish();
                    return false;
                }
            } else {
                Intent resultIntent = new Intent();

                resultIntent.putExtra("string", "");

                setResult(POPUP_RESULT_CODE, resultIntent);

                finish();
                return false;
            }
        }

        return super.onKeyDown(keyCode, event);
    }
}
