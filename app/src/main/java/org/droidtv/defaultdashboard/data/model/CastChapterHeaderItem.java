package org.droidtv.defaultdashboard.data.model;

import android.content.Context;

import org.droidtv.defaultdashboard.R;

/**
 * Created by nikhil.tk on 03-01-2018.
 */

public class CastChapterHeaderItem extends ChapterHeaderItem {

    public CastChapterHeaderItem(Context context,long id) {
        super(id, context.getString(org.droidtv.ui.strings.R.string.MAIN_CAST), R.drawable.icon_1025_cast_n_48x48);
    }
}
