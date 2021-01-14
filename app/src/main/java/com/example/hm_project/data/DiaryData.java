package com.example.hm_project.data;


import java.io.Serializable;

/**
 * 상세 일기, 일기 작성을 위한 DTO
 */
public class DiaryData implements Serializable {
    int userNO;
    int diaryID;
    String diaryDate;
    String diaryTitle;
    String diaryContent;
    boolean notifyCheck;

    String photoOne;
    String photoTwo;
    String photoThree;
    String photoFour;

    int notifyID;
    boolean notifySwitch;
    String notifyDate;
    String notifyTime;

    public DiaryData() {}

    //Set
    public void setUserNO(int userNO) {
        this.userNO = userNO;
    }

    public void setDiaryID(int diaryID) {
        this.diaryID = diaryID;
    }

    public void setDiaryDate(String diaryDate) {
        this.diaryDate = diaryDate;
    }

    public void setDiaryTitle(String diaryTitle) {
        this.diaryTitle = diaryTitle;
    }

    public void setDiaryContent(String diaryContent) {
        this.diaryContent = diaryContent;
    }

    public void setNotifyCheck(boolean notifyCheck) {
        this.notifyCheck = notifyCheck;
    }

    public void setPhotoOne(String photoOne) {
        this.photoOne = photoOne;
    }

    public void setPhotoTwo(String photoTwo) {
        this.photoTwo = photoTwo;
    }

    public void setPhotoThree(String photoThree) {
        this.photoThree = photoThree;
    }

    public void setPhotoFour(String photoFour) {
        this.photoFour = photoFour;
    }

    public void setNotifyDate(String notifyDate) {
        this.notifyDate = notifyDate;
    }

    public void setNotifyTime(String notifyTime) {
        this.notifyTime = notifyTime;
    }

    public void setNotifySwitch(boolean notifySwitch) { this.notifySwitch = notifySwitch; }

    public void setNotifyID(int notifyID) {
        this.notifyID = notifyID;
    }

    //Get
    public int getUserNO() {
        return userNO;
    }
    public int getDiaryID() {
        return diaryID;
    }

    public String getDiaryDate() {
        return diaryDate;
    }

    public String getDiaryTitle() {
        return diaryTitle;
    }

    public String getDiaryContent() {
        return diaryContent;
    }

    public boolean isNotifyCheck() {
        return notifyCheck;
    }

    public String getPhotoOne() {
        return photoOne;
    }

    public String getPhotoTwo() {
        return photoTwo;
    }

    public String getPhotoThree() {
        return photoThree;
    }

    public String getPhotoFour() {
        return photoFour;
    }

    public String getNotifyDate() {
        return notifyDate;
    }

    public String getNotifyTime() {
        return notifyTime;
    }
    public boolean isNotifySwitch() { return notifySwitch; }
    public int getNotifyID() {
        return notifyID;
    }


}