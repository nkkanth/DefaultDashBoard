package org.droidtv.defaultdashboard.data.model;

import android.graphics.drawable.Drawable;

import androidx.leanback.widget.HeaderItem;

/**
 * Created by sandeep.kumar on 11/10/2017.
 */

/**
 * A class that abstracts the title and icon/logo for a shelf
 */
public class ShelfHeaderItem extends HeaderItem {

    private Drawable mShelfIconDrawable;

    public ShelfHeaderItem(long id, String title, Drawable drawable) {
        super(id, title);
        mShelfIconDrawable = drawable;
    }

    public ShelfHeaderItem(String title, Drawable drawable) {
        super(title);
        mShelfIconDrawable = drawable;
    }

    /**
     * Returns the drawable object representing the icon for this shelf header
     *
     * @return The drawable object representing the icon for this shelf header
     */
    public Drawable getShelfIconDrawable() {
        return mShelfIconDrawable;
    }

    /**
     * Returns the title for this shelf header
     *
     * @return The title for this shelf header
     */
    public String getTitle() {
        return getName();
    }
}
