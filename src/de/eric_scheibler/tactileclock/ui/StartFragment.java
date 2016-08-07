package de.eric_scheibler.tactileclock.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.eric_scheibler.tactileclock.R;

public class StartFragment extends Fragment {

    // newInstance constructor for creating fragment with arguments
    public static StartFragment newInstance() {
        StartFragment startFragmentInstance = new StartFragment();
        return startFragmentInstance;
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

}
