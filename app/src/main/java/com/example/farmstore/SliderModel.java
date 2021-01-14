package com.example.farmstore;

public class SliderModel {

    private String banner;
    private String backGroundColor;

    public SliderModel(String banner, String backGroundColor) {
        this.banner = banner;
        this.backGroundColor = backGroundColor;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getBackGroundColor() {
        return backGroundColor;
    }

    public void setBackGroundColor(String backGroundColor) {
        this.backGroundColor = backGroundColor;
    }
}