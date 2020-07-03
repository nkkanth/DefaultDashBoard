package org.droidtv.defaultdashboard.data.query;

import android.media.tv.TvContract;
import android.net.Uri;

import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.htv.provider.HtvContract.HtvChannelList;

import java.util.ArrayList;

public class PreviewProgramsChannelSmartInfoQuery implements Query  {

    private static final String[] PROJECTION = {
            PreviewProgramsQuery.COLUMN_CHANNEL_ID,
            HtvChannelList.COLUMN_PACKAGE_NAME,
            HtvChannelList.COLUMN_DISPLAY_NAME,
            HtvChannelList.COLUMN_DESCRIPTION,
            PreviewProgramsQuery.COLUMN_CHANNEL_CATEGORY//This field is currently being used to store preview type which could be
                                                        //VOD, Games and AppRecommendations
                                                        //AppRecommendation-0, VOD-1, Games-2
            };

    private static final String SORT_ORDER = " CAST(" +  PreviewProgramsQuery.COLUMN_CHANNEL_ID + " AS INTEGER) ASC";
    private String mSelection;
    private String[] mSelectionArgs;

    public PreviewProgramsChannelSmartInfoQuery(String packageName) {
        ArrayList<String> selectionArgs = new ArrayList<>();
        mSelectionArgs = new String[3];

        mSelection = TvContract.Channels.COLUMN_PACKAGE_NAME.concat("=?");
        selectionArgs.add(packageName);

        mSelection = mSelection.concat(" AND ").concat(HtvChannelList.COLUMN_TYPE + "=?");
        selectionArgs.add("TYPE_PREVIEW");

        // Exclude hidden channels
        mSelection = mSelection.concat(" AND ").concat(HtvChannelList.COLUMN_LOCKED).concat("=?");
        selectionArgs.add("0");

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
