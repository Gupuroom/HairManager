package com.example.hm_project.etc;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/***
 * 마이페이지 뷰모델
 * 유저 이름과 유저 이메일 라이브데이터
 */
public class MyPageViewModel extends ViewModel {

    public MutableLiveData<String> userName;
    public MutableLiveData<String> userEmail;

    public MutableLiveData<String> getUserName(){
        if(userName  ==null){
            userName = new MutableLiveData<String>();
        }
        return userName;
    }

    public MutableLiveData<String>  getUserEmail(){
        if(userEmail  ==null){
            userEmail = new MutableLiveData<String>();
        }
        return userEmail;
    }
}
