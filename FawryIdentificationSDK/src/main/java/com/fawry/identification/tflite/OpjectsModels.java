package com.fawry.identification.tflite;

import android.graphics.Bitmap;

public class OpjectsModels {
    private Bitmap FrontIDImage, BackIDImage, CloseEYEImage, OpenEYEImage;
    private String FrontConfig, BackConfig;

    public OpjectsModels() {
    }

    public OpjectsModels(Bitmap frontIDImage, Bitmap backIDImage, Bitmap closeEYEImage, Bitmap openEYEImage, String frontConfig, String backConfig) {
        FrontIDImage = frontIDImage;
        BackIDImage = backIDImage;
        CloseEYEImage = closeEYEImage;
        OpenEYEImage = openEYEImage;
        FrontConfig = frontConfig;
        BackConfig = backConfig;
    }

    public Bitmap getFrontIDImage() {
        return FrontIDImage;
    }

    public void setFrontIDImage(Bitmap frontIDImage) {
        FrontIDImage = frontIDImage;
    }

    public Bitmap getBackIDImage() {
        return BackIDImage;
    }

    public void setBackIDImage(Bitmap backIDImage) {
        BackIDImage = backIDImage;
    }

    public Bitmap getCloseEYEImage() {
        return CloseEYEImage;
    }

    public void setCloseEYEImage(Bitmap closeEYEImage) {
        CloseEYEImage = closeEYEImage;
    }

    public Bitmap getOpenEYEImage() {
        return OpenEYEImage;
    }

    public void setOpenEYEImage(Bitmap openEYEImage) {
        OpenEYEImage = openEYEImage;
    }

    public String getFrontConfig() {
        return FrontConfig;
    }

    public void setFrontConfig(String frontConfig) {
        FrontConfig = frontConfig;
    }

    public String getBackConfig() {
        return BackConfig;
    }

    public void setBackConfig(String backConfig) {
        BackConfig = backConfig;
    }
}
