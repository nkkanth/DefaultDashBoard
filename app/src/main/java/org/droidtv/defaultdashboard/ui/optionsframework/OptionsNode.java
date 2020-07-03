/*
*  Copyright(C) 2012 TP Vision Holding B.V.,
*  All Rights Reserved.
*  This  source code and any compilation or derivative thereof is the
*  proprietary information of TP Vision Holding B.V.
*  and is confidential in nature.
*  Under no circumstances is this software to be exposed to or placed
*  under an Open Source License of any type without the expressed
*  written permission of TP Vision Holding B.V.
*
*/
package org.droidtv.defaultdashboard.ui.optionsframework;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.droidtv.ui.tvwidget2k15.R;

import java.util.ArrayList;
import java.util.List;

public class OptionsNode extends ViewGroup {

    private static final String ATTR_AVAILABLE = "available";
    private static final String ATTR_CONTROLLABLE = "controllable";
    private static final String ATTR_ACTIONABLE = "actionable";
    private static final String ATTR_ICON = "icon";

    private boolean mAvailable;
    private boolean mControllable;
    private boolean mActionable = false;
    protected RelativeLayout mView;
    private ListView mListView = null;
    private OptionsAdapter mAdapter;
    private List<OptionsNodeListener> mListeners = new ArrayList<OptionsNodeListener>();
    private OptionsManager mOptionsManager;
    private Drawable mIcon = null;

    public OptionsNode(Context context) {
        this(context, null, 0);
    }

    public OptionsNode(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OptionsNode(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (attrs != null) {
            String available = attrs.getAttributeValue(null, ATTR_AVAILABLE);
            if (available != null && available.equalsIgnoreCase("false")) {
                mAvailable = false;
            } else {
                mAvailable = true;
            }

            String controllable = attrs.getAttributeValue(null, ATTR_CONTROLLABLE);
            if (controllable != null && controllable.equalsIgnoreCase("false")) {
                mControllable = false;
            } else {
                mControllable = true;
            }

            String actionable = attrs.getAttributeValue(null, ATTR_ACTIONABLE);
            if (actionable != null && actionable.equalsIgnoreCase("true")) {
                mActionable = true;
            } else {
                mActionable = false;
            }

            int iconResID = attrs.getAttributeResourceValue(null, ATTR_ICON, -1);
            if (iconResID != -1) {
                mIcon = getContext().getResources().getDrawable(iconResID, null);
            }

        }

    }

    /**
     * attach listener to the node to get notifications on state changed
     *
     * @param listener
     */
    public void setListener(OptionsNodeListener listener) {
        mListeners.add(listener);
    }

    /**
     * get the OptionsManager instance associated with this node.
     *
     * @return instance of OptionsManager associated with this node
     */
    public OptionsManager getOptionsManager() {
        return mOptionsManager;
    }

    /**
     * set the OptionsManager instance associated with this node.
     *
     * @param OptionsManager
     */
    protected void setOptionsManager(OptionsManager optionsManager) {
        mOptionsManager = optionsManager;
    }

    /**
     * check if node is available or not
     *
     * @return true, if node is available else false
     */
    public boolean isAvailable() {
        return mAvailable;
    }

    /**
     * set the node's availability
     *
     * @param available
     */
    public void setAvailable(boolean available) {
        if (mAvailable != available) {
            mAvailable = available;
            if (!mListeners.isEmpty()) {
                for (int position = 0; position < mListeners.size(); position++) {
                    OptionsNodeListener listener = mListeners.get(position);
                    listener.onAvailabilityChanged(this, available);
                }
            }
        }
    }

    /**
     * check if node is controllable
     *
     * @return true, if node is controllable else false
     */
    public boolean isControllable() {
        return mControllable;
    }

    /**
     * set controllability of node
     *
     * @param controllable
     */
    public void setControllable(boolean controllable) {
        if (mControllable != controllable) {
            mControllable = controllable;
            if (!mListeners.isEmpty()) {
                for (int position = 0; position < mListeners.size(); position++) {
                    OptionsNodeListener listener = mListeners.get(position);
                    listener.onControllabilityChanged(this, controllable);
                }
            }
        }
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
    }

    /**
     * get number of available children of the is node
     *
     * @return count of available children
     */
    public int getAvailableChildCount() {
        int lAvailableChildCount = 0;
        int childCount = getChildCount();
        for (int position = 0; position < childCount; position++) {
            OptionsNode lChild = (OptionsNode) getChildAt(position);

            if (lChild.isAvailable()) {
                lAvailableChildCount++;
            }
        }
        return lAvailableChildCount;
    }

    /**
     * get available child node at given position
     *
     * @param availablePosition
     * @return available child node at the specified position
     */
    public OptionsNode getAvaialableChildAt(int availablePosition) {
        int aPos = 0;
        OptionsNode lAvailableChild = null;

        int childCount = getChildCount();
        for (int position = 0; position < childCount; position++) {
            OptionsNode lChild = (OptionsNode) getChildAt(position);

            if (lChild.isAvailable()) {
                if (aPos == availablePosition) {
                    lAvailableChild = lChild;
                    break;
                }
                aPos++;
            }
        }

        return lAvailableChild;
    }

    /**
     * Get the view associated with this node
     *
     * @return view associated with this node
     */
    public View getView() {
        return mView;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub

    }

    /**
     * Gets the view from loadView() which by default returns listview
     * or a custom view if overriden.
     * If there is a default listview instance, set data to it and return.
     *
     * @return view associated with the node.
     */
    protected View loadOptionsNodeView() {
        mView = loadView();
        mListView = (ListView) mView.findViewById(R.id.defaultListView);

        if (mAdapter == null) {
            mAdapter = new OptionsAdapter(getContext(), this);
        }
        if (mListView != null) {
            // if the node contains a default list view, set data to it.
            mListView.setAdapter(mAdapter);
            mListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
        }
        return mView;
    }

    protected OptionsAdapter getOptionsAdapter() {
        return mAdapter;
    }

    /**
     * get view to be displayed on the panel when this node is reached
     * [override this method when extending OptionsNode class and return your custom view]
     *
     * @return view to be displayed on the panel
     */
    protected RelativeLayout loadView() {
        return getOptionsManager().getFreeContainerViewFromCache();
    }

    public boolean isActionable() {
        return mActionable;
    }

    public void setActionable(boolean mActionable) {
        this.mActionable = mActionable;
    }

    interface OptionsNodeListener {
        public void onAvailabilityChanged(OptionsNode node, boolean available);

        public void onControllabilityChanged(OptionsNode node, boolean controllable);
    }
}

