package org.droidtv.defaultdashboard.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;

import org.droidtv.ui.tvwidget2k15.NPanelBrowser;
import org.droidtv.ui.tvwidget2k15.NPanelBrowser.NPanelBrowserAnimationListener;
import org.droidtv.ui.tvwidget2k15.NPanelBrowser.NPanelBrowserListener;
import org.droidtv.ui.tvwidget2k15.R;
import org.droidtv.ui.tvwidget2k15.utils.LogHelper;

/**
 * The options menu is a pop up over the UI. This menu helps browse the different
 * options arranged in hierarchical order.
 * <p>
 * The options menu is the npanel browser in a dialog.
 * <p>
 * To ease development team to avoid creating dialogs in the activity, the options menu is
 * is encapsulated in a singleton class.This class creates a dialog and populates
 * the dialog with an instance of NPB.
 * <p>
 * Rest of the behavior is same as NPB. Please follow the test app and design for
 * furthur information
 *
 * @author sudhir.prabhu
 */


public class OptionsMenu {
    private Dialog optionsDialog;
    private NPanelBrowser mNpb;
    private IOptionsMenuCallBack mIOptionsMenuCallBack;
    private boolean mIsRequiredDim = true;
    private Context mContext;

    public interface IOptionsMenuCallBack {
        public boolean dispatchUnhandledKeys(KeyEvent event);

        public boolean onOptionsMenuDismissed();
    }

    public void setOptionsMenuListner(IOptionsMenuCallBack argListener) {
        mIOptionsMenuCallBack = argListener;
    }

    public OptionsMenu(Context context) {
        mContext = context;
        optionsDialog = new Dialog(context);
        optionsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //AN-6268 ,AN 6264 fix
        //	changed the sequence , first positioning the the window then inflating the npb

        Window window = optionsDialog.getWindow();

        // Rankush : Dim value for outside window
        window.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        if (mIsRequiredDim) {
            window.setDimAmount(0.75f);
        } else {
            window.setDimAmount(0.0f);
        }

        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;  // calculate width in pixels given 704 dp
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        // set the position of the dialog
        lp.gravity = Gravity.TOP | Gravity.RIGHT;
        //AN-6100
        //lp.x = 0;
        //lp.y = 0;

        window.setAttributes(lp);// no title and title bar for this dialog
        window.getAttributes().windowAnimations = org.droidtv.defaultdashboard.R.style.OptionsMenuAnimation;

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(org.droidtv.defaultdashboard.R.layout.options_menu, null);
        mNpb = (NPanelBrowser) rootView.findViewById(org.droidtv.defaultdashboard.R.id.nPanelBrowser1);
        optionsDialog.setContentView(rootView);

        optionsDialog.setCanceledOnTouchOutside(false);
        optionsDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

                if (mIOptionsMenuCallBack != null) {
                    mIOptionsMenuCallBack.onOptionsMenuDismissed();
                }

            }
        });

        optionsDialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                Boolean ifHandled = false;

                if ((event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) ||
                        (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) ||
                        (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) ||
                        (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN)) {
                    // handled by child view npanel browser
                } else if (mIOptionsMenuCallBack != null) {
                    ifHandled = mIOptionsMenuCallBack.dispatchUnhandledKeys(event);
                }

                return ifHandled;
            }
        });
    }

    public void enableDiming(boolean isRequiedDim) {
        this.mIsRequiredDim = isRequiedDim;
        if (optionsDialog != null) {
            Window window = optionsDialog.getWindow();

            window.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            if (isRequiedDim) {
                window.setDimAmount(0.75f);
            } else {
                window.setDimAmount(0.0f);
            }
        }
    }

    @Deprecated
    public static OptionsMenu getInstance(Context context) {
        return null;
    }


    /**
     * this is used to show the options menu
     */


    public void show() {
        optionsDialog.getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        boolean isBno = mContext.getResources().getBoolean(R.bool.product_type_bno);
        if (isBno) {
	/*Android-P: Uncommnet below line once dimen is available, Currenlty its commented for DDB bringup*/
	    /*mNpb.setPadding(mNpb.getPaddingLeft(), mContext.getResources().getDimensionPixelOffset(R.dimen.optionsmenu_npanel_container_y1),
                    mNpb.getPaddingRight(), mNpb.getPaddingBottom());*/
        } else {
            mNpb.setPadding(mNpb.getPaddingLeft(), mContext.getResources().getDimensionPixelOffset(R.dimen.optionsmenu_stack_y1),
                    mNpb.getPaddingRight(), mNpb.getPaddingBottom());
        }
        optionsDialog.show();
    }

    public void show(boolean show) {
        if (show) {
            optionsDialog.show();
        } else {
            optionsDialog.hide();
        }
    }


    /**
     * this is used to hide the options menu
     */

    public void hide() {
        optionsDialog.cancel();
    }

    public boolean isShowing() {
        return optionsDialog == null ? false : optionsDialog.isShowing();
    }

    /**
     * Listner to the npb
     *
     * @param listener
     */

    public void setListener(NPanelBrowserListener listener) {
        LogHelper.d("jumpToNode", "OptionsMenu: setListener : " + (listener instanceof NPanelBrowserAnimationListener));
        mNpb.setNPanelBrowserListner(listener);
        if (listener instanceof NPanelBrowserAnimationListener) {
            mNpb.setAnimationListener((NPanelBrowserAnimationListener) listener);
        }
    }

    public NPanelBrowser getNPanelBrowser() {
        return mNpb;
    }

    @Deprecated
    public void reSize() {
        LayoutParams lp = mNpb.getLayoutParams();
        lp.width = LayoutParams.WRAP_CONTENT;
        mNpb.setLayoutParams(lp);
    }
}
