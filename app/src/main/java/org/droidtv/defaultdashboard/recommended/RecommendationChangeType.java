package org.droidtv.defaultdashboard.recommended;

import android.os.Parcel;
import android.os.Parcelable;

public enum RecommendationChangeType implements Parcelable {
    /**
     * Indicates that particular recommendation has been canceled by sender
     */
    CANCELED,
    /**
     * Indicates that particular recommendation has been newly posted
     */
    ADDED,
    /**
     * Indicates that particular recommendation has been updated
     */
    UPDATED;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<RecommendationChangeType> CREATOR = new Parcelable.Creator<RecommendationChangeType>() {
        @Override
        public RecommendationChangeType createFromParcel(Parcel in) {
            return RecommendationChangeType.valueOf(in.readString());
        }

        @Override
        public RecommendationChangeType[] newArray(int size) {
            return new RecommendationChangeType[size];
        }
    };
}

