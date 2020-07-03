package org.droidtv.defaultdashboard.data.model;

/**
 * Created by sandeep.kumar on 30/10/2017.
 */

/**
 * An interface that should be implemented by classes that want to provide
 * an action that can be performned
 */
public interface Action {

    /**
     * Execute the action.
     * Subclasses should implement this method to carry out any associated task/operation
     */
    void perform();
}
