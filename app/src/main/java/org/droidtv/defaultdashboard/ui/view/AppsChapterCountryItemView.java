package org.droidtv.defaultdashboard.ui.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.util.Constants;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by bhargava.gugamsetty on 02-02-2018.
 */

public class AppsChapterCountryItemView extends RelativeLayout {

    private TextView mAppsChapterCountryIcon;

    public AppsChapterCountryItemView(Context context) {
        this(context, null);
    }

    public AppsChapterCountryItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppsChapterCountryItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AppsChapterCountryItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    protected int getMarginEnd() {
        return getResources().getDimensionPixelSize(R.dimen.apps_chapter_shelf_item_margin_start_end);
    }

    protected int getMarginStart() {
        return getResources().getDimensionPixelSize(R.dimen.apps_chapter_shelf_item_margin_start_end);
    }

    protected int getMarginTop() {
        return getResources().getDimensionPixelSize(R.dimen.apps_chapter_shelf_item_margin_top);
    }

    protected int getMarginBottom() {
        return getResources().getDimensionPixelSize(R.dimen.apps_chapter_shelf_item_margin_bottom);
    }

    private void initView() {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.view_apps_chapter_country_item, this);
        LayoutParams layoutParams = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        layoutParams.setMargins(getMarginStart(), getMarginTop(), getMarginEnd(), getMarginBottom());
        setLayoutParams(layoutParams);
        setFocusable(true);
        setFocusableInTouchMode(true);
        mAppsChapterCountryIcon = (TextView) findViewById(R.id.apps_chapter_country_item_icon);
        mAppsChapterCountryIcon.setTypeface(Typeface.createFromAsset(mContext.getAssets(), Constants.ICONO_FONT_PATH));
        mAppsChapterCountryIcon.setText(Constants.GLOBE_ICON_UNICODE_CODE);
        final View mRootView = rootView;
        mRootView.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setClassName("");
                StringBuilder ttsText = new StringBuilder();
                ttsText.append(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_SELECT_YOUR_HOME_COUNTRY));
                info.setContentDescription(ttsText);
            }
        });
    }
}
