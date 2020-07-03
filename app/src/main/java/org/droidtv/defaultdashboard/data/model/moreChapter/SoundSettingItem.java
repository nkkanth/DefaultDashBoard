package org.droidtv.defaultdashboard.data.model.moreChapter;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.model.Action;
import org.droidtv.defaultdashboard.data.model.ContextualObject;


import static org.droidtv.defaultdashboard.util.Constants.SOUND_SETTINGS_INTENT;

/**
 * Created by sandeep.kumar on 24/11/2017.
 */

public class SoundSettingItem extends MoreChapterShelfItem {

    private Action mAction;

    public SoundSettingItem(Context context) {
        super(context.getString(org.droidtv.ui.strings.R.string.MAIN_SOUND), context.getDrawable(R.drawable.icon_23_smart_sound_n_54x54));
        mAction = new SoundSettingAction(context);
    }

    public SoundSettingItem(Context context, int id) {
        super(id, context.getString(org.droidtv.ui.strings.R.string.MAIN_SOUND), context.getDrawable(R.drawable.icon_23_smart_sound_n_54x54));
        mAction = new SoundSettingAction(context);
    }

    @Override
    public Action getAction() {
        return mAction;
    }

    private static class SoundSettingAction extends ContextualObject implements Action {

        protected SoundSettingAction(Context context) {
            super(context);
        }

        @Override
        public void perform() {
            Intent launchSoundIntent = new Intent();
            launchSoundIntent.setAction(SOUND_SETTINGS_INTENT);
            launchSoundIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);			
            getContext().startActivityAsUser(launchSoundIntent, UserHandle.CURRENT_OR_SELF);
        }
    }
}
