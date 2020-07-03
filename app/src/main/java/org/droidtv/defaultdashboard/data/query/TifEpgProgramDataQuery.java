package org.droidtv.defaultdashboard.data.query;

import android.media.tv.TvContract.Programs;
import android.net.Uri;

/**
 * Created by sandeep on 28-12-2017.
 */

public class TifEpgProgramDataQuery implements Query {

    private static final String[] PROJECTION = {
            Programs._ID,
            Programs.COLUMN_CHANNEL_ID,
            Programs.COLUMN_TITLE,
            Programs.COLUMN_SHORT_DESCRIPTION,
            Programs.COLUMN_START_TIME_UTC_MILLIS,
            Programs.COLUMN_END_TIME_UTC_MILLIS,
            Programs.COLUMN_CONTENT_RATING,
            Programs.COLUMN_CANONICAL_GENRE,
            Programs.COLUMN_POSTER_ART_URI
    };

    private static final String SELECTION = Programs.COLUMN_CHANNEL_ID + " = ? AND " +
            Programs.COLUMN_START_TIME_UTC_MILLIS + " <= ? AND " + Programs.COLUMN_END_TIME_UTC_MILLIS + " >= ?";

    private final int mChannelId;
    private final long mStartTime;


    public TifEpgProgramDataQuery(int channelId, long startTime) {
        mChannelId = channelId;
        mStartTime = startTime;
    }


    @Override
    public Uri getUri() {
        return Programs.CONTENT_URI;
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
        return new String[]{String.valueOf(mChannelId), String.valueOf(mStartTime), String.valueOf(mStartTime)};
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
