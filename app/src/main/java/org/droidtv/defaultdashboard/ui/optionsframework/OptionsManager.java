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

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.OptionsMenu;
import org.droidtv.defaultdashboard.ui.OptionsMenu.IOptionsMenuCallBack;
import org.droidtv.defaultdashboard.ui.optionsframework.OptionsNode.OptionsNodeListener;
import org.droidtv.ui.tvwidget2k15.NPanelBrowser;
import org.droidtv.ui.tvwidget2k15.NPanelBrowser.NPanelBrowserAnimationListener;
import org.droidtv.ui.tvwidget2k15.NPanelBrowser.NPanelBrowserListener;
import org.droidtv.ui.tvwidget2k15.R;
import org.droidtv.ui.tvwidget2k15.VerticalText;
import org.droidtv.ui.tvwidget2k15.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class OptionsManager implements NPanelBrowserListener,
        OnItemSelectedListener, OnKeyListener, OnItemClickListener,
        OptionsNodeListener, IOptionsMenuCallBack, NPanelBrowserAnimationListener {

    private static final String TAG = OptionsManager.class.getSimpleName();

    private static final int PANEL_INDEX = 0;

    private static final int CONTAINER_VIEWS_CACHE_SIZE = 3;

    private static final int SLIDER_VIEWS_CACHE_SIZE = 2;

    private Context mContext;
    private NPanelBrowser mNpb;
    private OptionsNode mRootNode = null;
    private OptionsNode mCurrentNode, mPrevNode;
    private int mCurrentIndex = -1;
    private OptionsListener mOptionsListener;
    private int clickedItemPosition = -1;
    private OptionsMenu mOptMenu;
    private Stack<OptionsNode> mJumpToNodeTrace = new Stack<OptionsNode>();
    private IOptionsMenuCallBack mOptionsMenuCallBackListener;
    private boolean mScrolledToCommonParent = false;
    private boolean mJumpMode = false;
    private OptionsNode tempDestNode;
    private OptionsNode tempCommonParent;
    private IOptionsNodeStateCallBack mIOptionsNodeStateCallBack;

    private List<RelativeLayout> mContainerViewsCache = new ArrayList<RelativeLayout>();

    private List<RelativeLayout> mSliderViewsCache = new ArrayList<RelativeLayout>();

    private LayoutInflater inflater;

    public interface IOptionsNodeStateCallBack {
        public void onOptionNodeEntered(OptionsNode node);

        public void onOptionNodeExited(OptionsNode node);
    }

    public OptionsManager(Context context) {
        super();
        this.mContext = context;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        initializeContainerViewsCache();
        initializeSliderViewsCache();
    }

    /**
     * set the Options Application Tree Hierarchy by passing the resource Id of
     * the layout file
     *
     * @param resId
     */
    public OptionsNode setOptionsLayout(int resId) {
        if (mRootNode == null) {
            mRootNode = (OptionsNode) inflater.inflate(resId, null);
            if (mRootNode != null) {
                setupOptionsNodes(mRootNode);
            }
            mOptMenu = new OptionsMenu(mContext);
            mOptMenu.enableDiming(mEnableDimming);
            mNpb = mOptMenu.getNPanelBrowser();
        } else {
            mNpb.clear();
            mRootNode = (OptionsNode) inflater.inflate(resId, null);
            if (mRootNode != null) {
                setupOptionsNodes(mRootNode);
            }
            mCurrentNode = null;
            mPrevNode = null;
            mCurrentIndex = -1;
            clickedItemPosition = -1;
        }
        return mRootNode;
    }

    public void setOptionsRootNode(OptionsNode root) {

        if (root != null) {
            mNpb.clear();
            mRootNode = root;
            mCurrentNode = null;
            mPrevNode = null;
            mCurrentIndex = -1;
            clickedItemPosition = -1;
            mOptMenu.setListener(this);
        }
    }

    /*
     * set listener to each node created to monitor its state changes and
     * refresh UI accordingly. set itself as the OptionsManager instance
     * associated with each node.
     */
    private void setupOptionsNodes(OptionsNode parent) {
        LogHelper.d(TAG, "setupOptionsNodes" + parent);
        if (parent != null) {
            parent.setListener(this);
            parent.setOptionsManager(this);
            int lChildCount = parent.getChildCount();
            for (int position = 0; position < lChildCount; position++) {
                OptionsNode lChild = (OptionsNode) parent.getChildAt(position);
                lChild.setListener(this);
                lChild.setOptionsManager(this);

                if (lChild.getChildCount() != 0) {
                    setupOptionsNodes(lChild);
                }
            }
        }
    }

    /*
     * initialize Container View cache
     * Caches 3 relative layout container views to enhance performance
     */
    private void initializeContainerViewsCache() {
        for (int i = 0; i < CONTAINER_VIEWS_CACHE_SIZE; i++) {
            RelativeLayout container = (RelativeLayout) inflater.inflate(R.layout.options_node_view, null);
            mContainerViewsCache.add(container);
        }
    }

    /**
     * OptionsNode's loadView invokes this method to get the cached container view.
     * reduces cost of inflating container view for each OptionsNode.
     *
     * @return containerView
     */
    public RelativeLayout getFreeContainerViewFromCache() {

        for (RelativeLayout container : mContainerViewsCache) {
            if (container.getParent() == null) {
                // return first unattached listView
                return container;
            }
        }

        return null;
    }

    /**
     * SliderSettingsNode invokes this method to get the cached list view.
     * reduces cost of inflating slider view for each slider settings node.
     *
     * @return sliderView
     */
    public RelativeLayout getFreeSliderViewFromCache() {

        for (RelativeLayout sliderView : mSliderViewsCache) {
            if (sliderView.getParent() == null) {
                // return first unattached listView
                return sliderView;
            }
        }

        return null;
    }

    /*
     * initialize Slider View cache
     * Caches 2 slider views to enhance performance
     */
    private void initializeSliderViewsCache() {
        for (int i = 0; i < SLIDER_VIEWS_CACHE_SIZE; i++) {
            RelativeLayout slider = (RelativeLayout) inflater.inflate(R.layout.min_max_slider_settings_node_2, null);
            mSliderViewsCache.add(slider);
        }
    }

    /**
     * Add an OptionsNode to the Options Tree Dynamically
     *
     * @param parentId - resID of the Parent Node
     * @param node     - Options node object which is pre-populated with its children
     * @param index    - Position in the Parent Node where this has to be added. If
     *                 the index is not in a valid bounds of the parent's child
     *                 count, it will added in the last position.
     */
    public void addOptionNode(int parentId, OptionsNode node, int index) {
        final OptionsNode parent = findOptionsNodeById(parentId);
        int insertPos;
        if (null != parent) {
            if (index < 0 || index > parent.getChildCount()) {
                insertPos = parent.getChildCount();
            } else {
                insertPos = index;
            }
            parent.addView(node, insertPos);
            DdbLogUtility.logCommon(TAG, "addOptionNode: " + node.getTag());
            setupOptionsNodes(node);
            ((Activity) mContext).runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    notifyOptionNodeDataSetChanged(parent);
                }
            });
        }
    }

    /**
     * Add an OptionsNode to the Options Tree Dynamically
     *
     * @param parentId         - resID of the Parent Node
     * @param OptionsNodeXmlId - resID of the XML File with the Options tree
     * @param index            - Position in the Parent Node where this has to be added. If
     *                         the index is not in a valid bounds of the parent's child
     *                         count, it will added in the last position.
     */
    public void addOptionNode(int parentId, int optionsNodeXmlId, int index) {
        OptionsNode node = (OptionsNode) inflater.inflate(optionsNodeXmlId,
                null);
        if (null != node) {
            addOptionNode(parentId, node, index);
        }
    }

    /**
     * Remove an OptionsNode in the Options tree
     *
     * @param parentId - resID of the Parent Node
     * @param node     - Options node object which is removed from the parent
     */
    public void removeOptionNode(int parentId, final OptionsNode node) {
        final OptionsNode parent = findOptionsNodeById(parentId);
        if (null != parent) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handleNodeRemoval(node);
                    parent.removeView(node);
                    notifyOptionNodeDataSetChanged(parent);
                }

            });
        }
    }

    private void handleNodeRemoval(OptionsNode node) {
        if (mOptMenu.isShowing()) {
            if (node.getId() == mCurrentNode.getId()) {
                scrollToPrevPage();
            } else if (node.findViewById(mCurrentNode.getId()) != null) { // Backward
                stepCount = 0;
                OptionsNode currParent = (OptionsNode) mCurrentNode.getParent()
                        .getParent();
                while (currParent != null && !currParent.equals(node)) {
                    stepCount++;
                    currParent = (OptionsNode) currParent.getParent();
                }
                while (stepCount > 0) {
                    scrollToPrevPage();
                    stepCount--;
                }
            }
        } else {
            if (mRootNode != null) {
                View v = mRootNode.findViewById(node.getId());
                ViewGroup vp = (v == null) ? null : (ViewGroup) v.getParent();
                if (vp != null) {
                    vp.removeView(v);
                }
            }
        }
    }

    private void notifyOptionNodeDataSetChanged(OptionsNode optionsNode) {
        final OptionsAdapter opAdapter = optionsNode.getOptionsAdapter();
        if (opAdapter != null) {
            opAdapter.notifyDataSetChanged();
        }
    }

    /**
     * set the listener for callback from Options Framework
     *
     * @param listener
     */
    @Deprecated
    public void setOptionsListener(OptionsListener listener) {
        mOptionsListener = listener;
    }

    /**
     * set the listener for callback from Options Framework
     * In addition to the Callback provided with earlier version.
     * This listener will also get a callback to set selection on a specific child
     *
     * @param listener
     */
    public void setOptionsListener(OptionsListener2 listener) {
        mOptionsListener = listener;
    }

    /**
     * set the N-Panel browser
     *
     * @param npb
     */
    public void setNPanelBrowser(NPanelBrowser npb) {
        mNpb = npb;
        mNpb.setNPanelBrowserListner(this);
        mNpb.setAnimationListener(this);
    }

    /**
     * get current node Id
     *
     * @return
     */
    public int getCurrentNodeId() {
        if (mOptMenu.isShowing()) {
            return mCurrentNode.getId();
        } else {
            return -1;
        }
    }

    /**
     * update panel with new view supplied
     *
     * @param view
     */
    public void updatePanel(View view) {
        ViewGroup lPanel = (ViewGroup) mNpb.getPanel(PANEL_INDEX);
        lPanel.removeViewAt(0);
        lPanel.addView(view, 0);
    }

    /**
     * scroll to next page
     */
    public void scrollToNextPage() {
        if (mOptMenu.isShowing()) {
            mNpb.scrollNextPage();
        }
    }

    /**
     * scroll to previous page
     */
    public void scrollToPrevPage() {
        if (mOptMenu.isShowing()) {
            mNpb.scrollPrevPage();
        }
    }

    /**
     * find the OptionsNode instance with ID
     *
     * @param resId
     * @return OptionsNode that was searched for if found else return null
     */
    public OptionsNode findOptionsNodeById(int resId) {
        return (OptionsNode) mRootNode.findViewById(resId);
    }

    /*
     * distance between searched node and the node that initiated the search
     */
    private int stepCount = 0;

    /*
     * Utility function to search for a node with particular resource Id in the
     * Tree hierarchy [used only in the case when an ancestor becomes
     * unavailable and we need to find the step count to scrollback that many
     * times] for any other purpose, use @findOptionsNodeById method
     */
    private OptionsNode searchNode(OptionsNode parent, int resId) {
        stepCount++;
        OptionsNode lNode = null;

        if (parent != null) {
            if (parent.getId() == resId) {
                lNode = parent;
            } else {
                int lChildCount = parent.getChildCount();
                for (int position = 0; position < lChildCount; position++) {
                    OptionsNode lChild = (OptionsNode) parent
                            .getChildAt(position);
                    lNode = searchNode(lChild, resId);
                    if (lNode != null) {
                        break;
                    }
                }
            }
        }

        return lNode;
    }

    /**
     * set controllability for a particular node
     *
     * @param resId
     * @param controllable
     */
    public void setControllability(int resId, boolean controllable)
            throws NoSuchFieldException {
        OptionsNode lChangedNode = findOptionsNodeById(resId);
        if (lChangedNode != null) {
            lChangedNode.setControllable(controllable);
        } else {
            throw new NoSuchFieldException("setControllability: The node requested for ResID: "
                    + resId + " was not found.");
        }
    }

    /**
     * set availability for a particular node
     *
     * @param resId
     * @param available
     */
    public void setAvaialability(int resId, boolean available)
            throws NoSuchFieldException {
        OptionsNode lChangedNode = findOptionsNodeById(resId);

        if (lChangedNode != null) {
            lChangedNode.setAvailable(available);
        } else {
            throw new NoSuchFieldException("setAvaialability: The node requested for ResID: "
                    + resId + " was not found.");
        }
    }

    /**
     * get availability for a particular node
     *
     * @param resId
     * @return true if available, otherwise false
     * @throws NoSuchFieldException
     */
    public boolean getAvailability(int resId) throws NoSuchFieldException {
        OptionsNode lNode = findOptionsNodeById(resId);
        if (lNode != null) {
            return lNode.isAvailable();
        } else {
            throw new NoSuchFieldException("getAvailability: The node requested for ResID: "
                    + resId + " was not found.");
        }
    }

    /**
     * get controllability for a particular node
     *
     * @param resId
     * @return true if controllable, otherwise false
     * @throws NoSuchFieldException
     */
    public boolean getControllability(int resId) throws NoSuchFieldException {
        OptionsNode lNode = findOptionsNodeById(resId);
        if (lNode != null) {
            return lNode.isControllable();
        } else {
            throw new NoSuchFieldException("getControllability: The node requested for ResID: "
                    + resId + " was not found.");
        }
    }

    /*
     * gets the DefaultListView instance from a node's view [A node's view is
     * encapsulated in a RelativeLayout. It can contain just the DefaultListView
     * and/or additional views]
     */
    private View getDefaultListViewInstance(View view) {
        View lRetView = null;
        lRetView = view.findViewById(R.id.defaultListView);
        return lRetView;
    }

    /*
     * To know if the children is available or not from the application
     */
    private void getChildrenAvailabilityAndConrollability(OptionsNode parent) {
        if (mOptionsListener != null && parent != null) {
            int lChildCount = parent.getChildCount();
            for (int position = 0; position < lChildCount; position++) {
                OptionsNode lChild = (OptionsNode) parent.getChildAt(position);
                boolean available = mOptionsListener
                        .getAvailabilityOfNode(lChild.getId());
                boolean controllable = mOptionsListener
                        .getControllablityOfNode(lChild.getId());
                lChild.setAvailable(available);
                lChild.setControllable(controllable);
            }
        }
    }

    /*
     * get current node's view
     */
    private View loadCurrentLayout() {
        // get children availability and controllability from application
        getChildrenAvailabilityAndConrollability(mCurrentNode);
        View lRetView = mCurrentNode.loadOptionsNodeView();
        if (mCurrentNode instanceof ListOptionsNode) {
            return lRetView;
        }
        int selectionIndex = 0;
        if (mOptionsListener instanceof OptionsListener2) {
            selectionIndex = ((OptionsListener2) mOptionsListener).getSelectionIndex();
        }
        ListView listView = (ListView) getDefaultListViewInstance(lRetView);
        if (listView != null) {
            if (selectionIndex < 0 || selectionIndex > listView.getCount()) {
                selectionIndex = 0;
            }
            listView.requestFocus();
            listView.setSelection(selectionIndex);
        }
        return lRetView;
    }

    /*
     * get item position to be selected in previous node while moving backwards
     */
    private int getPrevNodePositionToSelect() {
        int position = 0;
        OptionsNode lAvailableChild = mPrevNode.getAvaialableChildAt(position);
        while (!mCurrentNode.equals(lAvailableChild)) {
            if (lAvailableChild == null) {
                // if the currentNode has been made unavailable
                position = 0;
                break;
            }
            position++;
            lAvailableChild = mPrevNode.getAvaialableChildAt(position);
        }
        return position;
    }

    /*
     * get previous node's view [is called only when moving backwards. So it has
     * to know which item in the list to keep selected when loading is finished]
     */
    private View loadPreviousLayout() {
        // get children availability and controllability from application
        getChildrenAvailabilityAndConrollability(mPrevNode);

        int position = getPrevNodePositionToSelect();

        View lRetView = mPrevNode.loadOptionsNodeView();
        ListView listView = (ListView) getDefaultListViewInstance(lRetView);
        if (listView != null) {
            listView.requestFocus();
            listView.setSelection(position);
        }
        return lRetView;
    }

    /*
     * get the position of item selected in the current node
     */
    private int getSelectedPositionInCurrentNode() {
        int selectedPosition = -1;
        ViewGroup lPanel = (ViewGroup) mNpb.getPanel(PANEL_INDEX);
        View lPanelView = lPanel.getChildAt(0);
        if (lPanelView != null) {
            ListView listView = (ListView) getDefaultListViewInstance(lPanelView);
            if (listView != null) {
                if (clickedItemPosition > -1) {
                    selectedPosition = clickedItemPosition;
                } else {
                    selectedPosition = listView.getSelectedItemPosition();
                }
            }
        }
        return selectedPosition;
    }

    /*
     * Update mPrevNode and mCurrentNode
     */
    private void updatePrevAndCurrentNode(int index) {
        if (mCurrentIndex < index) {
            // moving to the children [rightwards]
            mPrevNode = mCurrentNode;
            if (index == 0) {
                mCurrentNode = mRootNode;
            } else {
                if (isJumpMode()) {
                    mCurrentNode = mJumpToNodeTrace.pop();
                } else {
                    int position = getSelectedPositionInCurrentNode();
                    if (position >= 0) {
                        mCurrentNode = (OptionsNode) mCurrentNode
                                .getAvaialableChildAt(position);
                    }
                }
            }
        } else if (mCurrentIndex > index) {
            // moving to the parent [leftwards]
            int backStep = mCurrentIndex - index - 1;
            while (backStep != 0) {
                mCurrentNode = mPrevNode;
                mPrevNode = (OptionsNode) mPrevNode.getParent();
                backStep--;
            }
        }
        // if currentNode and prevNode already at the right
        // position, no need to update
    }

    public View getPanelView(int index) {

        View lRetView = null;
        updatePrevAndCurrentNode(index);

        if (mCurrentIndex <= index) {
            // moving to the children [rightwards]
            lRetView = loadCurrentLayout();
            if (mIOptionsNodeStateCallBack != null) {
                mIOptionsNodeStateCallBack.onOptionNodeEntered(mCurrentNode);
            }
        } else {
            // moving to the parent [leftwards]
            lRetView = loadPreviousLayout();
            OptionsNode exitedNode = mCurrentNode;
            mCurrentNode = mPrevNode;
            if (mPrevNode != null) {
                mPrevNode = (OptionsNode) mPrevNode.getParent();
            }
            if (mOptionsListener != null && null != mCurrentNode) {
                mOptionsListener.onMovingBackwardFromNode(mCurrentNode.getId());
            }
            if (mIOptionsNodeStateCallBack != null) {
                mIOptionsNodeStateCallBack.onOptionNodeExited(exitedNode);
            }
        }

        mCurrentIndex = index;

        // to handle DPAD_DOWN, DPAD_UP keys in case of defaultListView
        ListView listView = (ListView) getDefaultListViewInstance(lRetView);
        if (listView != null && !(mCurrentNode instanceof ListOptionsNode)) {
            listView.setOnItemClickListener(this);
            listView.setOnItemSelectedListener(this);
            listView.setOnKeyListener(this);
        }

        return lRetView;
    }

    public View getBacktraceView(int depth, View v) {
        return createVertTextView(v);
    }

    public boolean isFocussable(int index) {
        boolean isFocusable = false;
        if (index >= mCurrentIndex) {
            // moving forward
            if (isJumpMode()) {
                isFocusable = !mJumpToNodeTrace.peek().isActionable();
            } else {
                int position = getSelectedPositionInCurrentNode();
                if (position >= 0) {
                    OptionsNode nextNode = (OptionsNode) mCurrentNode
                            .getAvaialableChildAt(position);
                    isFocusable = (nextNode == null) ? false : !nextNode.isActionable();
                }
            }
        } else {
            // moving backward and already at the root node
            isFocusable = (mPrevNode == null) ? false : true;
        }

        return isFocusable;
    }

    public boolean MoveToNextPage() {
        OptionsNode nextNode = null;
        if (isJumpMode()) {
            nextNode = mJumpToNodeTrace.peek();
        } else {
            int position = getSelectedPositionInCurrentNode();
            if (position >= 0) {
                nextNode = (OptionsNode) mCurrentNode
                        .getAvaialableChildAt(position);

            }
        }

        if (nextNode != null) {
            if (!nextNode.isControllable()) {
                return false;
            }
            if (mOptionsListener != null) {
                return mOptionsListener.canMoveToNextNode(nextNode.getId());
            }
        }
        return true;
    }

    public void setDepth(int depth) {
        // TODO Auto-generated method stub

    }

    public int getDepth() {
        // TODO Auto-generated method stub
        return 0;
    }

    private CheckedTextView mLastTextView;

    public void onItemSelected(AdapterView<?> adapter, View view, int pos,
                               long arg3) {
        // onItemSelected callback is put on an Handler and processed after layout pass
        // In a boundry condition, it might be processed after OptionsMenu is hidden
        // and all its pointers are cleared.
        // Thus, extra protection. [URTRACKER:337448]
        if (mCurrentNode != null) {
            if (mOptionsListener != null) {
                mOptionsListener.onNodeItemSelected(mCurrentNode
                        .getAvaialableChildAt(pos).getId());
            }
            clickedItemPosition = -1;
        }
        if (mLastTextView != null && !mLastTextView.isChecked() && mLastTextView.isEnabled()) {
            updateTextView(mLastTextView, false);
        }
        if (view != null) {
            View tempView = (TextView) view.findViewById(R.id.text1);
            if (tempView instanceof CheckedTextView) {
                mLastTextView = ((CheckedTextView) tempView);
                updateTextView(mLastTextView, true);
            }
        }

    }

    private void updateTextView(TextView tv, boolean highlight) {
        if (tv != null) {
            Typeface tf = null;
            if (highlight) {
                tf = Typeface.create("sans-serif", Typeface.NORMAL);
            } else {
                tf = Typeface.create("sans-serif-light", Typeface.NORMAL);
            }
            tv.setTypeface(tf);
        }
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }

    public void onItemClick(AdapterView<?> adapter, View view, int pos,
                            long arg3) {
        // onItemClicked callback is put on an Handler and processed after layout pass
        // In a boundry condition, it might be processed after OptionsMenu is hidden
        // and all its pointers are cleared.
        // Thus, extra protection. [URTRACKER:337448]
        if (mCurrentNode != null) {
            if (mOptionsListener != null) {
                if (mCurrentNode.getAvaialableChildAt(pos).isControllable()) { //AN-6576 fix
                    mOptionsListener.onNodeItemClicked(mCurrentNode
                            .getAvaialableChildAt(pos).getId());
                }
            }
            clickedItemPosition = pos;
            scrollToNextPage();
        }
    }

    private boolean handleUpDownKeys(View view, int keyCode, KeyEvent event) {
        /*
         * gives callback to the Settings Application before selecting a new
		 * list item
		 */
        ListView listView = (ListView) getDefaultListViewInstance(view);
        if (listView != null && event.getAction() == KeyEvent.ACTION_DOWN) {
            int currentPosition = listView.getSelectedItemPosition();
            int nextPosition;
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                nextPosition = currentPosition + 1;
                if (nextPosition >= listView.getCount()) {
                    nextPosition = listView.getCount() - 1;
                }
            } else {
                nextPosition = currentPosition - 1;
                if (nextPosition < 0) {
                    nextPosition = 0;
                }
            }
            if (mOptionsListener != null) {
                return !mOptionsListener.canSelectNodeItem(mCurrentNode.getAvaialableChildAt(nextPosition).getId());
            }
        }
        return false;
    }

    public boolean onKey(View view, int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            return handleUpDownKeys(view, keyCode, event);
        }
        return false;
    }

    /*
     * Backtrace text
     */
    private View createVertTextView(View view) {
        View lRetView;
        if (view == null) {
            lRetView = new VerticalText(mContext);
        } else {
            lRetView = view;
        }
        if (mCurrentIndex == -1) {
            ((VerticalText) lRetView).setText((String) mRootNode.getTag());
        } else {
            if (mCurrentNode != null) {
                OptionsNode nextNode = null;
                if (isJumpMode()) {
                    nextNode = mJumpToNodeTrace.peek();
                } else {
                    int position = getSelectedPositionInCurrentNode();
                    if (position >= 0) {
                        nextNode = (OptionsNode) mCurrentNode
                                .getAvaialableChildAt(position);
                    }
                }

                if (nextNode != null) {
                    ((VerticalText) lRetView).setText((String) nextNode.getTag());
                }

            }
        }
        return lRetView;
    }

    /**
     * Options listener interface
     */
    public interface OptionsListener {
        boolean canSelectNodeItem(int nodeResId);

        void onNodeItemSelected(int nodeResId);

        boolean canMoveToNextNode(int nodeResId);

        boolean getAvailabilityOfNode(int nodeResId);

        boolean getControllablityOfNode(int nodeResId);

        void onNodeItemClicked(int nodeResId);

        void onMovingBackwardFromNode(int nodeResId);
    }

    /**
     * Options listener interface - Version 2
     */
    public interface OptionsListener2 extends OptionsListener {
        int getSelectionIndex();
    }

    private void handleCurrentNodeUnavailability() {
        // if the currentNode is being made unavailable, then scroll back to
        // prev page
        scrollToPrevPage();
    }

    private void handleAncestorNodeUnavailability(OptionsNode node) {
        // search for any descendants of the node, that are visible on screen
        OptionsNode lParent = (OptionsNode) node.getParent();

        if (mPrevNode != null) {
            stepCount = 0;
            OptionsNode lPrevNode = searchNode(node, mPrevNode.getId());
            if (lPrevNode != null) {
                // if so, reset the mPrevNode and mCurrentNode pointers to
                // parent and sibling of changedNode respectively
                if (lParent != null) {
                    while (stepCount != 0) {
                        mNpb.scrollPrevPage();
                        stepCount--;
                    }
                } else {
                    LogHelper.e(TAG,
                            "onAvailabilityChanged::The Root node is made UNAVAILABLE. The settings hence terminated.");
                }
            }
        }
    }

    public void onAvailabilityChanged(OptionsNode node, boolean available) {
        if (!available) {
            // when a node is being made unavailable
            if (node.equals(mCurrentNode)) {
                handleCurrentNodeUnavailability();
            } else {
                handleAncestorNodeUnavailability(node);
            }
        }
    }

    public void onControllabilityChanged(OptionsNode node, boolean controllable) {
        // TODO Auto-generated method stub

    }

    public void show() {
        mOptMenu.setOptionsMenuListner(this);
        mOptMenu.setListener(this);
        mOptMenu.show();
    }

    private void npanelBrowserClear() {
        mNpb.clear();
        mCurrentNode = null;
        mPrevNode = null;
        mCurrentIndex = -1;
        clickedItemPosition = -1;
    }

    public void hide() {
        mOptMenu.hide();
    }
	
	/* a temporary fix for picture mode */
	/* do not use for other optns menu use cases */

    public void setVisibility(boolean value) {
        mOptMenu.show(value);
    }

    @Deprecated
    public void reSize() {

    }

    public int getParentId(int nodeId) {
        ViewParent vp = mRootNode.findViewById(nodeId).getParent();
        if (vp != null) {
            OptionsNode op = (OptionsNode) vp;
            return op.getId();
        } else {
            return -1;
        }
    }

    /**
     * This function returns the actual position of the node in its
     * container(parent).This will not consider the availability of other peer
     * nodes
     *
     * @param nodeId
     * @return
     */

    public int getActualNodePos(int nodeId) {
        View v = mRootNode.findViewById(nodeId);
        if (v != null) {
            ViewParent vp = v.getParent();

            OptionsNode op = (OptionsNode) vp;
            int i = -1;
            for (i = 0; i < op.getChildCount(); i++) {
                View cv = op.getChildAt(i);
                if (cv.equals(v)) {
                    break;
                }
            }
            return i;
        } else {
            return -1;
        }
    }

    private boolean mEnableDimming = true;

    public void enableDiming(boolean lEnable) {
        mEnableDimming = lEnable;
    }

    public void setOptionsMenuListner(IOptionsMenuCallBack argListener) {
        mOptionsMenuCallBackListener = argListener;
    }

    public int getNodePos(int nodeId) {

        // the two for loops below can this be merged into a simple single loop?
        View v = mRootNode.findViewById(nodeId);
        if (v != null) {
            ViewParent vp = v.getParent();
            OptionsNode op = (OptionsNode) vp;
            int count = op.getAvailableChildCount();
            for (int i = 0; i < count; i++) {
                if (v.equals(op.getAvaialableChildAt(i))) {
                    return i;
                }
            }
            return -1;
        } else {
            return -1;
        }
    }

    @Override
    public boolean dispatchUnhandledKeys(KeyEvent event) {
        if (null != mOptionsMenuCallBackListener) {
            return mOptionsMenuCallBackListener.dispatchUnhandledKeys(event);
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsMenuDismissed() {
        hide();
        npanelBrowserClear();
        if (null != mOptionsMenuCallBackListener) {
            return mOptionsMenuCallBackListener.onOptionsMenuDismissed();
        } else {
            return false;
        }
    }

    /**
     * used to jump directly to a particular node
     *
     * @param nodeId
     */
    public void jumpToNode(int nodeId) {
        setJumpMode(true);
        OptionsNode destNode = findOptionsNodeById(nodeId);
        OptionsNode srcNode = mCurrentNode;
        mScrolledToCommonParent = false;

        if (destNode != null) {
            // if a dest node is found in the tree
            OptionsNode commonParent = null;
            commonParent = findCommonParent(srcNode, destNode);

            scrollBackToCommonParent(srcNode, commonParent);

            if (mScrolledToCommonParent) {
                scrollForwardToDestNode(destNode, commonParent);
                setJumpMode(false);
            } else {
                // save src, dest and common-parent temporarily until animation ends.
                tempCommonParent = commonParent;
                tempDestNode = destNode;
            }
        } else {
            LogHelper.e(TAG, "jumpToNode: dest node not found for nodeID: " + nodeId);
        }

    }

    /*
     * Find the common parent for src and dest nodes, gives an idea of how much to roll back
     */
    private OptionsNode findCommonParent(OptionsNode src, OptionsNode dest) {
        OptionsNode commonParent;
        int srcLevel = 0, destLevel = 0;

        // find the depth of source node
        OptionsNode parent = src;
        int level = 0;
        for (level = 0; !parent.equals(mRootNode); level++) {
            parent = (OptionsNode) parent.getParent();
        }
        srcLevel = level;

        // find the depth of dest node
        parent = dest;
        level = 0;
        for (level = 0; !parent.equals(mRootNode); level++) {
            parent = (OptionsNode) parent.getParent();
        }
        destLevel = level;

        // reset both src and dest to same level in the tree
        int levelDifference = Math.abs(srcLevel - destLevel);
        if (levelDifference > 0) {
            if (srcLevel > destLevel) {
                int count = levelDifference;
                while (count > 0) {
                    src = (OptionsNode) src.getParent();
                    count--;
                }
            } else {
                int count = levelDifference;
                while (count > 0) {
                    mJumpToNodeTrace.push(dest);
                    dest = (OptionsNode) dest.getParent();
                    count--;
                }
            }
        }

        // search for common parent
        while (!src.equals(dest)) {
            mJumpToNodeTrace.push(dest);
            src = (OptionsNode) src.getParent();
            dest = (OptionsNode) dest.getParent();
        }

        // a common parent would be found. In the worst case, root node should be the common parent
        commonParent = src;
        return commonParent;
    }

    /*
     * Scroll back to common parent
     */
    private void scrollBackToCommonParent(OptionsNode src, OptionsNode commonParent) {
        // scroll back till common parent is reached
        if (src.equals(commonParent)) {
            // don't have to wait for animation to end.
            mScrolledToCommonParent = true;
            return;
        }

        while (!src.equals(commonParent)) {
            scrollToPrevPage();
            src = (OptionsNode) src.getParent();
        }
    }
	

	/*
	 * Scroll fwd to dest node
	 */


    private void scrollForwardToDestNode(OptionsNode dest, OptionsNode commonParent) {
        int position = 0;
        if (mRootNode.equals(commonParent)) {
            mCurrentNode = mRootNode;
        }
        OptionsNode lAvailableChild = mCurrentNode.getAvaialableChildAt(position);
        while (dest.equals(lAvailableChild)) {
            if (lAvailableChild == null) {
                // if the currentNode has been made unavailable
                position = 0;
                break;
            }
            position++;
            lAvailableChild = mCurrentNode.getAvaialableChildAt(position);
        }
        ViewGroup lPanel = (ViewGroup) mNpb.getPanel(PANEL_INDEX);
        View lPanelView = lPanel.getChildAt(0);
        if (lPanelView != null) {
            ListView listView = (ListView) getDefaultListViewInstance(lPanelView);
            if (listView != null) {
                listView.setSelection(position);
                clickedItemPosition = -1;
            }
        }
        while (!mJumpToNodeTrace.isEmpty()) {
            scrollToNextPage();
        }
    }

    public void setJumpMode(boolean isJumpMode) {
        mJumpMode = isJumpMode;
        mJumpToNodeTrace.clear();
        // reset temp src, dest and common-parent.
        tempCommonParent = null;
        tempDestNode = null;
    }

    public boolean isJumpMode() {
        return mJumpMode;
    }

    public void refreshUI() {

        if (mCurrentNode instanceof ListOptionsNode) {
            ((ListOptionsNode) mCurrentNode).refreshUI();
            return;
        } else {

            getChildrenAvailabilityAndConrollability(mCurrentNode);
            if (mCurrentNode.getAvailableChildCount() == 0) {
                if (mCurrentNode.equals(mRootNode)) {
                    hide();
                } else {
                    scrollToPrevPage();
                }
                return;
            }

            loadCurrentLayout();
        }
    }

    @Override
    public void onAnimationEnd() {
        if (isJumpMode() && !mScrolledToCommonParent) {
            scrollForwardToDestNode(tempDestNode, tempCommonParent);
            mScrolledToCommonParent = true;
            setJumpMode(false);
        }
    }

    @Override
    public void onAnimationStart() {

    }

    public void setOptionsNodeListener(IOptionsNodeStateCallBack nodeListener) {
        mIOptionsNodeStateCallBack = nodeListener;
    }
}
