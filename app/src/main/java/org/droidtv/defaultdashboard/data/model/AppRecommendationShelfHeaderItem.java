package org.droidtv.defaultdashboard.data.model;

import android.graphics.drawable.Drawable;

public class AppRecommendationShelfHeaderItem extends ShelfHeaderItem {

    private String mPreviewProgramsTitle;
    private String mPackageName;
    private Integer mChannelID;

    public AppRecommendationShelfHeaderItem(String title, Drawable drawable, String previewProgramsTitle, String packageName ,Integer channelId) {
        super(title, drawable);
        mPreviewProgramsTitle = previewProgramsTitle;
        mPackageName = packageName;
        mChannelID = channelId;
    }

    public String getPreviewProgramsTitle() {
        return mPreviewProgramsTitle;
    }

    public String getPackageName(){
        return mPackageName;
    }

    public Integer getChannelID(){return mChannelID;}
}
