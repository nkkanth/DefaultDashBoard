package org.droidtv.defaultdashboard.data.model.moreChapter;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.model.Action;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.ui.tvwidget2k15.dialog.ModalDialog;
import org.droidtv.ui.tvwidget2k15.dialog.ModalDialogInterface;

import static org.droidtv.defaultdashboard.util.Constants.CLEAR_HISTORY_INTENT;

/**
 * Created by sandeep.kumar on 24/11/2017.
 */

public class ClearHistoryItem extends MoreChapterShelfItem {

    public ClearHistoryAction mAction;


    public ClearHistoryItem(Context context) {
        super(context.getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_ITEM_CLEAR_HISTORY), context.getDrawable(R.drawable.icon_328_clear_all_n_54x54));
        mAction = new ClearHistoryAction(context);
    }

    @Override
    public Action getAction() {
        return mAction;
    }

    public void removeClearHistoryDialogue() {
        if(mAction != null) {
            mAction.removeClearHistoryDialogue();
            mAction = null;
        }
    }

    private static class ClearHistoryAction implements Action {

        private ModalDialog mDialog = null;
        private Context mContext = null;

        protected ClearHistoryAction(Context context) {
            mContext = context;
        }

        @Override
        public void perform() {
            initClearHistoryAlertDialog();
            if (!mDialog.isShowing()) {
                mDialog.show();
            }
        }

        private void initClearHistoryAlertDialog() {
            final ModalDialog.Builder builder = new ModalDialog.Builder(mContext, ModalDialog.HEADING_TYPE_NO_SUB_TITLE);

            builder.setButton(ModalDialog.BUTTON_LEFT,
                    mContext.getResources().getString(org.droidtv.ui.strings.R.string.MAIN_CANCEL), true,
                    new ModalDialogInterface.OnClickListener() {

                        @Override
                        public void onClick(ModalDialogInterface modalDialogInterface, int which) {
                            DdbLogUtility.logMoreChapter("ClearHistoryItem", "onClick() dismiss called ");
                            mDialog.dismiss();
                            return;
                        }
                    }).requestFocus();

            builder.setButton(ModalDialog.BUTTON_RIGHT,
                    mContext.getResources().getString(org.droidtv.ui.strings.R.string.MAIN_BUTTON_CONTINUE), true,
                    new ModalDialogInterface.OnClickListener() {

                        @Override
                        public void onClick(ModalDialogInterface modalDialogInterface, int which) {
                            DdbLogUtility.logMoreChapter("ClearHistoryItem", "onClick() Continue called ");
                            Intent clearHistoryIntent = new Intent(CLEAR_HISTORY_INTENT);
                            clearHistoryIntent.setPackage(Constants.PACKAGE_NAME_CLONEAPP_MGR);
                            mContext.sendBroadcastAsUser(clearHistoryIntent, UserHandle.CURRENT_OR_SELF);
                            mDialog.dismiss();
                        }
                    });
            builder.setHeading(mContext.getResources().getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_DELETE_ACC_CLEAR_HISTORY), null);
            builder.setMessage(mContext.getResources().getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_CLEAR_DATA_NOW_HELP));

            mDialog = builder.build(ModalDialog.MODAL_DIALOG_TYPE_LARGE);
        }

        public void removeClearHistoryDialogue() {
            if(mDialog != null && mDialog.isShowing()){
                mDialog.dismiss();
                mDialog = null;
            }
        }
    }
}
