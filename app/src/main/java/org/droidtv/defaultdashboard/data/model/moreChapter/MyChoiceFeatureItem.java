package org.droidtv.defaultdashboard.data.model.moreChapter;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.model.Action;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.util.Constants;

import java.util.ArrayList;

import static org.droidtv.defaultdashboard.util.Constants.ACTION_MY_CHOICE_PIN;

/**
 * Created by sandeep.kumar on 24/11/2017.
 */

public class MyChoiceFeatureItem extends MoreChapterShelfItem {

    private Action mAction;

    public MyChoiceFeatureItem(Context context) {
        super(context.getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_MYCHOICE), context.getDrawable(R.drawable.icon_359_mychoice_n_54x54));
        mAction = new MyChoiceFeatureAction(context);
    }

    @Override
    public Action getAction() {
        return mAction;
    }

    private static class MyChoiceFeatureAction extends ContextualObject implements Action {

        protected MyChoiceFeatureAction(Context context) {
            super(context);
        }

        @Override
        public void perform() {
            Intent launchMyChoiceIntent = new Intent(ACTION_MY_CHOICE_PIN);
            launchMyChoiceIntent.putIntegerArrayListExtra(Constants.EXTRA_MY_CHOICE_PIN_DIALOG_KEYS, new ArrayList<Integer>());
            launchMyChoiceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivityAsUser(launchMyChoiceIntent, UserHandle.CURRENT_OR_SELF);
        }
    }
}
