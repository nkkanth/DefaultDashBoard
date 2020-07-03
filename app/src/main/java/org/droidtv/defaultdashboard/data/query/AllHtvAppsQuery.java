package org.droidtv.defaultdashboard.data.query;

import android.net.Uri;

import org.droidtv.htv.provider.HtvContract;

/**
 * Created by sandeep.kumar on 06/02/2018.
 */

public class AllHtvAppsQuery implements Query {
    private static final String[] PROJECTION = {
            HtvContract.HtvAppList._ID,
            HtvContract.HtvAppList.COLUMN_TYPE,
            HtvContract.HtvAppList.COLUMN_NAME,
            HtvContract.HtvAppList.COLUMN_LABEL_NAME,
            HtvContract.HtvAppList.COLUMN_CATEGORY,
            HtvContract.HtvAppList.COLUMN_COUNTRY,
            HtvContract.HtvAppList.COLUMN_APP_RECOMMENDATION,
            HtvContract.HtvAppList.COLUMN_RECOMMENDED_APP,
            HtvContract.HtvAppList.COLUMN_POSITION
    };

    private static final String SORT_ORDER = HtvContract.HtvAppList.COLUMN_POSITION.concat(" ASC");

    public AllHtvAppsQuery() {

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
        return null;
    }

    @Override
    public String[] getSelectionArgs() {
        return null;
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
