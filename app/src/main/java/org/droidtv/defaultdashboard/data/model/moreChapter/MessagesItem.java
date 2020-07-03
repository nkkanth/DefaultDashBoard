package org.droidtv.defaultdashboard.data.model.moreChapter;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.model.Action;
import org.droidtv.defaultdashboard.data.model.ContextualObject;

import static org.droidtv.defaultdashboard.util.Constants.MESSAGES_ACTION_INTENT;
import static org.droidtv.defaultdashboard.util.Constants.MESSAGES_ACTIVITY_INTENT;
import static org.droidtv.defaultdashboard.util.Constants.MESSAGES_PACKAGE_INTENT;

/**
 * Created by sandeep.kumar on 24/11/2017.
 */

public class MessagesItem extends MoreChapterShelfItem {

    private Action mAction;

    public MessagesItem(Context context) {
        super(context.getString(org.droidtv.ui.htvstrings.R.string.HTV_WI_DASHBOARD_MESSAGES), context.getDrawable(R.drawable.icon_397_message_n_54x54));
        mAction = new MessagesAction(context);
    }

    @Override
    public Action getAction() {
        return mAction;
    }

    private static class MessagesAction extends ContextualObject implements Action {

        protected MessagesAction(Context context) {
            super(context);
        }

        @Override
        public void perform() {
            Intent launchMessageIntent = new Intent(MESSAGES_ACTION_INTENT);
            launchMessageIntent.setClassName(MESSAGES_PACKAGE_INTENT, MESSAGES_ACTIVITY_INTENT);
            launchMessageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivityAsUser(launchMessageIntent, UserHandle.CURRENT_OR_SELF);
        }
    }
}
