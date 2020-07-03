package org.droidtv.defaultdashboard.data.model;

import android.content.Context;

import org.droidtv.defaultdashboard.R;

/**
 * Created by nikhil.tk on 03-01-2018.
 */

public class MoreChapterHeaderItem extends ChapterHeaderItem {

    public MoreChapterHeaderItem(Context context, long id) {
        super(id, context.getString(org.droidtv.ui.strings.R.string.MAIN_BUTTON_MORE), R.drawable.icon_82_app_home_menu_n_48x48);
    }
}
