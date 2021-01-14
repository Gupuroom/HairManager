package com.example.hm_project.data;

/**
 * Notify DTO
 */

public class NotifyData {

    private String NTitle;
    private String NDate;
    private String NTime;
    private String diaryNO;
    private String notifyNO;
    private String Nonoff;
    private boolean NCheck = false;

    public NotifyData(String NTitle, String NDate, String NTime, String diaryNO,String Nonoff,String notifyNO) {
        this.NTitle = NTitle;
        this.NDate = NDate;
        this.NTime = NTime;
        this.diaryNO = diaryNO;
        this.Nonoff = Nonoff;
        this.notifyNO = notifyNO;
    }

    public String getNTitle() {
        return NTitle;
    }

    public void setNTitle(String NTitle) {
        this.NTitle = NTitle;
    }

    public String getNDate() {
        return NDate;
    }

    public void setNDate(String NDate) {
        this.NDate = NDate;
    }

    public String getNTime() {
        return NTime;
    }

    public void setNTime(String NTime) {
        this.NTime = NTime;
    }

    public String getDiaryNO() {
        return diaryNO;
    }

    public void setDiaryNO(String diaryNO) {
        this.diaryNO = diaryNO;
    }

    public String getNotifyNO() {
        return notifyNO;
    }

    public void setNotifyNO(String notifyNO) {
        this.notifyNO = notifyNO;
    }

    public String getNonoff() {
        return Nonoff;
    }

    public void setNonoff(String nonoff) {
        Nonoff = nonoff;
    }

    public boolean isNCheck() {
        return NCheck;
    }

    public void setNCheck(boolean NCheck) {
        this.NCheck = NCheck;
    }
}