package com.example.hm_project.view.activity;


import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.hm_project.Command.PopupListener;
import com.example.hm_project.Command.EditTextInput;
import com.example.hm_project.Command.InterfaceManager;
import com.example.hm_project.Command.JsonMaker;
import com.example.hm_project.R;
import com.example.hm_project.data.APIManager;
import com.example.hm_project.data.PreferenceManager;
import com.example.hm_project.databinding.ActivityUpdatepasswordBinding;
import com.example.hm_project.util.CodeManager;
import com.example.hm_project.util.Crypto;
import com.example.hm_project.util.NetworkManager;

import java.net.URL;

/***
 *  비밀번호 변경
 *  1- 비밀번호 변경 버튼 클릭시 이벤트
 *  2- 빈칸 확인 ( 비밀번호 찾기 필수정보 입력칸 )
 *  3- 툴바의 백 버튼 클릭시 이벤트
 *  4- 서버 체크
 */

public class UpdatePasswordActivity extends AppCompatActivity {

    private Context mContext = LoginActivity.mContext;
    private EditTextInput editTextInput = new EditTextInput();
    private PopupListener popupListener = new PopupListener();
    private ActivityUpdatepasswordBinding binding;

    private String Tag = "UpdatePasswordActivity 이동재";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_updatepassword);
        binding.setActivity(this);

        // 툴바 표시
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        //EditText에서 패스워드를 입력받을 때 정해진 규칙대로 입력하지 않으면 빨간색 테두리로 알려준다.
        editTextInput.inputPassword(binding.upInputPassword, binding.upErrorPassword);
        //EditText에서 변경할 패스워드를 입력받을 때 정해진 규칙대로 입력하지 않으면 빨간색 테두리로 알려준다.
        editTextInput.inputChangePassword(binding.upInputChangePassword, binding.upErrorChangePassword, binding.upInputPassword);
        //EditText에서 변경할 패스워드를 재입력받을 때 위에 입력된 패스워드와 일치하지 않으면 빨간색 경고문구로 알려준다.
        editTextInput.inputAgainPassword(binding.upInputAgainChangePassword, binding.upErrorAgainChangePassword, binding.upInputChangePassword);
    }

    // 1 - 비밀번호 변경 버튼 클릭시 이벤트
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onUpdatePasswordClick(View view) {

        if (!fpEmpty()) {
            // 패스워드입력을 하지 않았을 때
            if (EditTextInput.checkNPE(binding.upInputPassword.getText().toString())) {
                binding.upErrorPassword.setText("필수 정보입니다.");
            } // 변경할 패스워드입력을 하지 않았을 때
            if (EditTextInput.checkNPE(binding.upInputChangePassword.getText().toString())) {
                binding.upErrorChangePassword.setText("필수 정보입니다.");
            } // 변경할 패스워드 재입력을 하지 않았을 때
            if (EditTextInput.checkNPE(binding.upInputAgainChangePassword.getText().toString())) {
                binding.upErrorAgainChangePassword.setText("필수 정보입니다.");
            }
        } else {
            // 네트워크 연결 확인
            if (!NetworkManager.networkCheck(getApplicationContext())) {
                Log.i(Tag, "네트워크 연결 문제 발생");
                popupListener.viewPopup(this, CodeManager.NewtWork_Error);
                // 서버 연결 확인
            } else if (!serverCheck()) {
                Log.i(Tag, "서버 연결 문제 발생");
                popupListener.popupEvent(this, "서버 연결 오류", "비밀번호 변경 실패");
            } else {
                try {
                    URL url = new URL(APIManager.UpdatePassword_URL);

                    InterfaceManager task = new InterfaceManager(url);

                    Crypto.aesKeyGen();
                    String json = JsonMaker.jsonUPObjectMaker(PreferenceManager.getString(mContext, "userNO"),
                            Crypto.encryptAES256(binding.upInputPassword.getText().toString()), Crypto.encryptAES256(binding.upInputChangePassword.getText().toString()),
                            Crypto.secretKEY);
                    String code = task.execute(json).get(); // 9

                    switch(code){
                        case "SY_2000":
                            Log.i(Tag, "비밀번호 변경 성공");
                            popupListener.popupEvent(UpdatePasswordActivity.this, "비밀번호 변경 성공", "비밀번호 변경에 성공하였습니다.");
                            // 자동로그인을 위해서 쉐어드프리퍼런스에 저장되어 있는 비밀번호 자동으로 변경시켜줌
                            PreferenceManager.setString(mContext, "password", Crypto.encryptAES256(binding.upInputChangePassword.getText().toString()));
                            finish();
                            break;
                        case "MP_0002":
                            Log.i(Tag, "비밀번호 변경 실패 (원인 알수없음)");
                            popupListener.popupEvent(UpdatePasswordActivity.this, "비밀번호 변경 실패", "비밀번호 변경 실패 (원인 알수없음)");
                            break;
                        case "MP_0003":
                            Log.i(Tag, "임시비밀번호 변경 실패 (원인 알수없음)");
                            popupListener.popupEvent(UpdatePasswordActivity.this, "비밀번호 변경 실패", "임시비밀번호 변경 실패 (원인 알수없음)");
                            break;
                        default:
                            Log.i(Tag, "비밀번호 변경 실패 (코드 없음)");
                            popupListener.popupEvent(UpdatePasswordActivity.this, "비밀번호 변경 실패", "비밀번호 변경 실패 (코드 없음)");
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 2 - 빈칸 확인 ( 비밀번호 변경 필수정보 입력칸 )
    private boolean fpEmpty() {
        // 비밀번호입력을 안했을 때
        if (EditTextInput.checkNPE(binding.upInputPassword.getText().toString())) {
            return false;
            // 비밀번호 입력 형식이 틀렸을 때
        } else if (binding.upErrorPassword.getText().toString().equals("8~20자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")) {
            return false;
            // 변경할 비밀번호입력을 안했을 때
        } else if (EditTextInput.checkNPE(binding.upInputChangePassword.getText().toString())) {
            return false;
            // 변경할 비밀번호 입력형식이 틀렸을 때
        } else if (binding.upErrorChangePassword.getText().toString().equals("8~20자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")) {
            return false;
            // 변경할 비밀번호와 기존비밀번호가 똑같을 때
        } else if (binding.upErrorChangePassword.getText().toString().equals("기존과 동일합니다. 다르게 입력해주세요.")) {
            return false;
            // 변경할 비밀번호 재입력을 안했을 때
        } else if (EditTextInput.checkNPE(binding.upInputAgainChangePassword.getText().toString())) {
            return false;
            // 변경할 비밀번호와 변경할 비밀번호 재입력 값이 틀렸을 때
        } else if (binding.upErrorAgainChangePassword.getText().toString().equals("비밀번호가 일치하지 않습니다.")) {
            return false;
        } else
            return true;
    }

    // 3 - 툴바의 백 버튼 클릭시 이벤트
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    // 4 - 서버 체크
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