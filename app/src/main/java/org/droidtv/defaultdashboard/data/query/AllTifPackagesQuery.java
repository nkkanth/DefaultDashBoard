package org.droidtv.defaultdashboard.data.query;

import android.media.tv.TvContract.Channels;
import android.net.Uri;

import org.droidtv.defaultdashboard.data.model.Source;
import org.droidtv.htv.provider.HtvContract.HtvChannelList;

/**
 * Created by sandeep.kumar on 20/12/2017.
 */

public class AllTifPackagesQuery implements Query {
    private static final String[] PROJECTION = {
            HtvChannelList.COLUMN_INPUT_ID,
            HtvChannelList.COLUMN_PACKAGE_NAME
    };
    private static  final  String GROUPBY_INPUT_ID = ") GROUP BY (" + HtvChannelList.COLUMN_INPUT_ID;

    private static final String SELECTION = HtvChannelList.COLUMN_INPUT_ID + " NOT LIKE ? AND " +
                                            HtvChannelList.COLUMN_INPUT_ID + " NOT LIKE ? " +
                                            GROUPBY_INPUT_ID;
    private static final String[] SELECTION_ARGS = {Source.NON_THIRD_PARTY_INPUT_ID_PREFIX + "%", Source.MEDIA_CHANNELS_INPUT_ID_PREFIX + "%"};

    public static final int INDEX_COLUMN_INPUT_ID = 0;
    public static final int INDEX_COLUMN_PACKAGE_NAME = 1;

    public AllTifPackagesQuery() {

    }

    @Override
    public Uri getUri() {
        return Channels.CONTENT_URI;
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
        return SELECTION_ARGS;
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
