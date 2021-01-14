package com.example.hm_project.util;

import android.annotation.SuppressLint;
import android.util.Log;

import com.example.hm_project.data.DiaryData;
import com.example.hm_project.data.GalleryData;
import com.example.hm_project.data.LoginJsonData;
import com.example.hm_project.data.NotifyData;
import com.example.hm_project.data.UserHairData;
import com.example.hm_project.data.UserProfileData;
import com.example.hm_project.view.activity.CalendarActivity;
import com.kakao.usermgmt.response.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.List;

/***
 * 서버에서 응답 받은 데이터 JSON형식으로 만들어 파싱한 후 데이터 저장하기.
 *
 * JSON 파싱 기능
 * 1. MIF_Calendar_001 캘린더 달 정보  조회 서버 응답 JSON DATA 파싱
 * 2. MIF_Calendar_002 일기 상세 정보 조회 서버 응답 JSON Data 파싱
 * 3. MIF_Calendar_005 일기 삭제 서버 응답 JSON data 파싱
 * 4. MIF_MyPage_001 마이페이지 기본 정보 조회 서버 응답 JSON data 파싱
 * 5. MIF_MyPage_002 회원 헤어 정보 저장 서버 응답 JSOn data 파싱
 * 6. MIF_MyPage_003 회원 헤어 정보 조회 서버 응답 JSON data 파싱
 * 7.
 * get 기능
 * 1. getDiary() 일기 정보 반환
 * 2. getMonths() 달 정보 반환
 * 3. getTitles() 제목들반환
 * 4. getDiaryNOs 일기 번호 반환
 */
public class JsonParser {

    private String[] months;  //캘린더용
    private String[] titles; //캘린더용
    private int[] diaryNOs; //캘린더용

    private DiaryData diaryData; //상세일기 , 일기 작성 용
    private UserProfileData userProfileData = new UserProfileData();
    private UserHairData userHairData = new UserHairData();
    String code = "";
    String message = "";

    //캘린더 기록 가져오기
    public String MIF_Calendar_001(String result) {
        int total = 0;
        String diaryDate = "";
        int diaryNO = 0;
        String title = "";

        try {
            JSONObject json = new JSONObject(result); //String to JSON
            code = json.getString("code");  //JSON Parsing Key: code
            message = json.getString("message"); // Json Parsing Key: messege

            if(!code.equals("DB_0303")){
                JSONObject dataJson = json.getJSONObject("data"); // Json Parsing Key: data
                total = dataJson.getInt("total"); // Json Parsing Key: total
                JSONArray diaryListJson = dataJson.getJSONArray("diaryList"); // Json Parsing Key : diaryList

                //NPE 방지1 배열 값 초기화 시켜주기
                months = new String[diaryListJson.length()];
                titles = new String[diaryListJson.length()];
                diaryNOs = new int[diaryListJson.length()];
                //JSON DATA 파싱 LOG TEST1
                //   System.out.println("code: "+code+"\nmessage: "+message+"\ntotal: "+total);

                // Json Parsing value
                for (int i = 0; i < diaryListJson.length(); i++) {
                    JSONObject diaryJson = diaryListJson.getJSONObject(i);
                    diaryDate = diaryJson.getString("diaryDate");
                    diaryNO = diaryJson.getInt("diaryNO");
                    title = diaryJson.getString("title");
                    //NPE 방지2
                    if (months[i] == null) {
                        months[i] = diaryDate;
                    }
                    if (diaryNOs[i] == 0) {
                        diaryNOs[i] = diaryNO;
                    }
                    if (titles[i] == null) {
                        titles[i] = title;
                    }
                    //JSON DATA 파싱  LOG TEST2
                    // System.out.println("jsonNO :" + i + " DIARYDATE = " + diaryDate + " DIARYNO=" + diaryNO + " title = " + titles[i]);

                }
            }
        } catch (NullPointerException e) {
           e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return code;
    }

    //일기 상세 정보 조회
    public String MIF_Calendar_002(String result) {
        //int diaryID;
        String diaryDate;
        String diaryTitle;
        String diaryContent;
        boolean notifyCheck;
        boolean notifySwitch;

        String photoOne;
        String photoTwo;
        String photoThree;
        String photoFour;

        int notifyID;
        String notifyDate;
        String notifyTime;

        diaryData = new DiaryData();
        JSONObject json = null; //String to JSON
        try {
            json = new JSONObject(result);
            code = json.getString("code");  //JSON Parsing Key: code
            message = json.getString("message"); // Json Parsing Key: messege

            if(!code.equals("DB_0303")){
                JSONObject dataJson = json.getJSONObject("data"); // Json Parsing Key: data
                JSONObject photoListJson = dataJson.getJSONObject("photoList"); // Json Parsing Key : diaryList
                JSONObject notifyListJson = dataJson.getJSONObject("notify"); // Json Parsing Key : diaryList

                //   diaryID =dataJson.getInt("diaryID");
                diaryTitle = dataJson.getString("diaryTitle");
                diaryContent = dataJson.getString("diaryContent");
                diaryDate = dataJson.getString("diaryDate");
                notifyCheck = dataJson.getBoolean("notifyCheck");

                photoOne = photoListJson.getString("photoOne");
                photoTwo = photoListJson.getString("photoTwo");
                photoThree = photoListJson.getString("photoThree");
                photoFour = photoListJson.getString("photoFour");

                // diaryData.setDiaryID(diaryID);
                diaryData.setDiaryTitle(diaryTitle);
                diaryData.setDiaryContent(diaryContent);
                diaryData.setDiaryDate(diaryDate);
                diaryData.setNotifyCheck(notifyCheck);
                diaryData.setPhotoOne(photoOne);
                diaryData.setPhotoTwo(photoTwo);
                diaryData.setPhotoThree(photoThree);
                diaryData.setPhotoFour(photoFour);
                //알림
                if (notifyCheck) {
                    notifySwitch = notifyListJson.getBoolean("notifySwitch");
                    notifyID = notifyListJson.getInt("notifyID");
                    notifyDate = notifyListJson.getString("notifyDate");
                    notifyTime = notifyListJson.getString("notifyTime");
                    //NPE 오류 방지
                    if (notifyTime != null) {
                        String[] timeSplit = notifyTime.split(":"); // mm:ss:mm
                        if (timeSplit.length > 1) // ArrayIndexOutOfBoundsException  방지
                            notifyTime = timeSplit[0] + ":" + timeSplit[1];
                    }
                    diaryData.setNotifyID(notifyID);
                    diaryData.setNotifySwitch(notifySwitch);
                    diaryData.setNotifyDate(notifyDate);
                    diaryData.setNotifyTime(notifyTime);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return code;
    }

    //일기 삭제 응답
    public String MIF_Calendar_005(String result) {
        JSONObject json = null; //String to JSON
        try {
            json = new JSONObject(result);
            code = json.getString("code");  //JSON Parsing Key: code
            message = json.getString("message"); // Json Parsing Key: messege
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    //마이페이지 기본 정보 받아오기
    public String MIF_MyPage_001(String result) {
        String userName;
        String userEmail;
        String userProfilePhoto;
        try {
            JSONObject json = new JSONObject(result);
            code = json.getString("code");  //JSON Parsing Key: code
            message = json.getString("message"); // Json Parsing Key: messege

            if(!code.equals("DB_0303")){
                JSONObject dataJson = json.getJSONObject("data"); // Json Parsing Key: data

                userName = dataJson.getString("userName");
                userEmail = dataJson.getString("userEmail");
                userProfilePhoto = dataJson.getString("userProfilePhoto");

                if (userName != null) {
                    userProfileData.setUserName(userName);
                }

                userProfileData.setUserEmail(userEmail);
                userProfileData.setUserProfilePhoto(userProfilePhoto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }
    // 회원 헤어 정보 설정 응답 파싱
    public String MIF_MyPage_002(String result){
        JSONObject json = null; //String to JSON
        try {
            json = new JSONObject(result);
            code = json.getString("code");  //JSON Parsing Key: code
            message = json.getString("message"); // Json Parsing Key: messege

        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    //회원 헤어 조회
    public String MIF_MyPage_003(String result){
        JSONObject json = null; //String to JSON
        try {
            json = new JSONObject(result);
            code = json.getString("code");  //JSON Parsing Key: code
            message = json.getString("message"); // Json Parsing Key: messege

            JSONObject dataJson = json.getJSONObject("data"); // Json Parsing Key: data

            if(!code.equals("DB_0303")){
                int thinning = dataJson.getInt("thinning");
                int quality = dataJson.getInt("quality");
                int shape = dataJson.getInt("shape");
                String hairColor = dataJson.getString("hairColor");

                userHairData.setThinning(thinning);
                userHairData.setQuality(quality);
                userHairData.setShape(shape);
                userHairData.setHairColor(hairColor);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }
    //이번 달 기록된 일기 Class 반환
    public DiaryData getDiary() {
        return diaryData;
    } //일기 정보들 반환
    //이번 달 기록된 일기 Month들 반환
    public String[] getMonths() {
        return months;
    }
    //이번 달 기록된 일기 제목들 반환
    public String[] getTitles() {
        return titles;
    }
    //이번 달 기록된 일기 번호들 반환
    public int[] getDiaryNOs() {
        return diaryNOs;
    }

    //회원 정보 가져오기
    public UserProfileData getUserProfile() {
        return userProfileData;
    }
    //회원 헤어 정보 가져오기
    public UserHairData getUserHairData() {
        return userHairData;
    }

    // 로그인 데이터 파싱 후 반환
    @SuppressLint("LongLogTag")
    public LoginJsonData jsonParsingLogin(String string) {
        LoginJsonData loginJsonData = new LoginJsonData();
        try {
            JSONParser jsonParser = new JSONParser();
            org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) jsonParser.parse(string);

            String code = jsonObject.get("code").toString();
            String userNO = jsonObject.get("user_NO").toString();
            String db_password = jsonObject.get("db_password").toString();

            loginJsonData.setCode(code);
            loginJsonData.setData1(userNO);
            loginJsonData.setData2(db_password);

        } catch (Exception e) {

        }
        return loginJsonData;
    }

    @SuppressLint("LongLogTag")
    public LoginJsonData jsonParsingAutoLogin(String string) {
        LoginJsonData loginJsonData = new LoginJsonData();
        try {
            JSONParser jsonParser = new JSONParser();
            org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) jsonParser.parse(string);

            String code = jsonObject.get("code").toString();
            String userNO = jsonObject.get("user_NO").toString();

            loginJsonData.setCode(code);
            loginJsonData.setData1(userNO);

        } catch (Exception e) {

        }
        return loginJsonData;
    }

    // 이메일 찾기 데이터 파싱 후 반환
    public LoginJsonData jsonParsingFindEmail(String string) {
        LoginJsonData loginJsonData = new LoginJsonData();
        try {
            JSONParser jsonParser = new JSONParser();
            org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) jsonParser.parse(string);

            String code = jsonObject.get("code").toString();
            String user_email = jsonObject.get("user_email").toString();


            loginJsonData.setCode(code);
            loginJsonData.setData1(user_email);
        } catch (Exception e) {

        }
        return loginJsonData;
    }

    // 비밀번호 찾기 데이터 파싱 후 반환
    public LoginJsonData jsonParsingFindPassword(String string) {
        LoginJsonData loginJsonData = new LoginJsonData();
        try {
            JSONParser jsonParser = new JSONParser();
            org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) jsonParser.parse(string);

            String code = jsonObject.get("code").toString();
            String tempKey = jsonObject.get("tempkey").toString();
            String tempPassword = jsonObject.get("temppassword").toString();


            loginJsonData.setCode(code);
            loginJsonData.setData1(tempKey);
            loginJsonData.setData2(tempPassword);
        } catch (Exception e) {

        }
        return loginJsonData;
    }

    // 갤러리 데이터 파싱 후 반환
    public List<GalleryData> jsonParsingGallery(String string) {
        ArrayList<GalleryData> Glist = new ArrayList<GalleryData>();
        try {
            // JSONParser를 이용해 Json데이터를 파싱한다.
            JSONParser jsonParser = new JSONParser();
            Object obj = jsonParser.parse(string);
            org.json.simple.JSONArray jsonArray = (org.json.simple.JSONArray) obj;

            // 파싱 과정
            for (int i = 0; i < jsonArray.size(); i++) {
                org.json.simple.JSONObject Gdatas = (org.json.simple.JSONObject) jsonArray.get(i);//인덱스 번호로 접근해서 가져온
                // 페이지 마지막 확인용
                GalleryData Gdata = new GalleryData(Gdatas.get("GPhoto").toString(), Gdatas.get("GTitle").toString(), Gdatas.get("GDate").toString(), Gdatas.get("DiaryNO").toString());
                Glist.add(Gdata);
            }

        } catch (Exception e) {

        }
        return Glist;
    }

    // 알림 데이터 파싱 후 반환
    public List<NotifyData> jsonParsingNotify(String string) {
        ArrayList<NotifyData> Nlist = new ArrayList<NotifyData>();
        try {
            // JSONParser를 이용해 Json데이터를 파싱한다.
            JSONParser jsonParser = new JSONParser();
            Object obj = jsonParser.parse(string);
            org.json.simple.JSONArray jsonArray = (org.json.simple.JSONArray) obj;

            // 파싱 과정
            for (int i = 0; i < jsonArray.size(); i++) {
                org.json.simple.JSONObject Ndatas = (org.json.simple.JSONObject) jsonArray.get(i);//인덱스 번호로 접근해서 가져온
                // 페이지 마지막 확인용
                NotifyData Ndata = new NotifyData(Ndatas.get("NTitle").toString(), Ndatas.get("NDate").toString(),
                        Ndatas.get("NTime").toString(), Ndatas.get("DiaryNO").toString(), Ndatas.get("Nonoff").toString(), Ndatas.get("notifyNO").toString());
                Nlist.add(Ndata);
            }

        } catch (Exception e) {

        }
        return Nlist;
    }

}

