package org.droidtv.defaultdashboard.data.model;

/**
 * Contains info about an application retrieved from htvapplist
 */
public class AppInfo {

    private String mLabel;
    private String mDescription;
    private String mPackageName;
    private String mCategories;
    private String mCountries;
    private int mPosition;
    private boolean mIsAppRecommendationEnabled;
    private boolean mIsRecommendedAppEnabled;
    private boolean mIsEditModeEnabled;
    private String mAppType;

    AppInfo() {
        mLabel = null;
        mDescription = null;
        mPackageName = null;
        mCategories = null;
        mCountries = null;
        mIsAppRecommendationEnabled = false;
        mIsRecommendedAppEnabled = false;
        mIsEditModeEnabled = false;
        mAppType = null;
    }

    /**
     * Returns the name of this app
     *
     * @return
     */
    public String getLabel() {
        return mLabel;
    }

    /**
     * Returns the description for this app
     *
     * @return
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Returns the package name of this app
     *
     * @return
     */
    public String getPackageName() {
        return mPackageName;
    }

    /**
     * Returns a flattened string of list of categories which this app can be belongs to
     *
     * @return
     */
    public String getCategories() {
        return mCategories;
    }

    /**
     * Returns a flattened string of list of countries which this app can be shown for
     *
     * @return
     */
    public String getCountries() {
        return mCountries;
    }

    /**
     * Returns true is this app is enabled for showing recommendations
     *
     * @return
     */
    /**
     * Returns the Type for this app
     *
     * @return Portal,Local and PlayStore
     */
    public String getAppType() {
        return mAppType;
    }

    public void setAppType(String type) {
        mAppType = type;
    }

    public boolean isAppRecommendationEnabled() {
        return mIsAppRecommendationEnabled;
    }

    public int getAppPosition() {
        return mPosition;
    }

    /**
     * Returns true if this app is enabled as a recommended app
     *
     * @return
     */
    public boolean isRecommendedAppEnabled() {
        return mIsRecommendedAppEnabled;
    }

    public boolean getIsEditModeEnabled() {
        return mIsEditModeEnabled;
    }

    public void setIsEditModeEnabled(boolean isEditModeEnabled) {
        this.mIsEditModeEnabled = isEditModeEnabled;
    }

    void setLabel(String label) {
        mLabel = label;
    }

    void setDescription(String description) {
        mDescription = description;
    }

    void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    void setCategories(String categories) {
        mCategories = categories;
    }

    void setCountries(String countries) {
        mCountries = countries;
    }

    void setAppRecommendationEnabled(boolean appRecommendationEnabled) {
        mIsAppRecommendationEnabled = appRecommendationEnabled;
    }

    void setRecommendedAppEnabled(boolean recommendedAppEnabled) {
        mIsRecommendedAppEnabled = recommendedAppEnabled;
    }

    void setAppPosition(int position) {
        mPosition = position;
    }

    public static final class Builder {
        private AppInfo mAppInfo;

        public Builder() {
            mAppInfo = new AppInfo();
        }

        public Builder setPackageName(String packageName) {
            mAppInfo.setPackageName(packageName);
            return this;
        }

        public Builder setLabel(String label) {
            mAppInfo.setLabel(label);
            return this;
        }

        public Builder setDescription(String description) {
            mAppInfo.setDescription(description);
            return this;
        }

        public Builder setCategories(String categories) {
            mAppInfo.setCategories(categories);
            return this;
        }

        public Builder setCountries(String countries) {
            mAppInfo.setCountries(countries);
            return this;
        }

        public Builder setAppRecommendationEnabled(boolean enabled) {
            mAppInfo.setAppRecommendationEnabled(enabled);
            return this;
        }

        public Builder setRecommendedAppEnabled(boolean enabled) {
            mAppInfo.setRecommendedAppEnabled(enabled);
            return this;
        }
        public Builder setEditModeEnabled(boolean enabled) {
            mAppInfo.setIsEditModeEnabled(enabled);
            return this;
        }

        public Builder setAppPosition(int position) {
            mAppInfo.setAppPosition(position);
            return this;
        }

        public Builder SetAppType(String type){
            mAppInfo.setAppType(type);
            return this;
        }

        public AppInfo build() {
            return mAppInfo;
        }
    }
}
