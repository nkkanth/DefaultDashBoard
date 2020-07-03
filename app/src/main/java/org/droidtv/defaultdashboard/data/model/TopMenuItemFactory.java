package org.droidtv.defaultdashboard.data.model;

import android.content.Context;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.leanback.widget.TitleViewAdapter;

import org.droidtv.defaultdashboard.ui.view.TopMenuItemManager;

/**
 * Created by sandeep.kumar on 30/10/2017.
 */

public class TopMenuItemFactory {
    public static final int TYPE_CLOCK = 1;
    public static final int TYPE_LANGUAGE = 2;
    public static final int TYPE_WEATHER = 3;
    public static final int TYPE_ALARM = 4;
    public static final int TYPE_BILL = 5;
    public static final int TYPE_MESSAGES = 6;
    public static final int TYPE_USER_ACCOUNT = 7;
    public static final int TYPE_GOOGLE_ASSISTANT = 8;

    private Context mContext;

    public TopMenuItemFactory(Context context) {
        mContext = context;
    }

    public TopMenuItem getTopMenuItem(int type , TopMenuItemManager.TopMenuItemFocusChangeListener listener) {
        switch (type) {
            case TYPE_CLOCK:
                return new ClockMenuItem(mContext);

            case TYPE_LANGUAGE:
                return new LanguageMenuItem(mContext ,listener);

            case TYPE_WEATHER:
                return new WeatherMenuItem(mContext ,listener);

            case TYPE_ALARM:
                return new AlarmMenuItem(mContext ,listener);

            case TYPE_BILL:
                return new BillMenuItem(mContext,listener);

            case TYPE_MESSAGES:
                return new MessagesMenuItem(mContext ,listener);

            case TYPE_USER_ACCOUNT:
                return new UserAccountMenuItem(mContext ,listener);

            case TYPE_GOOGLE_ASSISTANT:
                return new GoogleAssistantMenuItem(mContext ,listener);

            default:
                throw new IllegalArgumentException("Unknown top menu item type:" + type);
        }
    }
}
