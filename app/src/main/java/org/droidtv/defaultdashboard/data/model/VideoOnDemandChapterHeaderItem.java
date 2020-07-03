package org.droidtv.defaultdashboard.data.model;

import android.content.Context;

import org.droidtv.defaultdashboard.R;

/**
 * Created by nikhil.tk on 03-01-2018.
 */

public class VideoOnDemandChapterHeaderItem extends ChapterHeaderItem {

    public VideoOnDemandChapterHeaderItem(Context context, long id) {
        super(id, context.getString(org.droidtv.ui.strings.R.string.MAIN_HE_VIDEO_ON_DEMAND), R.drawable.icon_80_video_on_demand_n_48x48);
    }
}
