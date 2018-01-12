package de.eric_scheibler.tactileclock.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;

import android.os.Bundle;

import android.support.v4.app.DialogFragment;

import android.text.Editable;

import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import de.eric_scheibler.tactileclock.listener.SelectTimeDialogCloseListener;
import de.eric_scheibler.tactileclock.R;
import de.eric_scheibler.tactileclock.utils.TextChangedListener;
import de.eric_scheibler.tactileclock.utils.TTSWrapper;

import java.util.Calendar;
import java.util.Locale;


public class SelectTimeDialog extends DialogFragment {

    // Store instance variables
    private SelectTimeDialogCloseListener selectTimeDialogCloseListener;
    private TTSWrapper ttsWrapperInstance;
    private int hour, minute;
    private boolean enabled;
    private EditText editHours, editMinutes;
    private Button buttonAddOneMinute, buttonAddTenMinutes, buttonAddOneHour;
    private Switch buttonWatchAutoSwitchOffEnabled;

    public static SelectTimeDialog newInstance() {
        SelectTimeDialog selectTimeDialogInstance = new SelectTimeDialog();
        return selectTimeDialogInstance;
    }

    @Override public void onAttach(Context context){
        super.onAttach(context);
        if (getTargetFragment() != null
                && getTargetFragment() instanceof SelectTimeDialogCloseListener) {
            selectTimeDialogCloseListener = (SelectTimeDialogCloseListener) getTargetFragment();
        } else if (context instanceof Activity
                && (Activity) context instanceof SelectTimeDialogCloseListener) {
            selectTimeDialogCloseListener = (SelectTimeDialogCloseListener) context;
        }
        ttsWrapperInstance = TTSWrapper.getInstance(context);
    }

    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            enabled = savedInstanceState.getBoolean("enabled");
            hour = savedInstanceState.getInt("hour");
            minute = savedInstanceState.getInt("minute");
        } else {
            enabled = true;
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        }

        // custom view
        final ViewGroup nullParent = null;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_select_time, nullParent);

        buttonWatchAutoSwitchOffEnabled = (Switch) view.findViewById(R.id.buttonWatchAutoSwitchOffEnabled);
        buttonWatchAutoSwitchOffEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (enabled != isChecked) {
                    enabled = isChecked;
                    updateUI();
                }
            }
        });

        editHours = (EditText) view.findViewById(R.id.editHours);
        editHours.addTextChangedListener(new TextChangedListener<EditText>(editHours) {
            @Override public void onTextChanged(EditText view, Editable s) {
                try {
                    hour = Integer.parseInt(editHours.getText().toString());
                } catch (NumberFormatException e) {
                    hour = -1;
                }
            }
        });
        editHours.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                editHours.selectAll();
            }
        });

        editMinutes = (EditText) view.findViewById(R.id.editMinutes);
        editMinutes.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    setNewTime();
                    return true;
                }
                return false;
            }
        });
        editMinutes.addTextChangedListener(new TextChangedListener<EditText>(editMinutes) {
            @Override public void onTextChanged(EditText view, Editable s) {
                try {
                    minute = Integer.parseInt(editMinutes.getText().toString());
                } catch (NumberFormatException e) {
                    minute = -1;
                }
            }
        });
        editMinutes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                editMinutes.selectAll();
            }
        });

        buttonAddOneMinute = (Button) view.findViewById(R.id.buttonAddOneMinute);
        buttonAddOneMinute.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if ((minute+1) >= 60) {
                    hour = (hour + 1) % 24;
                }
                minute = (minute + 1) % 60;
                updateUI();
                speakTime();
            }
        });

        buttonAddTenMinutes = (Button) view.findViewById(R.id.buttonAddTenMinutes);
        buttonAddTenMinutes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if ((minute+10) >= 60) {
                    hour = (hour + 1) % 24;
                }
                minute = (minute + 10) % 60;
                updateUI();
                speakTime();
            }
        });

        buttonAddOneHour = (Button) view.findViewById(R.id.buttonAddOneHour);
        buttonAddOneHour.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                hour = (hour + 1) % 24;
                updateUI();
                speakTime();
            }
        });

        return new AlertDialog.Builder(getActivity())
            .setTitle(getResources().getString(R.string.selectTimeDialogTitle))
            .setView(view)
            .setPositiveButton(
                    getResources().getString(R.string.dialogOK),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
            .setNeutralButton(
                    getResources().getString(R.string.dialogNow),
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
                    setNewTime();
                }
            });
            // neutral button
            Button buttonNeutral = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            buttonNeutral.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(System.currentTimeMillis());
                    hour = c.get(Calendar.HOUR_OF_DAY);
                    minute = c.get(Calendar.MINUTE);
                    updateUI();
                    speakTime();
                }
            });
            // negative button
            Button buttonNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            buttonNegative.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    dismiss();
                }
            });
            updateUI();
        }
    }

    @Override public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("enabled", enabled);
        savedInstanceState.putInt("hour", hour);
        savedInstanceState.putInt("minute", minute);
    }

    @Override public void onStop() {
        super.onStop();
        selectTimeDialogCloseListener = null;
    }

    private void setNewTime() {
        if (hour < 0 || hour > 23) {
            Toast.makeText(
                    getActivity(),
                    getResources().getString(R.string.messageInvalidHourValue),
                    Toast.LENGTH_LONG).show();
        } else if (minute < 0 || minute > 59) {
            Toast.makeText(
                    getActivity(),
                    getResources().getString(R.string.messageInvalidMinuteValue),
                    Toast.LENGTH_LONG).show();
        } else {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
            c.set(Calendar.SECOND, 0);
            if (selectTimeDialogCloseListener != null) {
                selectTimeDialogCloseListener.timeSelected(enabled, c.getTimeInMillis());
            }
            dismiss();
        }
    }

    private void speakTime() {
        ttsWrapperInstance.speak(
                String.format(Locale.ROOT, "%1$02d:%2$02d", hour, minute), true, true);
    }

    private void updateUI() {
        buttonWatchAutoSwitchOffEnabled.setChecked(enabled);
        editHours.setText(String.format(Locale.ROOT, "%1$02d", hour));
        editMinutes.setText(String.format(Locale.ROOT, "%1$02d", minute));
    }

}
