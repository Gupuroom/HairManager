package com.example.hm_project.etc;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.prolificinteractive.materialcalendarview.CalendarDay;

public final class ChangeDate {

    /**
     * 마리아 DB DATE 방식은 ex) 2020-05-05 로 yyyy-mm-dd 형식이지만
     *  Material Calendar 오픈소스는  정수형이기에 yyyy-m-d 형식이 되기도 한다.
     *  따라서 마리아 DB 형식으로 DATE를 변환 시키기 위한 클래스 이며
     *  날짜와 시간을 숫자형 문자형으로 변환해주는 메소드도 들어 있다.
     *
     *  @return CalendarDay format set the Date format to Maria DB
     *
     */

    @NonNull
    public static String set(@Nullable int year, int month, int day ){

        String Year= Integer.toString(year);
        String Month=Integer.toString(month);
        String Day = Integer.toString(day);
        if(month<=9){
            Month= "0"+Month;
        }
        if(day<=9){
            Day="0"+Day;
        }
        Year= Integer.toString(year);
        String date=Year+"-"+Month+"-"+Day;
        return date;
    }
    /**
     *
     * @param hour 시간 int로 받아옴
     * @param minute 분 int로 받아옴
     * @return int to String -> 마리아DB format으로
     *
     */

    @NonNull
    public static String time(@Nullable int hour, int minute ){

        String Hour = Integer.toString(hour);
        String Minute = Integer.toString(minute);

        String clock =Hour+":"+Minute;
        return clock;
    }

    /**
     *
     * @param date  날짜 ex) 2020-10-10
     * @param time  시간 ex) 17:30
     * @return dateTime 뷰로 보여질 String 보냄
     */
    @NonNull
    public static String DateTime(@Nullable String date, String time ){
        String[] dateArray = date.split("-");
        String year= dateArray[0];
        String month= dateArray[1];
        String day= dateArray[2];

        String[] timeArray = time.split(":");
        String hour = timeArray[0];
        String minute = timeArray[1];

        String dateTime = year+"년 "+month+"월 "+day+"일   "+hour+"시 "+minute+"분";

        return dateTime;
    }

    /**
     *
     * @param date 날짜 String으로 받아옴
     * @return String to String -> Year
     *
     */
    @NonNull
    public static String strYear(@Nullable String date ){
        String[] array = date.split("-");
        int year= Integer.parseInt(array[0]);
        int month= Integer.parseInt(array[1]);
        int day= Integer.parseInt(array[2]);

        String YYYY= Integer.toString(year);
        return YYYY;
    }

    /**
     *
     * @param date 날짜 String으로 받아옴
     * @return String to String -> Month
     *
     */
    @NonNull
    public static String strMonth(@Nullable String date ){
        String[] array = date.split("-");
        int year= Integer.parseInt(array[0]);
        int month= Integer.parseInt(array[1]);
        int day= Integer.parseInt(array[2]);

        String MM= Integer.toString(month);
        return MM;
    }

    /**
     *
     * @param date 날짜 String으로 받아옴
     * @return String to String -> Day
     *
     */
    @NonNull
    public static String strDay(@Nullable String date ){
        String[] array = date.split("-");
        int year= Integer.parseInt(array[0]);
        int month= Integer.parseInt(array[1]);
        int day= Integer.parseInt(array[2]);

        String DD= Integer.toString(day);
        return DD;
    }

    /**
     *
     * @param date 날짜 String으로 받아옴
     * @return String to int -> Year
     *
     */
    @NonNull
    public static int Year(@NonNull String date){
        String[] array = date.split("-");
        int Year= Integer.parseInt(array[0]);
        return Year;
    }
    /**
     *
     * @param date 날짜 String으로 받아옴
     * @return String to int -> Month
     *
     */
    @NonNull
    public static int Month(@NonNull String date){
        String[] array = date.split("-");
        int Month= Integer.parseInt(array[1]);
        return Month-1;
    }
    /**
     *
     * @param date 날짜 String으로 받아옴
     * @return String to int -> Day
     *
     */
    @NonNull
    public static int Day(@NonNull String date){
        String[] array = date.split("-");
        int Day= Integer.parseInt(array[2]);
        return Day;
    }
    /**
     *
     * @param date 날짜 String으로 받아옴
     * @return String to int -> Day
     *
     */
    @NonNull
    public static CalendarDay calendarDay(@NonNull String date){
        String[] array = date.split("-");
        int Year= Integer.parseInt(array[0]);
        int Month= Integer.parseInt(array[1]);
        int Day= Integer.parseInt(array[2]);
        CalendarDay calendar = new CalendarDay(Year,Month-1,Day);
        return calendar;
    }
    //시간만
    @NonNull
    public static int Hour(String notifyTime) {
        String[] timeArray = notifyTime.split(":");
        int hour = Integer.parseInt(timeArray[0]);
        int minute = Integer.parseInt(timeArray[1]);

        return hour;
    }
    //분만
    @NonNull
    public static int Minute(String notifyTime) {
        String[] timeArray = notifyTime.split(":");
        int hour = Integer.parseInt(timeArray[0]);
        int minute = Integer.parseInt(timeArray[1]);

        return minute;
    }
}
