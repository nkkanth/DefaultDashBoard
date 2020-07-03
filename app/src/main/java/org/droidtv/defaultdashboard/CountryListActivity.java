package org.droidtv.defaultdashboard;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.model.AppInfo;
import org.droidtv.defaultdashboard.data.model.appsChapter.CountryAppListItem;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.adapter.CountryListAdapter;
import org.droidtv.tv.persistentstorage.ITvSettingsManager;
import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static org.droidtv.defaultdashboard.data.model.appsChapter.CountryListConstants.COUNTRY_MAPPED_DRAWABLE_RES_ID_ARRAY;
import static org.droidtv.defaultdashboard.data.model.appsChapter.CountryListConstants.COUNTRY_MAPPED_STRING_RES_ID_ARRAY;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_ARGENTINA;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_AUSTRALIA;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_AUSTRIA;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_BELGIUM;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_BRAZIL;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_BULGARIA;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_CROATIA;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_CZECH_REPUBLIC;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_DENMARK;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_ESTONIA;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_FINLAND;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_FRANCE;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_GERMANY;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_GREECE;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_HUNGARY;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_INTERNATIONAL;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_IRELAND;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_ITALY;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_KAZAKISTHAN;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_LATVIA;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_LITHUANIA;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_LUXEMBERG;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_NETHERLAND;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_NEW_ZEALAND;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_NORWAY;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_POLAND;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_PORTUGAL;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_ROMANIA;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_RUSSIA;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_SERBIA;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_SLOVAKIA;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_SLOVENIA;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_SPAIN;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_SWEDEN;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_SWITZERLAND;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_TURKEY;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_UKRAINE;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_UNITED_KINGDOM;
import static org.droidtv.defaultdashboard.util.Constants.APPS_COUNTRY_CODE_USA;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_ARGENTINA;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_AUSTRALIA;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_AUSTRIA;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_BELGIUM;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_BRAZIL;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_BULGARIA;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_CROATIA;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_CZECHREP;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_DENMARK;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_ESTONIA;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_FINLAND;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_FRANCE;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_GERMANY;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_GREECE;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_HUNGARY;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_INTERNATIONAL;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_IRELAND;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_ITALY;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_KAZAKHSTAN;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_LATVIA;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_LITHUANIA;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_LUXEMBOURG;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_NETHERLANDS;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_NEWZEALAND;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_NORWAY;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_POLAND;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_PORTUGAL;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_ROMANIA;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_RUSSIA;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_SERBIA;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_SLOVAKIA;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_SLOVENIA;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_SPAIN;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_SWEDEN;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_SWITZERLAND;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_TURKEY;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_UK;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_UKRAINE;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_USA;


public class CountryListActivity extends Activity implements CountryListAdapter.CountryItemClickListener {

    private RecyclerView mCountryGridView;
    private CountryListAdapter mCountryListAdapter;
    private CountryAppListItem mCountryAppListItem;
    private ArrayList<CountryAppListItem> mCountryItemData;
    private DashboardDataManager mDashboardDataManager;
    private ITvSettingsManager mTvSettingsManager;
    private int mSelectedCountryConstant;

    private static final String[] APPS_COUNTRY_CODE_ARRAY = {
            APPS_COUNTRY_CODE_ARGENTINA, //Argentina //37
            APPS_COUNTRY_CODE_AUSTRIA,//Austria //0
            APPS_COUNTRY_CODE_AUSTRALIA,//Australia //28
            APPS_COUNTRY_CODE_BELGIUM,//Belgium //1
            APPS_COUNTRY_CODE_BULGARIA,//Bulgaria //33
            APPS_COUNTRY_CODE_BRAZIL,//Brazil //36
            APPS_COUNTRY_CODE_SWITZERLAND,//Switzerland //24
            APPS_COUNTRY_CODE_CZECH_REPUBLIC,//Czech Republic //3
            APPS_COUNTRY_CODE_GERMANY,//Germany //7
            APPS_COUNTRY_CODE_DENMARK,//Denmark //4
            APPS_COUNTRY_CODE_ESTONIA,//Estonia //29
            APPS_COUNTRY_CODE_SPAIN,//Spain //22
            APPS_COUNTRY_CODE_FINLAND,//Finland //5
            APPS_COUNTRY_CODE_FRANCE,//France //6
            APPS_COUNTRY_CODE_UNITED_KINGDOM,//UnitedKingdom // 26
            APPS_COUNTRY_CODE_GREECE,//Greece //8
            APPS_COUNTRY_CODE_CROATIA,//Croatia //2
            APPS_COUNTRY_CODE_HUNGARY,//Hungary //9
            APPS_COUNTRY_CODE_IRELAND,//Ireland //10
            APPS_COUNTRY_CODE_ITALY,//Italy //11
            APPS_COUNTRY_CODE_KAZAKISTHAN,//Kazakhstan //32
            APPS_COUNTRY_CODE_LITHUANIA,//Lithuania //30
            APPS_COUNTRY_CODE_LUXEMBERG,//Luxemburg //12
            APPS_COUNTRY_CODE_LATVIA,//Latvia //31
            APPS_COUNTRY_CODE_NETHERLAND,//Netherlands //13
            APPS_COUNTRY_CODE_NORWAY,// Norway //14
            APPS_COUNTRY_CODE_NEW_ZEALAND,//NewZealand //50
            APPS_COUNTRY_CODE_POLAND,//Poland //15
            APPS_COUNTRY_CODE_PORTUGAL,//Portugal 16
            APPS_COUNTRY_CODE_ROMANIA,//Romania //17
            APPS_COUNTRY_CODE_SERBIA,//Serbia //19
            APPS_COUNTRY_CODE_RUSSIA,//Russia //18
            APPS_COUNTRY_CODE_SWEDEN,//Sweden //23
            APPS_COUNTRY_CODE_SLOVENIA,//Slovenia //21
            APPS_COUNTRY_CODE_SLOVAKIA,//Slovakia //20
            APPS_COUNTRY_CODE_TURKEY,//Turkey //25
            APPS_COUNTRY_CODE_UKRAINE,//Ukraine //35
            APPS_COUNTRY_CODE_USA,//USA //83
            APPS_COUNTRY_CODE_INTERNATIONAL//International //default
    };
    List<String> mCountryNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_list);

        mDashboardDataManager = DashboardDataManager.getInstance();
        mTvSettingsManager = mDashboardDataManager.getTvSettingsManager();

        mCountryNameList = new ArrayList<>();
        initCountryItemData();
        mCountryGridView = (RecyclerView) findViewById(R.id.country_recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4, LinearLayoutManager.VERTICAL, false);
        mCountryGridView.setLayoutManager(gridLayoutManager);
        mCountryListAdapter = new CountryListAdapter(this, mCountryItemData);
        mCountryListAdapter.setCountryItemClickListener(this);
        mCountryGridView.setAdapter(mCountryListAdapter);
    }

    private void initCountryItemData() {
        mCountryItemData = new ArrayList<CountryAppListItem>();
        int currentSelectedCountryConstant = mDashboardDataManager.getPbsCountrySelectedCode();
        getAvailableAppCountryList();
        getSortedCountryList(currentSelectedCountryConstant);
    }

    private void getAvailableAppCountryList() {
        List<AppInfo> apps;
        for (int i = 0; i <= APPS_COUNTRY_CODE_ARRAY.length - 1; i++) {
            String countryCode = APPS_COUNTRY_CODE_ARRAY[i];
            apps = mDashboardDataManager.getAppsByCountryAndAllCategory(countryCode);
            if (apps != null && !apps.isEmpty()) {
                mCountryNameList.add(getCountryName(countryCode));
            }
        }
        DdbLogUtility.logAppsChapter("CountryListActivity", "getAvailableAppCountryList() called mCountryNameList: " +mCountryNameList.toString());
        Collections.sort(mCountryNameList);
    }

    private String getCountryName(String currentSelectedCountryCode) {
        String countryName;
        switch (currentSelectedCountryCode) {
            case APPS_COUNTRY_CODE_ARGENTINA:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_ARGENTINA);
                break;
            case APPS_COUNTRY_CODE_AUSTRIA:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_AUSTRIA);
                break;
            case APPS_COUNTRY_CODE_AUSTRALIA:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_AUSTRALIA);
                break;
            case APPS_COUNTRY_CODE_BELGIUM:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_BELGIUM);
                break;
            case APPS_COUNTRY_CODE_BULGARIA:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_BULGARIA);
                break;
            case APPS_COUNTRY_CODE_BRAZIL:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_BRAZIL);
                break;
            case APPS_COUNTRY_CODE_SWITZERLAND:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_SWITZERLAND);
                break;
            case APPS_COUNTRY_CODE_CZECH_REPUBLIC:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_CZECH_REP);
                break;
            case APPS_COUNTRY_CODE_GERMANY:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_GERMANY);
                break;
            case APPS_COUNTRY_CODE_DENMARK:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_DENMARK);
                break;
            case APPS_COUNTRY_CODE_ESTONIA:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_ESTONIA);
                break;
            case APPS_COUNTRY_CODE_SPAIN:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_SPAIN);
                break;
            case APPS_COUNTRY_CODE_FINLAND:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_FINLAND);
                break;
            case APPS_COUNTRY_CODE_FRANCE:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_FRANCE);
                break;
            case APPS_COUNTRY_CODE_UNITED_KINGDOM:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_UK);
                break;
            case APPS_COUNTRY_CODE_GREECE:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_GREECE);
                break;
            case APPS_COUNTRY_CODE_CROATIA:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_CROATIA);
                break;
            case APPS_COUNTRY_CODE_HUNGARY:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_HUNGARY);
                break;
            case APPS_COUNTRY_CODE_IRELAND:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_IRELAND);
                break;
            case APPS_COUNTRY_CODE_ITALY:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_ITALY);
                break;
            case APPS_COUNTRY_CODE_KAZAKISTHAN:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_KAZAKHSTAN);
                break;
            case APPS_COUNTRY_CODE_LITHUANIA:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_LITHUANIA);
                break;
            case APPS_COUNTRY_CODE_LUXEMBERG:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_LUXEMBOURG);
                break;
            case APPS_COUNTRY_CODE_LATVIA:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_LATVIA);
                break;
            case APPS_COUNTRY_CODE_NETHERLAND:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_NETHERLANDS);
                break;
            case APPS_COUNTRY_CODE_NORWAY:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_NORWAY);
                break;
            case APPS_COUNTRY_CODE_NEW_ZEALAND:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_NEWZEALAND);
                break;
            case APPS_COUNTRY_CODE_POLAND:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_POLAND);
                break;
            case APPS_COUNTRY_CODE_PORTUGAL:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_PORTUGAL);
                break;
            case APPS_COUNTRY_CODE_ROMANIA:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_ROMANIA);
                break;
            case APPS_COUNTRY_CODE_SERBIA:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_SERBIA);
                break;
            case APPS_COUNTRY_CODE_RUSSIA:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_RUSSIA);
                break;
            case APPS_COUNTRY_CODE_SWEDEN:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_SWEDEN);
                break;
            case APPS_COUNTRY_CODE_SLOVENIA:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_SLOVENIA);
                break;
            case APPS_COUNTRY_CODE_SLOVAKIA:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_SLOVAKIA);
                break;
            case APPS_COUNTRY_CODE_TURKEY:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_TURKEY);
                break;
            case APPS_COUNTRY_CODE_UKRAINE:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_UKRAINE);
                break;
            case APPS_COUNTRY_CODE_USA:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_USA);
                break;
            default:
                countryName = getString(org.droidtv.ui.strings.R.string.MAIN_INTERNATIONAL);
                break;
        }
        DdbLogUtility.logAppsChapter("CountryListActivity", "getCountryName() called with: currentSelectedCountryCode = ["
                + currentSelectedCountryCode + "]" + "countryName: "+ countryName);
        return countryName;
    }


    protected void getSortedCountryList(int countryConstant) {

        switch (countryConstant) {
            case TvSettingsDefinitions.InstallationCountryConstants.NETHERLANDS:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_NETHERLANDS;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.BELGIUM:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_BELGIUM;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.LUXEMBOURG:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_LUXEMBOURG;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.FRANCE:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_FRANCE;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.GERMANY:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_GERMANY;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.SWITZERLAND:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_SWITZERLAND;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.AUSTRIA:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_AUSTRIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.UK:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_UK;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.IRELAND:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_IRELAND;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.SPAIN:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_SPAIN;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.PORTUGAL:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_PORTUGAL;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.ITALY:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_ITALY;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.NORWAY:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_NORWAY;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.SWEDEN:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_SWEDEN;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.DENMARK:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_DENMARK;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.FINLAND:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_FINLAND;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.GREECE:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_GREECE;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.TURKEY:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_TURKEY;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.RUSSIA:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_RUSSIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.UKRAINE:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_UKRAINE;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.KAZAKHSTAN:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_KAZAKHSTAN;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.POLAND:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_POLAND;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.CZECHREP:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_CZECHREP;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.SLOVAKIA:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_SLOVAKIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.HUNGARY:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_HUNGARY;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.BULGARIA:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_BULGARIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.ROMANIA:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_ROMANIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.LATVIA:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_LATVIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.ESTONIA:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_ESTONIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.LITHUANIA:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_LITHUANIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.SLOVENIA:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_SLOVENIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.SERBIA:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_SERBIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.CROATIA:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_CROATIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.ARGENTINA:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_ARGENTINA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.BRAZIL:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_BRAZIL;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.AUSTRALIA:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_AUSTRALIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.NEWZEALAND:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_NEWZEALAND;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.USA:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_USA;
                break;
            default:
                mSelectedCountryConstant = COUNTRY_MAPPED_ARRAY_ROW_ID_INTERNATIONAL;
                break;
        }
        DdbLogUtility.logCommon("DashboardDataManager", "getSortedCountryList() called with: mSelectedCountryConstant = " + mSelectedCountryConstant);
        initNewCountryOrder(COUNTRY_MAPPED_STRING_RES_ID_ARRAY[mSelectedCountryConstant], COUNTRY_MAPPED_DRAWABLE_RES_ID_ARRAY[mSelectedCountryConstant]);
    }

    private void initNewCountryOrder(int[] countryLabelResourceId, int[] countryDrawableResourceId) {
        for (int i = 0; i < APPS_COUNTRY_CODE_ARRAY.length; i++) {
            mCountryAppListItem = new CountryAppListItem(countryLabelResourceId[i],
                    countryDrawableResourceId[i]);
            if (mCountryNameList.contains(getString(mCountryAppListItem.getCountryLabelResId()))) {
                mCountryItemData.add(mCountryAppListItem);
            }
        }
    }

    @Override
    public void onCountryItemClick(View view, int position) {
        if (view != null) {
            mDashboardDataManager.notifyOnCountryItemClick(mCountryItemData.get(position).getCountryLabelResId());
            finish();
        }
    }
}
