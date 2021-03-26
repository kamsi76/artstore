package com.uni4989.artstore;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public abstract class AbstractPopWebActive extends CommonActivity {

    private int layoutId;

    protected String mainUrl;

    protected final static int POPUP_REQUEST_CODE = 100;
    protected final static int POPUP_RESULT_CODE = 1;

    protected final static int VIEWIMG_REQUEST_CODE = 200;
    protected final static int SETTING_REQUEST_CODE = 900;


    public AbstractPopWebActive(int layoutId) {
        this.layoutId = layoutId;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId);

        getToken();

        Bundle extras = getIntent().getExtras();
        String url =  getString(R.string.default_url) + extras.getString("wepUrl");
        mainUrl = url;

        /*
         * WEB Active 생성
         */
        createWeb();

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setTextZoom(100);

        mWebView.addJavascriptInterface(new AndroidBridge(), "BRIDGE");

        mWebView.setWebViewClient(new WebViewClientClass());

        mWebView.setWebChromeClient(new WebChromeClientClass());

        //메인 주소는 이쪽
        mWebView.loadUrl(url);

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
                mWebView.loadUrl("javascript:callParntByChild('" + getString + "')");

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

            if (mWebView.getUrl().indexOf("t=login") > 0) {
                finish();
            } else if (mWebView.getUrl().indexOf("t=member&s=oauthForm") > 0) {
                mWebView.loadUrl(getString(R.string.default_url) + "/content.php?t=login");
            } else if(!mWebView.getUrl().equals(mainUrl)) {
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                } else {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("string", "");
                    setResult(POPUP_RESULT_CODE, resultIntent);
                    finish();
                }
            } else {
                Intent resultIntent = new Intent();

                resultIntent.putExtra("string", "");

                setResult(POPUP_RESULT_CODE, resultIntent);

                finish();
            }
            return false;
        }

        return super.onKeyDown(keyCode, event);
    }

    public class AndroidBridge {

        @JavascriptInterface //이게 있어야 웹에서 실행이 가능합니다.
        public void close() {
            Intent resultIntent = new Intent();

            resultIntent.putExtra("string", "");
            setResult(POPUP_RESULT_CODE, resultIntent);

            finish();
        }

        @JavascriptInterface //이게 있어야 웹에서 실행이 가능합니다.
        public void open(final String wepUrl) {
            openWeb(ChildPopupWebActivity.class, wepUrl);
        }

        @JavascriptInterface //이게 있어야 웹에서 실행이 가능합니다.
        public void openLink(final String subject, String linkUrl, String imageUrl) {
            createDynamicLink(subject, linkUrl, imageUrl);
        }

        @JavascriptInterface //이게 있어야 웹에서 실행이 가능합니다.
        public void viewImage(final String prductIndx) {
            openViewImage(prductIndx);
        }

        @JavascriptInterface //이게 있어야 웹에서 실행이 가능합니다.
        public void callParntByChild(final String jsonData) {
            try {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("string", jsonData);
                setResult(POPUP_REQUEST_CODE, resultIntent);
            }
            catch (Exception ex){
                Log.i("TAG","error : " + ex);
            }
        }

        @JavascriptInterface //이게 있어야 웹에서 실행이 가능합니다.
        public void callParntByChildClose(final String jsonData) {
            try {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("string", jsonData);
                setResult(POPUP_RESULT_CODE, resultIntent);

                finish();
            } catch (Exception ex) {
                Log.i("TAG", "error : " + ex);
            }
        }

        @JavascriptInterface //이게 있어야 웹에서 실행이 가능합니다.
        public void requestSecession() {
            try {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("actionType", "secession");
                setResult(SETTING_REQUEST_CODE, resultIntent);

                finish();
            } catch (Exception ex) {
                Log.i("TAG", "error : " + ex);
            }
        }
    }
}
