package org.droidtv.defaultdashboard.data.model.moreChapter;

import android.graphics.drawable.Drawable;

import org.droidtv.defaultdashboard.data.model.Action;

/**
 * Created by sandeep.kumar on 24/11/2017.
 */

public abstract class MoreChapterShelfItem {

    private String mChapterShelfItemTitle;
    private Drawable mChapterShelfItemIcon;
    private int mId;

    protected MoreChapterShelfItem(String name, Drawable icon) {
        mId = -1;
        mChapterShelfItemTitle = name;
        mChapterShelfItemIcon = icon;
    }

    protected MoreChapterShelfItem(int id, String name, Drawable icon) {
        mId = id;
        mChapterShelfItemTitle = name;
        mChapterShelfItemIcon = icon;
    }

    public String getName() {
        return mChapterShelfItemTitle;
    }

    public Drawable getIcon() {
        return mChapterShelfItemIcon;
    }

    public int getId() {
        return mId;
    }

    public abstract Action getAction();
}
