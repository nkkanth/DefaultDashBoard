package org.droidtv.defaultdashboard.data.model;

import android.content.Context;

/**
 * Created by nikhil.tk on 03-01-2018.
 */

public class TVChannelsChapterHeaderItem extends ChapterHeaderItem {

    public TVChannelsChapterHeaderItem(Context context,long id) {
        super(id, context.getString(org.droidtv.ui.strings.R.string.MAIN_TV_CHANNELS), org.droidtv.ui.tvwidget2k15.R.drawable.icon_74_tv_rc_n_48x48);
    }
}
