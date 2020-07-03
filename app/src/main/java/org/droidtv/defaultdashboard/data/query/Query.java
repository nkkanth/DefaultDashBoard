package org.droidtv.defaultdashboard.data.query;

import android.net.Uri;

/**
 * Created by sandeep.kumar on 20/11/2017.
 */

public interface Query {

    /**
     * Returns the Uri of this query
     */
    public abstract Uri getUri();

    /**
     * Returns the projection for this query
     */
    public abstract String[] getProjection();

    /**
     * Returns the selection String for this query
     */
    public abstract String getSelection();

    /**
     * Returns the selection arguments corresponding to the section String for this query
     */
    public abstract String[] getSelectionArgs();

    /**
     * Returns the sort order for this query
     */
    public String getSortOrder();

    /**
     * Returns the group-by String for this query
     */
    public String getGroupBy();
}
