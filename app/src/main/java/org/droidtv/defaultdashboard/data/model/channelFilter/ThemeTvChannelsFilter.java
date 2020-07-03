package org.droidtv.defaultdashboard.data.model.channelFilter;

/**
 * Created by sandeep.kumar on 20/11/2017.
 */

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;

import org.droidtv.defaultdashboard.util.Constants.ThemeTvGroup;
import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;

/**
 * A channel filter that represents one of the many ThemeTv packages.
 * The name of the filter indicates the specific ThemeTv package (e.g. ThemeTv 1, ThemeTv 2, etc)
 */
public class ThemeTvChannelsFilter extends ChannelFilter {

    private static final int[] THEME_TV_FILTER_IDS = {
            TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_THEME_1,
            TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_THEME_2,
            TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_THEME_3,
            TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_THEME_4,
            TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_THEME_5,
            TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_THEME_6,
            TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_THEME_7,
            TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_THEME_8,
            TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_THEME_9,
            TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_THEME_10
    };

    public ThemeTvChannelsFilter(ThemeTvGroup themeTvGroup, Context context, String title, Drawable logo, Cursor cursor) {
        super(THEME_TV_FILTER_IDS[themeTvGroup.ordinal()], title, logo, cursor);
    }
}
