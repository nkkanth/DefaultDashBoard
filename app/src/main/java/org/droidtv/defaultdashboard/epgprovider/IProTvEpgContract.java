package org.droidtv.defaultdashboard.epgprovider;

import android.content.ContentUris;
import android.net.Uri;

public class IProTvEpgContract {

	/**
	 * String for DVB EPG Authority
	 */
	public static final String EPG_PROVIDER_AUTHORITY 	= "org.droidtv.tvcontrolservice.epgprovider";
	public static final String BASE_PATH="epg";
	/**
	 * String that specifies the table containing the present and following informations in the database
	 */
	public static final String NOWNEXT_EVENT_TABLE 	= "nownexttable";
	/**
	 * URI(s) to be used to access data from now and next event data left joined for satellite medium
	 * <p>
	 * Typically used by Dashboard, Zap Bar, NowNextOverview
	 */
	public static final Uri CONTENT_URI	= Uri.parse("content://" + EPG_PROVIDER_AUTHORITY + "/" + NOWNEXT_EVENT_TABLE);
	
	
	
	/**
	 * Enums for the Genre of the Event
	 * <p>
	 * Typically used by Dashboard, Zap Bar, NowNextOverview
	 */
	public static enum GenreType {
		GENRE_RESERVED_0,
		GENRE_MOVIE,
		GENRE_NEWS,
		GENRE_SHOWS,
		GENRE_SPORTS,
		GENRE_CHILDREN,
		GENRE_MUSIC,
		GENRE_ARTS,
		GENRE_SOCIETY,
		GENRE_EDUCATION,
		GENRE_LEISURE,
		GENRE_SPECIALS,
		GENRE_RESERVED_C,	//0x0C
		GENRE_RESERVED_D,	//0x0D
		GENRE_RESERVED_E,	//0x0E
		GENRE_DRAMA,
		GENRE_UNKNOWN
	}

	

	/**
	 * Event ID as transmitted in EIT packets Now information
	 * type - int
	 */
	public static final String C_NOW_EVENTID			= "Now_eventid";
	/**
	 * Event ID as transmitted in EIT packets Next information
	 * type - int
	 */
	public static final String C_NEXT_EVENTID			= "Next_eventid";	
	
	/**
	 * Name of the Now event as transmitted in EIT packets, exposed as UTF-16
	 * type - String
	 */
	public static final String C_NOW_EVENTNAME 			= "Now_eventname";
	/**
	 * Name of the Next event as transmitted in EIT packets, exposed as UTF-16
	 * type - String
	 */
	public static final String C_NEXT_EVENTNAME 		= "Next_eventname";
	
	/**
	 * Version of the Now event id as transmitted in EIT packets.
	 * type - int
	 */
	public static final String C_NOW_VERSION 			= "Now_version";
	/**
	 * Version of the Next event id as transmitted in EIT packets.
	 * type - int
	 */
	public static final String C_NEXT_VERSION 			= "Next_version";
	
	/**
	 * Start time of the Now event in UTC.  Indicated as number of seconds from Linux EPOCH
	 * type - long (user can use DateUtils to convert this to date time)
	 */
	public static final String C_NOW_STARTTIME 			= "Now_starttime";
	/**
	 * Start time of the Next event in UTC.  Indicated as number of seconds from Linux EPOCH
	 * type - long (user can use DateUtils to convert this to date time)
	 */
	public static final String C_NEXT_STARTTIME 		= "Next_starttime";
	
	/**
	 * End time of the Now event in UTC.  Indicated as number of seconds from Linux EPOCH
	 * type - long (user can use DateUtils to convert this to date time)
	 */
	public static final String C_NOW_ENDTIME 			= "Now_endtime";
	/**
	 * End time of the Next event in UTC.  Indicated as number of seconds from Linux EPOCH
	 * type - long (user can use DateUtils to convert this to date time)
	 */
	public static final String C_NEXT_ENDTIME 			= "Next_endtime";
	
	/**
	 * Short description of the Now event exposed as UTF-16
	 * type - String
	 */
	public static final String C_NOW_SHORTINFO 			= "Now_shortinfo";
	/**
	 * Short description of the Next event exposed as UTF-16
	 * type - String
	 */
	public static final String C_NEXT_SHORTINFO 		= "Next_shortinfo";
	
	/**
	 * Extended description of the Now event exposed as UTF-16
	 * type - String
	 */
	public static final String C_NOW_EXTENDEDINFO 		= "Now_extendedinfo";
	
	/**
	 * Extended description of the Next event exposed as UTF-16
	 * type - String
	 */
	public static final String C_NEXT_EXTENDEDINFO 		= "Next_extendedinfo";	
	
	/**
	 * Genre classification for the Now event.
	 * type - {@link GenreType}
	 */
	public static final String C_NOW_GENRE			 	= "Now_genre";
	/**
	 * Genre classification for the Next event.
	 * type - {@link GenreType}
	 */
	public static final String C_NEXT_GENRE			 	= "Next_genre";
	
	/**
	 * DVB rating as mentioned in Parental rating descriptor of EIT
	 * type - int
	 */
	public static final String C_NOW_RATING				= "Now_rating";
	/**
	 * DVB rating as mentioned in Parental rating descriptor of EIT
	 * type - int
	 */
	public static final String C_NEXT_RATING			= "Next_rating";	
	public static final String C_NOW_SCRAMBLED			= "Now_scrambled";
	public static final String C_NEXT_SCRAMBLED			= "Next_scrambled";
	public static final String C_NOW_LOGO			= "Now_logo";
	public static final String C_NEXT_LOGO			= "Next_logo";
	/**
	 * Preset Number
	 * type - int
	 */
	public static final String C_CHANNEL_NUMBER			= "channel_presetnumber";
    /**
     * Channel Name
     * type - String
     */
    public static final String C_CHANNEL_NAME            = "Dbit_channelname";

	/**
	 * Builds a URI that points to a program logo.
	 *
	 * @param programId The ID of program.
	 */
	public static final Uri buildCurrentProgramLogoUri(long programId) {
		return ContentUris.withAppendedId(IProTvEpgContract.CONTENT_URI, programId);
	}



}
