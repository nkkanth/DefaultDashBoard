package org.droidtv.defaultdashboard.data.model.moreChapter;

import android.content.Context;
import android.content.Intent;

import org.droidtv.defaultdashboard.MenuLanguageActivity;
import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.model.Action;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import android.os.UserHandle;

/**
 * Created by sandeep.kumar on 24/11/2017.
 */

public class MenuLanguageSettingItem extends MoreChapterShelfItem {

    private Action mAction;

    public MenuLanguageSettingItem(Context context) {
        super(context.getString(org.droidtv.ui.strings.R.string.MAIN_MENU_LANGUAGE), context.getDrawable(R.drawable.icon_306_languages_n_54x54));
        mAction = new MenuLanguageSettingAction(context);
    }

    @Override
    public Action getAction() {
        return mAction;
    }

    private static class MenuLanguageSettingAction extends ContextualObject implements Action {

        protected MenuLanguageSettingAction(Context context) {
            super(context);
        }

        @Override
        public void perform() {
            Intent launchLanguageIntent = new Intent(getContext(), MenuLanguageActivity.class);
            launchLanguageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivityAsUser(launchLanguageIntent, UserHandle.CURRENT_OR_SELF);

        }
    }
}
