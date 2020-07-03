package org.droidtv.defaultdashboard.data.model.moreChapter;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.model.Action;
import org.droidtv.defaultdashboard.data.model.ContextualObject;

import static org.droidtv.defaultdashboard.util.Constants.BILL_ACTION_INTENT;
import static org.droidtv.defaultdashboard.util.Constants.BILL_ACTIVITY_INTENT;
import static org.droidtv.defaultdashboard.util.Constants.BILL_CLASS_INTENT;

/**
 * Created by sandeep.kumar on 24/11/2017.
 */

public class BillItem extends MoreChapterShelfItem {

    private Action mAction;

    public BillItem(Context context) {
        super(context.getString(org.droidtv.ui.htvstrings.R.string.HTV_WI_DASHBOARD_BILL), context.getDrawable(R.drawable.icon_376_bill_n_54x54));
        mAction = new BillAction(context);
    }

    @Override
    public Action getAction() {
        return mAction;
    }

    private static class BillAction extends ContextualObject implements Action {

        protected BillAction(Context context) {
            super(context);
        }

        @Override
        public void perform() {
            Intent launchBillIntent = new Intent(BILL_ACTION_INTENT);
            launchBillIntent.setClassName(BILL_CLASS_INTENT, BILL_ACTIVITY_INTENT);
            launchBillIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivityAsUser(launchBillIntent, UserHandle.CURRENT_OR_SELF);
        }
    }
}
