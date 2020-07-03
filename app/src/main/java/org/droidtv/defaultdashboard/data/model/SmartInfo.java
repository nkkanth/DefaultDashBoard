package org.droidtv.defaultdashboard.data.model;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.os.UserHandle;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.util.Constants;

/**
 * Created by sandeep.kumar on 08/02/2018.
 */

public class SmartInfo extends ContextualObject {

    private long mId;
    private String mTitle;
    private String mSubtitle;
    private String mDescription;
    private String mIconPath;
    private String mUrl;
    private String mUrlType;
    private Action mAction;

    private SmartInfo(Context context) {
        super(context);
    }

    public long getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSubtitle() {
        return mSubtitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getIconPath() {
        return mIconPath;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getUrlType() {
        return mUrlType;
    }

    public Action getAction() {
        return mAction;
    }

    void setId(long id) {
        mId = id;
    }

    void setTitle(String title) {
        mTitle = title;
    }

    void setSubtitle(String subtitle) {
        mSubtitle = subtitle;
    }

    void setDescription(String description) {
        mDescription = description;
    }

    void setIconPath(String iconPath) {
        mIconPath = iconPath;
    }

    void setUrl(String url) {
        mUrl = url;
    }

    void setUrlType(String urlType) {
        mUrlType = urlType;
    }

    void setAction(Action action) {
        mAction = action;
    }

    public static final class Builder {
        private SmartInfo mSmartInfo;

        public Builder(Context context) {
            mSmartInfo = new SmartInfo(context);
        }

        public Builder setId(long id) {
            mSmartInfo.setId(id);
            return this;
        }

        public Builder setTitle(String title) {
            mSmartInfo.setTitle(title);
            return this;
        }

        public Builder setSubtitle(String subtitle) {
            mSmartInfo.setSubtitle(subtitle);
            return this;
        }

        public Builder setDescription(String description) {
            mSmartInfo.setDescription(description);
            return this;
        }

        public Builder setIconPath(String iconPath) {
            mSmartInfo.setIconPath(iconPath);
            return this;
        }

        public Builder setUrl(String url) {
            mSmartInfo.setUrl(url);
            return this;
        }

        public Builder setUrlType(String urlType) {
            mSmartInfo.setUrlType(urlType);
            return this;
        }

        public SmartInfo build() {
            SmartInfoAction action = new SmartInfoAction(mSmartInfo.getContext(), mSmartInfo.getUrl(), mSmartInfo.getUrlType());
            mSmartInfo.setAction(action);
            return mSmartInfo;
        }
    }

    private static final class SmartInfoAction extends ContextualObject implements Action {

        private String mSmartInfoUrl;
        private String mSmartInfoUrlType;
        private Context mContext;

        private SmartInfoAction(Context context, String url, String urlType) {
            super(context);
            mSmartInfoUrl = url;
            mSmartInfoUrlType = urlType;
        }

        @Override
        public void perform() {
            Intent intent = new Intent(Constants.ACTION_SMARTINFO_BROWSER);
            intent.setPackage(Constants.PACKAGE_NAME_NETTVBROWSER);
            if (!TextUtils.isEmpty(mSmartInfoUrl)) {
                DdbLogUtility.logRecommendationChapter("SmartInfo", "perform() called mSmartInfoUrl: " + mSmartInfoUrl);
                intent.putExtra(Constants.EXTRA_SMARTINFO_RELATIVE_URL, mSmartInfoUrl);
            }
            if (!TextUtils.isEmpty(mSmartInfoUrlType)) {
                DdbLogUtility.logRecommendationChapter("SmartInfo", "perform() called mSmartInfoUrlType: " + mSmartInfoUrlType);
                intent.putExtra(Constants.EXTRA_SMARTINFO_URL_TYPE, mSmartInfoUrlType);
            }
            getContext().startServiceAsUser(intent, UserHandle.CURRENT_OR_SELF);
        }
    }
}
