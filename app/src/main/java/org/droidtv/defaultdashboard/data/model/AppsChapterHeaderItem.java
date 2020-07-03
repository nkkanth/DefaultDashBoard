package org.droidtv.defaultdashboard.data.model;

import android.content.Context;

import org.droidtv.defaultdashboard.R;

/**
 * Created by nikhil.tk on 03-01-2018.
 */

public class AppsChapterHeaderItem extends ChapterHeaderItem {

    public AppsChapterHeaderItem(Context context, long id) {
        super(id, context.getString(org.droidtv.ui.strings.R.string.MAIN_LAUNCHER_TITLE_APPS), R.drawable.icon_1000_md_apps_n_48x48);
    }
}
