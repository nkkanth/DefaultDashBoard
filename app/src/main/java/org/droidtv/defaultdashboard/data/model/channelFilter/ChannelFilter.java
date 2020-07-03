package org.droidtv.defaultdashboard.data.model.channelFilter;

import android.database.Cursor;
import android.graphics.drawable.Drawable;

import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;

/**
 * Created by sandeep.kumar on 20/11/2017.
 */

public abstract class ChannelFilter {

    private int mId;
    private String mName;
    private Cursor mCursor;
    private Drawable mIcon;

    protected ChannelFilter(int id, String name, Drawable icon, Cursor cursor) {
        DdbLogUtility.logTVChannelChapter("ChannelFilter","id = [\" + id + \"], name = [\" + name + \"], icon = [\" + icon + \"], cursor = [\" + cursor + \"]\"");
        mId = id;
        mName = name;
        mIcon = icon;
        mCursor = cursor;
    }

    /**
     * A unique identifier for this filter
     */
    public int getId() {
        return mId;
    }

    /**
     * The name of this channel filter
     */
    public String getName() {
        return mName;
    }

    /**
     * The icon associated with this channel filter
     */
    public Drawable getIcon() {
        return mIcon;
    }

    /**
     * The underlying cursor of channels represented by this filter
     */
    public Cursor getCursor() {
        return mCursor;
    }

    public void setCursor(Cursor cursor) {
        mCursor = cursor;
    }

    /**
     * Returns true if the underlying cursor has any Channel for this filter
     *
     * @return
     */
    public boolean hasChannels() {
        return mCursor != null && !mCursor.isClosed() && mCursor.getCount() > 0;
    }

    /**
     * Close the underlying cursor. Subclasses can implement additional logic to take care of specific clean up tasks
     */
    public void cleanUp() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    /**
     * Returns true if the specified channel filter is a TIF filter
     */
    public static boolean isTifChannelFilter(ChannelFilter filter) {
        return filter != null && filter.getId() == TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_TIF;
    }
}
