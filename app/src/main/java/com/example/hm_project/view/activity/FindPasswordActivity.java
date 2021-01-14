package com.example.hm_project.view.activity;


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
import com.example.hm_project.databinding.ActivityFindpasswordBinding;
import com.example.hm_project.util.CodeManager;
import com.example.hm_project.util.Crypto;
import com.example.hm_project.util.HM_Singleton;
import com.example.hm_project.util.JsonParser;
import com.example.hm_project.util.NetworkManager;

import java.net.URL;

/***
 *  비밀번호 찾기
 *  1- 비밀번호 찾기 버튼 클릭시 이벤트
 *  2- 비밀번호 찾기 성공시 이벤트
 *  3- 빈칸 확인 ( 비밀번호 찾기 필수정보 입력칸 )
 *  4- 서버 체크
 *  5- 뒤로가기 눌렀을 때 ( 로그인 화면으로 이동 )
 */

public class FindPasswordActivity extends AppCompatActivity {

    private JsonParser jsonParser = HM_Singleton.getInstance(new JsonParser());
    private EditTextInput editTextInput = new EditTextInput();
    private PopupListener popupListener = new PopupListener();
    private ActivityFindpasswordBinding binding;

    private String Tag = "FindPasswordActivity 이동재";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_findpassword);
        binding.setActivity(this);

        //EditText에서 이메일을 입력받을 때 이메일형식대로 입력하지 않으면 빨간색 테두리로 알려준다.
        editTextInput.inputEmail(binding.fpInputEmail, binding.fpErrorEmail);
        //EditText에서 이름을 입력받을 때 정해진 형식대로 입력하지 않으면 빨간색 테두리로 알려준다.
        editTextInput.inputName(binding.fpInputName, binding.fpErrorName);
        //EditText에서 핸드폰번호를 입력받을 때 정해진 형식대로 입력하지 않으면 빨간색 테두리로 알려준다.
        editTextInput.inputPhoneNO(binding.fpInputPhoneNO, binding.fpErrorPhoneNO);
    }

    // 1 - 비밀번호 찾기 버튼 클릭시 이벤트
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onFindPasswordClick(View view) {
        Context mContext = this;
        binding.RelativeLayoutFP.setClickable(false); // 연속으로 누르면 DB와 연속으로 통신하는 것을 방지하기 한번만 누르게 한다.
        if (!fpEmpty()) {
            // 이메일입력을 하지 않았을 때
            if (EditTextInput.checkNPE(binding.fpInputEmail.getText().toString())) {
                binding.fpErrorEmail.setText("필수 정보입니다.");
            }// 이름입력을 하지 않았을 때
            if (EditTextInput.checkNPE(binding.fpInputName.getText().toString())) {
                binding.fpErrorName.setText("필수 정보입니다.");
            } // 핸드폰번호입력을 하지 않았을 때
            if (EditTextInput.checkNPE(binding.fpInputPhoneNO.getText().toString())) {
                binding.fpErrorPhoneNO.setText("필수 정보입니다.");
            }
            binding.RelativeLayoutFP.setClickable(true); // 틀렸을 경우에는 다시 눌러야 하므로 풀어준다.
        } else {
            // 네트워크 연결 확인
            if (!NetworkManager.networkCheck(getApplicationContext())) {
                Log.i(Tag, "네트워크 연결 문제 발생");
                popupListener.viewPopup(this, CodeManager.NewtWork_Error);
                // 서버 연결 확인
            } else if (!serverCheck()) {
                Log.i(Tag, "서버 연결 문제 발생");
                popupListener.popupEvent(this, "서버 연결 오류", "비밀번호 찾기 실패");
            } else {
                try {
                    URL url = new URL(APIManager.FindPassword_URL);

                    InterfaceManager task = new InterfaceManager(url);
                    String json = JsonMaker.jsonObjectMaker(binding.fpInputEmail.getText().toString(), "",
                            binding.fpInputName.getText().toString(), binding.fpInputPhoneNO.getText().toString(), "", "", "", "", "");
                    String returns = task.execute(json).get(); // 9

                    // 서버로부터 온 값을 파싱해서 가져온다.
                    LoginJsonData loginJsonData = jsonParser.jsonParsingFindPassword(returns);

                    String code = loginJsonData.getCode();
                    String tempKey = loginJsonData.getData1();
                    String tempPassword = loginJsonData.getData2();

                    if (code.trim().equals("SY_2000")) {
                        Crypto.secretKEY = tempKey;
                        String decrpyt = Crypto.decryptAES256(tempPassword);
                        PreferenceManager.setString(mContext, "findPassword", decrpyt); //임시비밀번호 로그인 화면에 자동으로 띄워주기 위해 저장.
                        popupListener.popupEventReturn(FindPasswordActivity.this, "비밀번호 찾기 성공", "임시비밀번호 : " + decrpyt + "\n" + "유효기간 :  24시간");

                        Log.i(Tag, "임시비밀번호 발급 성공");
                    } else {
                        binding.RelativeLayoutFP.setClickable(true); // 틀렸을 경우에는 다시 눌러야 하므로 풀어준다.
                        popupListener.popupEvent(FindPasswordActivity.this, "비밀번호 찾기 실패", "입력정보를 확인해주세요.");
                        Log.i(Tag, "임시비밀번호 발급 실패 (오류 발생)");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 2 - 비밀번호 찾기 성공시 이벤트
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //데이터 받기
                Intent intent2 = new Intent(FindPasswordActivity.this, LoginActivity.class);
                startActivity(intent2);
                FindPasswordActivity.this.finish();
            }
        }
    }

    // 3 - 빈칸 확인 ( 비밀번호 찾기 필수정보 입력칸 )
    private boolean fpEmpty() {
        if (EditTextInput.checkNPE(binding.fpInputEmail.getText().toString())) {
            return false;
        } else if (binding.fpErrorEmail.getText().toString().equals("이메일 형식으로 입력해주세요.")) {
            return false;
        } else if (EditTextInput.checkNPE(binding.fpInputName.getText().toString())) {
            return false;
        } else if (EditTextInput.checkNPE(binding.fpInputPhoneNO.getText().toString())) {
            return false;
        } else if (binding.fpErrorPhoneNO.getText().toString().equals("핸드폰번호 형식에 맞게 입력해주세요.")) {
            return false;
        } else
            return true;
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

    // 5 - 뒤로가기 눌렀을 때 ( 로그인 화면으로 이동 )
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        popupListener.moveActivity(this, LoginActivity.class);
    }
}