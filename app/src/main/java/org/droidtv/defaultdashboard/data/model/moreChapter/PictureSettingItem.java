package org.droidtv.defaultdashboard.data.model.moreChapter;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.model.Action;
import org.droidtv.defaultdashboard.data.model.ContextualObject;

import static org.droidtv.defaultdashboard.util.Constants.PICTURE_SETTINGS_INTENT;

/**
 * Created by sandeep.kumar on 24/11/2017.
 */

public class PictureSettingItem extends MoreChapterShelfItem {

    private Action mAction;

    public PictureSettingItem(Context context) {
        super(context.getString(org.droidtv.ui.strings.R.string.MAIN_PICTURE), context.getDrawable(R.drawable.icon_21_smart_picture_n_54x54));
        mAction = new PictureSettingAction(context);
    }

    public PictureSettingItem(Context context, int id) {
        super(id, context.getString(org.droidtv.ui.strings.R.string.MAIN_PICTURE), context.getDrawable(R.drawable.icon_21_smart_picture_n_54x54));
        mAction = new PictureSettingAction(context);
    }

    @Override
    public Action getAction() {
        return mAction;
    }

    private static class PictureSettingAction extends ContextualObject implements Action {

        PictureSettingAction(Context context) {
            super(context);
        }

        @Override
        public void perform() {
            Intent launchPictureIntent = new Intent();
            launchPictureIntent.setAction(PICTURE_SETTINGS_INTENT);
            launchPictureIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivityAsUser(launchPictureIntent, UserHandle.CURRENT_OR_SELF);
        }
    }
}
