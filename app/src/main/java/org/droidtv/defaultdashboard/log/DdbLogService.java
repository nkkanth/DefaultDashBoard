package org.droidtv.defaultdashboard.log;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by bhargava.gugamsetty on 22-04-2019.
 */

public class DdbLogService extends IntentService {
    private static final String TAG = "DdbLogService";


    public DdbLogService() {
        super("DdbLogService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent() called with: intent = [" + intent.toString() + "]");
        if (intent != null) {
            String action = intent.getStringExtra(LogConstants.LOG_ACTION);
            boolean isStability = intent.getBooleanExtra(LogConstants.STABILITY, false);
            int chaptertype = intent.getIntExtra(LogConstants.CHAPTER_TYPE, 0);
            Log.d(TAG, String.format("onHandleIntent() called with: command : %s, stability : %b, chapter : %d", action, isStability, chaptertype));
            if (LogConstants.ENABLE.equalsIgnoreCase(action)) {
                DdbLogUtility.setLogLevel(chaptertype, isStability);
            } else if (LogConstants.DISABLE.equalsIgnoreCase(action)) {
                DdbLogUtility.unSetLogLevel(chaptertype, isStability);
            } else {
                Log.e(TAG, "Invalid logger command");
            }
        }
    }
}
