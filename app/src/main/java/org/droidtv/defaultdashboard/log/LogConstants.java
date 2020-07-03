package org.droidtv.defaultdashboard.log;

/**
 * Created by bhargava.gugamsetty on 17-04-2019.
 */

public class LogConstants {

    public static final String prop_log_level = "persist.sys.ddb.debug";
    public static final String STABILITY = "STABILITY";
    public static final String CHAPTER_TYPE = "CHAPTER_TYPE";

    public static final String LOG_ACTION = "LOG_ACTION";
    public static final String ENABLE = "enable";
    public static final String DISABLE = "disable";



    public static final int DDB_DEBUG_OFF = 0x00; //0
    public static final int DDB_DEBUG_ALL = 0x01; //1
    public static final int DDB_DEBUG_RECOMMENDEDATION = 0x02; //2
    public static final int DDB_DEBUG_TV_CHANNELS = 0x04; //4
    public static final int DDB_DEBUG_APPS = 0x08; //8
    public static final int DDB_DEBUG_VOD = 0x10; //16
    public static final int DDB_DEBUG_GAMES = 0x20; //32
    public static final int DDB_DEBUG_MORE = 0x40; //64
    public static final int DDB_DEBUG_CAST = 0x80; //128
    public static final int DDB_DEBUG_TOP_MENU = 0x100; //256
    public static final int RESET_ALL = 0xFF;



}
