package org.droidtv.defaultdashboard.data.query;

import android.net.Uri;

import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.util.Constants.ThemeTvGroup;
import org.droidtv.htv.provider.HtvContract.HtvThemeTvSetting;

/**
 * Created by sandeep.kumar on 20/11/2017.
 */

public class ThemeTvInfoQuery implements Query {

    private static final String[] PROJECTION = {
            HtvThemeTvSetting.COLUMN_NAME,
            HtvThemeTvSetting.COLUMN_LOGO_URI
    };

    private static final String SELECTION = HtvThemeTvSetting.COLUMN_MAPPED_THEME_FIELD + " = ?";

    private String[] mSelectionArgs;

    public ThemeTvInfoQuery(ThemeTvGroup themeTvGroup) {
        int mappedThemeId = themeTvGroup.ordinal() + 1;
        mSelectionArgs = new String[]{String.valueOf(mappedThemeId)};
        DdbLogUtility.logTVChannelChapter("ThemeTvInfoQuery","ThemeTvInfoQuery mSelectionArgs: "+mSelectionArgs);
    }

    @Override
    public Uri getUri() {
        return HtvThemeTvSetting.CONTENT_URI;
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
