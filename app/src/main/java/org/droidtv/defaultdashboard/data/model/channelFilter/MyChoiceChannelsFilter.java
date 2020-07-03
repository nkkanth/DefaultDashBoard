package org.droidtv.defaultdashboard.data.model.channelFilter;

/**
 * Created by sandeep.kumar on 20/11/2017.
 */

import android.content.Context;
import android.database.Cursor;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.util.Constants.MyChoicePackage;
import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;

/**
 * A channel filter that represents one of the many MyChoice packages.
 * The name of the filter indicates the specific MyChoice package (e.g. MyChoice Package 1, MyChoice Package 2, etc)
 */
public class MyChoiceChannelsFilter extends ChannelFilter {

    private static final int[] MY_CHOICE_FILTER_IDS = {
            TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_MYCHOICE_FREEPKG,
            TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_MYCHOICE_PAYPKG_1,
            TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_MYCHOICE_PAYPKG_2
    };

    private static final int[] MY_CHOICE_FILTER_NAME_RES_IDS = {
            org.droidtv.ui.htvstrings.R.string.HTV_MYCHOICE_FREE_PKG,
            org.droidtv.ui.htvstrings.R.string.HTV_MYCHOICE_PKG1,
            org.droidtv.ui.htvstrings.R.string.HTV_MYCHOICE_PKG2
    };

    private static final int[] MY_CHOICE_FILTER_LOGO_RES_IDS = {
            R.drawable.icon_359_mychoice_n_48x48,
            R.drawable.icon_388_mychoice_1_n_48x48,
            R.drawable.icon_389_mychoice_2_n_48x48
    };

    public MyChoiceChannelsFilter(Context context, MyChoicePackage myChoicePackage, Cursor cursor) {
        super(MY_CHOICE_FILTER_IDS[myChoicePackage.ordinal()], context.getString(MY_CHOICE_FILTER_NAME_RES_IDS[myChoicePackage.ordinal()]),
                context.getDrawable(MY_CHOICE_FILTER_LOGO_RES_IDS[myChoicePackage.ordinal()]), cursor);
    }
}
