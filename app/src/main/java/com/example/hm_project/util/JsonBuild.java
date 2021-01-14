package com.example.hm_project.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.hm_project.data.DiaryData;
import com.example.hm_project.view.activity.WriteDiaryActivity;
import com.kakao.auth.authorization.AuthorizationResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;

import javax.xml.transform.Result;

/***
 *  서버에 보낼 데이터를 JSON 형식으로 만들어준다.
 *  1.MIF_Calendar_003 일기 작성 데이터 JSON 빌드
 *  2.MIF_Calendar_004 일기 수정 데이터 JSON 빌드
 *  3.MIF_Calendar_005 일기 삭제 데이터 JSON 빌드
 *  4.MIF_MyPage_002 회원 헤어정보 저장 데이터 JSON 빌드
 *  5.MIF_MyPage_008 회원 프로필 사진 수정 데이터 JSON 빌드
 */
public class JsonBuild {
    //일기 작성 API MIF_Calendar_003
    public static String MIF_Calendar_003(DiaryData diaryData){
        //데이터 받기
        int userNO = diaryData.getUserNO();
        String diaryDate = diaryData.getDiaryDate();
        String diaryTitle = diaryData.getDiaryTitle();
        String diaryContent = diaryData.getDiaryContent();
        boolean notifyCheck = diaryData.isNotifyCheck();
        boolean notifySwitch = diaryData.isNotifySwitch();
        String notifyDate = diaryData.getNotifyDate();
        String notifyTime = diaryData.getNotifyTime();

        //Json Format
        JSONObject json = new JSONObject();
        try {
            json.put("userNO",userNO);
            json.put("diaryDate",diaryDate);
            json.put("diaryTitle",diaryTitle);
            json.put("diaryContent",diaryContent);
            json.put("notifyCheck",notifyCheck);

            JSONObject notifyJson = new JSONObject();
            notifyJson.put("notifySwitch",notifyCheck);
            notifyJson.put("notifySwitch",notifySwitch);
            notifyJson.put("notifyDate",notifyDate);
            notifyJson.put("notifyTime",notifyTime);
            json.put("notify",notifyJson);
            Log.i("값은 : ",json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
    //일기 수정시 API  MIF_Calendar_004
    public static String MIF_Calendar_004(DiaryData diaryData) {
        //데이터 받기
        int userNO = diaryData.getUserNO();
        int diaryID = diaryData.getDiaryID();
        String diaryDate = diaryData.getDiaryDate();
        String diaryTitle = diaryData.getDiaryTitle();
        String diaryContent = diaryData.getDiaryContent();
        boolean notifyCheck = diaryData.isNotifyCheck();
        int notifyID = diaryData.getNotifyID();
        boolean notifySwitch = diaryData.isNotifySwitch();
        String notifyDate = diaryData.getNotifyDate();
        String notifyTime = diaryData.getNotifyTime();

        String photoOne = diaryData.getPhotoOne();
        String photoTwo = diaryData.getPhotoTwo();
        String photoThree = diaryData.getPhotoThree();
        String photoFour = diaryData.getPhotoFour();

        //Json Format
        JSONObject json = new JSONObject();
        try {
            json.put("userNO",userNO);
            json.put("diaryID",diaryID);
            json.put("diaryDate",diaryDate);
            json.put("diaryTitle",diaryTitle);
            json.put("diaryContent",diaryContent);
            json.put("notifyCheck",notifyCheck);

            JSONObject notifyJson = new JSONObject();
            if(notifyCheck){
                notifyJson.put("notifyID",notifyID);
                notifyJson.put("notifySwitch",notifySwitch);
                notifyJson.put("notifyDate",notifyDate);
                notifyJson.put("notifyTime",notifyTime);
            }

            JSONObject photoJson = new JSONObject();

            photoJson.put("photoOne",photoOne);
            photoJson.put("photoTwo",photoTwo);
            photoJson.put("photoThree",photoThree);
            photoJson.put("photoFour",photoFour);
            json.put("photoList",photoJson);
            json.put("notify",notifyJson);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
    //일기 삭제 API MIF_Calendar_005
    public static String MIF_Calendar_005(int _userNo, int _diaryID, String _diaryDate) {
        //데이터 받기
        int userNO = _userNo;
        int diaryID = _diaryID;
        String diaryDate = _diaryDate;

        JSONObject json = new JSONObject();
        try {
            json.put("userNO",userNO);
            json.put("diaryID",diaryID);
            json.put("diaryDate",diaryDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  json.toString();
    }
    //회원 헤어 정보 저장 데이터 보내기 API  - 회원 헤어 정보를 JSON형식으로 만들어 준다.
    public static String MIF_MyPage_002(int userNO, int thinningCode, int qualityCode, int shapeCode, String hairColor) {
        JSONObject json = new JSONObject();
        try {
            json.put("userNO",userNO);
            json.put("thinning",thinningCode);
            json.put("quality",qualityCode);
            json.put("shape",shapeCode);
            json.put("hairColor",hairColor);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  json.toString();
    }
    //회원 프로필 사진 수정 -- 유저넘버만 보낸다.
    public static String MIF_MyPage_008(int userNO) {

        JSONObject json = new JSONObject();
        try {
            json.put("userNO",userNO);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  json.toString();
    }


}
