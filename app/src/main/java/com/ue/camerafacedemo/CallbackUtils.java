package com.ue.camerafacedemo;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.view.View;

/**
 * Created by hujiang on 2017/6/29.
 */

public class CallbackUtils {
    public static boolean isViewValid(View view) {
        if (view == null) {
            return false;
        }
        if (!ViewCompat.isAttachedToWindow(view)) {
            return false;
        }
        return true;
    }

    public static void dismissDialogFragment(DialogFragment fragment) {
        if (fragment == null) {
            return;
        }
        //avoid IllegalStateException:
        //Can not perform this action after onSaveInstanceState
        if (fragment.isAdded() && fragment.isResumed()) {
            fragment.dismiss();
        }
    }

    /**
     * fragment内部回调调用
     *
     * @param fragment
     * @return
     */
    public static boolean isFragmentValid(Fragment fragment) {
        if (fragment == null) {
            return false;
        }
        if (!isActivityValid(fragment.getActivity())) {
            return false;
        }
        if (!fragment.isAdded()) {
            return false;
        }
        return true;
    }

    public static boolean isActivityValid(Activity activity) {
        if (activity == null) {
            return false;
        }
        if (activity.isFinishing()) {
            return false;
        }
        return true;
    }
}
