package org.droidtv.defaultdashboard.data.model;

import androidx.leanback.widget.ObjectAdapter;

public class GamesReccomendationShelfRow extends ShelfRow {
    public GamesReccomendationShelfRow(GamesShelfHeaderItem header, ObjectAdapter adapter) {
        super(header, adapter);
    }

    @Override
    public GamesShelfHeaderItem getShelfHeader() {
        return (GamesShelfHeaderItem) super.getShelfHeader();
    }
}
