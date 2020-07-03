package org.droidtv.defaultdashboard.data.query;

import android.net.Uri;

import org.droidtv.defaultdashboard.data.model.Channel;
import org.droidtv.defaultdashboard.epgprovider.IProTvEpgContract;

/**
 * Created by Utamkumar.Bhuwania on 10-03-2019.
 */

public class ProTvEpgProgramDataQuery implements Query {

    private static final String[] PROJECTION = {
            IProTvEpgContract.C_NOW_EVENTID,
            IProTvEpgContract.C_CHANNEL_NUMBER,
            IProTvEpgContract.C_NOW_EVENTNAME,
            IProTvEpgContract.C_NOW_SHORTINFO,
            IProTvEpgContract.C_NOW_STARTTIME,
            IProTvEpgContract.C_NOW_ENDTIME,
            IProTvEpgContract.C_NOW_RATING,
            IProTvEpgContract.C_NOW_GENRE
    };

    private static final String SELECTION = IProTvEpgContract.C_CHANNEL_NUMBER + " = ?";

    private final Channel mChannel;

    public ProTvEpgProgramDataQuery(Channel channel) {
        mChannel = channel;
    }

    @Override
    public Uri getUri() {
        return IProTvEpgContract.CONTENT_URI;
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
        return new String[]{String.valueOf(mChannel.getDisplayNumber())};
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
