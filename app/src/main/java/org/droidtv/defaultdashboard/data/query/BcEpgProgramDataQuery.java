package org.droidtv.defaultdashboard.data.query;

import android.net.Uri;

import org.droidtv.defaultdashboard.data.model.Channel;
import org.droidtv.tv.provider.IEpgContract;

/**
 * Created by sandeep on 28-12-2017.
 */

public class BcEpgProgramDataQuery implements Query {

    private static final String[] PROJECTION = {
            IEpgContract.C_NOW_EVENTID,
            IEpgContract.C_NOW_PRESETID,
            IEpgContract.C_NOW_EVENTNAME,
            IEpgContract.C_NOW_SHORTINFO,
            IEpgContract.C_NOW_STARTTIME,
            IEpgContract.C_NOW_ENDTIME,
            IEpgContract.C_NOW_RATING,
            IEpgContract.C_NOW_GENRE
    };

    private static final String SELECTION = IEpgContract.C_NOW_PRESETID + " = ?";

    private final Channel mChannel;

    public BcEpgProgramDataQuery(Channel channel) {
        mChannel = channel;
    }

    @Override
    public Uri getUri() {
        /* antenna channel */
        if (Channel.isAntennaChannel(mChannel)) {
            return IEpgContract.CONTENT_URI_AntennaNowNextEventData;
        }

        /* cable channel */
        if (Channel.isCableChannel(mChannel)) {
            return IEpgContract.CONTENT_URI_CableNowNextEventData;
        }

        /* satellite channel */
        return IEpgContract.CONTENT_URI_SatelliteNowNextEventData;
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
        return new String[]{String.valueOf(mChannel.getId())};
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
