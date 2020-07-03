package org.droidtv.defaultdashboard.data.model;

import android.view.View;

/**
 * Created by sandeep.kumar on 24/10/2017.
 */

/**
 * An abtract class that represents an item in the top menu.
 * This class should be extended by subclasses that want to represent specific items in the top menu.
 */
public abstract class TopMenuItem {

    /**
     * Retrieve the underlying view represented by this top menu item.
     * Subclasses should override this method to return their implementation of the underlying view.
     *
     * @return The underlying view
     */
    public abstract View getView();


    /**
     * Retrieve the underlying action that this top menu item can perform.
     * Subclasses should override this method to return their implementation of the associated action
     * that this top menu item can perform.
     *
     * @return The associated action with this item
     */
    public abstract Action getAction();
}
