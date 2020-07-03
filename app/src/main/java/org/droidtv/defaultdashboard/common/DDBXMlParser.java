package org.droidtv.defaultdashboard.common;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.model.AppInfo;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.htv.provider.HtvContract;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.XMLConstants;

/**
 * Created by utamkumar.bhuwania on 20-11-2018.
 */

public class DDBXMlParser {

    private final String TAG = "DDBXMlParser";
    private static DDBXMlParser _instance = null;
    private Context mContext = null;
    private String XML_FILE_PATH = null;

    private final String ITEM = "item";
    private final String NAME = "Name";
    private final String VALUE = "Value";
    private final String CLONE_IN= "CloneIn";

    private String ROOT_ELEMENT_DDB_SETTINGS = "DefaultDashboardSettings";
    private static final String CLONE_DATA_DIRECTORY_NAME = "clone_data";
    private Map mClonedInData =  null;

    //DDB settings constants
    public static final String KEY_MAPPED_SIDEPANEL_HIGHLIGHTED_TEXT_COLOR = "UserInterfaceSetting.SidePanel.HighlightedTextColor";
    public static final String KEY_MAPPED_SIDEPANEL_HIGHLIGHTED_TEXT_COLOR_ALPHA = "UserInterfaceSetting.SidePanel.HighlightedTextColor.Alpha";
    public static final String KEY_MAPPED_SIDEPANEL_HIGHLIGHTED_TEXT_COLOR_RED = "UserInterfaceSetting.SidePanel.HighlightedTextColor.Red";
    public static final String KEY_MAPPED_SIDEPANEL_HIGHLIGHTED_TEXT_COLOR_GREEN = "UserInterfaceSetting.SidePanel.HighlightedTextColor.Green";
    public static final String KEY_MAPPED_SIDEPANEL_HIGHLIGHTED_TEXT_COLOR_BLUE = "UserInterfaceSetting.SidePanel.HighlightedTextColor.Blue";

    public static final String KEY_MAPPED_SIDEPANEL_NON_HIGHLIGHTED_TEXT_COLOR = "UserInterfaceSetting.SidePanel.NonHighlightedTextColor";
    public static final String KEY_MAPPED_SIDEPANEL_NON_HIGHLIGHTED_TEXT_COLOR_ALPHA = "UserInterfaceSetting.SidePanel.NonHighlightedTextColor.Alpha";
    public static final String KEY_MAPPED_SIDEPANEL_NON_HIGHLIGHTED_TEXT_COLOR_RED = "UserInterfaceSetting.SidePanel.NonHighlightedTextColor.Red";
    public static final String KEY_MAPPED_SIDEPANEL_NON_HIGHLIGHTED_TEXT_COLOR_GREEN = "UserInterfaceSetting.SidePanel.NonHighlightedTextColor.Green";
    public static final String KEY_MAPPED_SIDEPANEL_NON_HIGHLIGHTED_TEXT_COLOR_BLUE = "UserInterfaceSetting.SidePanel.NonHighlightedTextColor.Blue";

    public static final String KEY_MAPPED_SIDEPANEL_BACKGROUND_COLOR = "UserInterfaceSetting.SidePanel.BackgroundColor";
    public static final String KEY_MAPPED_SIDEPANEL_BACKGROUND_COLOR_ALPHA = "UserInterfaceSetting.SidePanel.BackgroundColor.Alpha";
    public static final String KEY_MAPPED_SIDEPANEL_BACKGROUND_COLOR_RED = "UserInterfaceSetting.SidePanel.BackgroundColor.Red";
    public static final String KEY_MAPPED_SIDEPANEL_BACKGROUND_COLOR_GREEN = "UserInterfaceSetting.SidePanel.BackgroundColor.Green";
    public static final String KEY_MAPPED_SIDEPANEL_BACKGROUND_COLOR_BLUE = "UserInterfaceSetting.SidePanel.BackgroundColor.Blue";

    public static final String KEY_MAPPED_MAIN_BACKGROUND_COLOR_FILTER  = "UserInterfaceSetting.MainBackgroundPanel.MainBackgroundColorFilter";
    public static final String KEY_MAPPED_MAIN_BACKGROUND_COLOR_FILTER_ALPHA  = "UserInterfaceSetting.MainBackgroundPanel.MainBackgroundColorFilter.Alpha";
    public static final String KEY_MAPPED_MAIN_BACKGROUND_COLOR_FILTER_RED  = "UserInterfaceSetting.MainBackgroundPanel.MainBackgroundColorFilter.Red";
    public static final String KEY_MAPPED_MAIN_BACKGROUND_COLOR_FILTER_GREEN  = "UserInterfaceSetting.MainBackgroundPanel.MainBackgroundColorFilter.Green";
    public static final String KEY_MAPPED_MAIN_BACKGROUND_COLOR_FILTER_BLUE  = "UserInterfaceSetting.MainBackgroundPanel.MainBackgroundColorFilter.Blue";

    public static final String KEY_MAPPED_SHOW_ACCOUNT_ICON = "UserInterfaceSetting.ShowAccountIcon";
	public static final String KEY_MAPPED_SHOW_ASSISTANT_ICON = "UserInterfaceSetting.ShowAssistantIcon";
    public static final String KEY_MAPPED_BACKGROUND_PANEL_ENABLED = "UserInterfaceSetting.BackgroundPanel.Enable";

    private DDBXMlParser(Context context) {
        mContext = context;
    }

    public  static DDBXMlParser getInstance(Context context){
        if (_instance == null) {
            _instance = new DDBXMlParser(context);
        }
        return _instance;
    }

    /**
     * return the data dir path of default dashboard app for ex. /data/user/0/org.droidtv.defaultdashboard/default_dashboard_settings.xml
     * @return path of the data dir
     */
    private String getDataDirPath(){
        File directory = mContext.getDataDir();
        String path = null;
        if (directory.getName().equals(mContext.getDataDir().getName())) {
            path = mContext.getDataDir().getPath();
            path = path.endsWith("/") ? path : path + "/";
            path = path + Constants.MAPPED_DDB_SETTINGS_FILE_NAME;
        }else{
            File [] listFiles =  getCloneFileListExludeCloneDirectory(directory);
            for(File kid: listFiles) {
                if (kid.isFile() && kid.getName().equalsIgnoreCase(Constants.MAPPED_DDB_SETTINGS_FILE_NAME)) {
                    path = kid.getPath().endsWith("/") ? path : path + "/";
                    path =  path + Constants.MAPPED_DDB_SETTINGS_FILE_NAME;
                }
            }
        }
        Log.d(TAG, "getDataDirPath path " + path);
        return path;
    }

    private static File[] getCloneFileListExludeCloneDirectory(File parent) {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !CLONE_DATA_DIRECTORY_NAME.equals(name);
            }
        };

       return parent.listFiles(filter);
    }

    public boolean createMappedXmlFromDDBSettingPref(Context context, Map settingsValue){
        XML_FILE_PATH = getDataDirPath();
        android.util.Log.d(TAG, "createXmlFromDDBSettingPref MAPPED_XML_FILE_PATH " + XML_FILE_PATH);
        try {
            Document document = getXmlDocument();
            if(null != document){
                Element rootElementDDBSetting = document.createElement(ROOT_ELEMENT_DDB_SETTINGS);
                document.appendChild(rootElementDDBSetting);

                Integer sidePanelTextHighlitedColorHex = (Integer) settingsValue.get(Constants.PREF_KEY_SIDEPANEL_HIGHLIGHTED_TEXT_COLOR);
                android.util.Log.d(TAG, "createXmlFromDDBSettingPref sidePanelTextHighlitedColorHex " + sidePanelTextHighlitedColorHex);
                parseAndInsertIntoXml(document, rootElementDDBSetting, KEY_MAPPED_SIDEPANEL_HIGHLIGHTED_TEXT_COLOR, sidePanelTextHighlitedColorHex);

                Integer sidePanelTextNonHighlitedColorHex = (Integer) settingsValue.get(Constants.PREF_KEY_SIDEPANEL_NON_HIGHLIGHTED_TEXT_COLOR);
                android.util.Log.d(TAG, "createXmlFromDDBSettingPref sidePanelTextNonHighlitedColorHex " + sidePanelTextNonHighlitedColorHex);
                parseAndInsertIntoXml(document, rootElementDDBSetting, KEY_MAPPED_SIDEPANEL_NON_HIGHLIGHTED_TEXT_COLOR, sidePanelTextNonHighlitedColorHex);

                Integer sidePanelBackGroundColorHex = (Integer) settingsValue.get(Constants.PREF_KEY_SIDEPANEL_BACKGROUND_COLOR);
                android.util.Log.d(TAG, "createXmlFromDDBSettingPref sidePanelBackGroundColorHex " + sidePanelBackGroundColorHex);
                parseAndInsertIntoXml(document, rootElementDDBSetting, KEY_MAPPED_SIDEPANEL_BACKGROUND_COLOR, sidePanelBackGroundColorHex);

                Integer mainBackgroundColorFilterHex = (Integer) settingsValue.get(Constants.PREF_KEY_MAIN_BACKGROUND_COLOR_FILTER);
                android.util.Log.d(TAG, "createXmlFromDDBSettingPref mainBackgroundColorFilterHex " + mainBackgroundColorFilterHex);
                parseAndInsertIntoXml(document, rootElementDDBSetting, KEY_MAPPED_MAIN_BACKGROUND_COLOR_FILTER, mainBackgroundColorFilterHex);


                Boolean showAccountIcon = (Boolean) settingsValue.getOrDefault(Constants.PREF_KEY_SHOW_ACCOUNT_ICON,
                        context.getResources().getBoolean(R.bool.enable_show_icon));
                android.util.Log.d(TAG, "createXmlFromDDBSettingPref showAccountIcon " + showAccountIcon);
                parseAndInsertIntoXml(document, rootElementDDBSetting, KEY_MAPPED_SHOW_ACCOUNT_ICON, showAccountIcon);


                Boolean showAssistantIcon = (Boolean) settingsValue.getOrDefault(Constants.PREF_KEY_SHOW_ASSISTANT_ICON,
                        context.getResources().getBoolean(R.bool.enable_show_icon));
                android.util.Log.d(TAG, "createXmlFromDDBSettingPref showAssistantIcon " + showAssistantIcon);
                parseAndInsertIntoXml(document, rootElementDDBSetting, KEY_MAPPED_SHOW_ASSISTANT_ICON, showAssistantIcon);

                Boolean backgroundPanelEnabled = (Boolean) settingsValue.getOrDefault(Constants.PREF_KEY_MAIN_BACKGROUND_ENABLED,
                        context.getResources().getBoolean(R.bool.enable_main_background));
                android.util.Log.d(TAG, "createXmlFromDDBSettingPref backgroundPanelEnabled " + backgroundPanelEnabled);
                parseAndInsertIntoXml(document, rootElementDDBSetting, KEY_MAPPED_BACKGROUND_PANEL_ENABLED, backgroundPanelEnabled);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                DOMSource domSource = new DOMSource(document);
                StreamResult streamResult = new StreamResult(new File(XML_FILE_PATH));
                transformer.transform(domSource, streamResult);
            }
        }catch (TransformerException tfe) {
            Log.e("DashBoardDataManager","Exception :"+tfe.getMessage());
        }
        return true;
    }

    public boolean parseClonnedInXmlNUpdateSharedPref(){
        InputStream is = null;
        try {
            XML_FILE_PATH = getDataDirPath();
            File file = new File(XML_FILE_PATH);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            parseMappedXmlFIle(doc);
            updateDDBSharedPref();
        }catch (Exception e){
           Log.e(TAG,"Exception :" +e.getMessage());
            return false;
        } finally {
            if(is != null){
                try {
                    is.close();
                }catch(IOException e){
                    Log.e(TAG,"Exception :" +e.getMessage());
                }
            }
        }

        return true;
    }

    public void createCloneOutAppListXml(Context c){
        List<AppInfo> allAppList = DashboardDataManager.getInstance().getAllApps();

        if(allAppList == null ) return;

        Document doc = getXmlDocument();
        if(null != doc){
            Element rootAppList = doc.createElement(Constants.NAME_APP_LIST);
            doc.appendChild(rootAppList);

            for(AppInfo app : allAppList){
                insertAppToXml(doc, app, rootAppList);
            }
            transformDocToXml(c, doc);
        }
    }

    private void transformDocToXml(Context c, Document appDocument){
        String appListXmlPath = getAppListPath(c);

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource(appDocument);
            StreamResult streamResult = new StreamResult(appListXmlPath);
            transformer.transform(domSource, streamResult);
        }catch (TransformerException  e){
            Log.e(TAG, "ERROR: Exception in transformDocToXml " + e.getMessage());
           Log.e(TAG,"Exception :" +e.getMessage());
        }
    }

    private void insertAppToXml(Document doc, AppInfo app, Element rootElementAppList){
        if(Constants.DEBUG)  Log.d(TAG, "createCloneOutAppListXml app " + app.getLabel());
        Element appItem = doc.createElement(Constants.ITEM_NAME_APP);

        appItem.setAttribute(HtvContract.HtvAppList.COLUMN_NAME, app.getPackageName());
        appItem.setAttribute(HtvContract.HtvAppList.COLUMN_RECOMMENDED_APP, Boolean.toString(app.isRecommendedAppEnabled()));
        appItem.setAttribute(HtvContract.HtvAppList.COLUMN_APP_RECOMMENDATION, Boolean.toString(app.isAppRecommendationEnabled()));

        rootElementAppList.appendChild(appItem);
    }

    public boolean readAppListXmlAndUpdateDB(Context context){
        String appListPath = getAppListPath(context);
        Document colnedInappListDoc = getAppListDocument(appListPath);

        if(colnedInappListDoc == null) return false;

        return updateAppListDatabase(context, colnedInappListDoc);
    }

    private boolean updateAppListDatabase(Context c, Document colnedInappListDoc){
        NodeList appList = colnedInappListDoc.getElementsByTagName(Constants.ITEM_NAME_APP);
        if(appList == null) return  false;

        int numberofApps = appList.getLength();
        ArrayList<ContentProviderOperation> updateOperations = new ArrayList<ContentProviderOperation>();
        Log.d(TAG, "updateAppListDatabase numberofApps " + numberofApps);
        for(int i=0; i < numberofApps; i++){
            ContentProviderOperation operation  = buildUpdateOperation(appList.item(i).getAttributes());
            updateOperations.add(operation);
        }
        try{
            c.getContentResolver().applyBatch(HtvContract.AUTHORITY, updateOperations);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    private ContentProviderOperation buildUpdateOperation(NamedNodeMap appAttributes){
        String packageName = appAttributes.getNamedItem(HtvContract.HtvAppList.COLUMN_NAME).getNodeValue();
        String appRecommendation = appAttributes.getNamedItem(HtvContract.HtvAppList.COLUMN_APP_RECOMMENDATION).getNodeValue();
        String recommendedApp = appAttributes.getNamedItem(HtvContract.HtvAppList.COLUMN_RECOMMENDED_APP).getNodeValue();

        int appRecommendationInt = convertBoolToInt(appRecommendation);
        int recommendedAppInt = convertBoolToInt(recommendedApp);

        ContentValues values = new ContentValues();
        values.put(HtvContract.HtvAppList.COLUMN_APP_RECOMMENDATION, appRecommendationInt);
        values.put(HtvContract.HtvAppList.COLUMN_RECOMMENDED_APP, recommendedAppInt);

        String selection = HtvContract.HtvAppList.COLUMN_NAME + "=?";
        String [] args = new String[]{ packageName };
        if(Constants.DEBUG) {
            Log.d(TAG, "buildUpdateOperation packageName " + packageName + " appRecommendation " + appRecommendation + " recommendedApp " + recommendedApp
                    + " appRecommendationInt  " + appRecommendationInt + " recommendedAppInt " + recommendedAppInt);
        }

        return  ContentProviderOperation.newUpdate(HtvContract.HtvAppList.CONTENT_URI)
                                        .withSelection(selection, args)
                                        .withValues(values)
                                        .build();
    }

    private int convertBoolToInt(String value){
        return   (!TextUtils.isEmpty(value) && value.equalsIgnoreCase("false") ? 0 : 1);
    }

    private String getAppListPath(Context context){
        String path = context.getDataDir().getPath();
        path = path.endsWith("/") ? path : path + "/";
        path = path + Constants.FILE_NAME_APP_LIST;
        Log.d(TAG, "getAppListPath " + path);
        return path;
    }

    private Document getAppListDocument(String appListPath){
        try {
            File file = new File(appListPath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();
            return doc;
        }catch (Exception e){
            Log.d(TAG, "ERROR: Exception in getAppListDocument " + e.getMessage());
            return  null;
        }
    }


    private void parseMappedXmlFIle(Document doc){
        try {
            NodeList nodeList = doc.getElementsByTagName(ITEM);
            Log.d(TAG, "parseMappedXmlFIle length " + nodeList.getLength() + "Root element  " + doc.getDocumentElement().getNodeName());
            mClonedInData = new HashMap(20);
            for (int i=0; i<nodeList.getLength(); i++){
              insertToHashMap(nodeList.item(i));
            }
        }catch (Exception e){
           Log.e(TAG,"Exception :" +e.getMessage());
        }
    }

    private void insertToHashMap(Node node){
        if(node.getNodeType() == Node.ELEMENT_NODE){
            Element element = (Element) node;
            mClonedInData.put(getTagValue(NAME, element), getTagValue(VALUE, element));
        }
    }

    private String getTagValue(String tag, Element element){
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodeList.item(0);
        return node.getNodeValue();
    }

    private void updateDDBSharedPref(){
        if(mClonedInData == null){
            Log.d(TAG, "updateDDBSharedPref mClonedInData is empty");
            return;
        }
        Log.d(TAG, "updateDDBSharedPref " +mClonedInData.size());
        SharedPreferences sharedPreferences = DashboardDataManager.getInstance().getDefaultSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int alpha = getAlpha(KEY_MAPPED_SIDEPANEL_HIGHLIGHTED_TEXT_COLOR_ALPHA);
        int sidePanelHighlightedTextColor = getColor(alpha, KEY_MAPPED_SIDEPANEL_HIGHLIGHTED_TEXT_COLOR_RED, KEY_MAPPED_SIDEPANEL_HIGHLIGHTED_TEXT_COLOR_GREEN, KEY_MAPPED_SIDEPANEL_HIGHLIGHTED_TEXT_COLOR_BLUE);
        editor.putInt(Constants.PREF_KEY_SIDEPANEL_HIGHLIGHTED_TEXT_COLOR, sidePanelHighlightedTextColor);

        alpha = getAlpha(KEY_MAPPED_SIDEPANEL_NON_HIGHLIGHTED_TEXT_COLOR_ALPHA);
        int sidePanelNonHighlightedTextColor = getColor(alpha, KEY_MAPPED_SIDEPANEL_NON_HIGHLIGHTED_TEXT_COLOR_RED, KEY_MAPPED_SIDEPANEL_NON_HIGHLIGHTED_TEXT_COLOR_GREEN, KEY_MAPPED_SIDEPANEL_NON_HIGHLIGHTED_TEXT_COLOR_BLUE);
        editor.putInt(Constants.PREF_KEY_SIDEPANEL_NON_HIGHLIGHTED_TEXT_COLOR, sidePanelNonHighlightedTextColor);

        alpha = getAlpha(KEY_MAPPED_SIDEPANEL_BACKGROUND_COLOR_ALPHA);
        int sidePanelBackgroundColor = getColor(alpha, KEY_MAPPED_SIDEPANEL_BACKGROUND_COLOR_RED, KEY_MAPPED_SIDEPANEL_BACKGROUND_COLOR_GREEN, KEY_MAPPED_SIDEPANEL_BACKGROUND_COLOR_BLUE);
        if(DashboardDataManager.getInstance().isBFLProduct() && (sidePanelBackgroundColor == DashboardDataManager.getInstance().getDefaultHflSidePanelBackgroundColor()))
        {
            editor.putInt(Constants.PREF_KEY_SIDEPANEL_BACKGROUND_COLOR, DashboardDataManager.getInstance().getDefaultBflSidePanelBackgroundColor());
        }else if( DashboardDataManager.getInstance().isHFLProduct() && (sidePanelBackgroundColor == DashboardDataManager.getInstance().getDefaultBflSidePanelBackgroundColor())){
            editor.putInt(Constants.PREF_KEY_SIDEPANEL_BACKGROUND_COLOR, DashboardDataManager.getInstance().getDefaultHflSidePanelBackgroundColor());
        }else{
            editor.putInt(Constants.PREF_KEY_SIDEPANEL_BACKGROUND_COLOR, sidePanelBackgroundColor);
        }
        alpha = getAlpha(KEY_MAPPED_MAIN_BACKGROUND_COLOR_FILTER_ALPHA);
        int mainBgColorFIlter = getColor(alpha, KEY_MAPPED_MAIN_BACKGROUND_COLOR_FILTER_RED, KEY_MAPPED_MAIN_BACKGROUND_COLOR_FILTER_GREEN, KEY_MAPPED_MAIN_BACKGROUND_COLOR_FILTER_BLUE);
        editor.putInt(Constants.PREF_KEY_MAIN_BACKGROUND_COLOR_FILTER, mainBgColorFIlter);

        String showAccountIcon = (String) mClonedInData.getOrDefault(KEY_MAPPED_SHOW_ACCOUNT_ICON,"");
        Boolean showAccountIconBool = showAccountIcon.equalsIgnoreCase("true") ? true : false;
        editor.putBoolean(Constants.PREF_KEY_SHOW_ACCOUNT_ICON, showAccountIconBool);

        String showAssistantIcon = (String) mClonedInData.getOrDefault(KEY_MAPPED_SHOW_ASSISTANT_ICON,"");
        Boolean showAssistantIconBool = showAssistantIcon.equalsIgnoreCase("true") ? true : false;
        editor.putBoolean(Constants.PREF_KEY_SHOW_ASSISTANT_ICON, showAssistantIconBool);		

        String backgroundPanelEnable = (String) mClonedInData.get(KEY_MAPPED_BACKGROUND_PANEL_ENABLED);
        Boolean backgroundPanelEnableBool = true;
       if(!TextUtils.isEmpty(backgroundPanelEnable)){
           backgroundPanelEnableBool = backgroundPanelEnable.equalsIgnoreCase("true") ? true : false;
       }
        editor.putBoolean(Constants.PREF_KEY_MAIN_BACKGROUND_ENABLED, backgroundPanelEnableBool);

        editor.commit();
    }

    private int getAlpha(String key){
        int alpha = Integer.parseInt((String)mClonedInData.getOrDefault(key, "-1"));
        Log.d(TAG, "getAlpha key " + key + " alpha " + alpha + " convertedAlpha " + (int) Math.rint((alpha * 255) / 100d));
        return (int) Math.rint((alpha * 255) / 100d);//To convert alpha value which will be between 1-100 received from XML to 0-255
    }

    private int getColor(int alpha, String KEY_RED, String KEY_GREEN, String KEY_BLUE){
        int red = Integer.parseInt((String)mClonedInData.getOrDefault(KEY_RED, "-1"));
        int green = Integer.parseInt((String)mClonedInData.getOrDefault(KEY_GREEN, "-1"));
        int blue = Integer.parseInt((String)mClonedInData.getOrDefault(KEY_BLUE, "-1"));
        Log.d(TAG, "getColor A " + alpha + " R " + red + " G " + green + " B " + blue);
        return Color.argb(alpha, red, green, blue);
    }

    /**
     * Function to parse pref keys to ARGB value and insert into converted xml file
     * For Ex : pref_key_sidepanel_highlighted_text_color to UserInterfaceSetting.SidePanel.HighlightedTextColor.RED
     *                                                      UserInterfaceSetting.SidePanel.HighlightedTextColor.Green
     *                                                      UserInterfaceSetting.SidePanel.HighlightedTextColor.Blue
     *                                                      UserInterfaceSetting.SidePanel.HighlightedTextColor.Alpha
     * @param document, mappedKey and hexValue(read from shared pref)
     */
    private void parseAndInsertIntoXml(Document document, Element rootElement, String mappedKey, Object  value){
        if(Constants.DEBUG) android.util.Log.d(TAG, "insertIntoXml mappedKey " + mappedKey + " value " + value);
        if(value instanceof  Integer) {
            String ALPHA = convertOpacityToAlpha(Color.alpha((int) value));//to convert to ragne 1-100 from 0-255
            insertIntoXml(document, rootElement, mappedKey.concat(".Alpha"), ALPHA);

            String RED = Integer.toString(Color.red((int) value));
            insertIntoXml(document, rootElement, mappedKey.concat(".Red"), RED);

            String GREEN = Integer.toString(Color.green((int) value));
            insertIntoXml(document, rootElement, mappedKey.concat(".Green"), GREEN);

            String BLUE = Integer.toString(Color.blue((int) value));
            insertIntoXml(document, rootElement, mappedKey.concat(".Blue"), BLUE);
        }else if(value instanceof Boolean){
            android.util.Log.d(TAG, "insertIntoXml boolean");
            insertIntoXml(document, rootElement, mappedKey, value.toString());
        }
    }

    private String convertOpacityToAlpha(int opacity) {
        return Integer.toString((int) Math.rint((opacity * 100) / 255d));
    }

    private void insertIntoXml(Document document, Element rootElement, String mappedKey, String value){
        if(Constants.DEBUG) android.util.Log.d(TAG, "insertIntoXml mappedKey " + mappedKey + " value " + value);
        Element item = document.createElement(ITEM);

        Element ddbSettingElementName = document.createElement(NAME);
        ddbSettingElementName.appendChild(document.createTextNode(mappedKey));
        item.appendChild(ddbSettingElementName);

        Element Value = document.createElement(VALUE);
        Value.appendChild(document.createTextNode(value.toString()));
        item.appendChild(Value);

        Element cloneIn = document.createElement(CLONE_IN);
        cloneIn.appendChild(document.createTextNode("Yes"));
        item.appendChild(cloneIn);

        rootElement.appendChild(item);
    }

    private Document getXmlDocument(){
        Document doc = null;
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            doc =  documentBuilder.newDocument();
        } catch (ParserConfigurationException pce) {
            Log.e("DashBoardDataManager","Exception :"+pce.getMessage());
        }
        return doc;
    }
}
