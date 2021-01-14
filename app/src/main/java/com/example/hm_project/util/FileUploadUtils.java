package com.example.hm_project.util;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.hm_project.data.APIManager;
import com.example.hm_project.view.activity.CalendarActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/***
 *  일기 작성시 수정시 OKHTTP3 으로 이미지 전송 , json 데이터 전송
 *  Multipart 폼으로 보냄
 *  기능
 *  1. sendServer 일기 작성,수정시 이미지가 있을 경우 서버로 이미지와 함께 데이터를 보낼 경우
 *  2. noImageSendServer 일기 작성,수정시 이미지가 없을 경우 서버로 데이터만 보낼 경우
 *  3. changeProfilePhoto  회원 프로필 정보를 이미지와 데이터를 같이 보낼 경우
 */
public class FileUploadUtils {
    private int code = 0;
    Handler handler; //Write 와 MyPage
    Message msg;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public FileUploadUtils(Handler mhandler){
        this.handler = mhandler;
        this.msg = Message.obtain(handler); //해당 핸들러 액티비티에 메세지 객체를 가져온다.
        msg.setTarget(handler); //핸들러 타겟 설정
        msg.setAsynchronous(true); //비동기로 보내겠다.
    }

    /**
     * 사진이 선택 되어 서버로 넘길 데이터가 있을 때 사용하는 메소드
     * @param paths 사진 URI 저장되어 있음
     * @param json 사진 외에 서버로 넘길 데이터
     * @param update 일기 수정 true false
     */
    public void sendServer(List<String> paths, String json, boolean update) {
        RequestBody requestBody = null;
        List<File> imageFiles = new ArrayList<>();

        //NPE 방지
        if (paths != null) {
            for (int i = 0; i < paths.size(); i++) {
                File file = new File(paths.get(i));
                imageFiles.add(file);

                if (i == (paths.size() - 1))
                    switch (i) {
                        //사진이 1개 선택 되었을 경우
                        case 0:
                          //  Log.i("사진선택 했음", "사진 선택 1개");
                            requestBody = new MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("file1", imageFiles.get(0).getName(), RequestBody.create(MultipartBody.FORM, imageFiles.get(0)))
                                    .addFormDataPart("json", json)
                                    .build();

                            break;
                        //사진이 2개 선택 되었을 경우
                        case 1:
                          //  Log.i("사진선택 했음", "사진 선택 2개");
                            requestBody = new MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("file1", imageFiles.get(0).getName(), RequestBody.create(MultipartBody.FORM, imageFiles.get(0)))
                                    .addFormDataPart("file2", imageFiles.get(1).getName(), RequestBody.create(MultipartBody.FORM, imageFiles.get(1)))
                                    .addFormDataPart("json", json)
                                    .build();
                            break;
                        //사진이 3개 선택 되었을 경우
                        case 2:
                            //Log.i("사진선택 했음", "사진 선택 3개");
                            requestBody = new MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("file1", imageFiles.get(0).getName(), RequestBody.create(MultipartBody.FORM, imageFiles.get(0)))
                                    .addFormDataPart("file2", imageFiles.get(1).getName(), RequestBody.create(MultipartBody.FORM, imageFiles.get(1)))
                                    .addFormDataPart("file3", imageFiles.get(2).getName(), RequestBody.create(MultipartBody.FORM, imageFiles.get(2)))
                                    .addFormDataPart("json", json)
                                    .build();
                            break;
                        //사진이 4개 선택 되었을 경우
                        case 3:
                           // Log.i("사진선택 했음", "사진 선택 4개");
                            requestBody = new MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("file1", imageFiles.get(0).getName(), RequestBody.create(MultipartBody.FORM, imageFiles.get(0)))
                                    .addFormDataPart("file2", imageFiles.get(1).getName(), RequestBody.create(MultipartBody.FORM, imageFiles.get(1)))
                                    .addFormDataPart("file3", imageFiles.get(2).getName(), RequestBody.create(MultipartBody.FORM, imageFiles.get(2)))
                                    .addFormDataPart("file4", imageFiles.get(3).getName(), RequestBody.create(MultipartBody.FORM, imageFiles.get(3)))
                                    .addFormDataPart("json", json)
                                    .build();
                            break;
                    }
            }
        }
        //선택된 사진이 없을 때 -- 실행이 안되는 코드 ---혹시 모르는 예외가 있을 경우로 인해 있는 코드
        else {
            Log.i("사진선택 X", "고른 사진이 없습니다.");
            requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("json", json)
                    .build();
        }

        // 보낼 데이터 양식 설정
        Request request;
        //일기 수정시
        if(update){
            request = new Request.Builder().url(APIManager.Calendar004_URL) // Server URL 은 본인 IP를 입력
                    .post(requestBody)
                    .build();
        }
        //일기 작성시
        else{
            request = new Request.Builder().url(APIManager.Calendar003_URL) // Server URL 은 본인 IP를 입력
                    .post(requestBody)
                    .build();
        }

        // okHttpClient 생성
        OkHttpClient client = new OkHttpClient();
        //서버에 데이터 보내기
        client.newCall(request).enqueue(new Callback() {
            //전송 성공 할 경우
            @RequiresApi(api = Build.VERSION_CODES.N) //Math.toIntExact
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                StringBuilder sb = new StringBuilder();
                InputStream is = response.body().byteStream();
                int size = Math.toIntExact(response.body().contentLength()); //Long -> int  RequiresApi Build.VERSION_CODES.N
                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8), size);
                String result;
                while ((result = br.readLine()) != null) {
                    if ((result.equals("<!DOCTYPE html>") == true) || result.matches(".*DOCTYPE*." ) || result.matches(".*doctype*.")) {
                        break;
                    } else
                        sb.append(result + "\n");
                }
                result = sb.toString();
                Log.i("서버 응답 : ", result);

                //서버 응답 Json 파싱
                code = CodeManager.CodeCheck(result);
                //서버 응답 정상일 경우
                if(code==CodeManager.ConnectionSuccess){
                    msg = Message.obtain(handler);
                    msg.what=1;
                    handler.sendMessage(msg);
                }
                //일기 작성 실패
                else {
                    msg = Message.obtain(handler);
                    msg.what=3;
                    msg.arg1 = code; //실패 코드를 보낸다.
                    Log.i("코드 ",""+code);
                    handler.sendMessage(msg);
                }

            }

            //전송 실패 할 경우
            @Override
            public void onFailure(Call call, java.io.IOException e) {
                Log.d("일기작성", "서버 접속 실패요");
                code = CodeManager.ConnectionException;
                msg = Message.obtain(handler);
                msg.what=2;
                handler.sendMessage(msg);
                e.printStackTrace();
            }
        });
    }

    /**
     * 사진 없이 데이터만 넘기는 메소드
     * @param json 사진 외에 서버로 넘길 데이터
     * @param update 일기 수정 true false
     */
    //이미지 없이 일기 작성을 할 경우 메소드
    public void noImageSendServer(String json, boolean update) {
        //선택된 사진이 없을 때
        RequestBody requestBody = null;
        Log.i("사진선택 X", "고른 사진이 없습니다.");
        requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("json", json)
                .build();

        // 보낼 데이터 양식 설정
        Request request;
        //일기 수정시
        if(update){
            request = new Request.Builder().url(APIManager.Calendar004_URL) // Server URL 은 본인 IP를 입력
                    .post(requestBody)
                    .build();
        }
        //일기 작성시
        else{
            request = new Request.Builder().url(APIManager.Calendar003_URL) // Server URL 은 본인 IP를 입력
                    .post(requestBody)
                    .build();
        }

        // okHttpClient 생성
        OkHttpClient client = new OkHttpClient();
        //서버에 데이터 보내기
        client.newCall(request).enqueue(new Callback() {
            //전송 성공 할 경우
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                StringBuilder sb = new StringBuilder();
                InputStream is = response.body().byteStream();
                int size = Math.toIntExact(response.body().contentLength()); //Long -> int
                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8), size);
                String result;
                while ((result = br.readLine()) != null) {
                    if ((result.equals("<!DOCTYPE html>") == true) || result.matches(".*DOCTYPE*.") || result.matches(".*doctype*.")) {
                        break;
                    } else
                        sb.append(result + "\n");
                }
                result = sb.toString();
                Log.i("서버 응답 : ", result);
                //서버 응답 Json 파싱
                code = CodeManager.CodeCheck(result);
                //서버 응답 정상일 경우
                if(code==CodeManager.ConnectionSuccess){
                    msg = Message.obtain(handler);
                    msg.what=1;
                    msg.arg1 = code;
                    handler.sendMessage(msg);
                }
                //일기 기록 3개 넘었을 경우 일기 작성 실패
                else {
                    msg = Message.obtain(handler);
                    msg.what = 3;
                    msg.arg1 = code;
                    handler.sendMessage(msg);
                }
            }

            //전송 실패 할 경우
            @Override
            public void onFailure(Call call, java.io.IOException e) {
                Log.d("일기작성", "서버 접속 실패요");
                code = CodeManager.ConnectionException;
                msg = Message.obtain(handler);
                msg.what=2;
                handler.sendMessage(msg);
                e.printStackTrace();
            }
        });

    }

    // 마이페이지 프로필 사진 수정 할 경우
    public void changeProfilePhoto(String path, String json){
        RequestBody requestBody = null;
        File userProfilePhoto = new File(path);

        requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("userProfilePhoto", userProfilePhoto.getName(), RequestBody.create(MultipartBody.FORM, userProfilePhoto))
                .addFormDataPart("json", json)
                .build();

        // 보낼 데이터 양식 설정
        Request request;
        //일기 수정시

        request = new Request.Builder().url(APIManager.MyPage_008_URL) // Server URL 은 본인 IP를 입력
                .post(requestBody)
                .build();
        // okHttpClient 생성
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                StringBuilder sb = new StringBuilder();
                InputStream is = response.body().byteStream();
                int size = Math.toIntExact(response.body().contentLength()); //Long -> int
                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8), size);
                String result;
                while ((result = br.readLine()) != null) {
                    if ((result.equals("<!DOCTYPE html>") == true) || result.matches(".*DOCTYPE*.")) {
                        break;
                    } else
                        sb.append(result + "\n");
                }
                result = sb.toString();
                Log.i("서버 응답 : ", result);
                //서버 응답 Json 파싱
                if(result!=null){
                    code = CodeManager.CodeCheck(result);

                    //서버 응답 정상일 경우
                    if(code==CodeManager.ConnectionSuccess){
                        msg = Message.obtain(handler);
                        msg.what=3;
                        msg.obj = path;//사진 path 넘김 : 서버에 사진 저장 후 사진 변경 하기 위해서
                        handler.sendMessage(msg);
                    }
                    //서버 예외처리 났을 경우
                    else{
                        msg = Message.obtain(handler);
                        msg.what = 4;
                        msg.arg1 = code;
                        handler.sendMessage(msg);
                    }
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("프로필 사진 수정", "서버 접속 실패요");
                code = CodeManager.ConnectionException;
                msg = Message.obtain(handler);
                msg.what=4;
                handler.sendMessage(msg);
                e.printStackTrace();
            }

        });
    }
}