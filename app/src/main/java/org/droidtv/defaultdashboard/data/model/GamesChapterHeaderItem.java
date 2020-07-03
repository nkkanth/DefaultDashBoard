package org.droidtv.defaultdashboard.data.model;

import android.content.Context;

import org.droidtv.defaultdashboard.R;

/**
 * Created by nikhil.tk on 03-01-2018.
 */

public class GamesChapterHeaderItem extends ChapterHeaderItem {

    public GamesChapterHeaderItem(Context context, long id) {
        super(id, context.getString(org.droidtv.ui.strings.R.string.MAIN_LAUNCHER_TITLE_OEM_GAMES), R.drawable.icon_91_game_n_48x48);
    }
}
