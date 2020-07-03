package org.droidtv.defaultdashboard.data.model.appsChapter;

/**
 * Created by bhargava.gugamsetty on 31-01-2018.
 */

public class CountryAppListItem {

    private int mCountryLabelResId;
    private int mCountryIconResId;


    public CountryAppListItem(int countryLabel, int countryIcon) {
        mCountryLabelResId = countryLabel;
        mCountryIconResId = countryIcon;
    }

    public int getCountryLabelResId() {
        return mCountryLabelResId;
    }

    public int getCountryIconResId() {
        return mCountryIconResId;
    }


}
