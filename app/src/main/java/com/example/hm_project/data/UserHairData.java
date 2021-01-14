package com.example.hm_project.data;
/**
 * 유저 헤어 정보를 위한 Data Object Class
 */
public class UserHairData {

    private int thinning;
    private int quality;
    private int shape;
    private String hairColor;

    public int getThinning() {
        return thinning;
    }

    public void setThinning(int thinning) {
        this.thinning = thinning;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getShape() {
        return shape;
    }

    public void setShape(int shape) {
        this.shape = shape;
    }

    public String getHairColor() {
        return hairColor;
    }

    public void setHairColor(String hairColor) {
        this.hairColor = hairColor;
    }


}
