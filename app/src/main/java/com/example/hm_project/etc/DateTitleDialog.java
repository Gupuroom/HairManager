package com.example.hm_project.etc;


import android.app.Dialog;
import android.content.Context;

import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;

import com.example.hm_project.R;

import java.util.Calendar;

/***
 * 캘린더 액티비티 타이틀 클릭하였을 때 나오는 Date Dialog 이다.
 * 1. 처음에 보여지는 날짜 셋팅
 * 2. 다이얼로그 설정 - 제목, 레이아웃 등
 * 3. 선택 가능한 날짜 최소 최대 정하기  - 2000년 부터  올해 +20 년 까지로 설정 하였음.
 * 4. !!콜백 메소드 캘린더 타이틀 날짜 클릭시 값 전달 !!
 * 기능
 * 1. Dialog 상속  - Date Change Listener 사용
 */
public class DateTitleDialog extends Dialog {
    int year=0; int month=0; int day=0; //선택 년 월 일
    int currentYear=0; //올해 년도
    Button dateCummit, dateCancle;
    DialogListner dialogListner;

    //콜백 인터페이스
    public interface DialogListner{
        void onPostiveClicked(int _year, int _month, int _day);
    }

    public void setDilalogListener(DialogListner _dialogListner){
        this.dialogListner= _dialogListner;
    }
    //다이얼로그 초기화
    public DateTitleDialog(final Context context, int _year, int _month, int _day, int _currentYear){
        //월, 년, 일
        super(context);
        this.year=_year;
        this.month=_month;
        this.day=_day;
        this.currentYear=_currentYear;

        //context 타겟 지정
        final Dialog dialog = new Dialog(context);

        //다이얼로그 세부 설정
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //다이얼로그 제목 없음
        dialog.setCanceledOnTouchOutside(true); //밖에 터치 Cancel 하기
        dialog.setContentView(R.layout.dialog_claendartitle); //다이얼로그 레이아웃 정하기

        //날짜 최소 최대 설정
        DatePicker datePicker = dialog.findViewById(R.id.vDatePicker);
        Calendar min = Calendar.getInstance();
        Calendar max = Calendar.getInstance();
        min.set(2000,0,1); //최소 2000년 부터 캘린더 조회 일기 작성 가능
        datePicker.setMinDate(min.getTimeInMillis());
        max.set(_currentYear+20,11,31); //최대 올해부터+20년까지 캘린더 조회 일기 작성 가능
        datePicker.setMaxDate(max.getTimeInMillis());

        //데이트 피커 설정
        datePicker.init(_year, _month, _day,mOnDateChangedListener);
        dialog.show(); //데이트 피커 보여주기

        dateCummit= dialog.findViewById(R.id.DateCommit); //확인
        dateCancle= dialog.findViewById(R.id.DateCancel); //취소
        //확인 취소 콜백 메소드
        dateCummit.setOnClickListener (new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                dialogListner.onPostiveClicked(year,month,day); //call 년 월 일 값 전달.
                dialog.dismiss(); //종료
            }
        });
        dateCancle.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel(); //종료
            }
        });
    }
    //데이트 변경할 경우
    DatePicker.OnDateChangedListener mOnDateChangedListener = new DatePicker.OnDateChangedListener(){
        @Override
        public void onDateChanged(DatePicker datePicker, int yy, int mm, int dd) {
            if(yy==0||dd==0){ //변경 안 할 경우
                return;
            }else{
                year = yy;
                month = mm;
                day = dd;
            }
        }
    };
}