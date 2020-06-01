package de.eric_scheibler.tactileclock.ui.fragment;

import android.support.v4.app.DialogFragment;


public abstract class AbstractFragment extends DialogFragment {

    /**
     * to be implemented
     */

    public abstract void fragmentVisible();
    public abstract void fragmentInvisible();


    /**
     * pause and resume
     */

    private boolean isResumed = false;
    private boolean isVisible = false;

    @Override public void onPause() {
        super.onPause();
        if (getDialog() != null || isVisible) {         // fragment is a dialog or embedded and visible
            fragmentInvisible();
        }
        isResumed = false;
    }

    @Override public void onResume() {
        super.onResume();
        if (getDialog() != null || isVisible) {         // fragment is a dialog or embedded and visible
            fragmentVisible();
        }
        isResumed = true;
    }

    @Override public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isResumed) {
            if (isVisibleToUser) {
                fragmentVisible();
            } else {
                fragmentInvisible();
            }
        }
        isVisible = isVisibleToUser;
    }

}
