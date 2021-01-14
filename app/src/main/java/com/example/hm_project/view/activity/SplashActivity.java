package com.example.hm_project.view.activity;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.hm_project.Command.EditTextInput;
import com.example.hm_project.Command.InterfaceManager;
import com.example.hm_project.Command.JsonMaker;
import com.example.hm_project.Command.SessionCallback;
import com.example.hm_project.R;
import com.example.hm_project.data.APIManager;
import com.example.hm_project.data.LoginJsonData;
import com.example.hm_project.data.PreferenceManager;
import com.example.hm_project.util.CodeManager;
import com.example.hm_project.util.HM_Singleton;
import com.example.hm_project.util.JsonParser;
import com.example.hm_project.util.NetworkManager;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.kakao.auth.AuthType;
import com.kakao.auth.Session;

import java.net.URL;
import java.util.ArrayList;

public class SplashActivity extends Activity {

    @SuppressLint("StaticFieldLeak")
    public static Context mContext;
    private JsonParser jsonParser = HM_Singleton.getInstance(new JsonParser());

    private SessionCallback sessionCallback = new SessionCallback(SplashActivity.this); //세션 받아옴

    String Tag = "SplashActivity 이동재";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Handler hd = new Handler();
                hd.postDelayed(new splashhandler(), 1000); // 1초 후에 hd handler 실행  3000ms = 3초
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Handler hd = new Handler();
                hd.postDelayed(new splashhandler(), 1000); // 1초 후에 hd handler 실행  3000ms = 3초
            }
        };

        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    private class splashhandler implements Runnable {
        public void run() {
            if (!NetworkManager.networkCheck(getApplicationContext())) {
                Log.i(Tag, "네트워크 연결 문제 발생");
                Toast.makeText(SplashActivity.this, "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
                moveLoginActivity();
            } else if (!serverCheck()) {
                Log.i(Tag, "서버 연결 문제 발생");
                Toast.makeText(SplashActivity.this, "서버연결오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                moveLoginActivity();
            } else {
                Session.getCurrentSession().addCallback(sessionCallback);
                Session.getCurrentSession().checkAndImplicitOpen(); // 카카오 자동로그인
                autoLogin();
            }
        }
    }

    private void autoLogin() {
        mContext = this;
        if (!EditTextInput.checkNPE(PreferenceManager.getString(mContext, "email")) && !EditTextInput.checkNPE(PreferenceManager.getString(mContext, "password"))) {

            String autoEmail = PreferenceManager.getString(mContext, "email");
            String autoPassword = PreferenceManager.getString(mContext, "password");

            try {
                URL url = new URL(APIManager.AutoLogin_URL);

                InterfaceManager task = new InterfaceManager(url);
                String json = JsonMaker.jsonObjectMaker(autoEmail, autoPassword, "", "", "", "", "", "", "");
                String returns = task.execute(json).get(); // 9

                Log.i(Tag, "서버에서 온 데이터 : " + returns);
                //JsonParserLogin Class를 이용하여 데이터를 파싱후 가져온다.
                LoginJsonData loginJsonData = jsonParser.jsonParsingAutoLogin(returns);

                String code = loginJsonData.getCode();

                switch (code) {
                    case "LO_0001":
                    case "LO_0007":
                        Toast.makeText(SplashActivity.this, "자동로그인 실패\n 아이디와 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
                        moveLoginActivity(); // 로그인 Activity로 이동
                        Log.i(Tag, "서버에서 온 AutoLogin Code: " + code);
                        break;
                    case "LO_0006": // 임시비밀번호가 만료되었을 경우 쉐어드프리퍼런스에 저장되어 있는 이메일과 패스워드를 지운다.
                        PreferenceManager.setString(mContext, "email", "");
                        PreferenceManager.setString(mContext, "password", "");
                        Toast.makeText(SplashActivity.this, "자동로그인 실패\n 임시 비밀번호 유효기한이 만료되었습니다.", Toast.LENGTH_SHORT).show();
                        moveLoginActivity();
                        Log.i(Tag, "서버에서 온 AutoLogin Code: " + code);
                        break;
                    case "LO_2000":
                        Toast.makeText(SplashActivity.this, "임시 비밀번호로 로그인 했습니다.\n 비밀번호를 변경해주세요.", Toast.LENGTH_SHORT).show();
                        moveMainActivity();
                        Log.i(Tag, "서버에서 온 AutoLogin Code: " + code);
                        break;
                    case "SY_2000":
                        Log.i(Tag, "서버에서 온 AutoLogin Code: " + code);
                        moveMainActivity();
                        break;
                    default:
                        Log.i(Tag, "서버에서 온 AutoLogin Code: " + code);
                        Toast.makeText(SplashActivity.this, "자동로그인 실패\n 서버 연결 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        moveLoginActivity();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            moveLoginActivity();
        }
    }

    private void moveLoginActivity() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent); //로딩이 끝난 후, ChoiceFunction 이동
        this.finish(); // 로딩페이지 Activity stack에서 제거
    }

    private void moveMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent); //로딩이 끝난 후, ChoiceFunction 이동
        SplashActivity.this.finish(); // 로딩페이지 Activity stack에서 제거
    }

    private boolean serverCheck() {
        try {
            URL url = new URL(APIManager.ServerCheck_URL);

            InterfaceManager task = new InterfaceManager(url);
            String serverData = task.execute("").get();
            if(serverData.equals("Yes")){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        //초반 플래시 화면에서 넘어갈때 뒤로가기 버튼 못누르게 함
    }
}