package com.example.hm_project.etc;

import android.os.Looper;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * LiveDate 적용한 클래스
 * WriteDiary Activity
 * Date Picker와 Time Picker 로 실시간으로 변하는것을 TextView에 보여줌
 * 1. 작성 날짜 변수
 * 2. 알림 날짜 시간 변수
 *
 */
public  class WriteDiaryViewModel extends ViewModel {
    public MutableLiveData<String> liveDiaryDate;
    public MutableLiveData<String> liveDiaryTitle;
    public MutableLiveData<String> liveDiaryContent;

    public MutableLiveData<String> liveNotifyDateTime;
    public MutableLiveData<Boolean> liveNotifyCheck;
    //일기 제목
    public MutableLiveData<String> getLiveDiaryTitle() {
        if (liveDiaryTitle == null) {
            liveDiaryTitle = new MutableLiveData<String>();
        }
        return liveDiaryTitle;
    }


    //일기 내용
    public MutableLiveData<String> getLiveDiaryContent() {
        if (liveDiaryContent == null) {
            liveDiaryContent = new MutableLiveData<String>();
        }
        return liveDiaryContent;
    }

    //작성날짜
    public MutableLiveData<String> getLiveDiaryDate() {
        if (liveDiaryDate == null) {
            liveDiaryDate = new MutableLiveData<String>();
        }
        return liveDiaryDate;
    }

    //알림 날짜, 시간
    public MutableLiveData<Boolean> getliveNotifyCheck() {
        if (liveNotifyCheck == null) {
            liveNotifyCheck = new MutableLiveData<Boolean>();
        }
        return liveNotifyCheck;
    }

    //알림 날짜, 시간
    public MutableLiveData<String> getliveNotifyDateTime() {
        if (liveNotifyDateTime == null) {
            liveNotifyDateTime = new MutableLiveData<String>();
        }
        return liveNotifyDateTime;
    }
}