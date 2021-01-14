package com.example.hm_project.data;

/**
 * Gallery DTO
 */

public class GalleryData {
    private String GPhoto;
    private boolean Gcheck = false;
    private String GTitle;
    private String GDate;
    private String DiaryNO;

    public GalleryData(String GPhoto, String GTitle, String GDate,String DiaryNO) {
        this.GPhoto = GPhoto;
        this.GTitle = GTitle;
        this.GDate = GDate;
        this.DiaryNO = DiaryNO;
    }

    public String getGPhoto() {
        return GPhoto;
    }

    public void setGPhoto(String GPhoto) {
        this.GPhoto = GPhoto;
    }

    public boolean isGcheck() {
        return Gcheck;
    }

    public void setGcheck(boolean gcheck) {
        Gcheck = gcheck;
    }

    public String getGTitle() {
        return GTitle;
    }

    public void setGTitle(String GTitle) {
        this.GTitle = GTitle;
    }

    public String getGDate() {
        return GDate;
    }

    public void setGDate(String GDate) {
        this.GDate = GDate;
    }

    public String getDiaryNO() {
        return DiaryNO;
    }

    public void setDiaryNO(String diaryNO) {
        DiaryNO = diaryNO;
    }
}