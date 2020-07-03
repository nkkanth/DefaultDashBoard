package org.droidtv.defaultdashboard.data.model;

import android.content.Context;

/**
 * Created by sandeep.kumar on 15/11/2017.
 */

public abstract class ContextualObject {

    private Context mContext;

    protected ContextualObject(Context context) {
        mContext = context;
    }

    protected Context getContext() {
        return mContext;
    }
}
