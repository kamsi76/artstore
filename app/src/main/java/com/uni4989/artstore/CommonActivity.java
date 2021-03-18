package com.uni4989.artstore;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonActivity extends AppCompatActivity {

    protected final static int POPUP_REQUEST_CODE = 100;
    protected final static int POPUP_RESULT_CODE = 1;

    protected final static int FILECHOOSER_NORMAL_REQ_CODE = 2001;
    protected final static int FILECHOOSER_LOLLIPOP_REQ_CODE = 2002;

    protected ValueCallback<Uri> filePathCallbackNormal;
    protected ValueCallback<Uri[]> filePathCallbackLollipop;

    protected String token;
    protected Uri cameraImageUri = null;

    public void openViewImage(String prductIndx) {}

    public void openWeb(Class clazz, String url) {
        Intent intent = new Intent(this, clazz);
        intent.putExtra("wepUrl", url);
        startActivityForResult(intent, POPUP_REQUEST_CODE);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }


    @SuppressLint({"MissingPermission", "HardwareIds"})
    public String getPhoneNumber() {
        String phoneNum = "";
        try {
            Log.d(this.getLocalClassName(), "onCreate: 휴대폰 번호 획득 시도!!!");
            TelephonyManager telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            phoneNum = telManager.getLine1Number();

            Log.d("PHONE NUMBER : ", phoneNum);
            if (phoneNum.startsWith("+82")) {
                phoneNum = phoneNum.replace("+82", "0");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(this.getLocalClassName(), "onCreate: 휴대폰 번호 획득 실패!!!");
        }

        return phoneNum;
    }

    public void getToken() {
        try {

            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w(this.getLocalClassName(), "getInstanceId Failed!!!", task.getException());
                    return;
                }
                token = task.getResult().getToken();

                Log.d("MainActivity", "getToken: " + token);
            });

        } catch (Exception e) {
            Log.d(this.getLocalClassName(), "onCreate: Firebase Setting 에러");
        }
    }


    public void createDynamicLink(final String subject, String PageURL, String ImgUrl){
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(PageURL))
                .setDomainUriPrefix("https://artstore.page.link")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder(getPackageName())
                                .build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle("너와 나의 예술품 직거리 장터 유니아트 :)")
                                .setImageUrl(Uri.parse(ImgUrl))
                                .build())
                .buildShortDynamicLink()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Uri ShortLink = task.getResult().getShortLink();
                        try {
                            Intent Sharing_Intent = new Intent();
                            Sharing_Intent.setAction(Intent.ACTION_SEND);
                            Sharing_Intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                            Sharing_Intent.putExtra(Intent.EXTRA_TEXT, ShortLink.toString());
                            Sharing_Intent.setType("text/plain");
                            startActivity(Intent.createChooser(Sharing_Intent, "초대하기"));
                        }
                        catch (Exception e) {}
                    }
                });
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

    protected class WebChromeClientClass extends WebChromeClient {
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
