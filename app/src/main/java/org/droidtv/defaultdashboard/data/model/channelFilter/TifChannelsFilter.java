package org.droidtv.defaultdashboard.data.model.channelFilter;

/**
 * Created by sandeep.kumar on 20/11/2017.
 */

import android.database.Cursor;
import android.graphics.drawable.Drawable;

import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;

/**
 * A channel filter that represents channel list from a third-paty TV input service e.g Google Play Movies, Haystack, Pluto TV, etc
 */
public class TifChannelsFilter extends ChannelFilter {

    private String mTifInputId;

    public TifChannelsFilter(String tifInputId, String tifApplicationName, Drawable tifIcon, Cursor cursor) {
        super(TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_TIF, tifApplicationName, tifIcon, cursor);
        mTifInputId = tifInputId;
    }

    /**
     * The input id for this TIF source
     */
    public String getTifInputId() {
        return mTifInputId;
    }
}
