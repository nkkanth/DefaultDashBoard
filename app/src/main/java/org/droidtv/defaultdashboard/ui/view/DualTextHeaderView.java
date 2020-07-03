package org.droidtv.defaultdashboard.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;


public class DualTextHeaderView extends LinearLayout {
    private TextView mDualTextHeaderTitleTextView;
    private TextView mDualTextHeaderSubTitleTextView;

    public DualTextHeaderView(Context context) {
        this(context, null);
    }

    public DualTextHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
		
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DoubleHeaderText);

        String titleText = typedArray.getString(R.styleable.DoubleHeaderText_titleText);
        String subTitleText = typedArray.getString(R.styleable.DoubleHeaderText_subTitleText);
        titleText = titleText == null ? "" : titleText;
        subTitleText = subTitleText == null ? "" : subTitleText;

        LayoutInflater.from(getContext()).inflate(R.layout.view_dual_text_header, this, true);
		setOrientation(LinearLayout.VERTICAL);
        
		mDualTextHeaderTitleTextView = (TextView) findViewById(R.id.title_text);
        mDualTextHeaderSubTitleTextView = (TextView) findViewById(R.id.sub_title_text);

        mDualTextHeaderTitleTextView.setText(titleText);
        mDualTextHeaderSubTitleTextView.setText(subTitleText);
		
        typedArray.recycle();
    }


    public void setTitleText(String text) {
        mDualTextHeaderTitleTextView.setText(text);
    }

    public void setSubTitleText(String text) {
        mDualTextHeaderSubTitleTextView.setText(text);
    }

}
