package org.droidtv.defaultdashboard.data.model.moreChapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.model.Action;
import org.droidtv.defaultdashboard.data.model.ContextualObject;

import static org.droidtv.defaultdashboard.util.Constants.PICTURE_FORMAT_INTENT;

/**
 * Created by sandeep.kumar on 24/11/2017.
 */

public class PictureFormatSettingItem extends MoreChapterShelfItem {

    private Action mAction;

    public PictureFormatSettingItem(Context context) {
        super(context.getString(org.droidtv.ui.strings.R.string.MAIN_PICTURE_FORMAT), context.getDrawable(R.drawable.icon_28_picture_format_n_54x54));
        mAction = new PictureFormatSettingAction(context);
    }

    public PictureFormatSettingItem(Context context, int id) {
        super(id, context.getString(org.droidtv.ui.strings.R.string.MAIN_PICTURE_FORMAT), context.getDrawable(R.drawable.icon_28_picture_format_n_54x54));
        mAction = new PictureFormatSettingAction(context);
    }

    @Override
    public Action getAction() {
        return mAction;
    }

    private static class PictureFormatSettingAction extends ContextualObject implements Action {

        protected PictureFormatSettingAction(Context context) {
            super(context);
        }

        @Override
        public void perform() {
            Intent launchPictureFormatIntent = new Intent();
            launchPictureFormatIntent.setAction(PICTURE_FORMAT_INTENT);
            launchPictureFormatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
				getContext().startActivityAsUser(launchPictureFormatIntent, UserHandle.CURRENT_OR_SELF);
            }catch (ActivityNotFoundException e){
                android.util.Log.d("PictureFormatSettingItem", "ActivityNotFoundException " + PICTURE_FORMAT_INTENT);
            }
        }
    }
}
