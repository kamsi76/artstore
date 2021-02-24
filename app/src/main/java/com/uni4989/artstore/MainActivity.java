package com.uni4989.artstore;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.os.Bundle;
import android.telephony.TelephonyManager;
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
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private final static int POPUP_REQUEST_CODE = 100;

    private FirebaseRemoteConfig remoteConfig;

    public ValueCallback<Uri> filePathCallbackNormal;
    public ValueCallback<Uri[]> filePathCallbackLollipop;
    public final static int FILECHOOSER_NORMAL_REQ_CODE = 2001;
    public final static int FILECHOOSER_LOLLIPOP_REQ_CODE = 2002;
    private Uri cameraImageUri = null;


    private WebView mWebView;
    private ProgressBar progressBar;

    private String token;
    private long latestVersion = 0;

    public void popWeb(String url) {
        Intent intent = new Intent(MainActivity.this, PopupWebActivity.class);

        intent.putExtra("wepUrl", url);
        startActivityForResult(intent, POPUP_REQUEST_CODE);

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    public class AndroidBridge {
        @JavascriptInterface //이게 있어야 웹에서 실행이 가능합니다.
        public void open(final String wepUrl) {
            popWeb(wepUrl);
        }
    }

    public void checkVersion() {

        remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                //.setMinimumFetchIntervalInSeconds(3600)
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);

        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        latestVersion = remoteConfig.getLong("latest_version");

                        try {
                            long appVersion;
                            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
                            if( Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                                appVersion = pi.getLongVersionCode();
                            } else {
                                appVersion = pi.versionCode;
                            }

                            if( latestVersion > appVersion ) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setTitle("업데이트 알림!");
                                builder.setMessage("최신 업데이트가 있습니다.\n업데이트 받으시기 바랍니다.")
                                        .setCancelable(false)
                                        .setPositiveButton("업데이트", (dialog, which) -> {
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse("market://details?id=" + getPackageName()));
                                            startActivity(intent);
                                            dialog.cancel();
                                            finish();
                                        })
                                        .create()
                                        .show();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, "Version Check Fail!!!!!!!");
                        }
                    }
                });


    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public String getPhoneNumber() {
        String phoneNum = "";
        try {
            Log.d(TAG, "onCreate: 휴대폰 번호 획득 시도!!!");
            TelephonyManager telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            phoneNum = telManager.getLine1Number();

            Log.d("PHONE NUMBER : ", phoneNum);
            if (phoneNum.startsWith("+82")) {
                phoneNum = phoneNum.replace("+82", "0");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onCreate: 휴대폰 번호 획득 실패!!!");
        }

        return phoneNum;
    }

    public void getToken() {
        try {

            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId Failed!!!", task.getException());
                            return;
                        }
                        token = task.getResult().getToken();
                    });

        } catch (Exception e) {
            Log.d(TAG, "onCreate: Firebase Setting 에러");
        }
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

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkVersion();
        checkVerify();
        getToken();

        mWebView = findViewById(R.id.activity_main_webview);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setTextZoom(100);

        mWebView.addJavascriptInterface(new AndroidBridge(), "BRIDGE");

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:")) {
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

                if (mWebView.getUrl().contains("jscall://")) {
                    mWebView.stopLoading();
                    progressBar.setVisibility(View.GONE);

                    String result = mWebView.getUrl().substring(mWebView.getUrl().lastIndexOf("(") + 2);

                    String tempUrl = result.substring(0, result.length() - 2);

                    popWeb(tempUrl);
                }
            }

            //웹페이지 로딩 종료시 호출
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                if (mWebView.getUrl().indexOf("t=login") > 0) {
                    mWebView.loadUrl("javascript:receiveToken('" + token + "')");
                }

                if (
                        mWebView.getUrl().indexOf("t=member&s=registForm") > 0 ||
                        mWebView.getUrl().indexOf("t=member&s=oauthForm") > 0 ||
                        mWebView.getUrl().indexOf("t=login&s=find") > 0
                ) {
                    mWebView.loadUrl("javascript:receivePhone('" + getPhoneNumber() + "')");
                }
            }
        });

        mWebView.setWebChromeClient(new WebChromeClientClass());  //웹뷰에 크롬 사용 허용. 이 부분이 없으면 크롬에서 alert가 뜨지 않음

        mWebView.loadUrl( getString(R.string.default_url) );

        //progressbar
        progressBar = findViewById(R.id.web_progress);
        progressBar.setVisibility(View.GONE);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d("onActivityResult() ","resultCode = " + requestCode);

        switch (requestCode) {
            case POPUP_REQUEST_CODE:
                String getString =  data.getStringExtra("string");
                mWebView.loadUrl("javascript:callParntByChild('" + getString + "')");

                if( mWebView.getUrl().indexOf("s=chatRooms") > 1 ) {
                    mWebView.reload();
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

    // 카메라 기능 구현
    @SuppressLint("IntentReset")
    private void runCamera(boolean _isCapture) {
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //intentCamera.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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

            Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.d(TAG, "이름" + mWebView.getUrl());

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(!mWebView.getUrl().equals(getString(R.string.default_url))) {
                if( mWebView.getUrl().indexOf("t=prduct&s=post") > 0) {

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

                    return false;
                } else if ( mWebView.getUrl().indexOf("t=member&s=oauthForm") > 0 ) {
                    mWebView.loadUrl( getString(R.string.default_url) + "/?t=login" );
                    return false;
                } else if (mWebView.canGoBack()) {
                    mWebView.goBack();
                    return false;
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("");
                    builder.setMessage("종료하시겠습니까?.");
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
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("");
                builder.setMessage("종료하시겠습니까?.");
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
        }

        return super.onKeyDown(keyCode, event);
    }

    private class WebChromeClientClass extends WebChromeClient {
        // 자바스크립트의 alert창
        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            new AlertDialog.Builder(view.getContext())
                    .setTitle("알림!")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> result.confirm())
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }

        // 자바스크립트의 confirm창
        @Override
        public boolean onJsConfirm(WebView view, String url, String message,
                                   final JsResult result) {
            new AlertDialog.Builder(view.getContext())
                    .setTitle("확인!")
                    .setMessage(message)
                    .setPositiveButton("예", (dialog, which) -> result.confirm())
                    .setNegativeButton("아니오", (dialog, which) -> result.cancel())
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
    }

}