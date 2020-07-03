package org.droidtv.defaultdashboard.data.model.moreChapter;

import android.content.Context;
import android.content.Intent;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.model.Action;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import android.os.UserHandle;

import static org.droidtv.defaultdashboard.util.Constants.EXTRA_SHOW_ACCOUNTS;
import static org.droidtv.defaultdashboard.util.Constants.USER_ACCOUNT_ANDROID_INTENT_CLASS_NAME;
import static org.droidtv.defaultdashboard.util.Constants.USER_ACCOUNT_ANDROID_INTENT_PACKAGE_NAME;

/**
 * Created by sandeep.kumar on 24/11/2017.
 */

public class ManageAccountItem extends MoreChapterShelfItem {

    private Action mAction;

    public ManageAccountItem(Context context) {
        super(context.getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_ITEM_MANAGE_ACCOUNT), context.getDrawable(R.drawable.icon_266_add_user_n_54x54));
        mAction = new ManageAccountAction(context);
    }

    @Override
    public Action getAction() {
        return mAction;
    }

    private static class ManageAccountAction extends ContextualObject implements Action {

        protected ManageAccountAction(Context context) {
            super(context);
        }

        @Override
        public void perform() {
            Intent androidSettingsIntent = new Intent();
            boolean showAccounts = false;
            showAccounts = true;
            androidSettingsIntent.setClassName(USER_ACCOUNT_ANDROID_INTENT_PACKAGE_NAME, USER_ACCOUNT_ANDROID_INTENT_CLASS_NAME);
            androidSettingsIntent.putExtra(EXTRA_SHOW_ACCOUNTS, showAccounts);
            androidSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivityAsUser(androidSettingsIntent, UserHandle.CURRENT_OR_SELF);
        }
    }
}
