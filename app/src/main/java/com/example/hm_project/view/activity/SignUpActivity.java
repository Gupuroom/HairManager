package com.example.hm_project.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.databinding.DataBindingUtil;

import com.example.hm_project.Command.PopupListener;
import com.example.hm_project.Command.EditTextInput;
import com.example.hm_project.Command.JsonMaker;
import com.example.hm_project.Command.SetDate;
import com.example.hm_project.Command.InterfaceManager;
import com.example.hm_project.R;
import com.example.hm_project.data.APIManager;
import com.example.hm_project.databinding.ActivitySignupBinding;
import com.example.hm_project.util.CodeManager;
import com.example.hm_project.util.Crypto;
import com.example.hm_project.util.NetworkManager;

import java.net.URL;

/***
 *  회원가입
 *  1- init ( 사용자가 정해진 형식으로 정보를 입력하는지 검사, 사용자로부터 성별을 입력받는다. )
 *  2- 이메일 중복확인 버튼 클릭시 이벤트
 *  3- 핸드폰번호 중복확인 버튼 클릭시 이벤트
 *  4- 생년월일 입력받기
 *  5- 서버통신 ( 입력받은 값 DB에 저장 )
 *  6- 회원가입 성공시 이벤트
 *  7- 빈칸 확인 ( 회원가입 필수정보 입력칸 )
 *  8- 서버 체크
 *  9- 뒤로가기 눌렀을 때 ( 로그인 화면으로 이동 )
 */

public class SignUpActivity extends AppCompatActivity {

    private EditTextInput editTextInput = new EditTextInput(); // 입력데이터 형식 검사하는 클래스
    private PopupListener popupListener = new PopupListener();
    private SetDate setDate = new SetDate(); // 날짜 입력받는 클래스
    private ActivitySignupBinding binding;

    private String user_sex = "M", Green = "#27AE60";
    boolean check_overlap_email = false, check_overlap_phoneNO = false;

    private String Tag = "SignUpActivity 이동재";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_signup);
        binding.setActivity(this);

        init();
    }

    // 1 - init ( 사용자가 정해진 형식으로 정보를 입력하는지 검사, 사용자로부터 성별을 입력받는다. )
    private void init() {
        //EditText에서 이메일을 입력받을 때 이메일형식대로 입력하지 않으면 빨간색 테두리로 알려준다.
        editTextInput.inputEmail(binding.inputEmail, binding.errorEmail);
        //EditText에서 패스워드를 입력받을 때 정해진 형식대로 입력하지 않으면 빨간색 경고문구로 알려준다.
        editTextInput.inputPassword(binding.inputPassword, binding.errorPassword);
        //EditText에서 패스워드를 재입력받을 때 위에 입력된 패스워드와 일치하지 않으면 빨간색 경고문구로 알려준다.
        editTextInput.inputAgainPassword(binding.inputAgainPassword, binding.errorAgainPassword, binding.inputPassword);
        //EditText에서 이름을 입력받을 때 정해진 형식대로 입력하지 않으면 빨간색 테두리로 알려준다.
        editTextInput.inputName(binding.inputName, binding.errorName);
        //EditText에서 핸드폰번호를 입력받을 때 정해진 형식대로 입력하지 않으면 빨간색 테두리로 알려준다.
        editTextInput.inputPhoneNO(binding.inputPhoneNO, binding.errorPhoneNO);


        // 성별 입력
        RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener = (radioGroup, i) -> {
            if (i == R.id.genderMan) {
                user_sex = "M";
            } else if (i == R.id.genderWoman) {
                user_sex = "F";
            }
        };
        binding.genderGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener);
    }

    // 2 - 이메일 중복확인 버튼 클릭시 이벤트
    public void onOverlapEmailClick(View view) {
        // 네트워크 연결 확인
        if (!NetworkManager.networkCheck(getApplicationContext())) {
            Log.i(Tag, "네트워크 연결 문제 발생");
            popupListener.viewPopup(this, CodeManager.NewtWork_Error);
            // 서버 연결 확인
        } else if (!serverCheck()) {
            Log.i(Tag, "서버 연결 문제 발생");
            popupListener.popupEvent(SignUpActivity.this, "서버 연결 오류", "이메일 중복 확인 실패");
        } else {
            if (EditTextInput.checkNPE(binding.inputEmail.getText().toString()) || "이메일 형식으로 입력해주세요.".equals(binding.errorEmail.getText().toString())) {
                popupListener.popupEvent(SignUpActivity.this, "이메일 인증 실패", "이메일을 입력해주세요.");
            } else {
                try {
                    URL url = new URL(APIManager.OverlapEmail_URL);

                    InterfaceManager task = new InterfaceManager(url);
                    String json = JsonMaker.jsonObjectMaker(binding.inputEmail.getText().toString(), "", "", "", "", "", "", "", "");
                    String code = task.execute(json).get();

                    switch (code) {
                        case "SY_2000":
                            binding.overlapEmail.setText("확인완료");
                            binding.overlapEmail.setClickable(false);
                            binding.inputEmail.setEnabled(false);
                            check_overlap_email = true; //중복확인을 했는지 check

                            binding.errorEmail.setTextColor(Color.parseColor(Green));
                            binding.errorEmail.setText("사용가능한 이메일입니다.");  // 경고 메세지
                            Log.i(Tag, "이메일 중복 확인 성공");
                            break;
                        case "LO_0002":
                            popupListener.popupEvent(SignUpActivity.this, "이메일 인증 실패", "이미 사용중인 이메일입니다.");
                            Log.i(Tag, "이메일 중복 확인 실패 (이미 사용중인 이메일)");
                            break;
                        default:
                            popupListener.popupEvent(SignUpActivity.this, "이메일 인증 실패", "알 수 없는 오류가 발생했습니다.");
                            Log.i(Tag, "이메일 중복 확인 실패 (알 수 없는 오류)");
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // 3 - 핸드폰번호 중복확인 버튼 클릭시 이벤트
    public void onOverlapPhoneNOClick(View view) {
        // 네트워크 연결 확인
        if (!NetworkManager.networkCheck(getApplicationContext())) {
            Log.i(Tag, "네트워크 연결 문제 발생");
            popupListener.viewPopup(this, CodeManager.NewtWork_Error);
            // 서버 연결 확인
        } else if (!serverCheck()) {
            Log.i(Tag, "서버 연결 문제 발생");
            popupListener.popupEvent(SignUpActivity.this, "서버 연결 오류", "핸드폰번호 중복 확인 실패");
        } else {
            if (EditTextInput.checkNPE(binding.inputPhoneNO.getText().toString()) || "핸드폰번호 형식에 맞게 입력해주세요.".equals(binding.errorPhoneNO.getText().toString())) {
                popupListener.popupEvent(SignUpActivity.this, "핸드폰 인증 실패", "핸드폰번호를 입력해주세요.");
            } else {
                try {
                    URL url = new URL(APIManager.OverlapPhoneNO_URL);
                    InterfaceManager task = new InterfaceManager(url);
                    String json = JsonMaker.jsonObjectMaker("", "", "", binding.inputPhoneNO.getText().toString(), "", "", "", "", "");
                    String code = task.execute(json).get(); // 9

                    switch (code) {
                        case "SY_2000":
                            binding.overlapPhoneNO.setText("확인완료");
                            binding.overlapPhoneNO.setClickable(false);
                            binding.inputPhoneNO.setEnabled(false);
                            check_overlap_phoneNO = true;

                            binding.errorPhoneNO.setText("사용가능한 핸드폰번호입니다.");
                            binding.errorPhoneNO.setTextColor(Color.parseColor(Green));
                            Log.i(Tag, "이메일 중복 확인 성공");
                            break;
                        case "LO_0005":
                            popupListener.popupEvent(SignUpActivity.this, "핸드폰 인증 실패", "이미 등록된 핸드폰번호입니다.");
                            Log.i(Tag, "이메일 중복 확인 실패 (이미 사용중인 이메일)");
                            break;
                        default:
                            popupListener.popupEvent(SignUpActivity.this, "핸드폰 인증 실패", "알 수 없는 오류입니다.");
                            Log.i(Tag, "이메일 중복 확인 실패 (알 수 없는 오류)");
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // 4 - 생년월일 입력받기
    public void onBirthdayClick(View view) {
        setDate.setBirthday(SignUpActivity.this, binding.inputBirthday, binding.errorBirthday);
    }

    // 5 - 서버통신 ( 입력받은 값 DB에 저장 )
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onSignUPClick(View view) {
        // 빈칸이 있을시 회원가입을 허용하지 않는다.
        if (!signupEmpty()) {
            // 이메일입력을 안했을 때
            if (EditTextInput.checkNPE(binding.inputEmail.getText().toString())) {
                binding.errorEmail.setText("필수 정보입니다.");
            } // 이메일 중복확인을 하지 않았을 때
            if (!check_overlap_email) {
                popupListener.popupEvent(SignUpActivity.this, "회원가입 실패", "이메일중복확인을 해주세요.");
            } // 비밀번호입력을 안했을 때
            if (EditTextInput.checkNPE(binding.inputPassword.getText().toString())) {
                binding.errorPassword.setText("필수 정보입니다.");
            } // 비밀번호와 비밀번호 재입력 값이 다를 때
            if (!binding.inputAgainPassword.getText().toString().equals(binding.inputPassword.getText().toString())) {
                binding.errorAgainPassword.setText("비밀번호가 일치하지 않습니다.");
            } // 이름입력을 안했을 때
            if (EditTextInput.checkNPE(binding.inputName.getText().toString())) {
                binding.errorName.setText("필수 정보입니다.");
            } // 핸드폰번호입력을 안했을 때
            if (EditTextInput.checkNPE(binding.inputPhoneNO.getText().toString())) {
                binding.errorPhoneNO.setText("필수 정보입니다.");
            } // 핸드폰번호 중복확인을 안했을 때
            if (!check_overlap_phoneNO) {
                popupListener.popupEvent(SignUpActivity.this, "회원가입 실패", "핸드폰번호 중복확인을 해주세요.");
            } // 생년월일을 입력하지 않았을 때
            if (EditTextInput.checkNPE(binding.inputBirthday.getText().toString())) {
                binding.errorBirthday.setText("필수 정보입니다.");
            }
        } else {
            // 네트워크 연결 확인
            if (!NetworkManager.networkCheck(getApplicationContext())) {
                Log.i(Tag, "네트워크 연결 문제 발생");
                popupListener.viewPopup(this, CodeManager.NewtWork_Error);
                // 서버 연결 확인
            } else if (!serverCheck()) {
                Log.i(Tag, "서버 연결 문제 발생");
                popupListener.popupEvent(SignUpActivity.this, "서버 연결 오류", "회원가입 실패");
            } else {
                try {
                    URL url = new URL(APIManager.SignUp_URL);

                    // 암호화 키 생성
                    Crypto.aesKeyGen();

                    InterfaceManager task = new InterfaceManager(url);
                    // 입력데이터 Json형식으로 변환
                    String json = JsonMaker.jsonObjectMaker(binding.inputEmail.getText().toString(),
                            Crypto.encryptAES256(binding.inputPassword.getText().toString()), binding.inputName.getText().toString(),
                            binding.inputPhoneNO.getText().toString(), binding.inputBirthday.getText().toString(), user_sex, Crypto.secretKEY, "", "");
                    String code = task.execute(json).get();

                    switch (code){
                        case "SY_2000":  // 회원가입 성공
                            popupListener.popupEventReturn(SignUpActivity.this, "정상 가입완료", "서비스 가입이 완료되었습니다.");
                            Log.i(Tag, "회원가입 성공");
                            break;
                        default: // 회원가입 실패
                            popupListener.popupEvent(SignUpActivity.this, "회원가입 실패", "서버로부터 데이터를 가져오지 못했습니다.\n  다시 시도하세요");
                            Log.i(Tag, "회원가입 실패");
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 6 - 회원가입 성공시 이벤트
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                SignUpActivity.this.finish();
            }
        }
    }

    // 7 - 빈칸 확인 ( 회원가입 필수정보 입력칸 )
    private boolean signupEmpty() {
        if (EditTextInput.checkNPE(binding.inputEmail.getText().toString())) {
            return false;
        } else if (!check_overlap_email) {
            return false;
        } else if (EditTextInput.checkNPE(binding.inputPassword.getText().toString())) {
            return false;
        } else if (binding.errorPassword.getText().toString().equals("8~20자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")) {
            return false;
        } else if (!binding.inputAgainPassword.getText().toString().equals(binding.inputPassword.getText().toString())) {
            return false;
        } else if (EditTextInput.checkNPE(binding.inputName.getText().toString())) {
            return false;
        } else if (binding.errorName.getText().toString().equals("한글(2~4)과 영문자(2~20)만 입력해주세요.")) {
            return false;
        } else if (EditTextInput.checkNPE(binding.inputPhoneNO.getText().toString())) {
            return false;
        } else if (!check_overlap_phoneNO) {
            return false;
        } else if (EditTextInput.checkNPE(binding.inputBirthday.getText().toString())) {
            return false;
        } else {
            return true;
        }
    }

    // 8 - 서버 체크
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

    // 9 - 뒤로가기 눌렀을 때 ( 로그인 화면으로 이동 )
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        popupListener.moveActivity(this, LoginActivity.class);
    }
}
