package com.example.hm_project.etc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.example.hm_project.R;
import com.example.hm_project.util.CodeManager;

/**
 * Class for Popup
 */

public class PopupActivity extends Activity {

    TextView txtText, txtTitle;
    String title ="";
    String data ="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_activity);

        //UI 객체생성
        txtText = findViewById(R.id.txtText);
        txtTitle = findViewById(R.id.txtTitle);

        //데이터 가져오기
        Intent intent = getIntent();

        //코드 받아오기
        int apiCode = intent.getIntExtra("code", 0);
        //코드만 받을 시
        if (apiCode != 0) {
            apiErrorCode(apiCode);
            if (!data.trim().equals("") || data != null)
                txtText.setText(data);
            if (!title.trim().equals("") || title != null)
                txtTitle.setText(title);
        } //코드 없이 title과 data  받을 경우
        else{
            data = intent.getStringExtra("data");
            title = intent.getStringExtra("title");
            txtText.setText(data);
            txtTitle.setText(title);
        }
    }

    //확인 버튼 클릭
    public void mOnClose(View v) {
        //데이터 전달하기
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        //액티비티(팝업) 닫기
        finish();
    }

    //오류 코드 별 제목과 내용 설정
    private void apiErrorCode(int code){
        //Exception
        if(code== CodeManager.Exception) {
            title = "알수 없는 오류";
            data ="확인되지 못한 오류입니다.";
        }
        //Connection Exception
        else if(code==CodeManager.ConnectionException) {
            title = "연결이 거부되었습니다.";
            data ="문제가 발생되어 서버와 연결이 안됩니다.";
        }
        //SocketTimeoutException
        else if(code==CodeManager.SocketTimeoutException) {
            title = "통신 신호가 약합니다.";
            data ="모바일 데이터 및 와이파이 연결 속도가 느립니다.";
        }
        //MalformedURLException
        else if(code==CodeManager.MalformedURLException) {
            title = "서버 연결이 어렵습니다.";
            data ="문제가 발생되어 서버와 연결이 안됩니다.";
        }
        //UnsupportedEncodingException
        else if(code==CodeManager.UnsupportedEncodingException) {
            title = "오류가 발생되었습니다.";
            data ="데이터를 올바르게 받지 못했습니다.";
        }
        //ProtocolException
        else if(code==CodeManager.ProtocolException) {
            title = "연결 오류";
            data ="연결 도중에 오류가 발생되었습니다.";
        }
        //IOException
        else if(code==CodeManager.IOException) {
            title = "서버와 연결이 어렵습니다.";
            data ="문제가 발생되어 서버와 연결이 안됩니다.";
        }

        /***
         *  API 응답 코드
         */
        //DB_0303은 액티비티 마다 사용자에게 전달하기 쉽게 다른 메세지(기본 실패 메세지) 전달함
        //이미지가 너무 클 경우
        else if(code==CodeManager.LargeImageDataException) {
            title = "사진의 용량이 큽니다.";
            data ="사진의 용량이 커서 서버에 전달되지 못했습니다.";
        }
        //MP_0001
        else if(code==CodeManager.IncorrectURL) {
            title = "사진의 주소가 올바르지 않습니다.";
            data ="사진의 주소가 올바르지 않으므로 사진을 표시하기 어렵습니다.";
        }
        //SY_0002
        else if(code==CodeManager.SendParameterException) {
            title = "데이터 형식이 올바르지 않습니다.";
            data ="데이터를 올바르게 보내지 못하였습니다.";
        }
        /***
         * HTTP Error CODE
         */
        // HTTP- 404 FileNotFoundException
        else if(code==CodeManager.FileNotFoundException) {
            title = "서버에 문제가 있습니다.";
            data ="서버가 정상적으로 작동되지 않습니다.";
        }
        //HTTP-500
        else if(code==CodeManager.HTTP_500) {
            title = "서버에 문제가 있습니다.";
            data ="서버가 정상적으로 작동되지 않습니다.";
        }
        /***
         * Common Error CODE
         */
        //NPE 오류
        else if(code==CodeManager.NPEWrong) {
            title = "공백 오류";
            data ="아무것도 입력하지 않으셨습니다.";
        }

        /***
         * Network Error CODE
         */
        //네트워크 연결 안됨
        else if(code==CodeManager.NewtWork_Error) {
            title = "네트워크 연결 오류";
            data ="네트워크 연결 상태를 확인해주세요.";
        }
        /***
         * Activity Error CODE
         */
        //일기 3개 기록 초과  -Calendar Activity
        else if(code==CodeManager.WriteDiaryOver) {
            title = "일기 기록 초과";
            data ="하루에 일기 3개까지 작성할 수 있습니다.";
        }
        //제목을 아무것도 적지 않았을 경우 -WriteDiary Activity
        else if(code==CodeManager.TitleNull) {
            title = "제목을 입력하세요.";
            data ="제목을 아무것도 입력하지 않으셨습니다.";
        }
        //제목을 너무 많이 적었을 때 Byte Over  -WriteDiary Activity
        else if(code==CodeManager.TitleOver) {
            title = "제목이 너무 깁니다.";
            data ="제목을 33자 이내로 적어주세요."; //33 자 가능 한글 기준
        }
        //내용을 아무것도 적지 않았을 경우 -WriteDiary Activity
        else if(code==CodeManager.ContentNull) {
            title = "내용을 입력하세요.";
            data ="내용을 아무것도 입력하지 않으셨습니다.";
        }
        //제목을 너무 많이 적었을 때 Byte Over  -WriteDiary Activity
        else if(code==CodeManager.ContentOver) {
            title = "내용이 너무 깁니다.";
            data ="내용을 줄여주세요 "; //5,592,405 자 가능 한글 기준
        }
        //일기 기록 전체 1100개가 넘을 경우 -WriteDiary Activity
        else if(code==CodeManager.WriteDiaryTotalOver) {
            title = "일기 최대 저장 갯수 초과";
            data ="회원의 최대 일기 저장 갯수는 1100개입니다.\n작성을 원하시면 일기를 지워주세요. "; //5,592,405 자 가능 한글 기준
        } //일기 기록 전체 1100개가 넘을 경우 -WriteDiary Activity
        else if(code==CodeManager.WriteDiaryTotalOver) {
            title = "일기 최대 저장 갯수 초과";
            data ="회원의 최대 일기 저장 갯수는 1100개입니다.\n작성을 원하시면 일기를 지워주세요. "; //5,592,405 자 가능 한글 기준
        }
        //알림 총저장 갯수 100개가 넘을 경우 -WriteDiary Activity
        else if(code==CodeManager.NotifyOver) {
            title = "알림 저장 갯수 초과";
            data ="알림 저장 총 저장 갯수100개가 넘습니다.\n작성을 원하시면 알림을 지워주세요."; //
        }
        //상세 일기 내용을 불러오지 못했을 경우 -- DetailDairy Activity
        else if(code==CodeManager.DetailNotRead){
            title = "일기 내용을 불러오지 못함";
            data ="기록한 일기 내용들을 제대로 불러오지 못했습니다.";
        }
        //일기 삭제가 안 됐을 경우 -- DetailDairy Activity
        else if(code==CodeManager.DetailNotRead){
            title = "일기 삭제 실패";
            data ="일기 삭제를 실패 했습니다.";
        }
        //회원정보를 불러오지 못 했을 경우 --MyPge Activity
        else if(code==CodeManager.MyPageError){
            title="기본 정보 읽기 실패";
            data="기본 정보를 불러오지 못하였습니다.";
        }
        //회원 프로필 사진을 변경하지 못한 경우. --MyPge Activity
        else if(code==CodeManager.UpdateProfilePhotoError){
            title="프로필 사진 변경 실패";
            data="프로필 사진이 서버에 업데이트 되지 않았습니다.";
        }
        //회원 프로필 사진을 변경하지 못한 경우. --MyPge Activity
        else if(code==CodeManager.HairUpdateError){
            title="헤어 정보 저장 실패";
            data="회원의 헤어 정보가 저장이 되지 않았습니다.";
        }
        //회원 프로필 사진을 변경하지 못한 경우. --MyPge Activity
        else if(code==CodeManager.HairQueryError){
            title="회원 헤어 조회 실패";
            data="회원의 헤어 정보를 불러오지 못하였습니다.";
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        return event.getAction() != MotionEvent.ACTION_OUTSIDE;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }
}