package org.droidtv.defaultdashboard.data.query;

import android.media.tv.TvContract;
import android.net.Uri;

import org.droidtv.htv.provider.HtvContract.HtvChannelList;

import java.util.ArrayList;

public class PreviewProgramDataQuery implements Query {

    public final static String COLUMN_ID = TvContract.PreviewPrograms._ID ;
    public final static String COLUMN_MAPPED_CHANNEL_ID = TvContract.PreviewPrograms.COLUMN_CHANNEL_ID;
    public static final String COLUMN_CATEGORY = PreviewProgramsQuery.COLUMN_CHANNEL_CATEGORY;

    private static final String[] PROJECTION = {COLUMN_ID, COLUMN_MAPPED_CHANNEL_ID, COLUMN_CATEGORY};

    private String mSelection;
    private String[] mSelectionArgs;

    public PreviewProgramDataQuery(String _id) {
        ArrayList<String> selectionArgs = new ArrayList<>();
        mSelectionArgs = new String[1];

        mSelection = TvContract.PreviewPrograms._ID + "=?";
        selectionArgs.add(_id);

        mSelectionArgs = selectionArgs.toArray(mSelectionArgs);
    }

    @Override
    public Uri getUri() {
        return TvContract.PreviewPrograms.CONTENT_URI;
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
        return null;
    }

    @Override
    public String getGroupBy() {
        return null;
    }
}
