package com.example.hm_project.Command;

import android.app.Activity;
import android.widget.TextView;

import com.example.hm_project.view.activity.SignUpActivity;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/***
 *  날짜입력 받을 때 쓰는 클래스 ( datePickerDialog )
 *  1 - setDate ( 날짜 입력 [Gallery] )
 *  2 - setBirthday ( 생년월일 입력 [SignUp] )
 */


public class SetDate {
    Calendar myCalendar = Calendar.getInstance();
    Calendar minDate = Calendar.getInstance();
    Calendar maxDate = Calendar.getInstance();

    Date date = new Date();

    // 1 - setDate ( datePickerDialog 정의 )
    public void setDate(Activity mActivity,TextView textView, TextView tvError) {
        final android.app.DatePickerDialog.OnDateSetListener birthdayPicker = (view, year, month, dayOfMonth) -> {
            view.setSpinnersShown(true);

            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String myFormat = "yyyy-MM-dd";    // 출력형식   2020-09-29
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.KOREA);

            //유저를 위해 화면에 유저가 선택한 날짜 표시
            textView.setText(sdf.format(myCalendar.getTime()));
            // 서버에 전송할 값 저장
            tvError.setText("");
        };
        minDate.set(2000,2-1, 1);
        maxDate.setTime(date);
        maxDate.add(Calendar.YEAR,20);
        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(mActivity, android.R.style.Theme_Holo_Light_Dialog_MinWidth, birthdayPicker, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setCalendarViewShown(false);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTime().getTime());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis()); // 최대값 지정
        datePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        datePickerDialog.show();
    }

    // 2 - setBirthday ( 생년월일 입력받는 것 정의 )
    public void setBirthday(SignUpActivity mActivity, TextView textView, TextView tvError) {
        final android.app.DatePickerDialog.OnDateSetListener birthdayPicker = (view, year, month, dayOfMonth) -> {
            view.setSpinnersShown(true);

            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String myFormat = "yyyy-MM-dd";    // 출력형식   2020-09-29
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.KOREA);

            //유저를 위해 화면에 유저가 선택한 날짜 표시
            textView.setText(sdf.format(myCalendar.getTime()));
            // 서버에 전송할 값 저장
            tvError.setText("");
        };

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(mActivity, android.R.style.Theme_Holo_Light_Dialog_MinWidth, birthdayPicker, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setCalendarViewShown(false);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis()); // 최대값 지정
        datePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        datePickerDialog.show();
    }
}
