package org.droidtv.defaultdashboard.ui.fragment;

import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.app.HeadersSupportFragment;

/**
 * Created by sandeep.kumar on 10/10/2017.
 */

public abstract class AbstractDashboardFragment extends BrowseSupportFragment {

    @Override
    public HeadersSupportFragment onCreateHeadersSupportFragment() {
        return new DashboardHeadersFragment();
    }
}
