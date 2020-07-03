package org.droidtv.defaultdashboard.recommended;

import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public final class Recommendation implements Parcelable {

    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImportance(RecommendationImportance importance) {
        this.importance = importance;        
    }

    public void setBackgroundImageUri(String backgroundImageUri) {
        this.backgroundImageUri = backgroundImageUri;
    }

    public void setContentType(String[] contentType) {
        if(contentType != null) {
            this.mContentType = contentType.clone();
        } else {
            this.mContentType = new String[0];
        }
    }

    public void setAutoCancelTrue(boolean autoCancelTrue) {
        isAutoCancelTrue = autoCancelTrue;
    }

    public void setLogo(Bitmap logo) {
        this.logo = logo;
    }

    public void setPendingIntent(PendingIntent pendingIntent) {
        this.pendingIntent = pendingIntent;
    }

    /**
     * enum class representing the level of importance for any recommendation<br>
     * Any recommendation with importance as {@link RecommendationImportance#HIGH} should be shown in the beginning<br>
     * Any recommendation with importance as {@link RecommendationImportance#NONE} should be shown in the end<br>
     */

    /**
     * id represents the unique recommendation id.
     */
    private int id;

    /**
     * A string that uniquely identifies this recommendation across all packages in the system
     */
    private String key;

    /**
     * Recommendation title to be shown on recommendation
     */
    private String title;
    /**
     * Subtitle for the recommendation
     */
    private String subtitle;
    /**
     * Description for the  recommendation
     */
    private String description;

    /**
     * level of importance of this recommendations
     * valid values: one of the values from enum class {@link RecommendationImportance} or <br>
     */
    private RecommendationImportance importance;
    /**
     * Uri of the background image that need to shown on selecting recommendation
     */
    private String backgroundImageUri;

    /**
     * content type represents the type of recommendations
     * to be posted to recommendation shelf
     */
    private String[] mContentType;

    /**
     * Setting this flag will make it so the recommendation is automatically canceled when the user clicks it in the panel.
     */
    private boolean isAutoCancelTrue;

    /**
     * logo represents large icon to be set for recommendation
     */
    private Bitmap logo;
    /**
     * pendingIntent represents intent to be launch when clicking on recommendation card.
     */
    private PendingIntent pendingIntent;

    private String logoUrl;
    public Recommendation(int id, String key, String title, String subtitle, String description, RecommendationImportance importance,
                          String backgroundImageUri, String[] contentType, boolean isAutoCancelTrue, Bitmap logo, PendingIntent pendingIntent) {
        this.id = id;
        this.key = key;
        this.title = title;
        this.subtitle = subtitle;
        this.description = description;
        this.importance = importance;
        this.backgroundImageUri = backgroundImageUri;
        if(contentType != null) {
            this.mContentType = contentType.clone();
        } else {
            this.mContentType = new String[0];
        }
        this.isAutoCancelTrue = isAutoCancelTrue;
        this.logo = logo;
        this.pendingIntent = pendingIntent;
    }

    public void setLogoUrl(String url) {
        logoUrl = url;
    }
    public String getLogoUrl() {
        return logoUrl;
    }

    /**
     * Function to get recommendation ID
     *
     * @return recommendation ID
     */
    public int getId() {       
        return id;
    }

    /**
     * Returns the key for this Recommendation object
     *
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * Function to get level of interruption of this recommendations
     *
     * @return the proposed position for the recommendation
     */
    public RecommendationImportance getImportance() {       
        return importance;
    }

    /**
     * Function to get the Uri of the background image that need to shown on selecting recommendation
     *
     * @return Uri for backgrong image
     */
    public String getBackgroundImageUri() {
        return backgroundImageUri;
    }

    /**
     * Function to get the content type of the recommendation
     *
     * @return array of content types to which a recommendation can belong to
     */
    public String[] getContentType() {
        return mContentType;
    }

    /**
     * Function to know if recommendation to be automatically canceled when the user clicks it in the UI
     *
     * @return true - automatically canceled when the user clicks,  false - do not cancel the recommendation
     */
    public boolean isAutoCancelTrue() {
        return isAutoCancelTrue;
    }

    /**
     * Function to get the title of recommendation
     *
     * @return string to be shown in first row
     */
    public String getTitle() {
        return title;
    }

    /**
     * function to get the subtitle of recommendation
     *
     * @return string to be shown in second row
     */
    public String getSubtitle() {
        return subtitle;
    }

    /**
     * function to get description of recommendation
     *
     * @return more information about the recommendation
     */
    public String getDescription() {
        return description;
    }

    /**
     * Function to get the image of the recommendation
     *
     * @return bitmap: image to be shown in UI
     */
    public Bitmap getLogo() {
        return logo;
    }

    /**
     * Function to get the pending intent
     *
     * @return intent to be launched on clicking of the recommendation
     */
    public PendingIntent getPendingIntent() {
        return pendingIntent;
    }

    public Recommendation() {

    }

    public Recommendation(final Parcel in) {
        readFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(key);
        dest.writeString(backgroundImageUri);
        dest.writeString(description);
        dest.writeString(title);
        dest.writeString(subtitle);
        dest.writeStringArray(mContentType);
        dest.writeInt(isAutoCancelTrue ? 1 : 0);
        // dest.writeParcelable(importance, flags);
        dest.writeString(importance.toString());
        dest.writeParcelable(logo, flags);
        dest.writeParcelable(this.pendingIntent, flags);
    }

    public void readFromParcel(Parcel in) {
        id = in.readInt();
        key = in.readString();
        backgroundImageUri = in.readString();
        description = in.readString();
        title = in.readString();
        subtitle = in.readString();
        mContentType = in.createStringArray();
        isAutoCancelTrue = in.readInt() != 0;
        // importance = in.readParcelable(Enum.class.getClassLoader());
        importance = RecommendationImportance.valueOf(in.readString());
        logo = in.readParcelable(Bitmap.class.getClassLoader());
        pendingIntent = in.readParcelable(PendingIntent.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Recommendation> CREATOR = new Parcelable.Creator<Recommendation>() {

        @Override
        public Recommendation createFromParcel(final Parcel source) {
            return new Recommendation(source);
        }

        @Override
        public Recommendation[] newArray(final int size) {
            return new Recommendation[size];
        }
    };

    @Override
    public String toString() {
        return "Recommendation{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", description='" + description + '\'' +
                ", importance=" + importance +
                ", backgroundImageUri='" + backgroundImageUri + '\'' +
                ", isAutoCancelTrue=" + isAutoCancelTrue +
                '}';
    }
}

