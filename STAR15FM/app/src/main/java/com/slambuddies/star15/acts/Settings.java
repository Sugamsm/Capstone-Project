package com.slambuddies.star15.acts;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.slambuddies.star15.R;
import com.slambuddies.star15.backs.PostService;
import com.slambuddies.star15.datab.FMContract;
import com.slambuddies.star15.tools.Tools;

public class Settings extends AppCompatActivity {

    private static boolean alive = false;
    static CoordinatorLayout coordinatorLayout;
    static String pd_data = "";
    public static String user_name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alive = true;
        setContentView(R.layout.settings_layout);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.c_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(R.id.settings_fl, new MyPreferenceFragment()).commit();

    }

    public static void Snack(String from, String msg) {
        if (alive) {
            SpannableString s = new SpannableString(msg);
            s.setSpan(new ForegroundColorSpan(Color.parseColor("#448AFF")), s.toString().indexOf("@"), msg.length(), 0);
            s.setSpan(new UnderlineSpan(), s.toString().indexOf("@"), msg.length(), 0);
            s.setSpan(new StyleSpan(Typeface.BOLD), 0, from.length(), 0);
            Snackbar snackbar = Snackbar.make(coordinatorLayout, s, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        static AlertDialog.Builder builder = null;
        static AlertDialog dialog = null;
        static String data = "", disp, name = "";
        static ProgressDialog pd;
        static boolean error = false, show = false, showing = false;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            pd = new ProgressDialog(getActivity());
            if (savedInstanceState != null) {
                data = savedInstanceState.getString("data", "");
                show = savedInstanceState.getBoolean("show", false);
                showing = savedInstanceState.getBoolean("showing", false);
                error = savedInstanceState.getBoolean("error", false);
                if (show) {
                    display(getActivity(), data);
                }
                if (showing) {
                    pd_data = savedInstanceState.getString("pd_data", "");
                    show(getActivity(), true, pd_data);
                }
            }


            Preference et = findPreference("un_edit");
            et.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Cursor cur = getActivity().getContentResolver().query(FMContract.UserData.CONTENT_URI, null, null, null, null);
                    name = Tools.getSelf(cur)[0];
                    display(getActivity(), name);
                    return false;
                }
            });
        }

        public static void display(final Context context, final String text) {
            builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.user_name_edit));
            final EditText input = new EditText(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setSingleLine(true);
            input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            input.setLayoutParams(lp);
            input.setText(text);

            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    error = false;
                    input.setError(null);
                    String all = s.toString().trim().toUpperCase();
                    disp = "@";
                    all = all.replaceAll(" ", "_");
                    all = all.replaceAll("@", "");
                    disp += all;
                    data = disp;
                    if (all.matches("@[0-9]+") || all.matches("[0-9]+")) {
                        error = true;
                    } else if (disp.length() < 4 || disp.length() > 20) {
                        input.setError(context.getString(R.string.et_error));
                        error = true;
                    } else if (!disp.matches("[a-zA-Z_@]{3,20}")) {
                        input.setError(context.getString(R.string.un_et_alphabet));
                        error = true;
                    } else if (disp.toLowerCase().contains("star") && disp.toLowerCase().contains("rj")) {
                        input.setError(context.getString(R.string.rj_error));
                        error = true;
                    }
                    Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    if (error) {
                        positive.setEnabled(false);
                    } else {
                        positive.setEnabled(true);
                    }
                }
            });

            builder.setView(input);
            builder.setPositiveButton("Done!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String final_name = disp;
                    if (!final_name.equals(name)) {
                        show = false;
                        data = "";
                        dialog.dismiss();
                        Settings.user_name = final_name;
                        show(context, true, context.getString(R.string.name_updating) + " " + final_name);
                        String[] data = new String[]{"UPDATE_UN", final_name, name};
                        context.startService(new Intent(context, PostService.class).putExtra("data", Tools.getJson(data, null)));
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    show = false;
                    data = "";
                    dialog.dismiss();
                }
            });

            dialog = builder.create();
            show = true;
            dialog.show();
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (error) {
                positive.setEnabled(false);
            } else {
                positive.setEnabled(true);
            }

        }

        private BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                show(context, false, "");
            }
        };

        public static void show(final Context context, boolean should, String data) {
            if (should) {
                pd_data = data;
                pd.setMessage(data);
                pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Settings.Snack(user_name, context.getString(R.string.cancelled_pd_update) + " " + user_name);
                        showing = false;
                    }
                });
                showing = true;
                pd.show();
            } else {
                pd_data = "";
                if (pd != null) {
                    if (pd.isShowing()) {
                        pd.dismiss();
                    }
                    showing = false;
                }
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putBoolean("show", show);
            outState.putBoolean("showing", showing);
            outState.putString("data", data);
            outState.putString("pd_data", pd_data);
            outState.putBoolean("error", error);
        }

        @Override
        public void onResume() {
            super.onResume();
            getActivity().registerReceiver(receiver, new IntentFilter(Tools.PREF_FRAG));
        }

        @Override
        public void onPause() {
            super.onPause();
            getActivity().unregisterReceiver(receiver);
            if (pd != null) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
            }
            if (builder != null && dialog != null) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        alive = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        alive = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        alive = false;
    }
}
