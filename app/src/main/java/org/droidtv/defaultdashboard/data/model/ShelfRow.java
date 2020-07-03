package org.droidtv.defaultdashboard.data.model;

import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ObjectAdapter;

/**
 * Created by sandeep.kumar on 11/10/2017.
 */

/**
 * A class that abstracts a header and a horizontal list of items
 */
public class ShelfRow extends ListRow {

    private ShelfHeaderItem mShelfHeaderItem;

    public ShelfRow(ShelfHeaderItem header, ObjectAdapter adapter) {
        super(adapter);
        mShelfHeaderItem = header;
    }

    public ShelfRow(long id, ShelfHeaderItem header, ObjectAdapter adapter) {
        super(id, null, adapter);
        mShelfHeaderItem = header;
    }

    /**
     * Returns the header item for this shelf
     *
     * @return The header item for this shelf
     */
    public ShelfHeaderItem getShelfHeader() {
        return mShelfHeaderItem;
    }

    public void setShelfHeaderItem(ShelfHeaderItem header){
        mShelfHeaderItem = header;
    }
}
