package com.example.hm_project.Command;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import java.util.regex.Pattern;

/***
 *  텍스트 입력 검사
 *  1 - null or "" 검사
 *  2 - 일기 제목 길이 검사
 *  3 - 일기 내용 길이 검사 ( 최대 5,592,405자 - 한글기준 )
 *  4 - 이메일 입력 형식 검사
 *  5 - 패스워드 입력 형식 검사
 *  6 - 변경할 패수어드 입력 형식 검사 ( 패스워드 변경시 사용 )
 *  7 - 패스워드 재확인시 패스워드와 값이 같은지 검사
 *  8 - 핸드폰 번호 입력 형식 검사
 *  9 - 이름 입력 형식 검사
 */


public class EditTextInput {
    private String Red = "#FF0000", Green = "#27AE60";

    // 1 - null or "" 검사
    public static boolean checkNPE(String str) {
        return str == null || str.trim().isEmpty();
    }

    // 2 - 일기 제목 길이 검사
    public static boolean checkTitle(String diaryTitle) {
        int size = diaryTitle.getBytes().length;
        //최대 33자 - 한글기준
        if (size > 100) {
            Log.i("제목 길이 오버 : ", "" + size);
            return true;
        } else
            Log.i("제목 길이 : ", "" + size);
        return false;
    }

    // 3 - 일기 내용 길이 검사 ( 최대 5,592,405자 - 한글기준 )
    public static boolean checkContent(String diaryContent) {
        int size = diaryContent.getBytes().length;
        return size > 16777215;
    }

    // 4 - 이메일 입력 형식 검사
    public void inputEmail(EditText editText, TextView textView) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches()) {
                    textView.setText("");         //에러 메세지 제거
                } else if (EditTextInput.checkNPE(editText.getText().toString())) {
                    textView.setText("필수 정보입니다.");    // 경고 메세지
                    textView.setTextColor(Color.parseColor(Red));
                } else {
                    textView.setText("이메일 형식으로 입력해주세요.");    // 경고 메세지
                    textView.setTextColor(Color.parseColor(Red));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // 5 - 패스워드 입력 형식 검사
    public void inputPassword(EditText editText, TextView textView) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Pattern.matches("^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-zA-Z]).{8,20}$", editText.getText().toString())) {
                    textView.setText("");         //에러 메세지 제거
                } else if (EditTextInput.checkNPE(editText.getText().toString())) {
                    textView.setText("필수 정보입니다.");    // 경고 메세지
                    textView.setTextColor(Color.parseColor(Red));
                } else {
                    textView.setText("8~20자 영문 대 소문자, 숫자, 특수문자를 사용하세요.");    // 경고 메세지
                    textView.setTextColor(Color.parseColor(Red));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // 6 - 변경할 패수어드 입력 형식 검사 ( 패스워드 변경시 사용 )
    public void inputChangePassword(EditText editText, TextView textView, EditText compare) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //에러 메세지 제거
                if (editText.getText().toString().equals(compare.getText().toString())) {
                    textView.setText("기존과 동일합니다. 다르게 입력해주세요.");
                    textView.setTextColor(Color.parseColor(Red));
                } else if (Pattern.matches("^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-zA-Z]).{8,20}$", editText.getText().toString())) {
                    textView.setText("");
                } else if (EditTextInput.checkNPE(editText.getText().toString())) {
                    textView.setText("필수 정보입니다.");    // 경고 메세지
                    textView.setTextColor(Color.parseColor(Red));
                } else {
                    textView.setText("8~20자 영문 대 소문자, 숫자, 특수문자를 사용하세요.");    // 경고 메세지
                    textView.setTextColor(Color.parseColor(Red));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // 7 - 패스워드 재확인시 패스워드와 값이 같은지 검사
    public void inputAgainPassword(EditText editText, TextView textView, EditText compare) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editText.getText().toString().equals(compare.getText().toString())) {
                    textView.setTextColor(Color.parseColor(Green));
                    textView.setText("비밀번호가 일치합니다.");
                } else if (EditTextInput.checkNPE(editText.getText().toString())) {
                    textView.setText("필수 정보입니다.");
                    textView.setTextColor(Color.parseColor(Red));
                } else {
                    textView.setTextColor(Color.parseColor(Red));
                    textView.setText("비밀번호가 일치하지 않습니다.");    // 경고 메세지
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // 8 - 핸드폰 번호 입력 형식 검사
    public void inputPhoneNO(EditText editText, TextView textView) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Pattern.matches("^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$", editText.getText().toString())) {
                    textView.setText("");         //에러 메세지 제거
                } else if (EditTextInput.checkNPE(editText.getText().toString())) {
                    textView.setText("필수 정보입니다.");    // 경고 메세지
                    textView.setTextColor(Color.parseColor(Red));
                } else {
                    textView.setText("핸드폰번호 형식에 맞게 입력해주세요.");    // 경고 메세지
                    textView.setTextColor(Color.parseColor(Red));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // 9 - 이름 입력 형식 검사
    public void inputName(EditText editText, TextView textView) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Pattern.matches("^[가-힣]{2,4}|[a-zA-Z]{2,20}", editText.getText().toString())) {
                    textView.setText("");         //에러 메세지 제거
                } else if (EditTextInput.checkNPE(editText.getText().toString())) {
                    textView.setText("필수 정보입니다.");    // 경고 메세지
                    textView.setTextColor(Color.parseColor(Red));
                } else {
                    textView.setText("한글(2~4)과 영문자(2~20)만 입력해주세요.");    // 경고 메세지
                    textView.setTextColor(Color.parseColor(Red));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}