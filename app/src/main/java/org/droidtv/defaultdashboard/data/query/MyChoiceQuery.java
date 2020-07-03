package org.droidtv.defaultdashboard.data.query;

import android.net.Uri;

import org.droidtv.htv.provider.HtvContract;
import org.droidtv.htv.provider.HtvContract.HtvChannelList;

public class MyChoiceQuery implements Query {

    private static final String[] PROJECTION = {
            HtvChannelList._ID,
            HtvChannelList.COLUMN_DISPLAY_NUMBER,
            HtvChannelList.COLUMN_DISPLAY_NAME,
            HtvChannelList.COLUMN_INPUT_ID,
            HtvChannelList.COLUMN_MEDIA_TYPE,
            HtvChannelList.COLUMN_FREEPKG,
            HtvChannelList.COLUMN_PAYPKG1,
            HtvChannelList.COLUMN_PAYPKG2
    };

    private static final String SELECTION = HtvChannelList.COLUMN_MEDIA_TYPE + " = '" + HtvChannelList.TYPE_APPS + "' OR " +
            HtvChannelList.COLUMN_MEDIA_TYPE + " = '" + HtvChannelList.TYPE_GOOGLE_CAST + "' OR " +
            HtvChannelList.COLUMN_MEDIA_TYPE + " = '" + HtvChannelList.TYPE_MEDIABROSWER + "' OR " +
            HtvChannelList.COLUMN_MEDIA_TYPE + " = '" + HtvChannelList.TYPE_SOURCE + "'";

    private static final String SORT_ORDER = HtvChannelList._ID;

    public MyChoiceQuery() {

    }

    @Override
    public Uri getUri() {
        return HtvContract.HtvMyChoiceChannelList.CONTENT_URI;
    }

    @Override
    public String[] getProjection() {
        return PROJECTION;
    }

    @Override
    public String getSelection() {
        return SELECTION;
    }

    @Override
    public String[] getSelectionArgs() {
        return null;
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
