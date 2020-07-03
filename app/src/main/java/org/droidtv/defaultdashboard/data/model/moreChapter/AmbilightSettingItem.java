package org.droidtv.defaultdashboard.data.model.moreChapter;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.model.Action;
import org.droidtv.defaultdashboard.data.model.ContextualObject;

import static org.droidtv.defaultdashboard.util.Constants.AMBILIGHT_SETTINGS_INTENT;

/**
 * Created by sandeep.kumar on 24/11/2017.
 */

public class AmbilightSettingItem extends MoreChapterShelfItem {

    private Action mAction;

    public AmbilightSettingItem(Context context) {
        super(context.getString(org.droidtv.ui.strings.R.string.MISC_AMBILIGHT), context.getDrawable(R.drawable.icon_1_ambilight_n_54x54));
        mAction = new AmbilightSettingAction(context);
    }

    public AmbilightSettingItem(Context context, int id) {
        super(id, context.getString(org.droidtv.ui.strings.R.string.MISC_AMBILIGHT), context.getDrawable(R.drawable.icon_1_ambilight_n_54x54));
        mAction = new AmbilightSettingAction(context);
    }

    @Override
    public Action getAction() {
        return mAction;
    }

    private static class AmbilightSettingAction extends ContextualObject implements Action {

        protected AmbilightSettingAction(Context context) {
            super(context);
        }

        @Override
        public void perform() {
            Intent launchAmbilightIntent = new Intent();
            launchAmbilightIntent.setAction(AMBILIGHT_SETTINGS_INTENT);
            launchAmbilightIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivityAsUser(launchAmbilightIntent, UserHandle.CURRENT_OR_SELF);

        }
    }
}
