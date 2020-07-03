package org.droidtv.defaultdashboard;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.model.appsChapter.CountryAppListItem;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.adapter.CountryListAdapter;
import org.droidtv.tv.persistentstorage.ITvSettingsManager;
import org.droidtv.tv.persistentstorage.TvSettingsConstants;
import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static org.droidtv.defaultdashboard.data.model.appsChapter.CountryListConstants.LANGUAGE_MAPPED_COUNTRY_BASED_STRING_RES_ID_ARRAY;
import static org.droidtv.defaultdashboard.data.model.appsChapter.CountryListConstants.LANGUAGE_MAPPED_COUNTRY_DRAWABLE_RES_ID_ARRAY;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_ARGENTINA;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_AUSTRALIA;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_AUSTRIA;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_BELGIUM;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_BRAZIL;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_BULGARIA;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_CANADA;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_CHINA;
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
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_MAPPED_ARRAY_ROW_ID_MEXICO;
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

/**
 * Created by bhargava.gugamsetty on 23-01-2018.
 */

public class MenuLanguageActivity extends Activity implements CountryListAdapter.LanguageItemClickListener {
    private Context mContext;
    private RecyclerView mLanguageGridView;
    private CountryListAdapter mLanguageListAdapter;
    private CountryAppListItem mLanguageAppListItem;
    private ArrayList<CountryAppListItem> mLanguageItemData;
    private DashboardDataManager mDashboardDataManager;
    private ITvSettingsManager mTvSettingsManager;
    private int mSelectedLanguageRowId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_language);

        mDashboardDataManager = DashboardDataManager.getInstance();
        mTvSettingsManager = mDashboardDataManager.getTvSettingsManager();

        initCountryItemData();
        mLanguageGridView = (RecyclerView) findViewById(R.id.country_language_recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4, LinearLayoutManager.VERTICAL, false);
        mLanguageGridView.setLayoutManager(gridLayoutManager);
        mLanguageListAdapter = new CountryListAdapter(this, mLanguageItemData);
        mLanguageListAdapter.setLanguageItemClickListener(this);
        mLanguageGridView.setAdapter(mLanguageListAdapter);
    }

    private void initCountryItemData() {
        mLanguageItemData = new ArrayList<CountryAppListItem>();
        int countrySelectedByGuestConstant = mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_APPS_COUNTRY, 0, -1);
        DdbLogUtility.logTopMenu("MenuLanguageActivity", "initCountryItemData() called countrySelectedByGuestConstant" + countrySelectedByGuestConstant);
        getSortedLanguageList(countrySelectedByGuestConstant);
    }

    protected void getSortedLanguageList(int countryConstant) {

        switch (countryConstant) {
            case TvSettingsDefinitions.InstallationCountryConstants.NETHERLANDS:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_NETHERLANDS;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.BELGIUM:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_BELGIUM;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.LUXEMBOURG:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_LUXEMBOURG;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.FRANCE:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_FRANCE;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.GERMANY:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_GERMANY;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.SWITZERLAND:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_SWITZERLAND;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.AUSTRIA:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_AUSTRIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.UK:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_UK;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.IRELAND:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_IRELAND;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.SPAIN:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_SPAIN;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.PORTUGAL:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_PORTUGAL;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.ITALY:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_ITALY;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.NORWAY:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_NORWAY;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.SWEDEN:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_SWEDEN;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.DENMARK:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_DENMARK;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.FINLAND:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_FINLAND;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.GREECE:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_GREECE;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.TURKEY:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_TURKEY;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.RUSSIA:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_RUSSIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.UKRAINE:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_UKRAINE;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.KAZAKHSTAN:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_KAZAKHSTAN;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.POLAND:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_POLAND;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.CZECHREP:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_CZECHREP;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.SLOVAKIA:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_SLOVAKIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.HUNGARY:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_HUNGARY;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.BULGARIA:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_BULGARIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.ROMANIA:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_ROMANIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.LATVIA:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_LATVIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.ESTONIA:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_ESTONIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.LITHUANIA:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_LITHUANIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.SLOVENIA:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_SLOVENIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.SERBIA:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_SERBIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.CROATIA:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_CROATIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.ARGENTINA:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_ARGENTINA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.BRAZIL:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_BRAZIL;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.AUSTRALIA:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_AUSTRALIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.NEWZEALAND:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_NEWZEALAND;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.USA:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_USA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.OTHER:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_INTERNATIONAL;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.CHINA:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_CHINA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.CANADA:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_CANADA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.MEXICO:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_MEXICO;
                break;
            default:
                mSelectedLanguageRowId = COUNTRY_MAPPED_ARRAY_ROW_ID_INTERNATIONAL;
                break;
        }
        initNewLanguageOrder(LANGUAGE_MAPPED_COUNTRY_BASED_STRING_RES_ID_ARRAY[mSelectedLanguageRowId], LANGUAGE_MAPPED_COUNTRY_DRAWABLE_RES_ID_ARRAY[mSelectedLanguageRowId]);
    }

    private void initNewLanguageOrder(int[] languageLabelResourceId, int[] languageDrawableResourceId) {
        int languageTvConstant = mTvSettingsManager.getInt(TvSettingsConstants.MENULANGUAGE, 0, -1);
        CountryAppListItem countryAppListItem = mDashboardDataManager.getCountryAppListItemForLanguage(languageTvConstant);
        mLanguageItemData.add(0, countryAppListItem);
        for (int i = 0; i < languageLabelResourceId.length; i++) {
            mLanguageAppListItem = new CountryAppListItem(languageLabelResourceId[i],
                    languageDrawableResourceId[i]);
            if( mLanguageAppListItem.getCountryIconResId()!= countryAppListItem.getCountryIconResId()){
                mLanguageItemData.add(mLanguageAppListItem);
            }
        }
    }

    @Override
    public void onLanguageItemClick(View view, final int position) {
        if (view != null) {
            mDashboardDataManager.notifyOnLanguageItemClick(mLanguageItemData.get(position).getCountryLabelResId());
            finish();
        }
    }
}
