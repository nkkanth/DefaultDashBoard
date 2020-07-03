package org.droidtv.defaultdashboard.util;

import android.content.Context;

import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;
import org.droidtv.tv.provider.IEpgContract;

/**
 * Created by sandeep.kumar on 01/03/2018.
 */

public class BcepgGenreHelper {

    public static String getGenreString(Context context, int installationCountry, int genre) {
        DdbLogUtility.logTVChannelChapter("BcepgGenreHelper", "getLatamSpecificGenreString installationCountry " + installationCountry + " genre " +genre);

        if (installationCountry == TvSettingsDefinitions.InstallationCountryConstants.BRAZIL ||
                installationCountry == TvSettingsDefinitions.InstallationCountryConstants.ARGENTINA) {
            return getLatamSpecificGenreString(context, genre);
        }

        return getNonLatamSpecificGenreString(context, genre);
    }

    private static String getLatamSpecificGenreString(Context context, int genre) {

        String genreString = "";
        if (genre == IEpgContract.LtGenreType.GENRE_NEWS.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_NEWS);
        }
        if (genre == IEpgContract.LtGenreType.GENRE_SPORTS.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_SPORTS);
        }
        if (genre == IEpgContract.LtGenreType.GENRE_EDUCATION.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_EDUCATION);
        }
        if (genre == IEpgContract.LtGenreType.GENRE_SOAP_OPERA.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_SOAP_OPERA);
        }
        if (genre == IEpgContract.LtGenreType.GENRE_MINI_SERIES.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_MINI_SERIES);
        }
        if (genre == IEpgContract.LtGenreType.GENRE_SERIES.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_SERIES);
        }
        if (genre == IEpgContract.LtGenreType.GENRE_VARIETY.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_VARIETY);
        }
        if (genre == IEpgContract.LtGenreType.GENRE_REALITY_SHOW.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_REALITY_SHOW);
        }
        if (genre == IEpgContract.LtGenreType.GENRE_INFORMATION.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_INFORMATION);
        }
        if (genre == IEpgContract.LtGenreType.GENRE_COMICAL.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_COMICAL);
        }
        if (genre == IEpgContract.LtGenreType.GENRE_CHILDREN.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_CHILDREN);
        }
        if (genre == IEpgContract.LtGenreType.GENRE_EROTIC.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_EROTIC);
        }
        if (genre == IEpgContract.LtGenreType.GENRE_MOVIE.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_MOVIE);
        }
        if (genre == IEpgContract.LtGenreType.GENRE_RAFFLES_TV.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_RAFFLES_TV);
        }
        if (genre == IEpgContract.LtGenreType.GENRE_DEBATE.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_DEBATE);
        }
        if (genre == IEpgContract.LtGenreType.GENRE_OTHER.ordinal() || genre == IEpgContract.LtGenreType.GENRE_UNKNOWN.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_OTHER);
        }
        return genreString;
    }

    private static String getNonLatamSpecificGenreString(Context context, int genre) {
        String genreString = "";
        DdbLogUtility.logTVChannelChapter("BcepgGenreHelper", "getNonLatamSpecificGenreString " +genre);
        if (genre == IEpgContract.GenreType.GENRE_RESERVED_0.ordinal() ||
                genre == IEpgContract.GenreType.GENRE_RESERVED_C.ordinal() ||
                genre == IEpgContract.GenreType.GENRE_RESERVED_D.ordinal() ||
                genre == IEpgContract.GenreType.GENRE_RESERVED_E.ordinal() ||
                genre == IEpgContract.GenreType.GENRE_UNKNOWN.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_OTHER);
        }
        if (genre == IEpgContract.GenreType.GENRE_MOVIE.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_INFO_MOVIE);
        }
        if (genre == IEpgContract.GenreType.GENRE_NEWS.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_INFO_NEWS);
        }
        if (genre == IEpgContract.GenreType.GENRE_SHOWS.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_INFO_SHOWS);
        }
        if (genre == IEpgContract.GenreType.GENRE_SPORTS.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_INFO_SPORTS);
        }
        if (genre == IEpgContract.GenreType.GENRE_CHILDREN.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_CHILDREN);
        }
        if (genre == IEpgContract.GenreType.GENRE_MUSIC.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_INFO_MUSIC);
        }
        if (genre == IEpgContract.GenreType.GENRE_ARTS.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_INFO_ARTS);
        }
        if (genre == IEpgContract.GenreType.GENRE_SOCIETY.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_INFO_SOCIETY);
        }
        if (genre == IEpgContract.GenreType.GENRE_EDUCATION.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_INFO_EDUCATION);
        }
        if (genre == IEpgContract.GenreType.GENRE_LEISURE.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_INFO_LEISURE);
        }
        if (genre == IEpgContract.GenreType.GENRE_SPECIALS.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_INFO_SPECIALS);
        }
        if (genre == IEpgContract.GenreType.GENRE_DRAMA.ordinal()) {
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_INFO_DRAMA);
        }

        return genreString;
    }
}
