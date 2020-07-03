package org.droidtv.defaultdashboard.data.model.channelFilter;

import android.content.Context;
import android.database.Cursor;

import org.droidtv.defaultdashboard.R;
import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;

/**
 * Created by sandeep.kumar on 20/11/2017.
 */

/**
 * A channel filter that represents the combined channels list
 */
public class AllChannelsFilter extends ChannelFilter {

    public AllChannelsFilter(Context context, Cursor cursor) {
        super(TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_ALL, context.getString(org.droidtv.ui.strings.R.string.MAIN_ALL_CHANNELS),
                context.getDrawable(R.drawable.icon_148_channels_n_48x48), cursor);
    }
}
