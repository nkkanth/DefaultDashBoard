/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.droidtv.defaultdashboard.ui.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.util.Log;
import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.model.TopMenuItem;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.fragment.DashboardFragment;

import androidx.fragment.app.Fragment;
import androidx.leanback.widget.TitleViewAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A view to be used as a custom title view in {@link DashboardFragment}.
 */
public class TopMenuView extends RelativeLayout implements TitleViewAdapter.Provider , TopMenuItemManager.TopMenuItemFocusChangeListener {
    private View mHotelInfoContainerView;
    private TextView mLocationNameView;
    private TextView mWelcomeMessageView;
    private View mEmptyView;
    private LinearLayout mTopMenuItemContainer;
    private boolean mIsRtl;
    private TopMenuItemManager mTopMenuItemManager;
    private final TitleViewAdapter mTitleViewAdapter;
    private Fragment mFragment;
    private UiHandler mUiHandler = new UiHandler();
    //TTS usecase  browse_headers is always getting focused when left key pressed
    @Override
    public void OnItemFocus(int itemID ,boolean focus) {
        Message msg = Message.obtain();
        msg.what = mUiHandler.ITEM_FOCUS_CHANGE;
        msg.arg1 = itemID;
        msg.obj = focus;
        mUiHandler.sendMessage(msg);
    }

    private void setFocus(int itemID, boolean focus){
        if(mFragment != null && mFragment.getView() != null && mFragment.isAdded()) {
            if(focus) {
                if (getPreviousFocusableView(itemID) == null) {
                    mFragment.getView().findViewById(R.id.browse_headers).setFocusable(true);
                } else {
                    mFragment.getView().findViewById(R.id.browse_headers).setFocusable(false);
                }
            }else{
                mFragment.getView().findViewById(R.id.browse_headers).setFocusable(true);
            }
        }
    }

    public static final class TopMenuViewAdapter extends TitleViewAdapter {

        private TopMenuView mTopMenuView;

        private TopMenuViewAdapter(TopMenuView topMenuView) {
            mTopMenuView = topMenuView;
        }

        @Override
        public View getSearchAffordanceView() {
            return mTopMenuView.mEmptyView;
        }

        @Override
        public void setTitle(CharSequence titleText) {

        }

        @Override
        public void setBadgeDrawable(Drawable drawable) {

        }

        @Override
        public void setOnSearchClickedListener(OnClickListener listener) {

        }

        @Override
        public void updateComponentsVisibility(int flags) {
            DdbLogUtility.logCommon("TopMenuView", "updateComponentsVisibility flags = " + flags );
            switch (flags) {
                case DashboardFragment.WELCOME_VISIBLE:
                case DashboardFragment.FULL_TOP_ROW_VISIBLE:
                    mTopMenuView.mLocationNameView.setVisibility(VISIBLE);
                    mTopMenuView.mWelcomeMessageView.setVisibility(VISIBLE);
                    break;
                case DashboardFragment.WELCOME_INVISIBLE:
                    mTopMenuView.mLocationNameView.setVisibility(INVISIBLE);
                    mTopMenuView.mWelcomeMessageView.setVisibility(INVISIBLE);
                    break;
                case DashboardFragment.TOP_MENU_ITEMS_INVISIBLE:
                    mTopMenuView.hideTopMenuItems();
                    break;
                case DashboardFragment.TOP_MENU_ITEMS_VISIBLE:
                    mTopMenuView.showTopMenuItems();
                    break;
            }
        }

        public void setPremiseName(String name) {
            mTopMenuView.mLocationNameView.setText(name);
        }

        public void setWelcomeMessage(String message) {
            mTopMenuView.mWelcomeMessageView.setText(message);
        }
		
		public void setHeadersFragments(Fragment headersFragments){
            mTopMenuView.mFragment = headersFragments;
        }

    }

    public TopMenuView(Context context) {
        this(context, null);
    }

    public TopMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopMenuView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.view_top_menu, this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                context.getResources().getDimensionPixelSize(R.dimen.top_menu_height));
        setLayoutParams(layoutParams);
        setClipChildren(false);
        mHotelInfoContainerView = findViewById(R.id.hotel_info_container_view);
        mLocationNameView = (TextView) mHotelInfoContainerView.findViewById(R.id.location_name_view);
        mWelcomeMessageView = (TextView) mHotelInfoContainerView.findViewById(R.id.welcome_message_view);
        mEmptyView = findViewById(R.id.empty_view);
        mTopMenuItemContainer = (LinearLayout) findViewById(R.id.top_menu_items_container);

        mTitleViewAdapter = new TopMenuViewAdapter(this);

        mTopMenuItemManager = new TopMenuItemManager(getContext() ,this);

        final int viewCount = mTopMenuItemManager.getTopMenuItemCount();
        for (int i = 0; i < viewCount; i++) {
            View view = mTopMenuItemManager.getTopMenuItemAt(i).getView();
            mTopMenuItemContainer.addView(view);
        }

        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        mIsRtl = getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;

    }

    @Override
    public TitleViewAdapter getTitleViewAdapter() {
        return mTitleViewAdapter;
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        if (direction == FOCUS_UP) {
            boolean focusSuccess = manageFocus(direction);
            if (focusSuccess) {
                return true;
            }
        }
        return super.requestFocus(direction, previouslyFocusedRect);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean keyeventHandled = super.dispatchKeyEvent(event);
        if (keyeventHandled) {
            return true;
        }
        int action = event.getAction();
        int keycode = event.getKeyCode();
        if (action == KeyEvent.ACTION_DOWN) {
            switch (keycode) {
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    return manageFocus(FOCUS_RIGHT);
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    return manageFocus(FOCUS_LEFT);
            }
        }
        if (action == KeyEvent.ACTION_UP) {
            switch (keycode) {
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    return true;
            }
        }
        return keyeventHandled;
    }

    private boolean manageFocus(int direction) {
        View currentFocus = findFocus();
        if (currentFocus != null) {
            int viewId = currentFocus.getId();

            View tobeFocused = null;
            if (direction == FOCUS_RIGHT) {
                tobeFocused = mIsRtl ? getPreviousFocusableView(viewId) : getNextFocusableView(viewId);
            } else if (direction == FOCUS_LEFT) {
                tobeFocused = mIsRtl ? getNextFocusableView(viewId) : getPreviousFocusableView(viewId);
            }

            // If the left most menu item has focus, then the next focus towards left direction
            // should go to the side panel. So, we will not return true from here as we have not handled the focus.
            if (!mIsRtl) {
                if(tobeFocused == null && direction == FOCUS_LEFT) {
                    return false;
                }
            }else{
                // RTL: If the right most menu item has focus, then the next focus towards right direction
                // should go to the side panel. So, we will not return true from here as we have not handled the focus.
                if (tobeFocused == null && direction == FOCUS_RIGHT) {
                    return false;
                }
            }

            if (tobeFocused != null && tobeFocused.isFocusable()) {
                return tobeFocused.requestFocus();
            }
            return true;
        } else {
            View tobeFocused = null;
            if (direction == FOCUS_UP) {
                tobeFocused = mTopMenuItemContainer.getChildAt(0);
            }
            if (tobeFocused != null && tobeFocused.isFocusable()) {
                return tobeFocused.requestFocus();
            }
        }
        return false;
    }

    private View getPreviousFocusableView(int viewId) {
        int topMenuItemCount = mTopMenuItemManager.getTopMenuItemCount();
        for (int i = topMenuItemCount - 1; i >= 0; i--) {
            View child = mTopMenuItemManager.getTopMenuItemAt(i).getView();
            if (child.getId() == viewId) {
                for (int j = i - 1; j >= 0; j--) {
                    View nextView = mTopMenuItemManager.getTopMenuItemAt(j).getView();
                    if (nextView.getVisibility() == VISIBLE) {
                        return nextView;
                    }
                }
            }
        }
        return null;
    }

    private View getNextFocusableView(int viewId) {
        int topMenuItemCount = mTopMenuItemManager.getTopMenuItemCount();
        for (int i = topMenuItemCount - 2; i >= 0; i--) {
            View child = mTopMenuItemManager.getTopMenuItemAt(i).getView();
            if (child.getId() == viewId) {
                for (int j = i + 1; j <= topMenuItemCount - 1; j++) {
                    View previousView = mTopMenuItemManager.getTopMenuItemAt(j).getView();
                    if (previousView.getVisibility() == VISIBLE) {
                        return previousView;
                    }
                }
            }
        }
        return null;
    }

    private void hideTopMenuItems() {
        mTopMenuItemContainer.setVisibility(GONE);
    }

    private void showTopMenuItems() {
        mTopMenuItemContainer.setVisibility(VISIBLE);
    }

    public class UiHandler extends Handler{
        final int ITEM_FOCUS_CHANGE = 0x0100;
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case ITEM_FOCUS_CHANGE:
                    setFocus(msg.arg1, (boolean)msg.obj);
                break;
            }
        }
    }

    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        if(mUiHandler != null) {
            mUiHandler.removeCallbacksAndMessages(null);
        }
    }
}
