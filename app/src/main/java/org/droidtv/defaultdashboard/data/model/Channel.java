package org.droidtv.defaultdashboard.data.model;

/**
 * Created by sandeep.kumar on 20/11/2017.
 */

import android.media.tv.TvContract;

import org.droidtv.htv.provider.HtvContract;

import static org.droidtv.defaultdashboard.data.model.Source.HDMI_INPUT_ID_SUBSTRING;
import static org.droidtv.defaultdashboard.data.model.Source.VGA_INPUT_ID_SUBSTRING;

/**
 * A Channel object represents an entity that can be displayed in various channel filter shelves and recommended channels shelf
 */
public class Channel {

    /**
     * HtvChannelList._ID
     */
    private int mId;

    /**
     * HtvChannelList.COLUMN_MAPPED_ID
     */
    private int mMappedId;

    /**
     * HtvChannelList.COLUMN_DISPLAY_NUMBER
     */
    private String mDisplayNumber;

    /**
     * HtvChannelList.COLUMN_INPUT_ID
     */
    private String mInputId;

    /**
     * Scrambled status of this channel
     * Derived from HtvChannelList.COLUMN_DESCRIPTION
     */
    private boolean mIsScrambled;

    /**
     * HtvChannelList.COLUMN_SERVICE_TYPE
     */
    private String mServiceType;

    /**
     * HtvChannelList.COLUMN_MEDIA_TYPE
     */
    private String mMediaType;

    /**
     * The type of the channel e.g. DVB-T, DVB-S, DVB-T, third-party TIF source,etc.
     * Derived from HtvChannelList.COLUMN_TYPE
     */
    private String mType;

    /**
     * HtvChannelList.COLUMN_DISPLAY_NAME
     */
    private String mDisplayName;

    /**
     * HtvChannelList.COLUMN_LOGO_URL
     */
    private String mLogoUrl;

    /**
     * Whether this channel is from a third party TIF source (apps like Google Play Movies, Haystack, Bloomberg TV, etc)
     */
    private boolean mIsTifChannel;

    /**
     * Whether this Channel belongs to MyChoice free package
     */
    private boolean mIsMyChoiceFreePackage;

    /**
     * Whether this Channel belongs to MyChoice package 1
     */
    private boolean mIsMyChoicePkg1;

    /**
     * Whether this Channel belongs to MyChoice package 2
     */
    private boolean mIsMyChoicePkg2;

    private boolean fallbackToBcEpg = false;

    public void setFallbackToBcEpg(boolean fallbackToBcEpg) {
        this.fallbackToBcEpg = fallbackToBcEpg;
    }

    public boolean isFallbackToBcEpg(){
        return fallbackToBcEpg;
    }

    private Channel(int id) {
        mId = id;
    }

    /**
     * Returns the id of this Channel
     */
    public int getId() {
        return mId;
    }

    /**
     * Returns the mapped id of this Channel
     */
    public int getMappedId() {
        return mMappedId;
    }

    /**
     * Returns the display number of this Channel
     */
    public String getDisplayNumber() {
        return mDisplayNumber;
    }

    /**
     * Returns the input id for this Channel
     */
    public String getInputId() {
        return mInputId;
    }

    /**
     * Returns the scrambled status of this Channel
     */
    public boolean isScrambled() {
        return mIsScrambled;
    }

    /**
     * Returns the service type of this Channel
     */
    public String getServiceType() {
        return mServiceType;
    }

    /**
     * Returns the media type of this Channel
     */
    public String getMediaType() {
        return mMediaType;
    }

    /**
     * Returns the type (DVB-T, DVB-S, DVB-T, third-party TIF source) of this Channel
     */
    String getType() {
        return mType;
    }

    /**
     * Returns the display name of this Channel
     */
    public String getDisplayName() {
        return mDisplayName;
    }

    /**
     * Returns the logo url of this Channel
     */
    public String getLogoUrl() {
        return mLogoUrl;
    }

    /**
     * Returns true if this channel is from a third party TIF source (apps like Google Play Movies, Haystack, Bloomberg TV, etc)
     */
    public boolean isTifChannel() {
        return mIsTifChannel;
    }

    /**
     * Returns true if this Channel is in MyChoice free package
     */
    public boolean isMyChoiceFreePackage() {
        return mIsMyChoiceFreePackage;
    }

    /**
     * Returns true if this Channel is in MyChoice package 1
     */
    public boolean isMyChoicePkg1() {
        return mIsMyChoicePkg1;
    }

    /**
     * Returns true if this Channel is in MyChoice package 2
     */
    public boolean isMyChoicePkg2() {
        return mIsMyChoicePkg2;
    }

    public static boolean isHdmiSource(Channel channel) {
        String channelMediaType = channel.getMediaType();
        String inputId = channel.getInputId();
        return channelMediaType.equals(HtvContract.HtvBaseDefs.TYPE_SOURCE) && inputId.contains(HDMI_INPUT_ID_SUBSTRING);
    }

    public static boolean isVgaSource(Channel channel) {
        String channelMediaType = channel.getMediaType();
        String inputId = channel.getInputId();
        return channelMediaType.equals(HtvContract.HtvBaseDefs.TYPE_SOURCE) && inputId.contains(VGA_INPUT_ID_SUBSTRING);
    }

    public static boolean isAntennaChannel(Channel channel) {
        String channelType = channel.getType();
        return channelType.equals(TvContract.Channels.TYPE_DVB_T) || channelType.equals(TvContract.Channels.TYPE_DVB_T2);
    }

    public static boolean isCableChannel(Channel channel) {
        String channelType = channel.getType();
        return channelType.equals(TvContract.Channels.TYPE_DVB_C) || channelType.equals(TvContract.Channels.TYPE_DVB_C2);
    }

    public static boolean isSatelliteChannel(Channel channel) {
        return !isAntennaChannel(channel) && !isCableChannel(channel);
    }

    public static boolean isTifChannel(Channel channel) {
        String channelMediaType = channel.getMediaType();
        return channelMediaType.equals(HtvContract.HtvBaseDefs.TYPE_TIF);
    }

    public static boolean isRadioChannel(Channel channel) {
        String serviceType = channel.getServiceType();
        return serviceType.equals(TvContract.Channels.SERVICE_TYPE_AUDIO);
    }

    public static boolean isApps(Channel channel) {
        String channelMediaType = channel.getMediaType();
        return channelMediaType.equals(HtvContract.HtvBaseDefs.TYPE_APPS);
    }

    public static boolean isGoogleCast(Channel channel) {
        String channelMediaType = channel.getMediaType();
        return channelMediaType.equals(HtvContract.HtvBaseDefs.TYPE_GOOGLE_CAST);
    }

    public static boolean isMediaBrowser(Channel channel) {
        String channelMediaType = channel.getMediaType();
        return channelMediaType.equals(HtvContract.HtvBaseDefs.TYPE_MEDIABROSWER);
    }

    void setMappedId(int mappedId) {
        mMappedId = mappedId;
    }

    void setDisplayNumber(String displayNumber) {
        mDisplayNumber = displayNumber;
    }

    void setInputId(String inputId) {
        mInputId = inputId;
    }

    void setScrambled(boolean scrambled) {
        mIsScrambled = scrambled;
    }

    void setServiceType(String serviceType) {
        mServiceType = serviceType;
    }

    void setMediaType(String mediaType) {
        mMediaType = mediaType;
    }

    void setType(String type) {
        mType = type;
    }

    void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }

    void setLogoUrl(String logoUrl) {
        mLogoUrl = logoUrl;
    }

    void setTifChannel(boolean isTifChannel) {
        mIsTifChannel = isTifChannel;
    }

    void setMyChoiceFreePackage(boolean isMyChoiceFreePackage) {
        mIsMyChoiceFreePackage = isMyChoiceFreePackage;
    }

    void setMyChoicePkg1(boolean isMyChoicePkg1) {
        mIsMyChoicePkg1 = isMyChoicePkg1;
    }

    void setMyChoicePkg2(boolean isMyChoicePkg2) {
        mIsMyChoicePkg2 = isMyChoicePkg2;
    }

    /**
     * A Builder class to create a Channel object
     */
    public static class Builder {

        private Channel mChannel;

        /**
         * Create a Builder object for a Channel
         *
         * @param id The id to be assigned to the Channel
         */
        public Builder(int id) {
            mChannel = new Channel(id);
        }

        /**
         * Set the mapped id of the Channel
         *
         * @param mappedId The mapped id to be assigned to the Channel
         * @return This Builder object to allow chaining of calls
         */
        public Builder setMappedId(int mappedId) {
            mChannel.setMappedId(mappedId);
            return this;
        }

        /**
         * Set the display number of the Channel
         *
         * @param displayNumber The display number to be assigned to the Channel
         * @return This Builder object to allow chaining of calls
         */
        public Builder setDisplayNumber(String displayNumber) {
            mChannel.setDisplayNumber(displayNumber);
            return this;
        }

        /**
         * Set the input id of the Channel
         *
         * @param inputId The input id to be assigned to the Channel
         * @return This Builder object to allow chaining of calls
         */
        public Builder setInputId(String inputId) {
            mChannel.setInputId(inputId);
            return this;
        }

        /**
         * Set the scrambled status of the Channel
         *
         * @param scrambled The scrambled status to be assigned to the Channel
         * @return This Builder object to allow chaining of calls
         */
        public Builder setScrambled(boolean scrambled) {
            mChannel.setScrambled(scrambled);
            return this;
        }

        /**
         * Set the service type of the Channel
         *
         * @param serviceType The service type to be assigned to the Channel
         * @return This Builder object to allow chaining of calls
         */
        public Builder setServiceType(String serviceType) {
            mChannel.setServiceType(serviceType);
            return this;
        }

        /**
         * Set the media type of the Channel
         *
         * @param mediaType The media type to be assigned to the Channel
         * @return This Builder object to allow chaining of calls
         */
        public Builder setMediaType(String mediaType) {
            mChannel.setMediaType(mediaType);
            return this;
        }

        /**
         * Set the type of the Channel
         *
         * @param type The type to be assigned to the Channel
         * @return This Builder object to allow chaining of calls
         */
        public Builder setType(String type) {
            mChannel.setType(type);
            return this;
        }

        /**
         * Set the display name of the Channel
         *
         * @param displayName The display name to be assigned to the Channel
         * @return This Builder object to allow chaining of calls
         */
        public Builder setDisplayName(String displayName) {
            mChannel.setDisplayName(displayName);
            return this;
        }

        /**
         * Set the logo url of the Channel
         *
         * @param logoUrl The logo url to be assigned to the Channel
         * @return This Builder object to allow chaining of calls
         */
        public Builder setLogoUrl(String logoUrl) {
            mChannel.setLogoUrl(logoUrl);
            return this;
        }

        /**
         * Set the third-party TIF channel status of this channel
         *
         * @param isTifChannel True if this is a third-party TIF channel
         * @return This Builder object to allow chaining of calls
         */
        public Builder setTifChannel(boolean isTifChannel) {
            mChannel.setTifChannel(isTifChannel);
            return this;
        }

        /**
         * Set the boolean value indicating if this channel is in MyChoice free package
         *
         * @param isMyChoiceFreePackage true if this channel is in MyChoice free package
         * @return This Builder object to allow chaining of calls
         */
        public Builder setMyChoiceFreePackage(boolean isMyChoiceFreePackage) {
            mChannel.setMyChoiceFreePackage(isMyChoiceFreePackage);
            return this;
        }

        /**
         * Set the boolean value indicating if this channel is in MyChoice package 1
         *
         * @param myChoiceFreePackage true if this channel is in MyChoice package 1
         * @return This Builder object to allow chaining of calls
         */
        public Builder setMyChoicePackage1(boolean isMyChoicePackage1) {
            mChannel.setMyChoicePkg1(isMyChoicePackage1);
            return this;
        }

        /**
         * Set the boolean value indicating if this channel is in MyChoice package 2
         *
         * @param myChoiceFreePackage true if this channel is in MyChoice package 2
         * @return This Builder object to allow chaining of calls
         */
        public Builder setMyChoicePackage2(boolean isMyChoicePackage2) {
            mChannel.setMyChoicePkg2(isMyChoicePackage2);
            return this;
        }

        /**
         * Returns a Channel object
         *
         * @return The Channel object created by this Builder
         */
        public Channel build() {
            return mChannel;
        }
    }

    @Override
    public String toString() {
        return "Channel{" +
                "mMappedId=" + mMappedId +
                ", mDisplayNumber='" + mDisplayNumber + '\'' +
                ", mIsScrambled=" + mIsScrambled +
                ", mServiceType='" + mServiceType + '\'' +
                ", mMediaType='" + mMediaType + '\'' +
                ", mType='" + mType + '\'' +
                ", mDisplayName='" + mDisplayName + '\'' +
                ", mLogoUrl='" + mLogoUrl + '\'' +
                ", mIsTifChannel=" + mIsTifChannel +
                ", mIsMyChoiceFreePackage=" + mIsMyChoiceFreePackage +
                ", mIsMyChoicePkg1=" + mIsMyChoicePkg1 +
                ", mIsMyChoicePkg2=" + mIsMyChoicePkg2 +
                '}';
    }
}
