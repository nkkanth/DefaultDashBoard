package org.droidtv.defaultdashboard.data.model;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.tv.TvInputInfo;
import android.text.TextUtils;
import android.util.Log;

import org.droidtv.defaultdashboard.DashboardApplication;

/**
 * Created by sandeep.kumar on 21/11/2017.
 */

/**
 * A Source object represents an entity that will be shown in the Sources shelf.
 * A Source object can represent apps (such as Cast, AirServer), physical input sources(like HDMI and VGA)
 */
public class Source {

    public static final String SOURCE_INTENT_BROWSE_USB = "org.droidtv.contentexplorer.BROWSE_USB";

    public static final String VIRTUAL_SOURCE_USB_ID = "media_browser_usb";
    public static final String AIRSERVER_SOURCE_INPUT_ID = "airserver";
    public static final String CAST_SOURCE_INPUT_ID = "googlecast";

    public static final String NON_THIRD_PARTY_INPUT_ID_PREFIX = "com.mediatek.tvinput";
    public static final String MEDIA_CHANNELS_INPUT_ID_PREFIX = "org.droidtv.mediachannels";

    public static final String HDMI_INPUT_ID_SUBSTRING = ".hdmi.HDMIInputService";
    public static final String VGA_INPUT_ID_SUBSTRING = ".vga.VGAInputService";

    public static final String HDMI_1_INPUT_ID_SUBSTRING = ".hdmi.HDMIInputService/HW5";
    public static final String HDMI_2_INPUT_ID_SUBSTRING = ".hdmi.HDMIInputService/HW6";
    public static final String HDMI_3_INPUT_ID_SUBSTRING = ".hdmi.HDMIInputService/HW7";
    public static final String HDMI_4_INPUT_ID_SUBSTRING = ".hdmi.HDMIInputService/HW8";

    /**
     * Supported Source types
     */
    public static final class SourceType {
        public static final int HDMI = TvInputInfo.TYPE_HDMI;
        public static final int VGA = TvInputInfo.TYPE_VGA;
        public static final int CAST = 1009;
        public static final int AIRSERVER = 1010;
        public static final int VIRTUAL = 1011;
    }

    private String mLabel;
    private String mDescription;
    private String mId;
    private int mType;
    private Intent mLaunchIntent;
    private Drawable mIcon;
    private String mDeviceLabel;
    private int mHdmiPortId;

    private Source(String id) {
        mId = id;
    }

    /**
     * Returns the label of this Source
     */
    public String getLabel() {
        return mLabel;
    }

    /**
     * Returns the description of this Source
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Returns the id of this Source
     */
    public String getId() {
        return mId;
    }

    /**
     * Returns the type of this source. See {@link org.droidtv.defaultdashboard.data.model.Source.SourceType} for supported Source types
     */
    public int getType() {
        return mType;
    }

    /**
     * Returns the Intent associated with launching/tuning to this source
     */
    public Intent getLaunchIntent() {
        return mLaunchIntent;
    }

    /**
     * Returns the drawable representing the icon of this Source
     */
    public Drawable getIcon() {
        return mIcon;
    }

    /**
     * Returns the Port Id of this HDMI Source
     */
    public int getHDMIPortId() {
        return mHdmiPortId;
    }

    /**
     * Returns the device label of this hdmi Source
     */
    public String getHdmiDeviceLabel() {
        return mDeviceLabel;
    }

    void setLabel(String label) {
        mLabel = label;
    }

    void setDescription(String description) {
        mDescription = description;
    }

    void setType(int type) {
        mType = type;
    }

    void setLaunchIntent(Intent launchIntent) {
        mLaunchIntent = launchIntent;
    }

    public void setIcon(Drawable drawable) {
        mIcon = drawable;
    }

    public void setHdmiDeviceLabel(String hdmiDevicelabel) {
        mDeviceLabel = hdmiDevicelabel;
    }

    void setHdmiPortId(int hdmiPortId) {
        mHdmiPortId = hdmiPortId;
    }

    public static boolean isGoogleCast(Source source) {
        return CAST_SOURCE_INPUT_ID.equals(source.getId());
    }

    public static boolean isMediaBrowser(Source source) {
        return VIRTUAL_SOURCE_USB_ID.equals(source.getId());
    }

    public static boolean isAirserver(Source source) {
        return AIRSERVER_SOURCE_INPUT_ID.equals(source.getId());
    }

    private static boolean isBedSideHDMI2(String label){
        return  DashboardApplication.getInstance().isBedSideTv() && !TextUtils.isEmpty(label) && label.equalsIgnoreCase("HDMI 2");
    }

    /**
     * A Builder class to create a Source object
     */
    public static class Builder {

        private Source mSource;

        /**
         * Create a Builder object for a Source
         *
         * @param id The id to be assigned to the Source
         */
        public Builder(String id) {
            mSource = new Source(id);
        }

        /**
         * Set the name of the Source
         *
         * @param label The label to be assigned to the Source
         * @return This Builder object to allow chaining of calls
         */
        public Builder setLabel(String label) {
            if(isBedSideHDMI2(label)){
                Log.d("Source", "bedside");
                label = "HDMI";
            }
            mSource.setLabel(label);
            mSource.setHdmiDeviceLabel(label);
            return this;
        }


        /**
         * Set the description for the Source
         *
         * @param description The description to be assigned to the Source
         * @return This Builder object to allow chaining of calls
         */
        public Builder setDescription(String description) {
            mSource.setDescription(description);
            return this;
        }

        /**
         * Set the type of the Source
         *
         * @param type The type to be assigned to the Source. See {@link org.droidtv.defaultdashboard.data.model.Source.SourceType} for
         *             supported Source types
         * @return This Builder object to allow chaining of calls
         */
        public Builder setType(int type) {
            mSource.setType(type);
            return this;
        }

        /**
         * Set the Intent to be used for launching/tuning to the Source
         *
         * @param launchIntent The Intent to be assigned to launch/tune to the Source.
         * @return This Builder object to allow chaining of calls
         */
        public Builder setLaunchIntent(Intent launchIntent) {
            mSource.setLaunchIntent(launchIntent);
            return this;
        }

        /**
         * Set the icon for the Source
         *
         * @param drawable The Drawable to be assigned as the icon for the Source.
         * @return This Builder object to allow chaining of calls
         */
        public Builder setIcon(Drawable drawable) {
            mSource.setIcon(drawable);
            return this;
        }

        /**
         * Set the device name of the hdmi Source
         *
         * @param hdmiDevicelabel The device label to be assigned to the hdmi Source
         * @return This Builder object to allow chaining of calls
         */
        public Builder setHdmiDeviceLabel(String hdmiDevicelabel) {
            mSource.setHdmiDeviceLabel(hdmiDevicelabel);
            return this;
        }

        /**
         * Set the HDMI Port Id for the HDMI Source
         *
         * @param hdmiPortId The HDMI port Id to be assigned as the port Id for the HDMI Source.
         * @return This Builder object to allow chaining of calls
         */
        public Builder setHdmiPortId(int hdmiPortId) {
            mSource.setHdmiPortId(hdmiPortId);
            return this;
        }

        /**
         * Returns a Source object
         *
         * @return The Source object created by this Builder
         */
        public Source build() {
            return mSource;
        }
    }

    @Override
    public String toString() {
        return "Source{" +
                "mId='" + mId + '\'' +
                ", mType=" + mType +
                ", mDeviceLabel='" + mDeviceLabel + '\'' +
                ", mHdmiPortId=" + mHdmiPortId +
                '}';
    }
}
