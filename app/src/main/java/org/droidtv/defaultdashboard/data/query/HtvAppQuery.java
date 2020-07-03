package org.droidtv.defaultdashboard.data.query;

import android.net.Uri;

import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.htv.provider.HtvContract;

/**
 * Created by sandeep.kumar on 06/02/2018.
 */

public class HtvAppQuery implements Query {
    private static final String[] PROJECTION = {
            HtvContract.HtvAppList._ID,
            HtvContract.HtvAppList.COLUMN_NAME
    };

    private static final String SELECTION = HtvContract.HtvAppList._ID + " = ?";
    private final String[] mSelectionArgs;

    public HtvAppQuery(int appId) {
        mSelectionArgs = new String[]{String.valueOf(appId)};
        DdbLogUtility.logAppsChapter("HtvAppQuery","HtvAppQuery mSelectionArgs: " +mSelectionArgs);
    }

    @Override
    public Uri getUri() {
        return HtvContract.HtvAppList.CONTENT_URI;
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
