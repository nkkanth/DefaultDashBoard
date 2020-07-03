package org.droidtv.defaultdashboard.data.model;

import android.content.Context;

import org.droidtv.defaultdashboard.R;

/**
 * Created by nikhil.tk on 03-01-2018.
 */

public class RecommendedChapterHeaderItem extends ChapterHeaderItem {

    public RecommendedChapterHeaderItem(Context context, long id) {
        super(id, context.getString(org.droidtv.ui.strings.R.string.MAIN_FILTER_RECOMMENDED), R.drawable.icon_1026_md_like_n_48x48);
    }
}
