package org.droidtv.defaultdashboard.data.model.moreChapter;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.model.Action;
import org.droidtv.defaultdashboard.data.model.ContextualObject;

import static org.droidtv.defaultdashboard.util.Constants.INTERNET_HOTSPOT_CATEGORY_INTENT;
import static org.droidtv.defaultdashboard.util.Constants.INTERNET_HOTSPOT_INTENT;

/**
 * Created by sandeep.kumar on 24/11/2017.
 */

public class InternetHotspotFeatureItem extends MoreChapterShelfItem {

    private Action mAction;

    public InternetHotspotFeatureItem(Context context) {
        super(context.getString(org.droidtv.ui.htvstrings.R.string.HTV_INTERNET_HOTSPOT), context.getDrawable(R.drawable.icon_14_network_n_54x54));
        mAction = new InternetHotspotFeatureAction(context);
    }

    @Override
    public Action getAction() {
        return mAction;
    }

    private static class InternetHotspotFeatureAction extends ContextualObject implements Action {

        protected InternetHotspotFeatureAction(Context context) {
            super(context);
        }

        @Override
        public void perform() {
           Intent launchInternetHotspotIntent = new Intent(INTERNET_HOTSPOT_INTENT);
           launchInternetHotspotIntent.addCategory(INTERNET_HOTSPOT_CATEGORY_INTENT);
           launchInternetHotspotIntent.putExtra("mode", 2011);
           launchInternetHotspotIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivityAsUser(launchInternetHotspotIntent, UserHandle.CURRENT_OR_SELF);
        }
    }
}
