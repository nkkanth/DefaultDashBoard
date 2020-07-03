package org.droidtv.defaultdashboard.data.model;

import android.content.Context;
import android.database.Cursor;
import android.media.tv.TvContract.Programs;
import android.text.TextUtils;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.epgprovider.IProTvEpgContract;
import org.droidtv.defaultdashboard.util.BcepgGenreHelper;
import org.droidtv.tv.provider.IEpgContract;
import org.droidtv.tv.smarttv.provider.IIpEpgContract;

/**
 * Created by sandeep on 28-12-2017.
 */

public class Program {

    private int mId;
    private int mChannelId;
    private String mTitle;
    private String mDescription;
    private long mStartTime;
    private long mEndTime;
    private String[] mRatings;
    private String mPosterArtUri;
    private String mGenre;

    private Program() {

    }

    public int getId() {
        return mId;
    }

    public int getChannelId() {
        return mChannelId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public long getEndTime() {
        return mEndTime;
    }

    public String[] getRatings() {
        return mRatings;
    }

    public String getPosterArtUri() {
        return mPosterArtUri;
    }

    public String getGenre() {
        return mGenre;
    }

    void setId(int id) {
        mId = id;
    }

    void setChannelId(int channelId) {
        mChannelId = channelId;
    }

    void setTitle(String title) {
        mTitle = title;
    }

    void setDescription(String description) {
        mDescription = description;
    }

    void setStartTime(long startTime) {
        mStartTime = startTime;
    }

    void setEndTime(long endTime) {
        mEndTime = endTime;
    }

    public void setRatings(String[] ratings) {
        if (ratings != null) {
            mRatings = ratings.clone();
        }
    }

    void setPosterArtUri(String posterArtUri) {
        mPosterArtUri = posterArtUri;
    }

    void setGenre(String genre) {
        mGenre = genre;
    }

    public static Program buildTifProgramDataFromCursor(Cursor cursor, Program program) {
        int id = cursor.getInt(cursor.getColumnIndex(Programs._ID));
        int channelId = cursor.getInt(cursor.getColumnIndex(Programs.COLUMN_CHANNEL_ID));
        String title = cursor.getString(cursor.getColumnIndex(Programs.COLUMN_TITLE));
        String description = cursor.getString(cursor.getColumnIndex(Programs.COLUMN_SHORT_DESCRIPTION));
        long startTime = cursor.getLong(cursor.getColumnIndex(Programs.COLUMN_START_TIME_UTC_MILLIS));
        long endTime = cursor.getLong(cursor.getColumnIndex(Programs.COLUMN_END_TIME_UTC_MILLIS));
        String flattenedRatingString = cursor.getString(cursor.getColumnIndex(Programs.COLUMN_CONTENT_RATING));
        String[] ratings = stringToContentRatings(flattenedRatingString);
        String genre = cursor.getString(cursor.getColumnIndex(Programs.COLUMN_CANONICAL_GENRE));
        String posterArtUri = cursor.getString(cursor.getColumnIndex(Programs.COLUMN_POSTER_ART_URI));

        if (program == null) {
            Builder programBuilder = new Builder();
            program = programBuilder
                    .setId(id)
                    .setChannelId(channelId)
                    .setTitle(title)
                    .setDescription(description)
                    .setStartTime(startTime)
                    .setEndTime(endTime)
                    .setRatings(ratings)
                    .setGenre(genre)
                    .setPosterArtUri(posterArtUri)
                    .build();
        } else {
            program.setId(id);
            program.setChannelId(channelId);
            program.setTitle(title);
            program.setDescription(description);
            program.setStartTime(startTime);
            program.setEndTime(endTime);
            program.setRatings(ratings);
            program.setGenre(genre);
            program.setPosterArtUri(posterArtUri);
        }
        return program;
    }

    public static Program buildBcepgProgramDataFromCursor(Context context, Cursor cursor, Program program) {
        int id = cursor.getInt(cursor.getColumnIndex(IEpgContract.C_NOW_EVENTID));
        int channelId = cursor.getInt(cursor.getColumnIndex(IEpgContract.C_NOW_PRESETID));
        String title = cursor.getString(cursor.getColumnIndex(IEpgContract.C_NOW_EVENTNAME));
        String description = cursor.getString(cursor.getColumnIndex(IEpgContract.C_NOW_SHORTINFO));
        // BCEPG db stores start time in seconds. It has to be converted to milliseconds
        long startTime = cursor.getLong(cursor.getColumnIndex(IEpgContract.C_NOW_STARTTIME)) * 1000L;
        //BCEPG db stores end time in seconds. It has to be converted to milliseconds
        long endTime = cursor.getLong(cursor.getColumnIndex(IEpgContract.C_NOW_ENDTIME)) * 1000L;
        String flattenedRatingString = String.valueOf(cursor.getInt(cursor.getColumnIndex(IEpgContract.C_NOW_RATING)));
        String[] ratings = stringToContentRatings(flattenedRatingString);
        String genre = BcepgGenreHelper.getGenreString(context, DashboardDataManager.getInstance().getInstallationCountry(),
                cursor.getInt(cursor.getColumnIndex(IEpgContract.C_NOW_GENRE)));

        if (program == null) {
            Builder programBuilder = new Builder();
            program = programBuilder
                    .setId(id)
                    .setChannelId(channelId)
                    .setTitle(title)
                    .setDescription(description)
                    .setStartTime(startTime)
                    .setEndTime(endTime)
                    .setRatings(ratings)
                    .setGenre(genre)
                    .build();
        } else {
            program.setId(id);
            program.setChannelId(channelId);
            program.setTitle(title);
            program.setDescription(description);
            program.setStartTime(startTime);
            program.setEndTime(endTime);
            program.setRatings(ratings);
            program.setGenre(genre);
            program.setPosterArtUri(null);
        }
        return program;
    }

    public static Program buildIpepgProgramDataFromCursor(Cursor cursor, Program program) {
        int id = cursor.getInt(cursor.getColumnIndex(IIpEpgContract.C_PROGRAM_ID));
        int channelId = cursor.getInt(cursor.getColumnIndex(IIpEpgContract.C_CCID));
        String title = cursor.getString(cursor.getColumnIndex(IIpEpgContract.C_TITLE));
        String description = cursor.getString(cursor.getColumnIndex(IIpEpgContract.C_TEXT));
        // IPEPG db stores start time in seconds. It has to be converted to milliseconds
        long startTime = cursor.getInt(cursor.getColumnIndex(IIpEpgContract.C_UTC_START_TIME)) * 1000L;
        // IPEPG db stores end time in seconds. It has to be converted to milliseconds
        long endTime = cursor.getInt(cursor.getColumnIndex(IIpEpgContract.C_UTC_END_TIME)) * 1000L;
        String flattenedRatingString = cursor.getString(cursor.getColumnIndex(IIpEpgContract.C_RATING));
        String[] ratings = stringToContentRatings(flattenedRatingString);
        String genre = cursor.getString(cursor.getColumnIndex(IIpEpgContract.C_GENRE));

        if (program == null) {
            Builder programBuilder = new Builder();
            program = programBuilder
                    .setId(id)
                    .setChannelId(channelId)
                    .setTitle(title)
                    .setDescription(description)
                    .setStartTime(startTime)
                    .setEndTime(endTime)
                    .setRatings(ratings)
                    .setGenre(genre)
                    .build();
        } else {
            program.setId(id);
            program.setChannelId(channelId);
            program.setTitle(title);
            program.setDescription(description);
            program.setStartTime(startTime);
            program.setEndTime(endTime);
            program.setRatings(ratings);
            program.setGenre(genre);
            program.setPosterArtUri(null);
        }
        return program;
    }

    public static Program buildProTvEpgProgramDataFromCursor(Context context, Cursor cursor, Program program) {

        int id = cursor.getInt(cursor.getColumnIndex(IProTvEpgContract.C_NOW_EVENTID));
        int channelId = cursor.getInt(cursor.getColumnIndex(IProTvEpgContract.C_CHANNEL_NUMBER));
        String title = cursor.getString(cursor.getColumnIndex(IProTvEpgContract.C_NOW_EVENTNAME));
        String description = cursor.getString(cursor.getColumnIndex(IProTvEpgContract.C_NOW_SHORTINFO));
        // BCEPG db stores start time in seconds. It has to be converted to milliseconds
        long startTime = cursor.getLong(cursor.getColumnIndex(IProTvEpgContract.C_NOW_STARTTIME)) * 1000L;
        //BCEPG db stores end time in seconds. It has to be converted to milliseconds
        long endTime = cursor.getLong(cursor.getColumnIndex(IProTvEpgContract.C_NOW_ENDTIME)) * 1000L;
        String flattenedRatingString = String.valueOf(cursor.getInt(cursor.getColumnIndex(IProTvEpgContract.C_NOW_RATING)));
        String[] ratings = stringToContentRatings(flattenedRatingString);
        String genre = BcepgGenreHelper.getGenreString(context, DashboardDataManager.getInstance().getInstallationCountry(),
                cursor.getInt(cursor.getColumnIndex(IEpgContract.C_NOW_GENRE)));

        if (program == null) {
            Builder programBuilder = new Builder();
            program = programBuilder
                    .setId(id)
                    .setChannelId(channelId)
                    .setTitle(title)
                    .setDescription(description)
                    .setStartTime(startTime)
                    .setEndTime(endTime)
                    .setRatings(ratings)
                    .setGenre(genre)
                    .build();
        } else {
            program.setId(id);
            program.setChannelId(channelId);
            program.setTitle(title);
            program.setDescription(description);
            program.setStartTime(startTime);
            program.setEndTime(endTime);
            program.setRatings(ratings);
            program.setGenre(genre);
            program.setPosterArtUri(null);
        }
        return program;
    }

    private static String[] stringToContentRatings(String commaSeparatedRatings) {
        if (TextUtils.isEmpty(commaSeparatedRatings)) {
            return null;
        }
        String[] ratings = commaSeparatedRatings.split("\\s*,\\s*");
        return ratings;
    }

    public static class Builder {

        private Program mProgram;

        public Builder() {
            mProgram = new Program();
        }

        public Builder setId(int id) {
            mProgram.setId(id);
            return this;
        }

        public Builder setChannelId(int channelId) {
            mProgram.setChannelId(channelId);
            return this;
        }

        public Builder setTitle(String title) {
            mProgram.setTitle(title);
            return this;
        }

        public Builder setDescription(String description) {
            mProgram.setDescription(description);
            return this;
        }

        public Builder setStartTime(long startTime) {
            mProgram.setStartTime(startTime);
            return this;
        }

        public Builder setEndTime(long endTime) {
            mProgram.setEndTime(endTime);
            return this;
        }

        public Builder setRatings(String[] ratings) {
            mProgram.setRatings(ratings);
            return this;
        }

        public Builder setPosterArtUri(String posterArtUri) {
            mProgram.setPosterArtUri(posterArtUri);
            return this;
        }

        public Builder setGenre(String genre) {
            mProgram.setGenre(genre);
            return this;
        }

        public Program build() {
            return mProgram;
        }
    }
}
