package org.droidtv.defaultdashboard.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.ui.optionsframework.MultiSliderView;
import org.droidtv.ui.tvwidget2k15.NPanelBrowser;

import androidx.core.view.ViewCompat;
import androidx.leanback.widget.VerticalGridView;

/**
 * Created by bhargava.gugamsetty on 02-01-2018.
 */

public class EditDashboardNPanelBrowser extends NPanelBrowser {

    private VerticalGridView mVerticalGridView;
    private MultiSliderView mMultiSliderView;

    public EditDashboardNPanelBrowser(Context context) {
        this(context, null);
    }

    public EditDashboardNPanelBrowser(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditDashboardNPanelBrowser(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int keyAction = event.getAction();
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && keyAction == KeyEvent.ACTION_DOWN) {
            super.dispatchKeyEvent(event);
            return false;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && keyAction == KeyEvent.ACTION_DOWN) {
            if (mVerticalGridView != null && mVerticalGridView.hasFocus()) {
                View currentFocus = mVerticalGridView.findFocus();
                if (currentFocus != null) {
                    View nextFocus = currentFocus.focusSearch(ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL ?
                            FOCUS_RIGHT : FOCUS_LEFT);
                    if (!currentFocus.equals(nextFocus)) {
                        return false;
                    }
                }
            } else if (mMultiSliderView != null && mMultiSliderView.hasFocus()) {
                View currentFocus = mMultiSliderView.findFocus();
                if (currentFocus != null) {
                    View nextFocus = currentFocus.focusSearch(FOCUS_LEFT);
                    if (nextFocus == null) {
                        return super.dispatchKeyEvent(event);
                    }
                    if (!currentFocus.equals(nextFocus)) {
                        return false;
                    }
                }
            }
            return super.dispatchKeyEvent(event);
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    protected boolean loadNextPanel() {
        boolean result = super.loadNextPanel();
        mVerticalGridView = (VerticalGridView) findViewById(R.id.thumbnail_list_vertical_grid_view);
        mMultiSliderView = (MultiSliderView) findViewById(R.id.msliderview);
        return result;
    }
}
