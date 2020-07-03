package org.droidtv.defaultdashboard.ui.view;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.leanback.widget.TitleViewAdapter;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.model.TopMenuItem;
import org.droidtv.defaultdashboard.data.model.TopMenuItemFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sandeep.kumar on 24/10/2017.
 */

/**
 * A class that is responsible for creating and providing access to a list of items to be displayed in the top menu
 */
public class TopMenuItemManager {

    private List<TopMenuItem> mTopMenuItems;
    private Context mContext;

    public TopMenuItemManager(Context context , TopMenuItemFocusChangeListener listener) {
        mContext = context;
        mTopMenuItems = new ArrayList<>();
        createTopMenuItems(listener);
    }

    /**
     * Returns a list of menu items that will be placed in the top menu
     *
     * @return List List of top menu items
     */
    public List<TopMenuItem> getTopMenuItems() {
        return mTopMenuItems;
    }

    /**
     * Return a count of items in the top menu (excluding the hotel logo, location name and welcome message views)
     *
     * @return int Count of top menu items
     */
    public int getTopMenuItemCount() {
        return mTopMenuItems.size();
    }

    /**
     * Return the top menu item at the specified position (index)
     *
     * @param position The position (index) of the item
     * @return The top menu item at the position (index)
     */
    public TopMenuItem getTopMenuItemAt(int position) {
        return mTopMenuItems.get(position);
    }

    /**
     * Return the top menu item having the view with the specified id
     *
     * @param viewId The view id to search for
     * @return The top menu item having the view with the view id
     */
    public TopMenuItem getTopMenuItemWithId(int viewId) {
        for (int i = 0; i < getTopMenuItemCount(); i++) {
            TopMenuItem topMenuItem = getTopMenuItemAt(i);
            if (viewId == topMenuItem.getView().getId()) {
                return topMenuItem;
            }
        }
        return null;
    }

    private void createTopMenuItems(TopMenuItemFocusChangeListener listener) {
        TopMenuItemFactory topMenuItemFactory = new TopMenuItemFactory(mContext);
	mTopMenuItems.add(topMenuItemFactory.getTopMenuItem(TopMenuItemFactory.TYPE_GOOGLE_ASSISTANT,listener));
        mTopMenuItems.add(topMenuItemFactory.getTopMenuItem(TopMenuItemFactory.TYPE_USER_ACCOUNT ,listener));
        mTopMenuItems.add(topMenuItemFactory.getTopMenuItem(TopMenuItemFactory.TYPE_MESSAGES,listener));
        mTopMenuItems.add(topMenuItemFactory.getTopMenuItem(TopMenuItemFactory.TYPE_BILL,listener));
        mTopMenuItems.add(topMenuItemFactory.getTopMenuItem(TopMenuItemFactory.TYPE_ALARM,listener));
        mTopMenuItems.add(topMenuItemFactory.getTopMenuItem(TopMenuItemFactory.TYPE_WEATHER,listener));
        mTopMenuItems.add(topMenuItemFactory.getTopMenuItem(TopMenuItemFactory.TYPE_LANGUAGE,listener));
        mTopMenuItems.add(topMenuItemFactory.getTopMenuItem(TopMenuItemFactory.TYPE_CLOCK,listener));
    }

    public interface TopMenuItemFocusChangeListener {
        void OnItemFocus(int itemID , boolean focus);
    }
}
