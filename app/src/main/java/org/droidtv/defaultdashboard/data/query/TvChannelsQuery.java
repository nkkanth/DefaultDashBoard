package org.droidtv.defaultdashboard.data.query;

import android.media.tv.TvContract;
import android.net.Uri;

import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.htv.provider.HtvContract.HtvChannelList;

import java.util.ArrayList;

/**
 * Created by sandeep.kumar on 20/11/2017.
 */

public class TvChannelsQuery implements Query {

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

    public TvChannelsQuery() {
        ArrayList<String> selectionArgs = new ArrayList<>();
        mSelectionArgs = new String[1];

        mSelection = "(" + HtvChannelList.COLUMN_MEDIA_TYPE + " = ? OR " +
                HtvChannelList.COLUMN_MEDIA_TYPE + " = ? OR " +
                HtvChannelList.COLUMN_MEDIA_TYPE + " = ? OR " +
                HtvChannelList.COLUMN_MEDIA_TYPE + " = ?)";
        selectionArgs.add(HtvChannelList.TYPE_TUNER);
        selectionArgs.add(HtvChannelList.TYPE_IP_MULTICAST);
        selectionArgs.add(HtvChannelList.TYPE_IP_UNICAST);
        selectionArgs.add(HtvChannelList.TYPE_IP_RTSP_RTP);

        mSelection = mSelection.concat(" AND ").concat(HtvChannelList.COLUMN_SERVICE_TYPE).concat(" = ?");
        selectionArgs.add(TvContract.Channels.SERVICE_TYPE_AUDIO_VIDEO);

        // Exclude hidden channels
        mSelection = mSelection.concat(" AND ").concat(HtvChannelList.COLUMN_HIDE).concat(" = ?");
        selectionArgs.add("0");

        mSelectionArgs = selectionArgs.toArray(mSelectionArgs);
        DdbLogUtility.logTVChannelChapter("TvChannelsQuery","TvChannelsQuery mSelectionArgs: "+mSelectionArgs);
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
