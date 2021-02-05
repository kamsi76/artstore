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

public abstract class AbstractPopWebActive extends AppCompatActivity {

    private int layoutId;

    protected WebView mPopWebView;
    private ProgressBar progressBar;

    protected ValueCallback<Uri> filePathCallbackNormal;
    protected ValueCallback<Uri[]> filePathCallbackLollipop;
    protected final static int FILECHOOSER_NORMAL_REQ_CODE = 2001;
    protected final static int FILECHOOSER_LOLLIPOP_REQ_CODE = 2002;
    private Uri cameraImageUri = null;

    protected String mainUrl;

    protected final static int POPUP_REQUEST_CODE = 100;
    protected final static int POPUP_RESULT_CODE = 1;

    protected final static int VIEWIMG_REQUEST_CODE = 200;

    public AbstractPopWebActive(int layoutId) {
        this.layoutId = layoutId;
    }

    // 카메라 기능 구현
    @SuppressLint("IntentReset")
    private void runCamera(boolean _isCapture) {
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //intentCamera.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PHOTO_" + timeStamp + ".png";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(storageDir, imageFileName);

        // File 객체의 URI 를 얻는다.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cameraImageUri = FileProvider.getUriForFile(this, "com.uni4989.artstore.fileprovider", file);
        } else {
            cameraImageUri = Uri.fromFile(file);
        }
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);

        if (!_isCapture) { // 선택팝업 카메라, 갤러리 둘다 띄우고 싶을 때

            Intent pickIntent = new Intent(Intent.ACTION_PICK);
            pickIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            pickIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");

            String pickTitle = "사진 가져올 방법을 선택하세요.";
            Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);

            // 카메라 intent 포함시키기..
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{intentCamera});
            startActivityForResult(chooserIntent, FILECHOOSER_LOLLIPOP_REQ_CODE);
        }
        else {// 바로 카메라 실행..
            startActivityForResult(intentCamera, FILECHOOSER_LOLLIPOP_REQ_CODE);
        }
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

        mPopWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Alert")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> result.confirm())
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message,
                                       final JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Confirm")
                        .setMessage(message)
                        .setPositiveButton("Yes", (dialog, which) -> result.confirm())
                        .setNegativeButton("No", (dialog, which) -> result.cancel())
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }

            // For Android 5.0+ 카메라 - input type="file" 태그를 선택했을 때 반응
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {
                //Log.d("MainActivity - onShowFileChooser", "5.0+");

                // Callback 초기화 (중요!)
                if (filePathCallbackLollipop != null) {
                    filePathCallbackLollipop.onReceiveValue(null);
                    filePathCallbackLollipop = null;
                }
                filePathCallbackLollipop = filePathCallback;

                boolean isCapture = fileChooserParams.isCaptureEnabled();

                Log.d("onShowFileChooser : " , String.valueOf(isCapture));
                runCamera(isCapture);
                return true;
            }
        });

        //메인 주소는 이쪽
        mPopWebView.loadUrl(url);

        //progressbar
        progressBar = (ProgressBar) findViewById(R.id.web_progress);
        progressBar.setVisibility(View.GONE);
    }

    public abstract void createWeb();

    public abstract void openWeb(String url);

    public void openViewImage(String prductIndx) {}

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
            openWeb(wepUrl);
        }

        @JavascriptInterface //이게 있어야 웹에서 실행이 가능합니다.
        public void viewImage(final String prductIndx) {
            openViewImage(prductIndx);
        }

        @JavascriptInterface //이게 있어야 웹에서 실행이 가능합니다.
        public void callParntByChild(final String jsonData) {
            try
            {
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d("onActivityResult() ","resultCode = " + requestCode);

        switch (requestCode) {
            case POPUP_REQUEST_CODE:
                String getString =  data.getStringExtra("string");
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
                Log.d("onActivityResult() ","FILECHOOSER_LOLLIPOP_REQ_CODE = " + FILECHOOSER_LOLLIPOP_REQ_CODE);

                if (resultCode == RESULT_OK) {
                    Log.d("onActivityResult() ","FILECHOOSER_LOLLIPOP_REQ_CODE 의 if문  RESULT_OK 안에 들어옴");

                    if (filePathCallbackLollipop == null) return;
                    if (data == null)
                        data = new Intent();
                    if (data.getData() == null)
                        data.setData(cameraImageUri);

                    filePathCallbackLollipop.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                    filePathCallbackLollipop = null;
                } else {
                    Log.d("onActivityResult() ","FILECHOOSER_LOLLIPOP_REQ_CODE 의 if문의 else문 안으로~");
                    if (filePathCallbackLollipop != null)
                    {   //  resultCode에 RESULT_OK가 들어오지 않으면 null 처리하지 한다.(이렇게 하지 않으면 다음부터 input 태그를 클릭해도 반응하지 않음)

                        Log.d("onActivityResult() ","FILECHOOSER_LOLLIPOP_REQ_CODE 의 if문의 filePathCallbackLollipop이 null이 아니면");
                        filePathCallbackLollipop.onReceiveValue(null);
                        filePathCallbackLollipop = null;
                    }

                    if (filePathCallbackNormal != null)
                    {
                        filePathCallbackNormal.onReceiveValue(null);
                        filePathCallbackNormal = null;
                    }
                }
                break;
            default:

                break;
        }
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
