package org.droidtv.defaultdashboard.log;

import android.os.SystemProperties;
import android.util.Log;

/**
 * Created by bhargava.gugamsetty on 17-04-2019.
 */

public class DdbLogUtility{

    private static final String TAG = DdbLogUtility.class.getSimpleName();
    private static int mChapterTpye = LogConstants.DDB_DEBUG_OFF;

    private static boolean debugAll = false;
    private static boolean castChapter = false;
    private static boolean recommendationChapter = false;
    private static boolean tvChannelsChapter = false;
    private static boolean appsChapter = false;
    private static boolean moreChapter = false;
    private static boolean gamesChapter = false;
    private static boolean vodChapter = false;
    private static boolean topMenu = false;
    private static boolean commonLogs = false;

    static { initLogger();}

    public static void logRecommendationChapter(String tag, String msg) {  if(recommendationChapter | debugAll) Log.d(tag, msg); }

    public static void logTVChannelChapter(String tag, String msg) { if(tvChannelsChapter | debugAll) Log.d(tag, msg); }

    public static void logVodChapter(String tag, String msg) { if(tvChannelsChapter | debugAll) Log.d(tag, msg); }

    public static void logCastChapter(String tag, String msg) { if(castChapter | debugAll) Log.d(tag, msg); }

    public static void logAppsChapter(String tag, String msg) { if(appsChapter | debugAll) Log.d(tag, msg); }

    public static void logGamesChapter(String tag, String msg) { if(gamesChapter | debugAll) Log.d(tag, msg); }

    public static void logMoreChapter(String tag, String msg) { if(moreChapter | debugAll) Log.d(tag, msg); }

    public static void logTopMenu(String tag, String msg) { if(topMenu | debugAll) Log.d(tag, msg); }

    public static void logCommon(String tag, String msg) { if(commonLogs) Log.d(tag, msg); }


    public static void initLogger() {
        mChapterTpye = SystemProperties.getInt(LogConstants.prop_log_level, LogConstants.DDB_DEBUG_OFF);
        initFlags(mChapterTpye);
    }

    public static void setLogLevel(int chapterType, boolean isStability) {
        Log.d(TAG, "setLogLevel() called with: chapterType = [" + chapterType + "], isStability = [" + isStability + "]");
        if(isStability){
            mChapterTpye = SystemProperties.getInt(LogConstants.prop_log_level, LogConstants.DDB_DEBUG_OFF);
            mChapterTpye = mChapterTpye | chapterType;
            SystemProperties.set(LogConstants.prop_log_level, Integer.toString(mChapterTpye));
        }else{
            mChapterTpye = mChapterTpye | chapterType;
        }
        initFlags(chapterType);
    }

    public static void unSetLogLevel(int chapterType, boolean isStability) {
        Log.d(TAG, "unSetLogLevel() called with: chapterType = [" + chapterType + "], isStability = [" + isStability + "]");
        if(isStability){
            mChapterTpye = SystemProperties.getInt(LogConstants.prop_log_level, LogConstants.DDB_DEBUG_OFF);
            mChapterTpye = (mChapterTpye & (~chapterType));
            SystemProperties.set(LogConstants.prop_log_level, Integer.toString(mChapterTpye));
        }else{
            mChapterTpye = (mChapterTpye & (~chapterType));
        }
        initFlags(chapterType);
    }

    private static  void initFlags(int chapterType){
        switch (chapterType) {
            case LogConstants.DDB_DEBUG_ALL:
                debugAll = isDebugAll(chapterType);
                break;
            case LogConstants.RESET_ALL:
                reset();
                break;
            case LogConstants.DDB_DEBUG_CAST:
                castChapter = isChapterEnabled(LogConstants.DDB_DEBUG_CAST) ? true : false;
                break;
            case LogConstants.DDB_DEBUG_RECOMMENDEDATION:
                recommendationChapter = isChapterEnabled(LogConstants.DDB_DEBUG_RECOMMENDEDATION) ? true : false;
                break;
            case LogConstants.DDB_DEBUG_TV_CHANNELS:
                tvChannelsChapter = isChapterEnabled(LogConstants.DDB_DEBUG_TV_CHANNELS) ? true : false;
                break;
            case LogConstants.DDB_DEBUG_GAMES:
                gamesChapter = isChapterEnabled(LogConstants.DDB_DEBUG_GAMES) ? true : false;
                break;
            case LogConstants.DDB_DEBUG_APPS:
                appsChapter = isChapterEnabled(LogConstants.DDB_DEBUG_APPS) ? true : false;
                break;
            case LogConstants.DDB_DEBUG_MORE:
                moreChapter = isChapterEnabled(LogConstants.DDB_DEBUG_MORE) ? true : false;
                break;
            case LogConstants.DDB_DEBUG_VOD:
                vodChapter = isChapterEnabled(LogConstants.DDB_DEBUG_VOD) ? true : false;
                break;
            case LogConstants.DDB_DEBUG_TOP_MENU:
                vodChapter = isChapterEnabled(LogConstants.DDB_DEBUG_TOP_MENU) ? true : false;
                break;
            default:
                break;
        }
        commonLogs = debugAll | castChapter | recommendationChapter | tvChannelsChapter | gamesChapter | appsChapter | moreChapter | vodChapter;
        Log.d(TAG, "initFlags: DDB_DEBUG_ALL " + debugAll
                                  + " DDB_DEBUG_CAST " + castChapter
                                  + " DDB_DEBUG_RECOMMENDEDATION " + recommendationChapter
                                  + " DDB_DEBUG_TV_CHANNELS " + tvChannelsChapter
                                  + " DDB_DEBUG_GAMES " + gamesChapter
                                  + " DDB_DEBUG_APPS " + appsChapter
                                  + " DDB_DEBUG_MORE " + moreChapter
                                  + " DDB_DEBUG_VOD " + vodChapter
                                  + " DDB_DEBUG_TOP_MENU " + topMenu
                                  + " COMMON_LOGS " + commonLogs);
    }

    private static boolean isChapterEnabled(int flag) {
        boolean enabled = (((mChapterTpye & flag) == flag) || debugAll);
        Log.d(TAG, "isChapterEnabled: mChapterTpye " + mChapterTpye + " flag " + flag + " enabled " + enabled);
        return enabled;
    }

    public static void log(String tag, String msg) { if(debugAll) Log.d(tag, msg); }

    private static  boolean isDebugEnabled(){ return (mChapterTpye | LogConstants.DDB_DEBUG_OFF) != LogConstants.DDB_DEBUG_OFF; }

    private static boolean isDebugAll(int chapterTpye){ return  (chapterTpye & LogConstants.DDB_DEBUG_ALL) == LogConstants.DDB_DEBUG_ALL; }

    public static void reset() {
        mChapterTpye = LogConstants.DDB_DEBUG_OFF;
        recommendationChapter = false;
        moreChapter = false;
        appsChapter = false;
        gamesChapter = false;
        castChapter = false;
        vodChapter = false;
        topMenu = false;
        commonLogs = false;
        debugAll = false;
        SystemProperties.set(LogConstants.prop_log_level, Integer.toString(mChapterTpye));
    }
}
