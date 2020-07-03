package org.droidtv.defaultdashboard.data.model.channelFilter;

/**
 * Created by sandeep.kumar on 20/11/2017.
 */

import android.content.Context;
import android.database.Cursor;

import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;

/**
 * A channel filter that represents TV tuner (Antenna/Cable/Satellite) channel list
 */
public class TvChannelsFilter extends ChannelFilter {

    public TvChannelsFilter(Context context, Cursor cursor) {
        super(TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_TV, context.getString(org.droidtv.ui.htvstrings.R.string.HTV_TV_CHANNELS),
                context.getDrawable(org.droidtv.ui.tvwidget2k15.R.drawable.icon_74_tv_rc_n_48x48), cursor);
    }
}
