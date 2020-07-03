package org.droidtv.defaultdashboard.data.model;

import android.graphics.drawable.Drawable;

/**
 * Created by Utam k Bhuwania on 31 May 2019
 */

public class GamesShelfHeaderItem extends ShelfHeaderItem {

    private String mPreviewProgramsTitle;

    public GamesShelfHeaderItem(String title, Drawable drawable, String previewProgramsTitle) {
        super(title, drawable);
        mPreviewProgramsTitle = previewProgramsTitle;
    }

    public String getPreviewProgramsTitle() {
        return mPreviewProgramsTitle;
    }

    public void setPreviewProgramsTitle(String mPreviewProgramsTitle) {
        this.mPreviewProgramsTitle = mPreviewProgramsTitle;
    }
}
