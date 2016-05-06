package de.eric_scheibler.tactileclock;

import android.app.Fragment;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class InfoFragment extends Fragment {

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

        String version = null;
        try {
            version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            version = null;
        } finally {
            if (version != null) {
                TextView labelApplicationVersion = (TextView) view.findViewById(R.id.labelApplicationVersion);
                labelApplicationVersion.setText(
                        String.format(
                            "%1$s%2$s",
                            getResources().getString(R.string.labelApplicationVersion),
                            version)
                        );
            }
        }
    }

}
