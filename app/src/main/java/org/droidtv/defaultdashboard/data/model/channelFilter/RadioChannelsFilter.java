package org.droidtv.defaultdashboard.data.model.channelFilter;

/**
 * Created by sandeep.kumar on 20/11/2017.
 */

import android.content.Context;
import android.database.Cursor;

import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;

/**
 * A channel filter that represents all radio channels
 */
public class RadioChannelsFilter extends ChannelFilter {

    public RadioChannelsFilter(Context context, Cursor cursor) {
        super(TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_RADIO, context.getString(org.droidtv.ui.htvstrings.R.string.HTV_RADIO_CHANNELS),
                context.getDrawable(org.droidtv.ui.tvwidget2k15.R.drawable.digital_radio_n_ico_40x30_48), cursor);
    }
}
