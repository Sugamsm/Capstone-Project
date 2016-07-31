package com.slambuddies.star15.tools;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

import com.slambuddies.star15.R;
import com.slambuddies.star15.acts.Settings;
import com.slambuddies.star15.backs.PostService;
import com.slambuddies.star15.datab.FMContract;

public class UserNameEditPrefs extends EditTextPreference {

    boolean error = false;
    String disp, name = "";

    public UserNameEditPrefs(Context context, AttributeSet attr) {
        super(context, attr);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        final EditText et = getEditText();
        name = Tools.getSelf(getContext().getContentResolver().query(FMContract.UserData.CONTENT_URI, null, null, null, null))[0];
        et.setText(name);
        et.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        error = false;
                        String all = s.toString().trim().toUpperCase();
                        if (all.contains("@")) {
                            all = all.replaceAll("@", "");
                        }
                        disp = "@";
                        try {
                            disp += all.replaceAll(" ", "_");
                        } catch (Exception e) {
                            disp += all;
                        }

                        if (all.matches("@[0-9]+") || all.matches("[0-9]+")) {
                            error = true;
                        } else if (disp.length() < 4 || disp.length() > 20) {
                            et.setError(getContext().getString(R.string.et_error));
                            error = true;
                        } else if (!disp.matches("[a-zA-Z_@]{3,20}")) {
                            et.setError(getContext().getString(R.string.un_et_alphabet));
                            error = true;
                        } else if (all.toLowerCase().contains("rjstar")) {
                            et.setError(getContext().getString(R.string.rj_error));
                            error = true;
                        }
                        Dialog d = getDialog();
                        if (d instanceof AlertDialog) {
                            AlertDialog alertDialog = (AlertDialog) d;
                            Button positive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            if (error) {
                                positive.setEnabled(false);
                            } else {
                                positive.setEnabled(true);
                            }
                        }
                    }
                }

        );
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            if (error) {
                Settings.Snack(name, getContext().getString(R.string.name_update_invalid) + " " + name);
            } else {
                String final_name = disp;
                if (!final_name.equals(name)) {
                    String[] data = new String[]{"UPDATE_UN", final_name, name};
                    getContext().startService(new Intent(getContext(), PostService.class).putExtra("data", Tools.getJson(data, null)));
                }
            }
        }

    }
}
