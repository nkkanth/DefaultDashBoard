package org.droidtv.defaultdashboard.data.model.moreChapter;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.model.Action;
import org.droidtv.defaultdashboard.data.model.ContextualObject;

import static org.droidtv.defaultdashboard.util.Constants.ACTION_ALARM_INTENT;
import static org.droidtv.defaultdashboard.util.Constants.SET_ALARM_EXTRA_CLASS_INTENT;
import static org.droidtv.defaultdashboard.util.Constants.SET_ALARM_EXTRA_PACKAGE_INTENT;

/**
 * Created by sandeep.kumar on 24/11/2017.
 */

public class SetAlarmFeatureItem extends MoreChapterShelfItem {

    private Action mAction;

    public SetAlarmFeatureItem(Context context) {
        super(context.getString(org.droidtv.ui.strings.R.string.MAIN_SET_ALARM),context.getDrawable(R.drawable.icon_158_reminders_n_54x54));
        mAction = new SetAlarmFeatureAction(context);
    }

    @Override
    public Action getAction() {
        return mAction;
    }

    private static class SetAlarmFeatureAction extends ContextualObject implements Action {

        protected SetAlarmFeatureAction(Context context) {
            super(context);
        }

        @Override
        public void perform() {
            Intent launchSetAlarmintent = new Intent();
            launchSetAlarmintent.setAction(ACTION_ALARM_INTENT);
            launchSetAlarmintent.putExtra(SET_ALARM_EXTRA_CLASS_INTENT, SET_ALARM_EXTRA_PACKAGE_INTENT);
            //launchSetAlarmintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            launchSetAlarmintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().getApplicationContext().startActivityAsUser(launchSetAlarmintent, UserHandle.CURRENT_OR_SELF);

        }
    }
}
