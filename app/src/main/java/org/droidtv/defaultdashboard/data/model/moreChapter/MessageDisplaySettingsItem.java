package org.droidtv.defaultdashboard.data.model.moreChapter;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.model.Action;
import org.droidtv.defaultdashboard.data.model.ContextualObject;

import static org.droidtv.defaultdashboard.util.Constants.MESSAGE_DISPLAY_ACTIVITY_INTENT;
import static org.droidtv.defaultdashboard.util.Constants.MESSAGE_DISPLAY_INTENT;
import static org.droidtv.defaultdashboard.util.Constants.MESSAGE_DISPLAY_PACKAGE_INTENT;

/**
 * Created by sandeep.kumar on 24/11/2017.
 */

public class MessageDisplaySettingsItem extends MoreChapterShelfItem {

    private Action mAction;

    public MessageDisplaySettingsItem(Context context) {
        super(context.getString(org.droidtv.ui.htvstrings.R.string.HTV_MESSAGE_DISPLAY), context.getDrawable(R.drawable.icon_377_email_setting_n_54x54));
        mAction = new MessageDisplaySettingsAction(context);
    }

    @Override
    public Action getAction() {
        return mAction;
    }

    private static class MessageDisplaySettingsAction extends ContextualObject implements Action {

        protected MessageDisplaySettingsAction(Context context) {
            super(context);
        }

        @Override
        public void perform() {
            Intent launchMessageDisplayIntent = new Intent();
            launchMessageDisplayIntent.setClassName(MESSAGE_DISPLAY_PACKAGE_INTENT, MESSAGE_DISPLAY_ACTIVITY_INTENT);
            launchMessageDisplayIntent.setAction(MESSAGE_DISPLAY_INTENT);
            launchMessageDisplayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivityAsUser(launchMessageDisplayIntent, UserHandle.CURRENT_OR_SELF);
        }
    }
}
