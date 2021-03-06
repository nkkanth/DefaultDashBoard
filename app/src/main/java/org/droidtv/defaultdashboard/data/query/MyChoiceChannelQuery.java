package org.droidtv.defaultdashboard.data.query;

import android.media.tv.TvContract;
import android.net.Uri;

import org.droidtv.defaultdashboard.data.model.Source;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.util.Constants.MyChoicePackage;
import org.droidtv.htv.provider.HtvContract.HtvChannelList;

import java.util.ArrayList;

/**
 * Created by sandeep.kumar on 20/11/2017.
 */

public class MyChoiceChannelQuery implements Query {

    public static final String MY_CHOICE_COLUMNS[] = {
            HtvChannelList.COLUMN_FREEPKG,
            HtvChannelList.COLUMN_PAYPKG1,
            HtvChannelList.COLUMN_PAYPKG2
    };

    private static final String[] PROJECTION = {
            HtvChannelList._ID,
            HtvChannelList.COLUMN_MAPPED_ID,
            HtvChannelList.COLUMN_INPUT_ID,
            HtvChannelList.COLUMN_MEDIA_TYPE,
            HtvChannelList.COLUMN_SERVICE_TYPE,
            HtvChannelList.COLUMN_TYPE,
            HtvChannelList.COLUMN_DISPLAY_NAME,
            HtvChannelList.COLUMN_DISPLAY_NUMBER,
            HtvChannelList.COLUMN_DESCRIPTION,
            HtvChannelList.COLUMN_LOGO_URL,
            HtvChannelList.COLUMN_FREEPKG,
            HtvChannelList.COLUMN_PAYPKG1,
            HtvChannelList.COLUMN_PAYPKG2
    };

    private String mSelection;
    private String[] mSelectionArgs;

    private static final String SORT_ORDER = " CAST(" + HtvChannelList.COLUMN_DISPLAY_NUMBER + " AS INTEGER) ASC";

    public MyChoiceChannelQuery(MyChoicePackage myChoicePackage) {
        // If a channel is part of this my choice package the value
        // in this theme tv column should be 1
        String column = MY_CHOICE_COLUMNS[myChoicePackage.ordinal()];
        mSelection = column + " = ? AND " + HtvChannelList.COLUMN_HIDE + " = ?";
        mSelectionArgs = new String[]{"1", "0"};
    }

    public MyChoiceChannelQuery(MyChoicePackage myChoicePackage, boolean tvChannelsEnabled, boolean radioChannelsEnabled, boolean mediaChannelsEnabled,
                                boolean tifChannesEnabled, boolean hdmi1Enabled, boolean hdmi2Enabled, boolean hdmi3Enabled, boolean hdmi4Enabled,
                                boolean vgaEnabled) {
        ArrayList<String> selectionArgs = new ArrayList<>();
        mSelectionArgs = new String[1];

        // If a channel is part of this my choice package the value
        // in this theme tv column should be 1
        String column = MY_CHOICE_COLUMNS[myChoicePackage.ordinal()];
        mSelection = column.concat(" == ?");
        selectionArgs.add("1");

        if (!tvChannelsEnabled) {
            mSelection = mSelection.concat(" AND ((").
                    concat(HtvChannelList.COLUMN_MEDIA_TYPE).concat(" != ? AND ").
                    concat(HtvChannelList.COLUMN_MEDIA_TYPE).concat(" != ? AND ").
                    concat(HtvChannelList.COLUMN_MEDIA_TYPE).concat(" != ? AND ").
                    concat(HtvChannelList.COLUMN_MEDIA_TYPE).concat(" != ?) OR ").
                    concat(HtvChannelList.COLUMN_SERVICE_TYPE).concat(" != ?)");
            selectionArgs.add(HtvChannelList.TYPE_TUNER);
            selectionArgs.add(HtvChannelList.TYPE_IP_MULTICAST);
            selectionArgs.add(HtvChannelList.TYPE_IP_UNICAST);
            selectionArgs.add(HtvChannelList.TYPE_IP_RTSP_RTP);
            selectionArgs.add(TvContract.Channels.SERVICE_TYPE_AUDIO_VIDEO);
        }

        if (!radioChannelsEnabled) {
            mSelection = mSelection.concat(" AND ").concat(HtvChannelList.COLUMN_SERVICE_TYPE).concat(" != ?");
            selectionArgs.add(TvContract.Channels.SERVICE_TYPE_AUDIO);
        }

        if (!mediaChannelsEnabled) {
            mSelection = mSelection.concat(" AND ").concat(HtvChannelList.COLUMN_MEDIA_TYPE).concat(" != ?");
            selectionArgs.add(HtvChannelList.TYPE_FILE);
        }

        if (!tifChannesEnabled) {
            mSelection = mSelection.concat(" AND ").concat(HtvChannelList.COLUMN_MEDIA_TYPE).concat(" != ?");
            selectionArgs.add(HtvChannelList.TYPE_TIF);
        }

        if (!hdmi1Enabled) {
            if (!mSelection.isEmpty()) {
                mSelection = mSelection.concat(" AND ");
            }
            mSelection = mSelection.concat(HtvChannelList.COLUMN_INPUT_ID).concat(" NOT LIKE ?");
            selectionArgs.add("%".concat(Source.HDMI_1_INPUT_ID_SUBSTRING));
        }

        if (!hdmi2Enabled) {
            if (!mSelection.isEmpty()) {
                mSelection = mSelection.concat(" AND ");
            }
            mSelection = mSelection.concat(HtvChannelList.COLUMN_INPUT_ID).concat(" NOT LIKE ?");
            selectionArgs.add("%".concat(Source.HDMI_2_INPUT_ID_SUBSTRING));
        }

        if (!hdmi3Enabled) {
            if (!mSelection.isEmpty()) {
                mSelection = mSelection.concat(" AND ");
            }
            mSelection = mSelection.concat(HtvChannelList.COLUMN_INPUT_ID).concat(" NOT LIKE ?");
            selectionArgs.add("%".concat(Source.HDMI_3_INPUT_ID_SUBSTRING));
        }

        if (!hdmi4Enabled) {
            if (!mSelection.isEmpty()) {
                mSelection = mSelection.concat(" AND ");
            }
            mSelection = mSelection.concat(HtvChannelList.COLUMN_INPUT_ID).concat(" NOT LIKE ?");
            selectionArgs.add("%".concat(Source.HDMI_4_INPUT_ID_SUBSTRING));
        }

        if (!vgaEnabled) {
            if (!mSelection.isEmpty()) {
                mSelection = mSelection.concat(" AND ");
            }
            mSelection = mSelection.concat(HtvChannelList.COLUMN_INPUT_ID).concat(" NOT LIKE ?");
            selectionArgs.add("%".concat(Source.VGA_INPUT_ID_SUBSTRING).concat("%"));
        }

        // Exclude hidden channels
        mSelection = mSelection.concat(" AND ").concat(HtvChannelList.COLUMN_HIDE).concat(" = ?");
        selectionArgs.add("0");

        mSelectionArgs = selectionArgs.toArray(mSelectionArgs);
        DdbLogUtility.logTVChannelChapter("MyChoiceChannelQuery","MyChoiceChannelQuery mSelectionArgs: "+ mSelectionArgs);
    }

    @Override
    public Uri getUri() {
        return HtvChannelList.CONTENT_URI;
    }

    @Override
    public String[] getProjection() {
        return PROJECTION;
    }

    @Override
    public String getSelection() {
        return mSelection;
    }

    @Override
    public String[] getSelectionArgs() {
        return mSelectionArgs;
    }

    @Override
    public String getSortOrder() {
        return SORT_ORDER;
    }

    @Override
    public String getGroupBy() {
        return null;
    }
}
