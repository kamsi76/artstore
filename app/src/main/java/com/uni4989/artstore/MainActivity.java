package com.uni4989.artstore;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.uni4989.artstore.preference.CustomPreferenceManager;
import com.uni4989.artstore.preference.SettingActivity;

public class MainActivity extends CommonActivity {

    private static final String TAG = "MainActivity";
    protected final static int SETTING_REQUEST_CODE = 900;

    private SwipeRefreshLayout swipeRefreshLayout;

    private long backBtnTime = 0;

    private void popWeb(String url) {
        super.openWeb(PopupWebActivity.class, url);
    }

    /**
     * 권한 획득 여부 확인
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void checkVerify() {

        if (
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            //카메라 또는 저장공간 권한 획득 여부 확인
            if (
                    ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_NETWORK_STATE) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_PHONE_NUMBERS) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_PHONE_STATE)
            ) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("알림");
                builder.setMessage("권한을 허용해주셔야 유니아트 앱을 사용할 수 있습니다.");
                builder.setPositiveButton("예", (dialog, which) -> {
                    Intent appDetail = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                    appDetail.addCategory(Intent.CATEGORY_DEFAULT);
                    appDetail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(appDetail);
                });

                builder.setNegativeButton("아니오", (dialog, which) -> finish());

                builder.create().show();


                Toast.makeText(getApplicationContext(), "권한 관련 요청을 허용해 주셔야 카메라 캡처이미지 사용등의 서비스를 이용가능합니다.", Toast.LENGTH_SHORT).show();

            } else {
                // 카메라 및 저장공간 권한 요청
                ActivityCompat.requestPermissions(this, new String[]{
                        android.Manifest.permission.INTERNET,
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.ACCESS_NETWORK_STATE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_PHONE_NUMBERS,
                        android.Manifest.permission.READ_PHONE_STATE
                }, 1);
            }
        }
    }

    private void changeActivityUrl(Bundle extras) {
        String msgType = extras.getString("msgType");
        String targetUrl = extras.getString("targetUrl");

        if( "detail".equals(msgType)) popWeb(targetUrl);
        else if( "chat".equals(msgType) ) {
            //mWebView.loadUrl(getString(R.string.default_url) + "/content.php?t=prduct&s=chatRooms");
            popWeb(targetUrl);
        }
    }

    private void setting() {
        boolean firstActiveApp = CustomPreferenceManager.getBoolean(this, "firstActiveApp");
        if( !firstActiveApp ) {

            //환경에서 Notifi 받는 것으로 설정
            CustomPreferenceManager.setBoolean(this, "pref_notification", true);
            CustomPreferenceManager.setBoolean(this, "firstActiveApp", true);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkVersion();
        checkVerify();
        getToken();
        setting();

        swipeRefreshLayout = findViewById(R.id.swiperefreshlayout);
        mWebView = findViewById(R.id.activity_main_webview);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setTextZoom(100);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            /* Webview를 새로고침한다. */
            mWebView.reload();
            /* 업데이트가 끝났음을 알림 */
            swipeRefreshLayout.setRefreshing(false);
        });

        mWebView.addJavascriptInterface(new AndroidBridge(), "BRIDGE");

        mWebView.setWebViewClient(new WebViewClientClass() );

        mWebView.setWebChromeClient(new WebChromeClientClass());  //웹뷰에 크롬 사용 허용. 이 부분이 없으면 크롬에서 alert가 뜨지 않음

        mWebView.loadUrl( getString(R.string.default_url) );

        //progressbar
        progressBar = findViewById(R.id.web_progress);
        progressBar.setVisibility(View.GONE);

        //푸시를 통해 넘어온 항목을 확인하여 해당 화면으로 이동 또는 팝업을 띄운다.
        Bundle extras = getIntent().getExtras();
        if( extras != null ) {
            changeActivityUrl(extras);
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d("onActivityResult() ","resultCode = " + requestCode);

        switch (requestCode) {
            case SETTING_REQUEST_CODE:
                if( data != null ) {
                    String actionType = data.getStringExtra("actionType");
                    if ("logout".equals(actionType)) {
                        mWebView.loadUrl(getString(R.string.default_url) + "/sources/Controller/Login.php?exec=logout");
                    } else if ("secession".equals(actionType)) {
                        mWebView.loadUrl(getString(R.string.default_url) + "/sources/Controller/Login.php?exec=logout");
                    }
                }
                break;
            case POPUP_REQUEST_CODE:
                if( data != null ) {
                    String getString = data.getStringExtra("string");
                    mWebView.loadUrl("javascript:callParntByChild('" + getString + "')");

                    if (mWebView.getUrl().indexOf("s=chatRooms") > 1) {
                        mWebView.reload();
                    }
                }
                break;
            case FILECHOOSER_NORMAL_REQ_CODE:
                if (resultCode == RESULT_OK) {
                    if (filePathCallbackNormal == null) return;
                    Uri result = data == null ? null : data.getData();
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

                    Uri[] results = null;
                    if (data == null) {
                        // If there is not data, then we may have taken a photo
                        if (cameraImageUri != null) {
                            results = new Uri[]{Uri.parse(String.valueOf(cameraImageUri))};
                        }
                    } else {

                        ClipData clipData = data.getClipData();

                        String dataString = data.getDataString();
                        if (clipData != null) {
                            results = new Uri[clipData.getItemCount()];
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                ClipData.Item item = clipData.getItemAt(i);
                                results[i] = item.getUri();
                            }
                        }

                        if (dataString != null) {
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }

                    filePathCallbackLollipop.onReceiveValue(results);
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

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.d(TAG, "이름" + mWebView.getUrl());

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if( mWebView.getUrl().equals(getString(R.string.default_url)) ) {

                long curTime = System.currentTimeMillis();
                long gapTime = curTime - backBtnTime;

                if (0 < gapTime && 2000 >= gapTime) {
                    //super.onBackPressed();
                    finish();
                } else {
                    backBtnTime = curTime;
                    Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                }

            } else {
                if (mWebView.getUrl().indexOf("t=prduct&s=post") > 0) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("");
                    builder.setMessage("등록화면에서 나가시겠습니까?");
                    builder.setPositiveButton("확인",
                            (dialog, which) -> {
                                //Toast.makeText(getApplicationContext(),"예를 선택했습니다.",Toast.LENGTH_LONG).show();
                                mWebView.goBack();
                            });
                    builder.setNegativeButton("취소",
                            (dialog, which) -> {
                                //Toast.makeText(getApplicationContext(),"예를 선택했습니다.",Toast.LENGTH_LONG).show();
                            });
                    builder.show();

                } else if (mWebView.canGoBack()) {
                    mWebView.goBack();
                    /*
                    else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("");
                        builder.setMessage(getString(R.string.app_name) + " 를\n종료하시겠습니까?.");
                        builder.setPositiveButton("확인",
                                (dialog, which) -> {
                                    //Toast.makeText(getApplicationContext(),"예를 선택했습니다.",Toast.LENGTH_LONG).show();
                                    finish();
                                });
                        builder.setNegativeButton("취소",
                                (dialog, which) -> {
                                    //Toast.makeText(getApplicationContext(),"예를 선택했습니다.",Toast.LENGTH_LONG).show();
                                });
                        builder.show();
                        return false;
                    }
                    */
                } else {

                    long curTime = System.currentTimeMillis();
                    long gapTime = curTime - backBtnTime;

                    if (0 < gapTime && 2000 >= gapTime) {
                        //super.onBackPressed();
                        finish();
                    } else {
                        backBtnTime = curTime;
                        Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                    }
                }
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
            openWeb(PopupWebActivity.class, wepUrl);
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

        @JavascriptInterface
        public void setting() {
            Intent i = new Intent(MainActivity.this, SettingActivity.class);
            startActivityForResult(i, SETTING_REQUEST_CODE);
        }
    }

}