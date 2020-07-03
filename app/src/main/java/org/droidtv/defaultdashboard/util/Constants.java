package org.droidtv.defaultdashboard.util;

import android.content.Intent;

/**
 * Created by sandeep.kumar on 11/12/2017.
 */

public class Constants {
    public static final boolean DEBUG = false;

    public static final String PATH_IMAGES = "/images";
    public static final String PATH_MAIN_BACKGROUND = PATH_IMAGES + "/main_background";
    public static final String PATH_HOTEL_LOGO = PATH_IMAGES + "/hotel_logo";
    public static final String PATH_SHARING_BACKGROUND = PATH_IMAGES + "/sharing_background";
    public static final String PATH_CAST_APP_SHARING_BACKGROUND = "/data/misc/HTV/cast_background";

    public static final String PREF_KEY_SIDEPANEL_HIGHLIGHTED_TEXT_COLOR = "pref_key_sidepanel_highlighted_text_color";
    public static final String PREF_KEY_SIDEPANEL_NON_HIGHLIGHTED_TEXT_COLOR = "pref_key_sidepanel_non_highlighted_text_color";
    public static final String PREF_KEY_SIDEPANEL_BACKGROUND_COLOR = "pref_key_sidepanel_background_color";

    public static final String PREF_KEY_MAIN_BACKGROUND_ENABLED = "pref_key_main_background_enabled";
    public static final String PREF_KEY_MAIN_BACKGROUND_COLOR_FILTER = "pref_key_main_background_color_filter";

    public static final String PREF_KEY_SHOW_ACCOUNT_ICON = "pref_key_show_account_icon";
    public static final String PREF_KEY_SHOW_ASSISTANT_ICON = "pref_key_show_assistant_icon";
    public static final String PREF_KEY_ACCOUNT_ACCESS_GRANT_PERMISSION = "google_account_access_permission";

    public static final String MAPPED_DDB_SETTINGS_FILE_NAME = "Dashboardsettings.xml";
    public static final String SHARED_PREF_DEVICE_CONTEXT_DDB = "ddb_shared_prefs";
    public static final String SHARED_PREF_APP_CONTEXT_DDB = "ddb_shared_prefs";

    public static final String FILE_NAME_APP_LIST = "AppList.xml";
    public static final String NAME_APP_LIST = "AppList";
    public static final String ITEM_NAME_APP = "App";

    public static final int GUEST_ACCOUNT_PROFILE_OFF = 0;
    public static final int GUEST_ACCOUNT_PROFILE_ON = 1;

    public static final String MORE_SECTION_CHAPTER_TITLE_SETTINGS = "Settings";
    public static final String MORE_SECTION_CHAPTER_TITLE_FEATURES = "Features";
    public static final String MORE_SECTION_CHAPTER_TITLE_PERSONAL = "Personal";

    public static final String MORE_SECTION_CHAPTER_SHELF_ITEM_PICTURE = "Picture";
    public static final String MORE_SECTION_CHAPTER_SHELF_ITEM_SOUND = "Sounds";
    public static final String MORE_SECTION_CHAPTER_SHELF_ITEM_AMBILIGHT = "Ambilight";
    public static final String MORE_SECTION_CHAPTER_SHELF_ITEM_PICTURE_FORMAT = "Picture format";
    public static final String MORE_SECTION_CHAPTER_SHELF_ITEM_MENU_LANGUGAGE = "Menu Language";
    public static final String MORE_SECTION_CHAPTER_SHELF_ITEM_EDIT_lAUNCHER = "Edit Launcher";
    public static final String PACKAGE_NAME_NET_TV = "org.droidtv.nettv";

    public static final String INTENT_ACTION_DEFAULT_DASHBOARD = "org.droidtv.intent.action.DEFAULT_DASHBOARD";
    public static final String INTENT_ACTION_MAIN = "android.intent.action.MAIN";
    public static final String INTENT_ACTION_APPS = "org.droidtv.intent.action.APPS";
    public static final String INTENT_ACTION_GUEST_MENU = "org.droidtv.intent.action.GUEST_MENU";
    public static final String INTENT_ACTION_APP_SYNC_CONTROL_SERVICE = "org.droidtv.intent.action.APPSYNCCONTROLSERVICE";
    public static final String INTENT_CATEGORY_PROFFESIONAL_DISPLAY_CONFIGURATION = "com.philips.professionaldisplaysolutions.intent.category.CONFIG_HTV_HOME";
    public static final String INTENT_NOTIFY_APPLIST_UPDATING_STATUS = "com.xmic.cloneappmgr.NOTIFY_APPLIST_UPDATING_STATUS";
    public static final String EXTRA_CONFIG_MODE = "CONFIG_MODE";
    public static final String ACTION_ANDROID_BOOT_COMPLETED = Intent.ACTION_BOOT_COMPLETED;
    public static final String ACTION_MTK_TV_READY = "mtk.intent.tvremoteservice_ready";
    public static final String ACTION_LOCAL_BROADCAST_BOOT_COMPLETED = "org.droidtv.intent.action.DDB_LOCAL_BOOT_COMPLETED";
    public static final String ACTION_ANDROID_LOCALE_CHANGED = "android.intent.action.LOCALE_CHANGED";

    public static final String ACTION_LOCAL_BROADCAST_LOCKED_BOOT_COMPLETED = "org.droidtv.intent.action.DDB_LOCAL_LOCKED_BOOT_COMPLETED";
    public static final String USER_ACCOUNT_ANDROID_INTENT_PACKAGE_NAME = "com.android.tv.settings";
    public static final String USER_ACCOUNT_ANDROID_INTENT_CLASS_NAME = "com.android.tv.settings.MainSettings";
    public static final String EXTRA_MENU_MODE = "menuMode";
    public static final String EXTRA_SHOW_ACCOUNTS = "ShowAccounts";
    public static final int GOOGLE_SIGN_IN_DELAY = 2* 60 * 1000;

    public static final String PICTURE_SETTINGS_INTENT = "htv.android.settings.PICTURE_SETTINGS";
    public static final String SOUND_SETTINGS_INTENT = "htv.android.settings.SOUND_SETTINGS";
    public static final String AMBILIGHT_SETTINGS_INTENT = "htv.android.settings.AMBILIGHT_SETTINGS";
    public static final String PICTURE_FORMAT_INTENT = "htv.android.settings.PICTURE_FORMAT_SETTINGS";
    public static final String MORE_SECTION_CHAPTER_SHELF_ITEM_MENU_LANGUAGE_INTENT = "";

    public static final String MORE_SECTION_CHAPTER_SHELF_ITEM_EDIT_SET_ALARM = "Set Alarm";
    public static final String MORE_SECTION_CHAPTER_SHELF_ITEM_EDIT_SLEEP_TIMER = "Sleep timer";
    public static final String MORE_SECTION_CHAPTER_SHELF_ITEM_INTERNET_HOTSPOT = "Internet Hotspot";
    public static final String MORE_SECTION_CHAPTER_SHELF_ITEM_WEATHER = "Weather";
    public static final String MORE_SECTION_CHAPTER_SHELF_MY_CHOICE = "MyChoice";

    public static final String ACTION_ALARM_INTENT = "org.droidtv.HtvClockAlarm.HTV_CLOCK";
    public static final String SYSTEM_PROPERTY_CLOCK_SOURCE = "sys.droidtv.clock.source";
    public static final String SET_ALARM_MESSAGE = "current clock source = ";
    public static final String SET_ALARM_SOURCE_VALUE = "-1";
    public static final String ACTION_VALID_CLOCK_SOURCE_ACQUIRED = "org.droidtv.tv.tvclock.VALIDSOURCE";

    public static final String SET_ALARM_EXTRA_CLASS_INTENT = "HTV_CLOCK_ACTION_SOURCE";
    public static final String SET_ALARM_EXTRA_PACKAGE_INTENT = "org.droidtv.HtvClockAlarm.ALARM_SETTINGS";
    public static final String SYSTEM_PROPERTY_HTV_ALARM_STATE = "persist.alarm.state";
    public static final String SYSTEM_PROPERTY_HTV_ALARM_TIME_HOURS = "persist.alarm.time.high";
    public static final String SYSTEM_PROPERTY_HTV_ALARM_TIME_MINUTES = "persist.alarm.time.low";
    public static final String CLOCK_FORMAT_AM_PM = "hh:mm";
    public static final String CLOCK_FORMAT_AM_PM_A = "hh:mm a";
    public static final String CLOCK_FORMAT_24 = "HH:mm";

    public static final String SLEEP_TIMER_INTENT = "htv.android.settings.SLEEP_TIME_SETTINGS";
    public static final String INTERNET_HOTSPOT_INTENT = "org.droidtv.hotspot.castwizardactivity";
    public static final String INTERNET_HOTSPOT_CATEGORY_INTENT = "android.intent.category.DEFAULT";
    public static final String INTERNET_HOTSPOT_INTENT_EXTRA_KEY = "mode";
    public static final String INTERNET_HOTSPOT_INTENT_EXTRA_VALUE = "2011";
    public static final String WEATHER_ACTIVITY_INTENT = "org.droidtv.weather.WeatherActivity";
    public static final String WEATHER_PACKAGE_INTENT = "org.droidtv.weather";
    public static final String ACTION_MY_CHOICE_PIN = "org.droidtv.intent.action.MYCHOICE_PIN_DIALOG";
    public static final String EXTRA_MY_CHOICE_PIN_DIALOG_KEYS = "Keys";
    public static final String ACTION_LAUNCH_HTV_GUIDE = "org.droidtv.action.HTV_GUIDE";
    public static final String ACTION_LAUNCH_CTV_GUIDE = "org.droidtv.action.CTV_GUIDE";
    public static final String EXTRA_TALK_BALK_SHOW = "ShowTTS";
    public static final String PACKAGE_NAME_TTS = "com.android.tv.settings";
    public static final String CLASS_NAME_TTS = "com.android.tv.settings.MainSettings";
    public static final String CATEGORY_JEDI_THIRD_PARTY_EPG = "com.philips.professionaldisplaysolutions.jedi.intent.category.PRO_TV_EPG";
    public static final String EXTRA_JEDI_SHOW_UI = "com.philips.professionaldisplaysolutions.jedi.SHOW_UI";

    public static final String MORE_SECTION_CHAPTER_SHELF_ITEM_Bill = "Bill";
    public static final String MORE_SECTION_CHAPTER_SHELF_ITEM_MESSAGES = "Messages";
    public static final String MORE_SECTION_CHAPTER_SHELF_ITEM_MESSAGE_DISPLAY = "Message Display";
    public static final String MORE_SECTION_CHAPTER_SHELF_ITEM_MANAGE_ACCOUNT = "Manage account";
    public static final String MORE_SECTION_CHAPTER_SHELF_ITEM_CLEAR_HISTORY = "Clear History";

    public static final String BILL_ACTION_INTENT = "Intent.ACTION_MAIN";
    public static final String BILL_CLASS_INTENT = "org.droidtv.bill";
    public static final String BILL_ACTIVITY_INTENT = "org.droidtv.bill.MainActivity";
    public static final String MESSAGES_ACTION_INTENT = "Intent.ACTION_MAIN";
    public static final String MESSAGES_PACKAGE_INTENT = "org.droidtv.message";
    public static final String MESSAGES_ACTIVITY_INTENT = "org.droidtv.message.MainActivity";
    public static final String MESSAGE_DISPLAY_INTENT = "htv.android.settings.MESSAGE_DISPLAY_SETTINGS";
    public static final String MESSAGE_DISPLAY_PACKAGE_INTENT = "org.droidtv.settings";
    public static final String MESSAGE_DISPLAY_ACTIVITY_INTENT = "org.droidtv.settings.setupmenu.SetupMenuActivity";
    public static final String MANAGE_ACCOUNT_INTENT = "android.provider.Settings.ACTION_SETTINGS";
    public static final String CLEAR_HISTORY_INTENT = "htv.android.action.DELETE_ACCOUNT_CLEAR_HISTORY";
    public static final String COUNTRY_FILTER_INTENT = "org.droidtv.defaultdashboard";
    public static final String COUNTRY_FILTER_CLASS_NAME_INTENT = "org.droidtv.defaultdashboard.CountryListActivity";


    public static final String ACTION_FACTORY_RESET = "org.droidtv.settings.FACTORY_RESET";
    public static final String ACTION_USB_BREAKOUT = "org.droidtv.tv.usbbreakin.ACTION_USB_BREAKOUT";
    public static final String ACTION_USB_BREAKIN = "org.droidtv.tv.usbbreakin.ACTION_USB_BREAKIN";
    public static final String LOCAL_ACTION_USB_BREAKOUT = "ACTION_USB_BREAKOUT";
    public static final String LOCAL_ACTION_USB_BREAKIN = "ACTION_USB_BREAKIN";
    public static final String LOCAL_MEDIA_SCANNER_STARTED = "MEDIA_SCANNER_STARTED";
    public static final String LOCAL_MEDIA_SCANNER_FINISHED = "MEDIA_SCANNER_FINISHED";

    public static final String LOCAL_CLEAR_APP_DATA = "ACTION_CLEAR_DATA_TRIGGERED";
    public static final String CLEAR_APP_DATA_INTENT = "org.droidtv.intent.action.api.CLEAR_DATA";

    public static final String OEM_START_INTENT_NOTIFICATION = "OEM_NOTFICATION_RECEIVED";
    public static final String OEM_START_INTENT = "org.droidtv.tv.intent.action.OEM_SHELF_NOTIFICATION_START";
    public static final String OEM_SHELF_NOTIFICATION = "org.droidtv.tv.intent.action.OEM_SHELF_NOTIFICATION";
    public static final String NOTIFICATION_TITLE = "org.droidtv.tv.intent.extra.notification_title";
    public static final String NOTIFICATION_SORT = "org.droidtv.tv.intent.extra.notification_sort";
    public static final String NOTIFICATION_ID = "org.droidtv.tv.intent.extra.notification_id";
    public static final String NOTIFICATION_BITMAP = "org.droidtv.tv.intent.extra.notification_bitmap";
    public static final String NOTIFICATION_PENDING_INTENT = "org.droidtv.tv.intent.extra.notification_pi";
    public static final String NOTIFICATION_COLOR = "org.droidtv.tv.intent.extra.notification_color";
    public static final String UPDATE_ADVERT_NOTIFICATION = "org.droidtv.tv.smarttv.permission.UPDATE_ADVERTISEMENT";
    public static final String NOTIFICATION_REPLACE_PACKAGE = "org.droidtv.tv.intent.extra.notification_replace_package";
    public static final int DEFAULT_PACKAGE_COLOR = 745175;


    public static final String ACTION_OSD_SUPPRESS = "org.droidtv.localosddisplay.OSD_SUPPRESS";
    public static final String ACTION_CLONE_IN = "org.droidtv.defaultdashboard.action.CLONEIN";
    public static final String ACTION_STANDBY = "org.droidtv.defaultdashboard.action.STANDBY";
    public static final String ACTION_STOP_DDB = "org.droidtv.defaultdashboard.action.STOPDDB";
    public static final String APPS_TYPE_PORTAL = "Portal";
    public static final int EXTRA_VALUE_CHANNEL_LIST = 9;
    public static final String PERMISSION_TPV_BROADCAST = "org.droidtv.permission.TPVISION_BROADCASTS";
    public static final String PACKAGE_NAME_CLONEAPP_MGR = "com.xmic.cloneappmgr";
    public static final String CLASS_NAME_CLONEAPP_MGR_SERVICE = "com.xmic.cloneappmgr.CloneAppMgrService";
    public static final String FORMAT_REVIEW_RATING = "#.#";
    public static final String APP_TYPE_PREVIEW = "TYPE_PREVIEW";
    public static final int SDK_TARGET_VERSION_O = 26;
    public static final String CATEGORY_SMART_INFO = "com.philips.professionaldisplaysolutions.jedi.intent.category.SMART_INFO";
    public static final long SIXTY_FOUR_MB = 64 * 1024 * 1024;
    public static final String COLUMN_NAME_HTVAPPLIST_HITCOUNT = "hitcount";
    public static final String EXTRA_LAUNCH_MODE_SWOF = "LAUNCH_MODE_SWOF";
    public static final String EXTRA_LAUNCH_MODE_SWOF_REASON_COLD_BOOT = "LAUNCH_MODE_SWOF_REASON_COLD_BOOT";

    public static String INTENT_NAU_MESSAGE_COUNTER_CHANGE = "org.droidtv.intent.action.message.nau_message_counter_change";
    public static String INTENT_ACTION_GUEST_CHECK_IN_STATUS_CHANGE = "org.droidtv.intent.action.pms.check_in";
    public static String INTENT_ACTION_GUEST_CHECK_OUT_STATUS_CHANGE = "org.droidtv.intent.action.pms.check_out";
    public static String INTENT_GUEST_CHECK_IN_SYSTEM_PROPERTY_KEY = "persist.sys.roomstatus";
    public static String INTENT_GUEST_CHECK_IN_STATUS_OCCUPIED_VALUE = "occupied";
    public static final int MAIN_BACKGROUND_WIDTH = 1920;
    public static final int MAIN_BACKGROUND_HEIGHT = 1080;
    public static final int HOTEL_LOGO_WIDTH = 476;
    public static final int HOTEL_LOGO_HEIGHT = 320;


    public static final int RC_SIGN_IN = 001;
    public static final String ACCOUNT_TYPE_GOOGLE = "com.google";
    public static final String ACCOUNT_TYPE_GMAIL = "gmail.com";
    public static final String ACCOUNT_HTTP_ERROR_TAG = "getHttpInputStreamConnection";

    public static final int REQUEST_CODE_ACTIVITY_GATEWAY_PAGE = 10;
    public static final String ACTION_CAST_WIZARD = "org.droidtv.hotspot.castwizardactivity";
    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_LAUNCH_BY_DDB = "launch_by_DDB";
    public static final String EXTRA_PRCS_MY_CHOICE = "prcs_MyChoice";
    public static final int CAST_MODE_GOOGLE_CAST_WIZARD_ACTIVITY = 2010;
    public static final String ACTION_SHOW_CAST_READY_SCREEN = "org.droidtv.defaultdashboard.intent.action.SHOW_CAST_READY_SCREEN";

    public static final int REQUEST_CODE_MYCHOICE_PIN_DIALOG = 2;

    public static final String EXTRA_CHANNEL_ID = "channel_id";
    public static final String EXTRA_ZAP_METHOD = "zap_method";
    public static final String EXTRA_METHOD_ID = "method_id";
    public static final String EXTRA_UI_NAME = "ui_name";


    public static final String ACTION_SMARTINFO_BROWSER = "org.droidtv.intent.action.SMART_INFO";
    public static final String PACKAGE_NAME_NETTVBROWSER = "org.droidtv.nettvbrowser";
    public static final String EXTRA_SMARTINFO_RELATIVE_URL = "org.droidtv.relativeurl";
    public static final String EXTRA_SMARTINFO_URL_TYPE = "org.droidtv.urltype";
    public static final String SMARTINFO_URL_TYPE_MAIN = "main";
    public static final String SMARTINFO_URL_TYPE_TILE = "tile";
    public static final String ACTION_SMARTINFO_DOWNLOAD_COMPLETED = "org.intent.action.SMARTINFO_DOWNLOAD_COMPLETED";
    public static final String ACTION_CLONE_RESULT_CDB_SMARTINFO_BANNER = "org.droidtv.intent.action.clone.result.CSB";
    public static final String EXTRA_SMARTINFO_CLONED_RESULT = "smartinfo_browser_cloned";
    public static final String SMART_INFO_BROWSER_INDEX_FILE_NAME_REGEX = "(SmartInfoBrowserIndex\\.x?html)|(index\\.x?html)";
    public static final String SMART_INFO_BROWSER_METADATA_FILE_NAME = "SmartInfoBrowserMetaData.xml";
    public static final String SMART_INFO_FILE_PATH_LOCAL = "/data/misc/HTV/Clone/Clone_data/SmartInfoBrowser";
    public static final String SMART_INFO_FILE_PATH_SERVER = "/data/misc/HTV/Clone/Clone_data/ServerSmartInfoBrowser";
    public static final String SMART_INFO_EU_FILE_PATH_USB = "/TPM181HE_CloneData/MasterCloneData/SmartInfoBrowser";
    public static final String SMART_INFO_NAFTA_FILE_PATH_USB = "/TPM191HN_CloneData/MasterCloneData/SmartInfoBrowser";
    public static final String CONTENT_TYPE_EMPTY_RECOMMENDATION = "-empty-recommendation-";
    public static final String CONTENT_TYPE_APP_RECOMMENDATION = "-app-recommendation-";
    public static final String CONTENT_TYPE_SMART_INFO_RECOMMENDATION = "SmartInfo";
    public static final String CONTENT_TYPE_VOD_RECOMMENDATION = "VOD";
    public static final String CONTENT_TYPE_GAMING_RECOMMENDATION = "Gaming";
    public static final String GOOGLECAST_TERM_AND_CONDITION_FILE_PATH ="/data/misc/HTV/Clone/Clone_data/ProfessionalAppsData/GooglecastHotspotTNC/GooglecastHotspotTNC.json";

    public static final int CHANNEL_CONTENT_TYPE_APP_RECOMMENDATION = 0;
    public static final int CHANNEL_CONTENT_TYPE_VOD_RECOMMENDATION = 1;
    public static final int CHANNEL_CONTENT_TYPE_GAMES_RECOMMENDATION = 2;
    public static final int CHANNEL_CONTENT_TYPE_SMART_INFO_RECOMMENDATION = 4;
    public static final int CHANNEL_CONTENT_TYPE_EMPTY_RECOMMENDATION = 5;

    public static final String APP_CATEGORY_GAME = "Games";

    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_ALBANIAN = 0;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_ARABIC = 1;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_BAHASA_MELAYU = 2;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_BULGARIAN = 3;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_SIMPLIFIEDCHINESE = 4;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_TRADITIONALCHINESE = 5;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_CROATIAN = 6;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_CZECH = 7;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_DANISH = 8;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_DUTCH = 9;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_ENGLISH_UK = 10;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_ESTONIAN = 11;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_FINNISH = 12;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_FRENCH = 13;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_GERMAN = 14;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_GREEK = 15;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_HEBREW = 16;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_HUNGARIAN = 17;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_INDONESIAN = 18;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_IRISH = 19;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_ITALIAN = 20;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_KAZAKH = 21;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_LATVIAN = 22;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_LITHUANIAN = 23;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_MACEDONIAN = 24;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_NORWEGIAN = 25;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_POLISH = 26;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_PORTUGUESE = 27;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_BRAZILIAN_PORTUGUESE = 28;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_ROMANIAN = 29;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_RUSSIAN = 30;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_SERBIAN = 31;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_SLOVAK = 32;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_SLOVENIAN = 33;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_SPANISH = 34;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_SPANISH_US = 35; //LATIN SPAINISH
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_SWEDISH = 36;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_THAI = 37;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_TURKISH = 38;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_UKRAINIAN = 39;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_VIETNAMESE = 40;
    public static final int LANGUAGE_MAPPED_ARRAY_ROW_ID_ENGLISH_US = 41;


    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_NETHERLANDS = 0;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_BELGIUM = 1;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_LUXEMBOURG = 2;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_FRANCE = 3;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_GERMANY = 4;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_SWITZERLAND = 5;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_AUSTRIA = 6;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_UK = 7;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_IRELAND = 8;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_SPAIN = 9;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_PORTUGAL = 10;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_ITALY = 11;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_NORWAY = 12;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_SWEDEN = 13;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_DENMARK = 14;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_FINLAND = 15;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_GREECE = 16;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_TURKEY = 17;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_RUSSIA = 18;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_UKRAINE = 19;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_KAZAKHSTAN = 20;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_POLAND = 21;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_CZECHREP = 22;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_SLOVAKIA = 23;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_HUNGARY = 24;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_BULGARIA = 25;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_ROMANIA = 26;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_LATVIA = 27;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_ESTONIA = 28;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_LITHUANIA = 29;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_SLOVENIA = 30;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_SERBIA = 31;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_CROATIA = 32;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_ARGENTINA = 33;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_BRAZIL = 34;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_AUSTRALIA = 35;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_NEWZEALAND = 36;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_USA = 37;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_INTERNATIONAL = 38;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_CHINA = 39;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_CANADA = 40;
    public static final int COUNTRY_MAPPED_ARRAY_ROW_ID_MEXICO = 41;


    public static final String APPS_COUNTRY_CODE_ARGENTINA = "AR";
    public static final String APPS_COUNTRY_CODE_AUSTRIA = "AT";
    public static final String APPS_COUNTRY_CODE_AUSTRALIA = "AU";
    public static final String APPS_COUNTRY_CODE_BELGIUM = "BE";
    public static final String APPS_COUNTRY_CODE_BULGARIA = "BG";
    public static final String APPS_COUNTRY_CODE_BRAZIL = "BR";
    public static final String APPS_COUNTRY_CODE_SWITZERLAND = "CH";
    public static final String APPS_COUNTRY_CODE_CZECH_REPUBLIC = "CZ";
    public static final String APPS_COUNTRY_CODE_GERMANY = "DE";
    public static final String APPS_COUNTRY_CODE_DENMARK = "DK";
    public static final String APPS_COUNTRY_CODE_ESTONIA = "EE";
    public static final String APPS_COUNTRY_CODE_SPAIN = "ES";
    public static final String APPS_COUNTRY_CODE_FINLAND = "FI";
    public static final String APPS_COUNTRY_CODE_FRANCE = "FR";
    public static final String APPS_COUNTRY_CODE_UNITED_KINGDOM = "GB";
    public static final String APPS_COUNTRY_CODE_GREECE = "GR";
    public static final String APPS_COUNTRY_CODE_CROATIA = "HR";
    public static final String APPS_COUNTRY_CODE_HUNGARY = "HU";
    public static final String APPS_COUNTRY_CODE_IRELAND = "IE";
    public static final String APPS_COUNTRY_CODE_ITALY = "IT";
    public static final String APPS_COUNTRY_CODE_KAZAKISTHAN = "KZ";
    public static final String APPS_COUNTRY_CODE_LITHUANIA = "LT";
    public static final String APPS_COUNTRY_CODE_LUXEMBERG = "LU";
    public static final String APPS_COUNTRY_CODE_LATVIA = "LV";
    public static final String APPS_COUNTRY_CODE_NETHERLAND = "NL";
    public static final String APPS_COUNTRY_CODE_NORWAY = "NO";
    public static final String APPS_COUNTRY_CODE_NEW_ZEALAND = "NZ";
    public static final String APPS_COUNTRY_CODE_POLAND = "PL";
    public static final String APPS_COUNTRY_CODE_PORTUGAL = "PT";
    public static final String APPS_COUNTRY_CODE_ROMANIA = "RO";
    public static final String APPS_COUNTRY_CODE_SERBIA = "RS";
    public static final String APPS_COUNTRY_CODE_RUSSIA = "RU";
    public static final String APPS_COUNTRY_CODE_SWEDEN = "SE";
    public static final String APPS_COUNTRY_CODE_SLOVENIA = "SI";
    public static final String APPS_COUNTRY_CODE_SLOVAKIA = "SK";
    public static final String APPS_COUNTRY_CODE_TURKEY = "TR";
    public static final String APPS_COUNTRY_CODE_UKRAINE = "UA";
    public static final String APPS_COUNTRY_CODE_USA = "US";
    public static final String APPS_COUNTRY_CODE_INTERNATIONAL = "ZZ";
    public static final String APPS_COUNTRY_CODE_ALL = "ALL";


    public static final String APPS_CATEGORY_ADULT = "Adult";
    public static final String APPS_CATEGORY_CHILDREN = "Children";
    public static final String APPS_CATEGORY_ENTERTAINMENT = "Entertainment";
    public static final String APPS_CATEGORY_FINANCIAL = "Financial";
    public static final String APPS_CATEGORY_HEALTH = "Health and Fitness";
    public static final String APPS_CATEGORY_LIFE_STYLE = "Lifestyle";
    public static final String APPS_CATEGORY_LOCAL_INFO = "Local Info";
    public static final String APPS_CATEGORY_MAGAZINE = "Magazines";
    public static final String APPS_CATEGORY_MOVIES = "Movies";
    public static final String APPS_CATEGORY_MUSIC = "Music";
    public static final String APPS_CATEGORY_NEWS = "News";
    public static final String APPS_CATEGORY_OTHER = "Other";
    public static final String APPS_CATEGORY_SOCIAL = "Social";
    public static final String APPS_CATEGORY_SPORTS = "Sports";
    public static final String APPS_CATEGORY_TECHNOLOGY = "Technology";
    public static final String APPS_CATEGORY_TRAVEL = "Travel";
    public static final String APPS_CATEGORY_TV_CHANNELS = "TV Channels";
    public static final String APPS_CATEGORY_WEATHER = "Weather";
    public static final String APPS_CATEGORY_MY_APPS = "MyApps";


    public static enum MyChoicePackage {
        MY_CHOICE_FREE_PKG,
        MY_CHOICE_PKG_1,
        MY_CHOICE_PKG_2
    }

    public static enum ThemeTvGroup {
        THEME_TV_GROUP_1,
        THEME_TV_GROUP_2,
        THEME_TV_GROUP_3,
        THEME_TV_GROUP_4,
        THEME_TV_GROUP_5,
        THEME_TV_GROUP_6,
        THEME_TV_GROUP_7,
        THEME_TV_GROUP_8,
        THEME_TV_GROUP_9,
        THEME_TV_GROUP_10
    }

    public static final float SHELF_ITEM_FOCUS_ELEVATION = 6f;

    public static final String GOOGLE_CAST_ICON_UNICODE_CODE = "\uE152";
    public static final String DOT_SEPARATOR_UNICODE_CODE = "\u2022";
    public static final String GLOBE_ICON_UNICODE_CODE = "\uE0E8";
    public static final String ICON_STAR = " " + "\uE0BB";

    public static final String ICONO_FONT_PATH = "fonts/Icono-Regular.ttf";

    public static final int HDMI1_MHL_PORT_ID = 1;

    public static final String EXTRA_TUNE_FROM = "trigger_source";
    public static final String EXTRA_TUNE_VALUE = "DDB";
}
