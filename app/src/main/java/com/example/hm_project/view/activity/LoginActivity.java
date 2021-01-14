package com.example.hm_project.view.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.hm_project.Command.PopupListener;
import com.example.hm_project.Command.EditTextInput;
import com.example.hm_project.Command.JsonMaker;
import com.example.hm_project.R;
import com.example.hm_project.Command.InterfaceManager;
import com.example.hm_project.data.APIManager;
import com.example.hm_project.data.LoginJsonData;
import com.example.hm_project.data.PreferenceManager;
import com.example.hm_project.databinding.ActivityLoginBinding;

import com.example.hm_project.Command.SessionCallback;
import com.example.hm_project.util.CodeManager;
import com.example.hm_project.util.Crypto;
import com.example.hm_project.util.HM_Singleton;
import com.example.hm_project.util.JsonParser;
import com.example.hm_project.util.NetworkManager;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.kakao.auth.AuthType;
import com.kakao.auth.Session;

import java.net.URL;

/***
 *  로그인
 *  1- 로그인버튼 클릭시 이벤트
 *  2- 이메일 찾기 버튼 클릭시 이벤트
 *  3- 비밀번호 찾기 버튼 클릭시 이벤트
 *  4- 회원가입 버튼 클릭시 이벤트
 *  5- 카카오로그인 버튼 클릭시 이벤트
 *  6- 임시비밀번호 자동입력하는 기능
 *  7- 자동 로그인 기능
 *  8- 로그인 성공, 자동 로그인 성공시 이벤트
 *  9- 엑티비티 종료
 *  10- 서버 체크
 */

public class LoginActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    public static Context mContext = SplashActivity.mContext;
    // 서버데이터(Json 형식)을 파싱하기 위한 클래스
    private JsonParser jsonParser = HM_Singleton.getInstance(new JsonParser());
    // 버튼 클릭시 해당 화면으로 이동시켜주는 클래스
    private PopupListener popupListener = new PopupListener();
    private SessionCallback sessionCallback = new SessionCallback(LoginActivity.this); //세션 받아옴
    private ActivityLoginBinding binding;

    private String Tag = "LoginActivity 이동재";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setActivity(this);

        set_FindPassword(); // 패스워드 찾기 했을 때 임시비밀번호 셋팅
    }

    // 1 - 로그인 버튼 클릭시 이벤트
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onLoginClick(View view) {
        if (EditTextInput.checkNPE(binding.TextInputEditTextEmail.getText().toString()) || EditTextInput.checkNPE(binding.TextInputEditTextPassword.getText().toString())) {
            if (EditTextInput.checkNPE(binding.TextInputEditTextEmail.getText().toString()))
                binding.loginErrorEmail.setText("아이디를 입력해주세요.");
            else
                binding.loginErrorEmail.setText("");
            if (EditTextInput.checkNPE(binding.TextInputEditTextPassword.getText().toString()))
                binding.loginErrorPassword.setText("비밀번호를 입력해주세요.");
            else
                binding.loginErrorPassword.setText("");
        } else {

            binding.loginErrorEmail.setText("");
            binding.loginErrorPassword.setText("");

            // 네트워크 연결 확인
            if (!NetworkManager.networkCheck(getApplicationContext())) {
                Log.i(Tag, "네트워크 연결 문제 발생");
                popupListener.viewPopup(this, CodeManager.NewtWork_Error);
            // 서버 연결 확인
            } else if (!serverCheck()) {
                Log.i(Tag, "서버 연결 문제 발생");
                popupListener.popupEvent(LoginActivity.this, "서버 연결 오류", "로그인 실패");
            } else {
                try {
                    URL url = new URL(APIManager.Login_URL);
                    // 암호화 키 생성
                    Crypto.aesKeyGen();
                    // 서버통신
                    InterfaceManager task = new InterfaceManager(url);
                    String json = JsonMaker.jsonObjectMaker(binding.TextInputEditTextEmail.getText().toString(),
                            Crypto.encryptAES256(binding.TextInputEditTextPassword.getText().toString()), "", "", "", "", Crypto.secretKEY, "", "");
                    String returns = task.execute(json).get(); // 9

                    // 데이터 파싱
                    LoginJsonData loginJsonData = jsonParser.jsonParsingLogin(returns);

                    String code = loginJsonData.getCode();
                    String userNO = loginJsonData.getData1();
                    String db_password = loginJsonData.getData2();

                    switch (code) {
                        case "LO_0001": // 일반 로그인 실패
                        case "LO_0007": // 임시 비밀번호 로그인 실패
                            popupListener.popupEvent(LoginActivity.this, "로그인 실패", "아이디와 비밀번호를 확인하세요.");
                            // 임시패스워드 로그인 성공
                            Log.i(Tag, "서버에서 온 Login Code: " + code);
                            break;
                        case "LO_0006": // 임시 비밀번호 유효기간 만료
                            popupListener.popupEvent(LoginActivity.this, "로그인 실패", "임시 비밀번호 유효기한이 \n만료되었습니다. 다시 발급받으세요.");
                            Log.i(Tag, "서버에서 온 Login Code: " + code);
                            break;
                        case "LO_2000": // 임시 비밀번호로 로그인 성공
                            //자동로그인을 위해 쉐어드프리퍼런스에 이메일이랑 암호화된 패스워드 저장
                            PreferenceManager.setString(mContext, "email", binding.TextInputEditTextEmail.getText().toString());
                            PreferenceManager.setString(mContext, "password", db_password);
                            PreferenceManager.setString(mContext, "userNO", userNO);
                            PreferenceManager.setInt(mContext, "kakaoCheck", 0);

                            popupListener.popupEventReturn(LoginActivity.this, "로그인 성공", "임시 비밀번호로 로그인 했습니다.\n 비밀번호를 변경해주세요.");
                            Log.i(Tag, "서버에서 온 Login Code: " + code);
                            break;
                        case "SY_2000": // 일반 로그인 성공
                            // 자동로그인을 위해 쉐어드프리퍼런스에 이메일이랑 암호화된 패스워드 저장
                            PreferenceManager.setString(mContext, "email", binding.TextInputEditTextEmail.getText().toString());
                            PreferenceManager.setString(mContext, "password", db_password);
                            PreferenceManager.setString(mContext, "userNO", userNO);
                            PreferenceManager.setInt(mContext, "kakaoCheck", 0);

                            Log.i(Tag, "서버에서 온 Login Code: " + code);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            LoginActivity.this.finish();
                            break;
                        default:
                            popupListener.popupEvent(LoginActivity.this, "로그인 실패", "알 수 없는 오류입니다");
                            break;
                    }
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        }
    }


    // 2 - 이메일 찾기 버튼 클릭시 이벤트
    public void onFindEmailClick(View view) {
        popupListener.moveActivity(LoginActivity.this, FindEmailActivity.class);
    }

    // 3 - 비밀번호 찾기 버튼 클릭시 이벤트
    public void onFindPasswordClick(View view) {
        popupListener.moveActivity(LoginActivity.this, FindPasswordActivity.class);
    }

    // 4 - 회원가입 버튼 클릭시 이벤트
    public void onSignUPClick(View view) {
        popupListener.moveActivity(LoginActivity.this, SignUpActivity.class);
    }

    // 5 - 카카오로그인 버튼 클릭시 이벤트
    public void onKakaoLoginClick(View view) {
        if (!NetworkManager.networkCheck(getApplicationContext())) {
            Log.i(Tag, "네트워크 연결 문제 발생");
            popupListener.viewPopup(this, CodeManager.NewtWork_Error);
        } else if (!serverCheck()) {
            Log.i(Tag, "서버 연결 문제 발생");
            popupListener.popupEvent(LoginActivity.this, "서버 연결 오류", "로그인 실패");

        } else {
            Session.getCurrentSession().addCallback(sessionCallback);
            Session.getCurrentSession().open(AuthType.KAKAO_TALK_ONLY, LoginActivity.this);
        }
    }

    // 6 - 임시비밀번호 자동입력하는 기능 ( 사용자가 비밀번호 찾기를 성공했을 때 임시 비밀번호를 비밀번호 칸에 세팅해준다. )
    private void set_FindPassword() {
        mContext = this;
        String findPassword = PreferenceManager.getString(mContext, "findPassword");
        if (!findPassword.equals("")) {
            binding.TextInputEditTextPassword.setText(findPassword);
            // 한번 임시 로그인 한 후에 임시비밀번호 지우기
            PreferenceManager.setString(mContext, "findPassword", "");
        }
    }


    // 8 - 로그인 성공, 자동 로그인 성공시 이벤트
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //데이터 받기
                Intent intent2 = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent2);
                LoginActivity.this.finish();
            }
        }
    }

    // 9 - 엑티비티 종료
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    // 10 - 서버 체크
    private boolean serverCheck() {
        try {
            URL url = new URL(APIManager.ServerCheck_URL);

            InterfaceManager task = new InterfaceManager(url);
            String serverData = task.execute("").get();
            if (serverData.equals("Yes")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
