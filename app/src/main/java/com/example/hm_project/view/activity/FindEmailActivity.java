package com.example.hm_project.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.hm_project.Command.PopupListener;
import com.example.hm_project.Command.EditTextInput;
import com.example.hm_project.Command.JsonMaker;
import com.example.hm_project.Command.SetDate;
import com.example.hm_project.R;
import com.example.hm_project.Command.InterfaceManager;
import com.example.hm_project.data.APIManager;
import com.example.hm_project.data.LoginJsonData;
import com.example.hm_project.databinding.ActivityFindemailBinding;
import com.example.hm_project.util.CodeManager;
import com.example.hm_project.util.HM_Singleton;
import com.example.hm_project.util.JsonParser;
import com.example.hm_project.util.NetworkManager;

import java.net.URL;

/***
 *  이메일 찾기
 *  1- 날짜 입력 버튼 클릭시 이벤트
 *  2- 이메일 찾기 버튼 클릭시 이벤트
 *  3- 이메일 찾기 성공시 이벤트
 *  4- 빈칸 확인 ( 이메일 찾기 필수정보 입력칸 )
 *  5- 서버 체크
 *  6- 뒤로가기 눌렀을 때 ( 로그인 화면으로 이동 )
 */

public class FindEmailActivity extends AppCompatActivity {

    private JsonParser jsonParser = HM_Singleton.getInstance(new JsonParser());
    private EditTextInput editTextInput = new EditTextInput();
    private PopupListener popupListener = new PopupListener();
    private SetDate setDate = new SetDate();
    private ActivityFindemailBinding binding;

    private String Tag = "FindEmailActivity 이동재";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_findemail);
        binding.setActivity(this);

        // 이름 형식에 맞는지 검사
        editTextInput.inputName(binding.feInputName, binding.feErrorName);
        // 핸드폰번호 형식에 맞는지 검사
        editTextInput.inputPhoneNO(binding.feInputPhoneNo, binding.feErrorPhoneNO);
    }

    // 1 - 날짜 입력 버튼 클릭시 이벤트
    public void onBirthDayClick(View view) {
        setDate.setDate(FindEmailActivity.this, binding.feInputBirthday, binding.feErrorBirthday);
    }

    // 2 - 이메일 찾기 버튼 클릭시 이벤트
    public void onFindEmailClick(View view) {
        if (!feEmpty()) {
            // 이름입력을 안했을 때
            if (EditTextInput.checkNPE(binding.feInputName.getText().toString())) {
                binding.feErrorName.setText("필수 정보입니다.");
            }
            // 핸드폰번호입력을 안했을 때
            if (EditTextInput.checkNPE(binding.feInputPhoneNo.getText().toString())) {
                binding.feErrorPhoneNO.setText("필수 정보입니다.");
            }
            // 생년월일입력을 안했을 때
            if (EditTextInput.checkNPE(binding.feInputBirthday.getText().toString())) {
                binding.feErrorBirthday.setText("필수 정보입니다.");
            }
        } else {
            // 네트워크 연결 확인
            if (!NetworkManager.networkCheck(getApplicationContext())) {
                Log.i(Tag, "네트워크 연결 문제 발생");
                popupListener.viewPopup(this, CodeManager.NewtWork_Error);
                // 서버 연결 확인
            } else if (!serverCheck()) {
                Log.i(Tag, "서버 연결 문제 발생");
                popupListener.popupEvent(this, "서버 연결 오류", "회원가입 실패");
            } else {
                try {
                    URL url = new URL(APIManager.FindEmail_URL);

                    InterfaceManager task = new InterfaceManager(url);
                    String json = JsonMaker.jsonObjectMaker("", "", binding.feInputName.getText().toString(),
                            binding.feInputPhoneNo.getText().toString(), binding.feInputBirthday.getText().toString(), "", "", "", "");
                    String returns = task.execute(json).get(); // 9

                    // 서버에 온 Json 데이터 파싱하기
                    LoginJsonData loginJsonData = jsonParser.jsonParsingFindEmail(returns);

                    String code = loginJsonData.getCode();
                    String userEmail = loginJsonData.getData1();

                    if ("SY_2000".equals(code)) {
                        popupListener.popupEventReturn(FindEmailActivity.this, "아이디 찾기 성공", "아이디 : " + userEmail);
                        Log.i(Tag, "아이디 찾기 성공");
                    } else {
                        popupListener.popupEvent(FindEmailActivity.this, "아이디 찾기 실패", "입력정보를 확인해주세요.");
                        Log.i(Tag, "아이디 찾기 실패");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 3 - 이메일 찾기 성공시 이벤트
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //데이터 받기
                Intent intent = new Intent(FindEmailActivity.this, LoginActivity.class);
                startActivity(intent);
                FindEmailActivity.this.finish();
            }
        }
    }

    // 4 - 빈칸 확인 ( 이메일 찾기 필수정보 입력칸 )
    private boolean feEmpty() {
        if (EditTextInput.checkNPE(binding.feInputName.getText().toString())) {
            return false;
        } else if (binding.feErrorName.getText().toString().equals("한글(2~4)과 영문자(2~20)만 입력해주세요.")) {
            return false;
        } else if (EditTextInput.checkNPE(binding.feInputPhoneNo.getText().toString())) {
            return false;
        } else if (binding.feErrorPhoneNO.getText().toString().equals("핸드폰번호 형식에 맞게 입력해주세요.")) {
            return false;
        } else if (EditTextInput.checkNPE(binding.feInputBirthday.getText().toString())) {
            return false;
        } else
            return true;
    }

    // 5 - 서버 체크
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

    // 6 - 뒤로가기 눌렀을 때 ( 로그인 화면으로 이동 )
    @Override
    public void onBackPressed() {
        popupListener.moveActivity(this, LoginActivity.class);
    }
}