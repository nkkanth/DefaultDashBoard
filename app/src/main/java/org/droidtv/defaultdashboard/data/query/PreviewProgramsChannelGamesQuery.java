package org.droidtv.defaultdashboard.data.query;

import android.net.Uri;

import org.droidtv.htv.provider.HtvContract.HtvChannelList;

import java.util.ArrayList;

public class PreviewProgramsChannelGamesQuery implements Query  {

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

    public PreviewProgramsChannelGamesQuery() {
        ArrayList<String> selectionArgs = new ArrayList<>();
        mSelectionArgs = new String[2];

        mSelection = HtvChannelList.COLUMN_TYPE + "=?";
        selectionArgs.add("TYPE_PREVIEW");
        // Exclude hidden channels
        mSelection = mSelection.concat(" AND ").concat(HtvChannelList.COLUMN_LOCKED).concat("=?");
        selectionArgs.add("0");

        //Include only Gaming channel
      //  mSelection = mSelection.concat(" AND ").concat(PreviewProgramsQuery.COLUMN_CHANNEL_CATEGORY).concat("=?");
        //selectionArgs.add(Integer.toString(Constants.CHANNEL_CONTENT_TYPE_GAMES_RECOMMENDATION));

        android.util.Log.d("PreviewProgramsChannelGamesQuery",  mSelection);
        mSelectionArgs = selectionArgs.toArray(mSelectionArgs);
        android.util.Log.d("PreviewProgramsChannelGamesQuery 0 ",  mSelectionArgs[0]);
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
