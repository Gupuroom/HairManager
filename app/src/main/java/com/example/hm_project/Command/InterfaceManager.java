package com.example.hm_project.Command;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/***
 *  서버와 통신하는 인터페이스 정의
 *  1 - 생성자 ( 통신할 서버 url 입력 받음 )
 *  2 - doInBackground ( 실제 서버와 통신 서버로부터 값을 받아서 반환함 )
 */

public class InterfaceManager extends AsyncTask<String, Void, String> {

    String sendMsg, receiveMsg;
    URL url;

    // 1 - 생성자
    public InterfaceManager(URL url) {
        this.url = url;
    }

    // 2 - doInBackground ( 실제 서버와 통신 서버로부터 값을 받아서 반환함 )
    @Override
    public String doInBackground(String... strings) {

        try {
            String str;
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1000); //서버에 연결되는 Timeout 시간 설정
            conn.setReadTimeout(1000); // InputStream 읽어 오는 Timeout 시간 설정
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");


            sendMsg = "json=" + java.net.URLEncoder.encode(strings[0]);


            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            osw.write(sendMsg);
            osw.flush();

            if (conn.getResponseCode() == conn.HTTP_OK) {
                InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                BufferedReader reader = new BufferedReader(tmp);
                StringBuffer buffer = new StringBuffer();
                while ((str = reader.readLine()) != null) {
                    buffer.append(str);
                }
                receiveMsg = buffer.toString();

                conn.disconnect();

            } else {
                Log.i("통신 결과", conn.getResponseCode() + "에러");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return receiveMsg;
    }
}

