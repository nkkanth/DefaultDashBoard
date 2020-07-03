package org.droidtv.defaultdashboard.data.query;

import android.media.tv.TvContract;
import android.net.Uri;

import org.droidtv.htv.provider.HtvContract.HtvChannelList;

import java.util.ArrayList;

public class ChannelTypeQuery implements Query  {

    private static final String[] PROJECTION = {TvContract.Channels._ID,
                                                TvContract.Channels.COLUMN_PACKAGE_NAME,
                                                TvContract.Channels.COLUMN_TYPE};

    private static final String SORT_ORDER = null;
    private String mSelection = TvContract.Channels.COLUMN_PACKAGE_NAME + "=?";
    private String[] mSelectionArgs;

    public ChannelTypeQuery(String packageName) {
        ArrayList<String> selectionArgs = new ArrayList<>();
        mSelectionArgs = new String[1];

        selectionArgs.add(packageName);

        mSelectionArgs = selectionArgs.toArray(mSelectionArgs);
    }

    @Override
    public Uri getUri() {
        return PreviewProgramsQuery.PROVIDER_CONTENT_URI;
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
