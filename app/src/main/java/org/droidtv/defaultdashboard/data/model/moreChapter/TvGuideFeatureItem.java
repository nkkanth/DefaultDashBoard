package org.droidtv.defaultdashboard.data.model.moreChapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.UserHandle;
import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.model.Action;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.data.query.AppDetailsQuery;
import org.droidtv.defaultdashboard.data.query.Query;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.tv.persistentstorage.TvSettingsConstants;

import static org.droidtv.defaultdashboard.util.Constants.ACTION_LAUNCH_CTV_GUIDE;
import static org.droidtv.defaultdashboard.util.Constants.ACTION_LAUNCH_HTV_GUIDE;
import static org.droidtv.defaultdashboard.util.Constants.CATEGORY_JEDI_THIRD_PARTY_EPG;
import static org.droidtv.defaultdashboard.util.Constants.EXTRA_JEDI_SHOW_UI;


/**
 * Created by sandeep.kumar on 24/11/2017.
 */

public class TvGuideFeatureItem extends MoreChapterShelfItem{

    private Action mAction;
    private Context mContext;

    public TvGuideFeatureItem(Context context) {
        super(context.getString(org.droidtv.ui.strings.R.string.MAIN_TV_GUIDE), context.getDrawable(R.drawable.tv_guide_n_54x54));
        mContext = context;
        mAction = new TvGuideFeatureAction(context);
    }

    @Override
    public Action getAction() {
        return mAction;
    }

    private class TvGuideFeatureAction extends ContextualObject implements Action {

        protected TvGuideFeatureAction(Context context) {
            super(context);
        }

        @Override
        public void perform() {
            Intent launchTvGuideIntent = getTvGuideIntent();
            if(launchTvGuideIntent != null) {
                if(DashboardDataManager.getInstance().isEpgSourceApp()){
                    startEpgProviderApp(launchTvGuideIntent);
                }else {
                    launchTvGuideIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    launchTvGuideIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    getContext().startActivityAsUser(launchTvGuideIntent, UserHandle.CURRENT_OR_SELF);
                }
            }
        }
    }

    private void  startEpgProviderApp(Intent epgAppIntent){
        epgAppIntent.addCategory(CATEGORY_JEDI_THIRD_PARTY_EPG);
        epgAppIntent.putExtra(EXTRA_JEDI_SHOW_UI, true);
        mContext.startForegroundServiceAsUser(epgAppIntent, UserHandle.CURRENT_OR_SELF);
    }

    private  Intent getTvGuideIntent(){
        Intent tvGuideIntent;
        String action;
        if(DashboardDataManager.getInstance().isEpgSourceApp()) {
            tvGuideIntent = getEpgProviderAppIntent(getEpgProviderPkgName());
        }else{
            action = DashboardDataManager.getInstance().isProfessionalModeEnabled() ? ACTION_LAUNCH_HTV_GUIDE : ACTION_LAUNCH_CTV_GUIDE;
            tvGuideIntent = new Intent(action);
        }
        return tvGuideIntent;
    }

    private Intent getEpgProviderAppIntent(String pkgName){
        AppDetailsQuery query = new AppDetailsQuery(pkgName);
        Intent epgProvierIntent = null;
        Cursor c = executeQuery(query);

        if(c != null && c.moveToFirst()){
           String packageName = c.getString(c.getColumnIndex(query.getColumnName()));
           String className = c.getString(c.getColumnIndex(query.getClassName()));
           DdbLogUtility.logMoreChapter("TvGuideFeatureItem", "getEpgProviderAppIntent packageName " + packageName + " className " +className);
           if(packageName != null && className != null){
              epgProvierIntent = new Intent();
              epgProvierIntent.setComponent(new ComponentName(packageName, className));
           }
        }
        if(c != null) c.close();
        return epgProvierIntent;
    }

    private String getEpgProviderPkgName(){
        return  DashboardDataManager.getInstance().getTvSettingsManager().getString(TvSettingsConstants.PBSMGR_PROPERTY_EPG_SOURCE_FROM_APP,0, null);
    }

    private Cursor executeQuery(Query query) {
       return mContext.getContentResolver().query(query.getUri(), query.getProjection(), query.getSelection(), query.getSelectionArgs(), query.getSortOrder());
    }

    private boolean isAvailable(){
        return  DashboardDataManager.getInstance().isEpgEnabled();
    }
}
