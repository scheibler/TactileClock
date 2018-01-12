package de.eric_scheibler.tactileclock.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;

import android.os.Bundle;

import android.support.v4.app.DialogFragment;

import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import de.eric_scheibler.tactileclock.listener.SelectIntegerDialogCloseListener;
import de.eric_scheibler.tactileclock.R;


public class SelectIntegerDialog extends DialogFragment {

    public static final int TOKEN_POWER_BUTTON_LOWER_SUCCESS_BOUNDARY = 14101;
    public static final int TOKEN_POWER_BUTTON_UPPER_SUCCESS_BOUNDARY = 14102;
    public static final int TOKEN_WATCH_INTERVAL = 14103;

    // Store instance variables
    private SelectIntegerDialogCloseListener selectIntegerDialogCloseListener;
    private int token, defaultValue;
    private EditText editInteger;

    public static SelectIntegerDialog newInstance(int token, int preSelectedInteger, int defaultValue) {
        SelectIntegerDialog selectIntegerDialogInstance = new SelectIntegerDialog();
        Bundle args = new Bundle();
        args.putInt("token", token);
        args.putInt("preSelectedInteger", preSelectedInteger);
        args.putInt("defaultValue", defaultValue);
        selectIntegerDialogInstance.setArguments(args);
        return selectIntegerDialogInstance;
    }

    @Override public void onAttach(Context context){
        super.onAttach(context);
        if (getTargetFragment() != null
                && getTargetFragment() instanceof SelectIntegerDialogCloseListener) {
            selectIntegerDialogCloseListener = (SelectIntegerDialogCloseListener) getTargetFragment();
        } else if (context instanceof Activity
                && (Activity) context instanceof SelectIntegerDialogCloseListener) {
            selectIntegerDialogCloseListener = (SelectIntegerDialogCloseListener) context;
        }
    }

    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        token = getArguments().getInt("token", -1);
        defaultValue = getArguments().getInt("defaultValue", -1);

        String dialogTitle;
        switch (token) {
            case TOKEN_WATCH_INTERVAL:
                dialogTitle = getResources().getString(R.string.selectWatchIntervalDialogTitle);
                break;
            case TOKEN_POWER_BUTTON_LOWER_SUCCESS_BOUNDARY:
                dialogTitle = getResources().getString(R.string.selectPowerButtonLowerSuccessBoundaryDialogTitle);
                break;
            case TOKEN_POWER_BUTTON_UPPER_SUCCESS_BOUNDARY:
                dialogTitle = getResources().getString(R.string.selectPowerButtonUpperSuccessBoundaryDialogTitle);
                break;
            default:
                dialogTitle = "";
                break;
        }

        // custom view
        final ViewGroup nullParent = null;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_select_integer, nullParent);

        editInteger = (EditText) view.findViewById(R.id.editInteger);
        editInteger.setText(
                String.valueOf(getArguments().getInt("preSelectedInteger", -1)));
        editInteger.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    setNewInteger();
                    return true;
                }
                return false;
            }
        });
        editInteger.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                editInteger.selectAll();
            }
        });

        return  new AlertDialog.Builder(getActivity())
            .setTitle(dialogTitle)
            .setView(view)
            .setPositiveButton(
                    getResources().getString(R.string.dialogOK),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
            .setNeutralButton(
                    getResources().getString(R.string.dialogDefault),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
            .setNegativeButton(
                    getResources().getString(R.string.dialogCancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
            .create();
    }

    @Override public void onStart() {
        super.onStart();
        final AlertDialog dialog = (AlertDialog)getDialog();
        if(dialog != null) {
            // positive button
            Button buttonPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            buttonPositive.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    setNewInteger();
                }
            });
            // neutral button
            Button buttonNeutral = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            buttonNeutral.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    editInteger.setText(String.valueOf(defaultValue));
                }
            });
            // negative button
            Button buttonNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            buttonNegative.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    dismiss();
                }
            });
        }
    }

    @Override public void onStop() {
        super.onStop();
        selectIntegerDialogCloseListener = null;
    }

    private void setNewInteger() {
        int newInteger = -1;
        try {
            newInteger = Integer.parseInt(editInteger.getText().toString());
        } catch (NumberFormatException e) {}
        if (newInteger <= -1) {
            Toast.makeText(
                    getActivity(),
                    getResources().getString(R.string.messageEnteredInvalidValue),
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (selectIntegerDialogCloseListener != null) {
            selectIntegerDialogCloseListener.integerSelected(token, newInteger);
        }
        dismiss();
    }

}
