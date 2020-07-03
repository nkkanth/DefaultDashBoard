package org.droidtv.defaultdashboard.data.query;

import android.media.tv.TvContract;
import android.net.Uri;

import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.htv.provider.HtvContract.HtvChannelList;

import java.util.ArrayList;

/**
 * Created by sandeep.kumar on 20/11/2017.
 */

public class RadioChannelsQuery implements Query {

    private static final String[] PROJECTION = {
            HtvChannelList._ID,
            HtvChannelList.COLUMN_MAPPED_ID,
            HtvChannelList.COLUMN_INPUT_ID,
            HtvChannelList.COLUMN_MEDIA_TYPE,
            HtvChannelList.COLUMN_SERVICE_TYPE,
            HtvChannelList.COLUMN_TYPE,
            HtvChannelList.COLUMN_DISPLAY_NAME,
            HtvChannelList.COLUMN_DISPLAY_NUMBER,
            HtvChannelList.COLUMN_DESCRIPTION,
            HtvChannelList.COLUMN_LOGO_URL,
            HtvChannelList.COLUMN_FREEPKG,
            HtvChannelList.COLUMN_PAYPKG1,
            HtvChannelList.COLUMN_PAYPKG2
    };

    private static final String SORT_ORDER = " CAST(" + HtvChannelList.COLUMN_DISPLAY_NUMBER + " AS INTEGER) ASC";

    private String mSelection;
    private String[] mSelectionArgs;

    public RadioChannelsQuery() {
        mSelection = HtvChannelList.COLUMN_SERVICE_TYPE + " = ? AND " + HtvChannelList.COLUMN_HIDE + " = ?";
        mSelectionArgs = new String[]{TvContract.Channels.SERVICE_TYPE_AUDIO, "0"};
    }

    public RadioChannelsQuery(boolean tifChannelsEnabled) {
        ArrayList<String> selectionArgs = new ArrayList<>();
        mSelectionArgs = new String[1];

        mSelection = HtvChannelList.COLUMN_SERVICE_TYPE + " = ?";
        selectionArgs.add(TvContract.Channels.SERVICE_TYPE_AUDIO);

        if (!tifChannelsEnabled) {
            mSelection = mSelection.concat(" AND ").concat(HtvChannelList.COLUMN_SERVICE_TYPE).concat(" != ?");
            selectionArgs.add(HtvChannelList.TYPE_TIF);
        }

        // Exclude hidden channels
        mSelection = mSelection.concat(" AND ").concat(HtvChannelList.COLUMN_HIDE).concat(" = ?");
        selectionArgs.add("0");

        mSelectionArgs = selectionArgs.toArray(mSelectionArgs);
        DdbLogUtility.logTVChannelChapter("RadioChannelsQuery","RadioChannelsQuery mSelectionArgs: "+mSelectionArgs);
    }

    @Override
    public Uri getUri() {
        return HtvChannelList.CONTENT_URI;
    }

    @Override
    public String[] getProjection() {
        return PROJECTION;
    }

    @Override
    public String getSelection() {
        return mSelection;
    }

    @Override
    public String[] getSelectionArgs() {
        return mSelectionArgs;
    }

    @Override
    public String getSortOrder() {
        return SORT_ORDER;
    }

    @Override
    public String getGroupBy() {
        return null;
    }
}
