package org.droidtv.defaultdashboard.data.model;

import androidx.leanback.widget.ObjectAdapter;

public class AppReccomendationShelfRow extends ShelfRow {
    public AppReccomendationShelfRow(AppRecommendationShelfHeaderItem header, ObjectAdapter adapter) {
        super(header, adapter);
    }

    @Override
    public AppRecommendationShelfHeaderItem getShelfHeader() {
        return (AppRecommendationShelfHeaderItem) super.getShelfHeader();
    }
}
