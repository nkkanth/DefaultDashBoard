package org.droidtv.defaultdashboard.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;

import androidx.annotation.Nullable;

/**
 * Created by bhargava.gugamsetty on 14-11-2017.
 */

public class ThirdPartyActivity extends Activity {

    String mTextViewName;
    Bundle mExtras;
    TextView mTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mExtras = getIntent().getExtras();
        mTextViewName = mExtras.getString("textName");
        setContentView(R.layout.activity_third_party);
        mTextView = (TextView) findViewById(R.id.app_name);
        mTextView.setText(mTextViewName);
    }


}
