package org.droidtv.defaultdashboard.data.query;

public class PreviewChannelDetails {

    String _id;
    String mappedChannelId;
    int category;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getMappedChannelId() {
        return mappedChannelId;
    }

    public void setMappedChannelId(String mappedChannelId) {
        this.mappedChannelId = mappedChannelId;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }
}
