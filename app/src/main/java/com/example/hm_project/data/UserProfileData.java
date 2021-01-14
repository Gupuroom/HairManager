package com.example.hm_project.data;

import java.io.Serializable;
/**
 * 유저 프로필 정보를 위한 Data Obejct Class
 */
public class UserProfileData implements Serializable {

    private String userName;
    private String userEmail;
    private String userProfilePhoto;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserProfilePhoto() {
        return userProfilePhoto;
    }

    public void setUserProfilePhoto(String userProfilePhoto) {
        this.userProfilePhoto = userProfilePhoto;
    }



}
