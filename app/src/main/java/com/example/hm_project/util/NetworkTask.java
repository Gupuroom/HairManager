package com.example.hm_project.util;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.hm_project.view.activity.CalendarActivity;
import com.example.hm_project.view.activity.DetailDiaryActivity;

import org.json.JSONObject;

/**
 * AsyncTask Class
 * GET 방식 POST 방식을 각각 받아서
 * HttpurlConnectrion을 사용한다.
 * 서버연결 결과 값을 code로 구분하여 이후 처리를 진행한다.
 *
 */
public class NetworkTask extends AsyncTask<String,Integer,Integer> {
    String API_NAME; //통신할 API_NAME;
    String url;  //통신할 URL
    String json; // 서버로 보낼 Json Data
    String result ; // HttpUrlConnection 결과 값.
    Handler handler; //액티비티 핸들러
    int message; // Message.what
    int arg;  // Message.arg
    String method;
    //POST 방식
    public NetworkTask(String api_name , String _url, String _json,  Handler _handler){
        this.API_NAME=api_name;
        this.url = _url;
        this.json  = _json; //body에 보낼 JSON데이터
        this.handler=_handler; //결과 처리위한 핸들러
        this.method="POST";
        Log.i("현재 URL",url);
    }
    //GET 방식
    public NetworkTask(String api_name, String _url, Handler _handler ){
        this.API_NAME=api_name;
        this.url = _url;
        this.handler=_handler; //결과 처리위한 핸들러
        this.method="GET";
        Log.i("현재 URL",url);
    }
    //준비
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
    //실행 ~ 종료
    @Override
    protected Integer doInBackground(String... strings) {

        Log.i(API_NAME,"API 연결 시도");
        RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
        //GET
        if(method.equals("GET")){
            result = requestHttpURLConnection.requestGet(url, API_NAME);
        }
        else if(method.equals("POST")){
            result = requestHttpURLConnection.requestPost(url, json, API_NAME);
        }
        //결과 값 return
        int code = checkCode(result);
        return code;

    }
    //실행중 Update
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }
        //update ...ing

    //비정상 종료
    @Override
    protected void onCancelled(Integer s) {
        super.onCancelled(s);
        Log.w("AsyncTask Error","AsyncTask Cancelled");
    }

    //종료 직전
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1) // setAsynchronous 22 API 이상 사용
    @Override
    protected void onPostExecute(Integer code) {
        super.onPostExecute(code);
        Message msg = Message.obtain(handler); //해당 핸들러 액티비티에 메세지 객체를 가져온다.
        msg.setTarget(handler); //핸들러 타겟 설정
        msg.setAsynchronous(true); //비동기로 보내겠다.

        msg.what=message;
        msg.arg1=code;
        msg.obj=API_NAME;

        handler.sendMessage(msg);
    }

    /***
     *  오류 예외사항을 모두 만들었고  Log로 찍히기 때문에 해당 문제가 있을 경우 파악할 수 있고
     *  고객이 예외사항을 디테일하게 알 필요는 없기 때문에
     *  실제 예외사항들이 캘린더, 다이어리, 마이페이지에서 전부 쓰이지는 않는다.
     *
     * @param code
     * @return ComeManager.CODE  Message.arg1 으로 들어가 데이터
     */
    //코드 별 에러 처리 내용
    private int checkCode(String code){

        if(code.equals(CodeManager.Success)) {
            arg = CodeManager.ConnectionSuccess;
            message = 1; //정상 실행

        }else{
            message = 2; //오류 팝업 실행
            //Exception
            if(code.equals("NW-1000")) {
                arg = CodeManager.Exception;
                Log.e(code+"Exception 에러","발견하지 못한 애러입니다. 애러 처리가 필요함.");
            }
            //Connection Exception
            else if(code.equals("NW-1001")) {
                arg = CodeManager.ConnectionException;
                Log.e(code+"서버 연결 거부","포트번호 및 서버 설정 점검하기");
            }
            //SocketTimeoutException
            else if(code.equals("NW-1002")) {
                arg = CodeManager.SocketTimeoutException;
                Log.e(code+"네트워크 에러","서버와 접속 시간이 길어져 데이터를 받지 못함.");
            }
            //MalformedURLException
            else if(code.equals("NW-1003")) {
                arg = CodeManager.MalformedURLException;
                Log.e(code+"URL 에러","URL 형식이 잘못 되었습니다.  점검 및 체크가 필요합니다");
            }
            //UnsupportedEncodingException
            else if(code.equals("NW-1004")) {
                arg = CodeManager.UnsupportedEncodingException;
                Log.e(code+"인코딩 에러","서버와 클라이언트 간의 인코딩을 확인하세요");
            }
            //ProtocolException
            else if(code.equals("NW-1005")) {
                arg = CodeManager.ProtocolException;
                Log.e(code+"TCP 에러","연결 도중 TCP 오류가 발생되었습니다. 서버와 정상적인 연결이 가능한지 확인이 필요합니다.");
            }
            //IOException
            else if(code.equals("NW-1006")) {
                arg = CodeManager.IOException;
                Log.e(code+"IO 에러","서버와 클라이언트간에 IO 체크");
            }
            //FileNotFoundException
            else if(code.equals("HTTP-404")) {
                arg = CodeManager.FileNotFoundException;
                Log.e(code+"404 에러","HTTP 404 오류 파일 체크 및 URL 체크");
            }
            //Exception 아직 못잡아줌.
            else if(code.equals("HTTP-500")) {
                arg = CodeManager.HTTP_500;
                Log.e(code+"500 에러","HTTP 500 서버 소스 검사 및 수정하기");
            }  //DB 트랜잭션중 오류가 났을 경우
            else if(code.equals("DB_0303")) {
                arg = CodeManager.DBTransactionException;
                Log.e(code+"서버 DB 오류","서버 쿼리 및 트랜잭션 검사 필요");
            }
            //확인이 아직 못된 애러들
            else{
                arg = CodeManager.UnknownError;
                Log.e(code+"Exception 에러","발견하지 못한 애러입니다. 애러 처리가 필요함.");
            }
        }
        return arg;
    }
}
