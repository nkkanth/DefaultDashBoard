package org.droidtv.defaultdashboard.data.model.moreChapter;

import android.content.Context;

import org.droidtv.defaultdashboard.EditDashboardMenu;
import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.model.Action;
import org.droidtv.defaultdashboard.data.model.ContextualObject;

/**
 * Created by sandeep.kumar on 24/11/2017.
 */

public class DashboardConfigurationSettingItem extends MoreChapterShelfItem {

    private Action mAction;

    public DashboardConfigurationSettingItem(Context context) {
        super(context.getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_ITEM_EDIT_LAUNCHER), context.getDrawable(R.drawable.icon_22_smart_settings_n_54x54));
        mAction = new DashboardConfigurationSettingAction(context);
    }

    @Override
    public Action getAction() {
        return mAction;
    }

    private static class DashboardConfigurationSettingAction extends ContextualObject implements Action {

        protected DashboardConfigurationSettingAction(Context context) {
            super(context);
        }

        @Override
        public void perform() {
            EditDashboardMenu editMenu = new EditDashboardMenu(getContext());
            editMenu.show();
        }
    }
}
