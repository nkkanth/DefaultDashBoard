package org.droidtv.defaultdashboard.data.query;

import android.net.Uri;

import org.droidtv.tv.smarttv.provider.IIpEpgContract;

/**
 * Created by sandeep on 28-12-2017.
 */

public class IpEpgProgramDataQuery implements Query {

    private static final String[] PROJECTION = {
            IIpEpgContract.C_PROGRAM_ID,
            IIpEpgContract.C_CCID,
            IIpEpgContract.C_TITLE,
            IIpEpgContract.C_TEXT,
            IIpEpgContract.C_UTC_START_TIME,
            IIpEpgContract.C_UTC_END_TIME,
            IIpEpgContract.C_RATING,
            IIpEpgContract.C_GENRE
    };

    private static final String SELECTION = IIpEpgContract.C_CCID + " = ? AND " +
            IIpEpgContract.C_UTC_START_TIME + " <= ? AND " + IIpEpgContract.C_UTC_END_TIME + " >= ?";

    private final int mChannelId;
    private final long mStartTime;

    public IpEpgProgramDataQuery(int channelId, long startTime) {
        mChannelId = channelId;
        mStartTime = startTime;
    }

    @Override
    public Uri getUri() {
        return IIpEpgContract.CONTENT_URI_PROGRAM_DATA;
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
