package org.droidtv.defaultdashboard.data.query;

import android.media.tv.TvContract;
import android.net.Uri;

import org.droidtv.htv.provider.HtvContract.HtvChannelList;

import java.util.ArrayList;

public class PreviewProgramsQuery implements Query {

    public final static String COLUMN_ID = TvContract.Channels._ID ;
    public final static String COLUMN_CHANNEL_ID = TvContract.Channels._ID ;
    public final static String COLUMN_MAPPED_CHANNEL_ID = TvContract.PreviewPrograms.COLUMN_CHANNEL_ID;

    public final static Uri PROVIDER_CONTENT_URI = TvContract.Channels.CONTENT_URI;
    public final static Uri PREVIEW_PROGRAM_CONTENT_URI = TvContract.PreviewPrograms.CONTENT_URI;
    public final static String COLUMN_PACKAGE_NAME = "package_name";
    public final static String COLUMN_TITLE = "title";
    public final static String COLUMN_SHORT_DESC = "short_description";
    public final static String COLUMN_POSTER_ART_URI = "poster_art_uri";
    public final static String COLUMN_INTENT_URI = "intent_uri";
    public final static String COLUMN_LOGO_URI = "logo_uri";
    public final static String COLUMN_RELEASE_DATE = TvContract.PreviewPrograms.COLUMN_RELEASE_DATE;
    public final static String COLUMN_DURATION_MILLIS = TvContract.PreviewPrograms.COLUMN_DURATION_MILLIS;
    public final static String COLUMN_OFFER_PRICE = TvContract.PreviewPrograms.COLUMN_OFFER_PRICE;
    public final static String COLUMN_REVIEW_RATING = TvContract.PreviewPrograms.COLUMN_REVIEW_RATING;


    public final static String COLUMN_CHANNEL_CATEGORY = HtvChannelList.COLUMN_INTERNAL_PROVIDER_FLAG4;//This field is currently being used to store preview type which could be
                                                                                               //VOD, Games and AppRecommendations
                                                                                               //AppRecommendation-0, VOD-1, Games-2, SmartInfo-3, Empty-4
    private static final String[] PROJECTION = {
            COLUMN_ID,
            COLUMN_MAPPED_CHANNEL_ID,
            COLUMN_PACKAGE_NAME,
            COLUMN_TITLE,
            COLUMN_SHORT_DESC,
            COLUMN_POSTER_ART_URI,
            COLUMN_INTENT_URI,
            COLUMN_LOGO_URI,
            COLUMN_CHANNEL_CATEGORY,
            COLUMN_RELEASE_DATE,
            COLUMN_DURATION_MILLIS,
            COLUMN_OFFER_PRICE,
            COLUMN_REVIEW_RATING
    };

    private String mSelection;
    private String[] mSelectionArgs;

    public PreviewProgramsQuery(int channelId) {
        ArrayList<String> selectionArgs = new ArrayList<>();
        mSelectionArgs = new String[1];

        mSelection = PreviewProgramsQuery.COLUMN_MAPPED_CHANNEL_ID + "=?";
        selectionArgs.add(String.valueOf(channelId));

        mSelectionArgs = selectionArgs.toArray(mSelectionArgs);

    }

    @Override
    public Uri getUri() {
        return TvContract.PreviewPrograms.CONTENT_URI;
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
        return null;
    }

    @Override
    public String getGroupBy() {
        return null;
    }
}
