package org.droidtv.defaultdashboard.data.model;

import androidx.leanback.widget.ObjectAdapter;

public class SmartInfoPreviewProgramShelfRow extends ShelfRow {
    public SmartInfoPreviewProgramShelfRow(long id, AppRecommendationShelfHeaderItem header, ObjectAdapter adapter) {
        super(id, header, adapter);
    }

    @Override
    public AppRecommendationShelfHeaderItem getShelfHeader() {
        return (AppRecommendationShelfHeaderItem) super.getShelfHeader();
    }
}
