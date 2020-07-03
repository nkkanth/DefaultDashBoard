package org.droidtv.defaultdashboard.recommended;
import java.util.List;

public class PreviewProgramsChannel {

    private int mId;
    private String mPackageName;
    private String mDisplayName;
    private String mDescription;
    private int category;
    private List<Recommendation> mPreviewProgramList;

    public int getId() {
        return mId;
    }

    public void setId(int Id) {
        this.mId = Id;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String PackageName) {
        this.mPackageName = PackageName;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String DisplayName) {
        this.mDisplayName = DisplayName;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String Description) {
        this.mDescription = Description;
    }

    public int getCategory() { return category; }

    public void setCategory(int category) { this.category = category; }

    public List<Recommendation> getPreviewProgramList() {
        return mPreviewProgramList;
    }

    public void setPreviewProgramList(List<Recommendation> previewProgramList) {
        this.mPreviewProgramList = previewProgramList;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PreviewProgramsChannel) {
            PreviewProgramsChannel channel = (PreviewProgramsChannel) obj;
            return (mId == channel.mId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mId;
    }
}
