package de.eric_scheibler.tactileclock.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;

import de.eric_scheibler.tactileclock.R;
import de.eric_scheibler.tactileclock.utils.SettingsManager;


public class HelpDialog extends DialogFragment {
    public static final String REQUEST_DIALOG_CLOSED = "dialogClosed";

    private SettingsManager settingsManagerInstance;

    public static HelpDialog newInstance() {
        HelpDialog helpDialogInstance = new HelpDialog();
        return helpDialogInstance;
    }

    @Override public void onAttach(Context context){
        super.onAttach(context);
        settingsManagerInstance = new SettingsManager();
    }

    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ViewGroup nullParent = null;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_help, nullParent);

        return new AlertDialog.Builder(getActivity())
            .setTitle(getResources().getString(R.string.helpDialogTitle))
            .setView(view)
            .setPositiveButton(
                    getResources().getString(R.string.dialogOK),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Bundle result = new Bundle();
                            getParentFragmentManager().setFragmentResult(REQUEST_DIALOG_CLOSED, result);
                            dismiss();
                        }
                    })
            .create();
    }

}
