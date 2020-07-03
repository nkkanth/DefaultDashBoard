package org.droidtv.defaultdashboard.data.model;

import androidx.leanback.widget.HeaderItem;

/**
 * Created by nikhil.tk on 28-12-2017.
 */
public class ChapterHeaderItem extends HeaderItem {

    private int mIconResourceId;

    public ChapterHeaderItem(long id, String name, int iconResourceId) {
        super(id, name);
        mIconResourceId = iconResourceId;
    }

    public ChapterHeaderItem(long id, String name) {
        super(id, name);
    }

    public ChapterHeaderItem(String name) {
        super(name);
    }

    public int getIconResourceId() {
        return mIconResourceId;
    }
}
