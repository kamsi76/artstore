package com.uni4989.artstore.firebase;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class CustomFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = CustomFirebaseInstanceIDService.class.getSimpleName();

    // 토큰 재생성
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.

        Log.d(TAG, "token 확인 ");

        String token = FirebaseInstanceId.getInstance().getToken();

        sendRegistrationToServer(token);

    }

    private void sendRegistrationToServer(String token){
        //디바이스 토큰이 생성되거나 재생성 될 시 동작할 코드 작성
    }

}
