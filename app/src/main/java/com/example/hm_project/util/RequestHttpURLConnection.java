package com.example.hm_project.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.hm_project.etc.PopupActivity;
import com.example.hm_project.view.activity.CalendarActivity;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.crashlytics.internal.CrashlyticsNativeComponent;
import com.google.firebase.crashlytics.internal.common.CrashlyticsCore;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/***
 * 서버와 직접적으로 연겨하는 곳이며
 * Get 방식과 Post 방식이 있고
 * Responser 값을 받아 이후 처리함
 *
 */
public class RequestHttpURLConnection {
    URL url=null;
    String API_NAME=null;
    String json = null;
    HttpURLConnection connection = null;
    String resultCode; //결과 값
    InputStream inputStream =null;
    public String requestGet(String _url, String API_NAME){
        try {
            this.url = new URL(_url);
            this.API_NAME=API_NAME;
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET"); //전송방식
            connection.setDoInput(true);        //데이터를 읽어올지 설정
            connection.setChunkedStreamingMode(4096); //4k body의 길이를 모를 때 쓴다.
            //setFixedLengthStreamingMode() : body의 길이를 미리 알고있을때 쓴다.
            connection.setReadTimeout(1000);
            connection.setConnectTimeout(1000);
            int responseStatusCode = connection.getResponseCode();
            Log.i("Network_TASK", "GET response code - " + responseStatusCode);

            if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                resultCode=CodeManager.Success;
                // 정상적인 응답 데이터
                inputStream = connection.getInputStream();
            } else {
                //에러 발생
                inputStream = connection.getErrorStream();
            }

            StringBuilder sb = new StringBuilder();
            InputStream is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8),connection.getContentLength());
            String result;
            while((result = br.readLine())!=null ){
                if((result.equals("<!DOCTYPE html>")==true))
                {
                    break;
                }else
                    sb.append(result+"\n");
            }
            result = sb.toString();
            System.out.println(result);
            JsonParser jp= HM_Singleton.getInstance(new JsonParser());
            if(API_NAME.equals("MIF-Calendar-001")){
                resultCode =  jp.MIF_Calendar_001(result); //인터페이스 MIF_Calendar_001 JSON 파싱 캘린더 기록 불러오기
            }else if(API_NAME.equals("MIF-Calendar-002")){
               resultCode = jp.MIF_Calendar_002(result); //인터페이스 MIF_Calendar_002 JSON 파싱 상세 일기 읽기
            }else if(API_NAME.equals("MIF-MyPage-001")){
               resultCode = jp.MIF_MyPage_001(result); //인터페이스 "MIF-MyPage-001 JSON 파싱 회원 정보 읽기
            }else if(API_NAME.equals("MIF-MyPage-003")){
               resultCode = jp.MIF_MyPage_003(result); //인터페이스 "MIF-MyPage-001 JSON 파싱 회원 헤어 정보 읽기
            }

        }catch (ConnectException e) {
            FirebaseCrashlytics.getInstance().log("커낵션오류");
            FirebaseCrashlytics.getInstance().recordException(e);
            resultCode="NW-1001";  //사용자가 애플리케이션에서 요청하는 프로세스를 작성할 수 없어서 백엔드 서버에 연결할 수 없다는 것
        }catch (SocketTimeoutException e){
            FirebaseCrashlytics.getInstance().log("연결 시간 오류");
            FirebaseCrashlytics.getInstance().recordException(e);
            resultCode="NW-1002";  //서버와 연결 시간이 길어져서 정상적인 통신이 안됐을 경우
        }catch(FileNotFoundException e){
            FirebaseCrashlytics.getInstance().setCustomKey("테스트3","URL 주소 오류");
            resultCode="HTTP-404"; //주소 URL이 잘못 되어서 파일을 못 찾았을 경우
        }catch (MalformedURLException e) {
            resultCode="NW-1003";   //URL형식이 잘못됐을 경우
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            resultCode="NW-1004";   //문자의 인코딩이 지원 되지 않을 경우
            e.printStackTrace();
        } catch (ProtocolException e) {
            resultCode="NW-1005"; //현재 사용하고 있는 프로토콜에서 (TCP 에러등) 에러 발생할 경우
            e.printStackTrace();
        } catch (IOException e) {
            resultCode="NW-1006";  //읽기 쓰기중 오류 걸렸을 경우
            e.printStackTrace();
        } catch (Exception e) {
            resultCode = "NW-1000";
            e.printStackTrace();
        }finally{
            connection.disconnect(); //연결 종료
        }
        return resultCode;
    }
    //POST
    public String requestPost(String _url , String _json, String api_name){

        try {
            this.url = new URL(_url);
            this.json = _json;
            this.API_NAME = api_name;

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST"); //전송방식
            connection.setDoOutput(true);       //데이터를 쓸 지 설정
            connection.setDoInput(true);        //데이터를 읽어올지 설정
            connection.setChunkedStreamingMode(4096); //4k body의 길이를 모를 때 쓴다.
            //setFixedLengthStreamingMode() : body의 길이를 미리 알고있을때 쓴다.
            connection.setReadTimeout(1000);
            connection.setConnectTimeout(2000);

            //POST 방식 데이터 보내기
            String sendMsg = "json=" + java.net.URLEncoder.encode(json);

            OutputStreamWriter wr= new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
            wr.write(sendMsg);
            wr.flush();
            wr.close();

            int responseStatusCode = connection.getResponseCode();
            Log.i("Network_TASK", "Post response code - " + responseStatusCode);
            if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                resultCode=CodeManager.Success;
                // 정상적인 응답 데이터
                inputStream = connection.getInputStream();
            } else {
                //에러 발생
                inputStream = connection.getErrorStream();
            }

            StringBuilder sb = new StringBuilder();
            InputStream is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8),connection.getContentLength());
            String result;
            while((result = br.readLine())!=null ){
                if((result.equals("<!DOCTYPE html>")==true))
                {
                    break;
                }else
                    sb.append(result+"\n");
            }
            result = sb.toString();
            System.out.println(result);
            JsonParser jp= HM_Singleton.getInstance(new JsonParser());
            if(API_NAME.equals("MIF-Calendar-005")){
                resultCode = jp.MIF_Calendar_005(result); //인터페이스 MIF_Calendar_005 JSON 파싱 캘린더 기록 불러오기
             }else if(API_NAME.equals("MIF-MyPage-002")){
                resultCode = jp.MIF_MyPage_002(result);
            }

        }catch (ConnectException e) {

            FirebaseCrashlytics.getInstance().log("커낵션오류");
            FirebaseCrashlytics.getInstance().recordException(e);
            resultCode="NW-1001";  //사용자가 애플리케이션에서 요청하는 프로세스를 작성할 수 없어서 백엔드 서버에 연결할 수 없다는 것
        }catch (SocketTimeoutException e){

            FirebaseCrashlytics.getInstance().log("연결 시간 오류");
            FirebaseCrashlytics.getInstance().recordException(e);
            resultCode="NW-1002";  //서버와 연결 시간이 길어져서 정상적인 통신이 안됐을 경우
        }catch(FileNotFoundException e){
            FirebaseCrashlytics.getInstance().setCustomKey("테스트3","URL 주소 오류");
            resultCode="HTTP-404"; //주소 URL이 잘못 되어서 파일을 못 찾았을 경우
        }catch (MalformedURLException e) {
            resultCode="NW-1003";   //URL형식이 잘못됐을 경우
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            resultCode="NW-1004";   //문자의 인코딩이 지원 되지 않을 경우
            e.printStackTrace();
        } catch (ProtocolException e) {
            resultCode="NW-1005"; //현재 사용하고 있는 프로토콜에서 (TCP 에러등) 에러 발생할 경우
            e.printStackTrace();
        } catch (IOException e) {
            resultCode="NW-1006";  //읽기 쓰기중 오류 걸렸을 경우
            e.printStackTrace();
        } catch (Exception e) {
            resultCode = "NW-1000";
            e.printStackTrace();
        }finally{
            connection.disconnect(); //연결 종료
        }
        return resultCode;
    }
}


