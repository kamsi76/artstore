package com.uni4989.artstore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.JavascriptInterface;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonActivity extends AppCompatActivity {

    protected final static int POPUP_REQUEST_CODE = 100;

    public void openWeb(Class clazz, String url) {
        Intent intent = new Intent(this, clazz);
        intent.putExtra("wepUrl", url);
        startActivityForResult(intent, POPUP_REQUEST_CODE);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }


    public void createDynamicLink(final String subject, String PageURL, String ImgUrl){
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
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
                        catch (Exception e) {
                        }
                    }
                });
    }
}
