package org.droidtv.defaultdashboard.recommended;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sulakshana.vasanth on 12/27/2017.
 */

public enum RecommendationImportance implements Parcelable {
    /**
     * Importance none
     */
    NONE,
    /**
     * Importance mininum
     */
    MIN,
    /**
     * Importance low
     */
    LOW,
    /**
     * Importance default
     */
    DEFAULT,
    /**
     * Importance high
     */
    HIGH,

    /**
     * Importance max
     */
    MAX;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<RecommendationImportance> CREATOR = new Parcelable.Creator<RecommendationImportance>() {
        @Override
        public RecommendationImportance createFromParcel(Parcel in) {
            return RecommendationImportance.valueOf(in.readString());
        }

        @Override
        public RecommendationImportance[] newArray(int size) {
            return new RecommendationImportance[size];
        }
    };


}
