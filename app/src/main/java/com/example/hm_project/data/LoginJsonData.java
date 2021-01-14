package com.example.hm_project.data;

/**
 * Login ( Server 수신용 ) DTO
 */

public class LoginJsonData {
    String code = "";
    String data1 = "";
    String data2 = "";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getData1() {
        return data1;
    }

    public void setData1(String data1) {
        this.data1 = data1;
    }

    public String getData2() {
        return data2;
    }

    public void setData2(String data2) {
        this.data2 = data2;
    }
}
