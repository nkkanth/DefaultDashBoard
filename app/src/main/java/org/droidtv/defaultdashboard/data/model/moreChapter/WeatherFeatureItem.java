package org.droidtv.defaultdashboard.data.model.moreChapter;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.model.Action;
import org.droidtv.defaultdashboard.data.model.ContextualObject;

import static org.droidtv.defaultdashboard.util.Constants.WEATHER_ACTIVITY_INTENT;
import static org.droidtv.defaultdashboard.util.Constants.WEATHER_PACKAGE_INTENT;

/**
 * Created by sandeep.kumar on 24/11/2017.
 */

public class WeatherFeatureItem extends MoreChapterShelfItem {

    private Action mAction;

    public WeatherFeatureItem(Context context) {
        super(context.getString(org.droidtv.ui.strings.R.string.MAIN_WEATHER), context.getDrawable(R.drawable.icon_270_weather_partly_cloudy_n_54x54));
        mAction = new WeatherFeatureAction(context);
    }

    @Override
    public Action getAction() {
        return mAction;
    }

    private static class WeatherFeatureAction extends ContextualObject implements Action {

        protected WeatherFeatureAction(Context context) {
            super(context);
        }

        @Override
        public void perform() {
            Intent launchWeatherIntent = new Intent();
            launchWeatherIntent.setClassName(WEATHER_PACKAGE_INTENT, WEATHER_ACTIVITY_INTENT);
            launchWeatherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivityAsUser(launchWeatherIntent, UserHandle.CURRENT_OR_SELF);
        }
    }
}
