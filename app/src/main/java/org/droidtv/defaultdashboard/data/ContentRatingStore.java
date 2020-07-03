package org.droidtv.defaultdashboard.data;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.media.tv.TvContentRating;
import android.media.tv.TvContentRatingSystemInfo;
import android.media.tv.TvInputManager;
import android.net.Uri;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.tv.persistentstorage.ITvSettingsManager;
import org.droidtv.tv.persistentstorage.TvSettingsConstants;
import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ContentRatingStore {

    private static final String TAG = "ContentRatingStore";

    private Context mContext = null;

    private static final String SEC_INCOMPLETE = "section is incomplete or section ending tag is missing";
    private static final String MALFORMED = "Malformed XML: TAG MISSING";
    private static final String DOMAIN_SYSTEM_RATINGS = "com.android.tv";
    private static final String DOMAIN_NAME = "org.droidtv.playtv";

    private static final String TAG_RATING_SYSTEM_DEFINITIONS = "rating-system-definitions";
    private static final String TAG_RATING_SYSTEM_DEFINITION = "rating-system-definition";
    private static final String TAG_SUB_RATING_DEFINITION = "sub-rating-definition";
    private static final String TAG_RATING_DEFINITION = "rating-definition";
    private static final String TAG_SUB_RATING = "sub-rating";
    private static final String TAG_RATING = "rating";
    private static final String TAG_RATING_ORDER = "rating-order";

    private static final String ATTR_VERSION_CODE = "versionCode";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_TITLE = "title";
    private static final String ATTR_COUNTRY = "country";
    private static final String ATTR_ICON = "icon";
    private static final String ATTR_DESCRIPTION = "description";
    private static final String ATTR_CONTENT_AGE_HINT = "contentAgeHint";
    private static final String VERSION_CODE = "1";

    private static final int MAX_RATING_INDEX = 23;
    private static final int MAX_RATING_RATING = 22;

    private String mXmlVersionCode;
    private String mInstalledCountry;
    private String mRatingSystem;
    private String mDomain;
    private ConcurrentHashMap<String, RatingInfo> mRatingMapping;
    private boolean mRatingParsed;

    private static final Map<Integer, String> installedCountryMap = new HashMap<>();

    static {
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.ALBANIA, "AL");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.CZECHREP, "CZ");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.ISRAEL, "IL");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.MACEDONIAFYROM, "MK");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.ARMENIA, "AM");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.AUSTRIA, "AT");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.AZERBAIJAN, "AZ");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.BELARUS, "BY");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.BOSNIAANDHERZEGOVINA, "BA");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.BULGARIA, "BG");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.CROATIA, "HR");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.DENMARK, "DK");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.BELGIUM, "BE");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.ESTONIA, "EE");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.FINLAND, "FI");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.FRANCE, "FR");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.GEORGIA, "GE");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.GERMANY, "DE");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.GREECE, "GR");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.ITALY, "IT");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.HUNGARY, "HU");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.KAZAKHSTAN, "KZ");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.LATVIA, "LV");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.LITHUANIA, "LT");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.LUXEMBOURG, "LU");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.MONTENEGRO, "ME");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.NETHERLANDS, "NL");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.NORWAY, "NO");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.POLAND, "PL");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.PORTUGAL, "PT");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.ROMANIA, "RO");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.RUSSIA, "RU");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.SLOVAKIA, "SK");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.SLOVENIA, "SI");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.SERBIA, "RS");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.SPAIN, "ES");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.SWEDEN, "SE");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.SWITZERLAND, "CH");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.TURKEY, "TR");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.UKRAINE, "UA");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.NEWZEALAND, "NZ");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.SINGAPORE, "SG");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.MALAYSIA, "MY");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.TAIWAN, "TW");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.INDONESIA, "ID");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.OTHER, "US");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.THAILAND, "TH");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.AUSTRALIA, "AU");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.IRELAND, "IE");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.UK, "GB");
        installedCountryMap.put(TvSettingsDefinitions.InstallationCountryConstants.ICELAND, "IS");
    }


    public static class RatingInfo {
        private int mStringId;
        private int mContentAgeHint;

        public RatingInfo() {
            mStringId = -1;
            mContentAgeHint = -1;
        }

        public void setStringId(int stringid) {
            mStringId = stringid;
        }

        public void setContentAgeHint(int ageHint) {
            mContentAgeHint = ageHint;
        }

        public int getStringId() {
            return mStringId;
        }

        public int getContentAgeHint() {
            return mContentAgeHint;
        }
    }

    public ContentRatingStore(Context context) {
        mContext = context;
        mRatingMapping = new ConcurrentHashMap<>();
    }

    public void invalidate() {
        parseContentrating();
    }

    public ConcurrentHashMap<String, RatingInfo> getRatingStringMap() {
        return mRatingMapping;
    }

    private int getCurrentCountry() {
        ITvSettingsManager tvSettingsManager = DashboardDataManager.getInstance().getTvSettingsManager();
        return tvSettingsManager.getInt(TvSettingsConstants.INSTALLATIONCOUNTRY, 0, 0);
    }

    private void parseContentrating() {
        mRatingParsed = false;
        mDomain = "dummy";
        reset();
        TvInputManager manager = (TvInputManager) mContext.getSystemService(Context.TV_INPUT_SERVICE);
        List<TvContentRatingSystemInfo> infos = manager.getTvContentRatingSystemList();
        mInstalledCountry = installedCountryMap.get(getCurrentCountry());

        for (TvContentRatingSystemInfo info : infos) {
            XmlResourceParser parser = null;
            Uri uri = info.getXmlUri();
            String packageName = uri.getAuthority();
            int resId = (int) ContentUris.parseId(uri);
            parser = mContext.getPackageManager().getXml(packageName, resId, null);
            if (parser == null) {
                throw new IllegalArgumentException("Cannot get XML with URI " + uri);
            }
            if (info.isSystemDefined()) {
                try {
                    parseXML(parser, packageName);
                } catch (XmlPullParserException | IOException e) {
                }
            }
        }
    }

    private void parseXML(XmlResourceParser parser, String dom) throws XmlPullParserException, IOException {
        String domain = dom;
        if (domain.equals(DOMAIN_NAME)) {
            mDomain = DOMAIN_NAME;

            while (parser.next() == XmlPullParser.START_DOCUMENT) {
                // Nothing to do
            }
            int eventType = parser.getEventType();
            assertEquals(eventType, XmlPullParser.START_TAG, MALFORMED);
            assertEquals(parser.getName(), TAG_RATING_SYSTEM_DEFINITIONS, MALFORMED + TAG_RATING_SYSTEM_DEFINITIONS);
            boolean hasVersionAttr = false;
            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String attr = parser.getAttributeName(i);
                if (ATTR_VERSION_CODE.equals(attr)) {
                    hasVersionAttr = true;
                    mXmlVersionCode = parser.getAttributeValue(i);
                }
            }
            if (!hasVersionAttr) {
                throw new XmlPullParserException(MALFORMED
                        + " Should contains a version attribute" + " in "
                        + TAG_RATING_SYSTEM_DEFINITIONS);
            }
            parseXMLEndDoc(parser);
        } else {
        }
    }

    private void parseXMLEndDoc(XmlResourceParser parser) throws XmlPullParserException,
            IOException {
        int eventType = -1;
        while ((parser.next() != XmlPullParser.END_DOCUMENT) && (false == mRatingParsed)) {
            switch (parser.getEventType()) {
                case XmlPullParser.START_TAG:
                    if (TAG_RATING_SYSTEM_DEFINITION.equals(parser.getName())) {
                        parseRatingSystemDefinition(parser);
                    } else {
                        checkVersion(MALFORMED + TAG_RATING_SYSTEM_DEFINITION);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (TAG_RATING_SYSTEM_DEFINITIONS.equals(parser.getName())) {
                        eventType = parser.next();
                        assertEquals(eventType, XmlPullParser.END_DOCUMENT,
                                MALFORMED + TAG_RATING_SYSTEM_DEFINITIONS);
                    } else {
                        checkVersion(MALFORMED + TAG_RATING_SYSTEM_DEFINITIONS);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void parseRatingSystemDefinition(XmlResourceParser parser)
            throws XmlPullParserException, IOException {
        boolean countryMatched = false;
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String attr = parser.getAttributeName(i);
            switch (attr) {
                case ATTR_NAME:
                    mRatingSystem = parser.getAttributeValue(i);
                    break;
                case ATTR_COUNTRY:
                    for (String country : parser.getAttributeValue(i).split(
                            "\\s*,\\s*")) {
                        if (country.equals(mInstalledCountry)) {
                            countryMatched = true;
                            break;
                        }
                    }
                    break;
                case ATTR_TITLE:
                    break;
                case ATTR_DESCRIPTION:
                    break;
                default:
                    checkVersion(MALFORMED + " Unknown attribute " + attr + " in the " + TAG_RATING_SYSTEM_DEFINITION);
                    break;
            }
        }
        if (countryMatched) {
            countryMatched = false;
            parseRatingSystemDefinitionEndDoc(parser);
        }
    }

    private void parseRatingSystemDefinitionEndDoc(XmlResourceParser parser)
            throws XmlPullParserException, IOException {
        while ((parser.next() != XmlPullParser.END_DOCUMENT) && (false == mRatingParsed)) {
            switch (parser.getEventType()) {
                case XmlPullParser.START_TAG:
                    String tag = parser.getName();
                    switch (tag) {
                        case TAG_RATING_DEFINITION:
                            parseRatingDefinition(parser);
                            break;
                        case TAG_SUB_RATING_DEFINITION:
                            break;
                        case TAG_RATING_ORDER:
                            break;
                        default:
                            checkVersion(MALFORMED + " Unknown tag " + tag + " in  tag"
                                    + TAG_RATING_SYSTEM_DEFINITION);
                            break;
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (TAG_RATING_SYSTEM_DEFINITION.equals(parser.getName())) {
                        mRatingParsed = true;
                    } else {
                        checkVersion(MALFORMED + " Tag mismatch for " + TAG_RATING_SYSTEM_DEFINITION);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void parseRatingDefinition(XmlResourceParser parser)
            throws XmlPullParserException, IOException {

        String rating = null;
        int contentAgeHint = -1;
        Integer stringId = -1;

        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String attr = parser.getAttributeName(i);
            switch (attr) {
                case ATTR_NAME:
                    rating = parser.getAttributeValue(i);
                    break;
                case ATTR_TITLE:
                    break;
                case ATTR_DESCRIPTION:
                    stringId = parser.getAttributeResourceValue(i, 0);
                    break;
                case ATTR_ICON:
                    /*Nothing to DO*/
                    break;
                case ATTR_CONTENT_AGE_HINT:
                    try {
                        contentAgeHint = Integer.parseInt(parser
                                .getAttributeValue(i));
                    } catch (NumberFormatException e) {
                    }

                    if (contentAgeHint < 0) {
                        throw new XmlPullParserException(MALFORMED
                                + ATTR_CONTENT_AGE_HINT
                                + " should be a non-negative number");
                    }
                    break;
                default:
                    checkVersion(MALFORMED + " Unknown attribute " + attr
                            + " in attr " + TAG_RATING_DEFINITION);
                    break;
            }
        }
        try {
            TvContentRating contentRating = TvContentRating.createRating(mDomain, mRatingSystem, rating);
            String flattenString = contentRating.flattenToString();
            RatingInfo lRatingInfo = mRatingMapping.get(flattenString);
            if (lRatingInfo == null) {
                lRatingInfo = new RatingInfo();
            }
            lRatingInfo.setContentAgeHint(contentAgeHint);
            lRatingInfo.setStringId(stringId);
            mRatingMapping.put(flattenString, lRatingInfo);
        } catch (Exception e) {
        }

    }

    private void checkVersion(String msg) throws XmlPullParserException {
        if (!VERSION_CODE.equals(mXmlVersionCode)) {
            throw new XmlPullParserException(msg);
        }
    }

    private static void assertEquals(int a, int b, String msg)
            throws XmlPullParserException {
        if (a != b) {
            throw new XmlPullParserException(msg);
        }
    }

    private static void assertEquals(String a, String b, String msg)
            throws XmlPullParserException {
        if (!b.equals(a)) {
            throw new XmlPullParserException(msg);
        }
    }

    private void reset() {
        mRatingMapping.clear();
    }
}
