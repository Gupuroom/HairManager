package com.example.hm_project.Command;

import com.example.hm_project.data.GalleryData;
import com.example.hm_project.data.NotifyData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/***
 *  Json 형식으롭 변환 시켜주는 클래스 ( 서버 통신용 )
 *  1 - jsonObjectMaker ( 로그인 , 회원가입 등에서 쓰는 가장 보편적인 Json 형식 )
 *  2 - jsonArrayMaker ( 갤러리에서 삭제할 일기 데이터를 서버로 보낼 때 쓰는 JsonArray 형식 )
 *  3 - jsonNotifyArrayMaker ( 알람 on/off 변경 값을 DB에 반영할 때 쓰는 JsonArray 형식 )
 *  4 - jsonUPObjectMaker ( 패스워드 변경할 때 사용하는 Json 형식 )
 */

public class JsonMaker {

    //  1 - jsonObjectMaker ( 로그인 , 회원가입 등에서 쓰는 가장 보편적인 Json 형식 )
    public static String jsonObjectMaker(String... strings){
        try {
            JSONObject object = new JSONObject(); //JSON오브젝트
            object.put("user_email", strings[0]);
            object.put("user_password", strings[1]);
            object.put("user_name", strings[2]);
            object.put("user_phoneNO", strings[3]);
            object.put("user_birthday", strings[4]);
            object.put("user_sex", strings[5]);
            object.put("user_key", strings[6]);
            object.put("user_profile", strings[7]);
            object.put("user_NO", strings[8]);

            return object.toString();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return "오류가 발생했습니다.";
    }

    // 2 - jsonArrayMaker ( 갤러리에서 삭제할 일기 데이터를 서버로 보낼 때 쓰는 JsonArray 형식 )
    public static String jsonArrayMaker(List<GalleryData> gData){
        try {
            JSONArray array = new JSONArray();
            for(GalleryData galleryData : gData) {
                JSONObject object = new JSONObject(); //JSON오브젝트
                object.put("diaryNO",galleryData.getDiaryNO());
                array.put(object);
            }

            return array.toString();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return "오류가 발생했습니다.";
    }

    // 3 - jsonNotifyArrayMaker ( 알람 on/off 변경 값을 DB에 반영할 때 쓰는 JsonArray 형식 )
    public static String jsonNotifyArrayMaker(List<NotifyData> nData){
        try {
            JSONArray array = new JSONArray();
            for(NotifyData notifyData : nData) {
                JSONObject object = new JSONObject(); //JSON오브젝트
                object.put("notifyNO",notifyData.getNotifyNO());
                object.put("Nonoff",notifyData.getNonoff());
                object.put("diaryNO",notifyData.getDiaryNO());
                array.put(object);
            }

            return array.toString();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return "오류가 발생했습니다.";
    }

    // 4 - jsonUPObjectMaker ( 패스워드 변경할 때 사용하는 Json 형식 )
    public static String jsonUPObjectMaker(String... strings){
        try {
            JSONObject object = new JSONObject(); //JSON오브젝트
            object.put("user_NO", strings[0]);
            object.put("user_password", strings[1]);
            object.put("change_password", strings[2]);
            object.put("key", strings[3]);

            return object.toString();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return "오류가 발생했습니다.";
    }
}
