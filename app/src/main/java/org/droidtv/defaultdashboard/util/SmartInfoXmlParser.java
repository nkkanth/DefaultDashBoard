package org.droidtv.defaultdashboard.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.data.model.SmartInfo;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sandeep.kumar on 08/02/2018.
 */

public class SmartInfoXmlParser extends ContextualObject {

    private static final String TAG = "SmartInfoXmlParser";
    private List<SmartInfo> mSmartInfoData;
    private String mIconPath;
    private String mTitle;
    private String mDescription;
    private String mSmartInfoMainUrl;
    private String mLocaleLanguage;
    private Map<String, String> mLanguageMap;
    private boolean mSmartInfoRootSectionStarted;

    private static final String XML_ATTR_LANG = "xml:lang";

    public SmartInfoXmlParser(Context context) {
        super(context);
        initLanguageMap();
        mLocaleLanguage = context.getResources().getConfiguration().getLocales().get(0).getISO3Language();
    }

    public void parseXml(String xmlFilePath) {
        mSmartInfoRootSectionStarted = false;
        BufferedInputStream inputStream = null;
        String specificLanguage = getLanguageFromMap(mLocaleLanguage);
        String[] attributeValues = {mLocaleLanguage, specificLanguage};
        String value = "";
        SmartInfo.Builder smartInfoBuilder = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(xmlFilePath));
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xmlFactoryObject.newPullParser();
            parser.setInput(inputStream, "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        mSmartInfoData = new ArrayList<SmartInfo>();
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("SmartInfoDetails")) {
                            mSmartInfoRootSectionStarted = true;
                        } else if (parser.getName().equals("Title")) {
                            value = getNextTextByAttribute(parser, XML_ATTR_LANG, attributeValues);
                            if (mSmartInfoRootSectionStarted && !TextUtils.isEmpty(value)) {
                                mTitle = value;
                            }
                        } else if (parser.getName().equals("Description")) {
                            value = getNextTextByAttribute(parser, XML_ATTR_LANG, attributeValues);
                            if (!TextUtils.isEmpty(value)) {
                                if (mSmartInfoRootSectionStarted) {
                                    mDescription = value;
                                } else if (smartInfoBuilder != null) {
                                    smartInfoBuilder.setDescription(value);
                                }
                            }
                        } else if (parser.getName().equals("StartPageURL")) {
                            value = getNextTextByAttribute(parser, XML_ATTR_LANG, attributeValues);
                            if (mSmartInfoRootSectionStarted && !TextUtils.isEmpty(value)) {
                                mSmartInfoMainUrl = value.substring(1);
                            }
                        } else if (parser.getName().equals("Tile")) {
                            smartInfoBuilder = new SmartInfo.Builder(getContext());
                        } else if (parser.getName().equals("Icon") || parser.getName().equals("IconLandscape")) {
                            value = getNextTextByAttribute(parser, XML_ATTR_LANG, attributeValues);
                            if (!TextUtils.isEmpty(value)) {
                                if (value.startsWith(".")) {
                                    // The icon url will be in this format :  ./xxx/xxx.../xxx.png or ./xxx/xxx.../xxx.jpg , etc
                                    // So we remove the leading '.' dot character before setting the value in the smart info object
                                    value = value.substring(1);
                                } else if (!value.startsWith("/")) {
                                    // We expect the icon url to be atleast in this format : /xxx/xxx.../xxx.png or /xxx/xxx.../xxx.jpg, etc
                                    // If not, then we have to add a leading "/" as a path separator
                                    value = "/".concat(value);
                                }
                                if (mSmartInfoRootSectionStarted) {
                                    mIconPath = value;
                                } else if (smartInfoBuilder != null) {
                                    smartInfoBuilder.setIconPath(value);
                                }
                            }
                        } else if (parser.getName().equals("Label")) {
                            value = getNextTextByAttribute(parser, XML_ATTR_LANG, attributeValues);
                            if (smartInfoBuilder != null && !TextUtils.isEmpty(value)) {
                                smartInfoBuilder.setTitle(value);
                            }
                        } else if (parser.getName().equals("Link")) {
                            value = getNextTextByAttribute(parser, XML_ATTR_LANG, attributeValues);
                            if (smartInfoBuilder != null && !TextUtils.isEmpty(value)) {
                                smartInfoBuilder.setUrl(value.substring(1));
                                smartInfoBuilder.setUrlType(Constants.SMARTINFO_URL_TYPE_TILE);
                            }
                        }

                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("SmartInfoDetails")) {
                            mSmartInfoRootSectionStarted = false;
                        } else if (parser.getName().equals("Tile")) {
                            // Set the if of this smart info object to simply its index in the collection
                            if(null!= smartInfoBuilder){
                                smartInfoBuilder.setId(mSmartInfoData.size());
                                SmartInfo smartInfo = smartInfoBuilder.build();
                                if (!TextUtils.isEmpty(smartInfo.getUrl())) {
                                    mSmartInfoData.add(smartInfo);
                                }
                            }
                        }

                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "#### smart info xml file parse error");
           Log.e(TAG,"Exception :" +e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                   Log.e(TAG,"Exception :" +e.getMessage());
                }
            }
        }
    }

    public List<SmartInfo> getSmartInfoData() {
        return mSmartInfoData;
    }

    public String getSmartInfoIconPath() {
        return mIconPath;
    }

    public String getSmartInfoTitle() {
        return mTitle;
    }

    public String getSmartInfoDescription() {
        return mDescription;
    }

    public String getSmartInfoMainUrl() {
        return mSmartInfoMainUrl;
    }

    private void initLanguageMap() {
        mLanguageMap = new HashMap<String, String>();
        mLanguageMap.put("alb", "sqi"); /* English name of Language: Albanian */
        mLanguageMap.put("arm", "hye"); /* Armenian */
        mLanguageMap.put("baq", "eus"); /* Basque */
        mLanguageMap.put("bur", "mya"); /* Burmese */
        mLanguageMap.put("chi", "zho"); /* Chinese */
        mLanguageMap.put("cze", "ces"); /* Czech */
        mLanguageMap.put("dut", "nld"); /* Dutch, Flemish */
        mLanguageMap.put("fre", "fra"); /* French */
        mLanguageMap.put("geo", "kat"); /* Georgian */
        mLanguageMap.put("ger", "deu"); /* German */
        mLanguageMap.put("gre", "ell"); /* Greek, Modern */
        mLanguageMap.put("ice", "isl"); /* Icelandic */
        mLanguageMap.put("mac", "mkd"); /* Macedonian */
        mLanguageMap.put("may", "msa"); /* Malay */
        mLanguageMap.put("mao", "mri"); /* Maori */
        mLanguageMap.put("rum", "ron"); /* Moldavian, Moldovan, Romanian */
        mLanguageMap.put("per", "fas"); /* Persian */
        mLanguageMap.put("slo", "slk"); /* Slovak */
        mLanguageMap.put("tib", "bod"); /* Tibetan */
        mLanguageMap.put("wel", "cym"); /* Welsh */
    }

    private String getLanguageFromMap(String language) {
        for (Map.Entry<String, String> entry : mLanguageMap.entrySet()) {
            if (entry.getValue().equals(language)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private String getNextTextByAttribute(XmlPullParser parser, String attributeName, String[] attributeValues)
            throws XmlPullParserException, IOException {
        String value = null;
        int attributeCount = parser.getAttributeCount();
        if (attributeCount > 0) {
            for (int i = 0; i < attributeCount; ++i) {
                if (parser.getAttributeName(i).equals(attributeName)) {
                    String attributeValue = parser.getAttributeValue(i);
                    if (attributeValue.equals(attributeValues[0]) ||
                            (null != attributeValues[1] && attributeValue.equals(attributeValues[1]))) {
                        return parser.nextText();
                    }
                }
            }
            return null;
        }
        return parser.nextText();
    }
}
