package org.droidtv.defaultdashboard.data.model;

import androidx.leanback.widget.ObjectAdapter;

public class VODReccomendationShelfRow extends ShelfRow {
    public VODReccomendationShelfRow(VideoOnDemandShelfHeaderItem header, ObjectAdapter adapter) {
        super(header, adapter);
    }

    @Override
    public VideoOnDemandShelfHeaderItem getShelfHeader() {
        return (VideoOnDemandShelfHeaderItem) super.getShelfHeader();
    }
}
