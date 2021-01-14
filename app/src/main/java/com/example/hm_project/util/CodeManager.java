package com.example.hm_project.util;

import org.json.JSONObject;
/**
 * 상황별 이벤트 코드들 모음
 */
public class CodeManager {

    //Common Error CODE
    public static final int NPEWrong = 1; //String Null일 경우
    public static final int JsonParsing_Error = 2;

    //Network Connection Error CODE
    public static final int  NewtWork_Error = 10;

    //Activity Error CODE
    //Calendar Activity 20~29
    public static final int  WriteDiaryOver = 20; //일기 3개 초과 할 경우

    //WriteDiary Activity 30~39
    public static final int  TitleNull = 30; //String Null일 경우
    public static final int  TitleOver = 31; //String 크기가 너무 클 경우
    public static final int  ContentNull = 32; //String Null일 경우
    public static final int  ContentOver = 33; //String 크기가 너무 클 경우
    public static final int  WriteDiaryTotalOver = 34; //일기 기록 전체 1100개 초과 할 경우
    public static final int  MissImage = 35; //이미지 못 가져 왔을 경우
    public static final int  ImageMaxSizeOver = 36; //이미지가 너무 클 경우
    public static final int  NotifyOver = 36; //알림 최대 저장 갯수 100개 초과 할 경우
    public static final int DetailNotRead = 38; //상세 페이지 정보 못 읽었을 경우
    public static final int DeleteFail = 39; //일기 삭제 못했을 경우

    //MyPage Activity 40~49
    public static final int MyPageError = 40; //마이페이지 정보를 못 읽었을 경우
    public  static final int  UpdateProfilePhotoError =41; //마이페이지 프로필 사진 변경 실패;
    public static final int  HairUpdateError = 42; //마이페이지 헤어 셋팅 저장 실패
    public static final int  HairQueryError = 43; //마이페이지 헤어 조회 실패

    // HTTP Connection Error CODE
    public static final int FileNotFoundException = 404;  //HTTP_404
    public static final int HTTP_500 = 500;

    //HttpUrlConnection CODE   //실패
    public static final int UnknownError = 999;
    public static final int Exception = 1000;
    public static final int ConnectionException = 1001;
    public static final int SocketTimeoutException = 1002;
    public static final int MalformedURLException = 1003;
    public static final int UnsupportedEncodingException = 1004;
    public static final int ProtocolException = 1005;
    public static final int IOException = 1006;
                            //성공 SY-2000
    public static final int ConnectionSuccess = 2000;
    public static final String Success = "SY_2000";

    //API응답 코드
    public static final int DBTransactionException = 2000; //실패 서버  DB 오류  // DB_0303
    public static final int LargeImageDataException =2001; //사진 크기가 너무 클 경우 //CA_0005
    public static final int IncorrectURL = 2002; //서버에서 받은 프로필 사진 주소가 올바르지 않을 경우 //MP_0001
    public static final int SendParameterException = 2003; //파라메터 오류 // SY_0002

    //API 응답 코드에 따라 Code를 검색하고 CodeManager에 맞게 대칭해주고 값을 반환해준다.
    public static int CodeCheck(String result){
        JSONObject json = null; //String to JSON
        int resultCode=0;
       // String message;
        try {
            json = new JSONObject(result);
            String code = json.getString("code");  //JSON Parsing Key: code
            // message = json.getString("message"); // Json Parsing Key: messege

            if(code.equals("SY_2000")){
                resultCode= ConnectionSuccess;
            }else if(code.equals("CA_0001")){
                resultCode= TitleOver;
            }else if(code.equals("CA_0002")){
                resultCode= ContentOver;
            }else if(code.equals("CA_0003")){
                resultCode= WriteDiaryOver;
            }else if(code.equals("CA_0004")){
                resultCode= WriteDiaryTotalOver;
            }else if(code.equals("CA_0005")){
                resultCode= ImageMaxSizeOver;
            }else if(code.equals("CA_0006")){
                resultCode= NotifyOver;
            } else if(code.equals("MP_0001")){
                resultCode= ConnectionSuccess;
            }else if(code.equals("MP_0002")){
                resultCode= ConnectionSuccess;
            }else if(code.equals("MP_0003")){
                resultCode= ConnectionSuccess;
            }else if(code.equals("DB_0303")){
                resultCode= ConnectionSuccess;
            }else if(code.equals("DBTransactionException")){
                resultCode= ConnectionSuccess;
            }else if(code.equals("SY_0002")){
                resultCode= SendParameterException;
            }else if(code.equals("HTTP_500")){
                resultCode= HTTP_500;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultCode;
    }
}
