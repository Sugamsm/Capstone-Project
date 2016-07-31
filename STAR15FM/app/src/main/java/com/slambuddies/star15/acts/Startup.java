package com.slambuddies.star15.acts;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.slambuddies.star15.R;
import com.slambuddies.star15.backs.SongFetch;
import com.slambuddies.star15.datab.FMContract;
import com.slambuddies.star15.tools.JsonParser;
import com.slambuddies.star15.tools.Tools;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;


public class Startup extends AppCompatActivity {

    private static final int REQUEST_READ_PHONE_STATE = 15;
    int[] ids = new int[]{R.mipmap.back_1, R.mipmap.back_2, R.mipmap.back_3, R.mipmap.back_4, R.mipmap.back_5, R.mipmap.back_6, R.mipmap.back_7, R.mipmap.back_8, R.mipmap.back_9, R.mipmap.back_10, R.mipmap.back_11, R.mipmap.back_12, R.mipmap.back_13, R.mipmap.back_14, R.mipmap.back_15, R.mipmap.back_16, R.mipmap.back_17, R.mipmap.back_18, R.mipmap.back_19, R.mipmap.back_20, R.mipmap.back_21, R.mipmap.back_22, R.mipmap.back_23, R.mipmap.back_24, R.mipmap.back_25, R.mipmap.back_26, R.mipmap.back_27, R.mipmap.back_28, R.mipmap.back_29, R.mipmap.back_30, R.mipmap.back_31};
    int rndId = 0;
    ImageView iv;
    TextView tv1, tv2, tv3, tv4, un_ex;
    String disp;
    EditText uname;
    Button b;
    String name, imei;
    ProgressDialog pd;
    private TextInputLayout textInputLayout;
    public static boolean found = false, showing = false;
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup);
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.cancel(Tools.NOTIFICATION_ID);
        Tools.storePrefs(this, "notif_counter", "0");
        Tools.storeBoolPrefs(this, "NOTIFIED", false);

        pd = new ProgressDialog(Startup.this);
        rndId = Tools.getRandomInteger(30, 0);
        iv = (ImageView) findViewById(R.id.startup_iv);
        tv1 = (TextView) findViewById(R.id.mHTag);
        tv2 = (TextView) findViewById(R.id.mHTag2);
        tv3 = (TextView) findViewById(R.id.mTag);
        tv4 = (TextView) findViewById(R.id.mTag2);
        un_ex = (TextView) findViewById(R.id.un_ex);
        textInputLayout = (TextInputLayout) findViewById(R.id.su_et);
        uname = (EditText) findViewById(R.id.signupName);
        b = (Button) findViewById(R.id.suB);

        Picasso.with(this).load(ids[rndId]).error(R.mipmap.back_14).into(iv);

        if (savedInstanceState != null) {
            showing = savedInstanceState.getBoolean("showing");
            if (showing) {
                Show(showing);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bundle = ActivityOptions.makeSceneTransitionAnimation(Startup.this).toBundle();
        } else {
            bundle = null;
        }


        if (Tools.loadPrefs(this, "reg")) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        uname.setVisibility(View.GONE);
                        b.setVisibility(View.GONE);

                        if (Tools.loadPrefs(Startup.this, "playing")) {
                            Thread.sleep(1000);
                        } else {
                            Thread.sleep(2000);
                        }


                    } catch (Exception e) {
                    } finally {
                        Intent intent = new Intent(Startup.this, StarMain.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent, bundle);

                        finish();
                    }
                }
            });
            t.start();
        }

        uname.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        un_ex.setText(getString(R.string.un_et_length));
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String all = s.toString().trim().toUpperCase();
                        all = all.replaceAll(" ", "_");
                        all = all.replaceAll("@", "");
                        disp = "@";
                        try {
                            disp += all.replaceAll(" ", "_");
                        } catch (Exception e) {
                            disp += all;
                        }
                        if (all.length() > 0 || !all.isEmpty()) {
                            String name_disp = getString(R.string.disp_name) + " " + disp;
                            un_ex.setText(name_disp);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                }

        );

        b.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (Tools.isNetConn(Startup.this)) {
                                if (Tools.checkPlayServices(Startup.this)) {
                                    if (validate()) {
                                        name = uname.getText().toString().trim();
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            checkPermission();
                                        } else {
                                            TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                                            imei = tManager.getDeviceId();
                                            new Reg().execute();
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(Startup.this, getString(R.string.net_error), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                        }
                    }
                }
        );
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.mipmap.star15_logo_final_save);
                builder.setTitle(getString(R.string.imei_title));
                builder.setMessage(getString(R.string.imei_msg));
                builder.setNeutralButton(getString(R.string.got_it), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.create().show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        REQUEST_READ_PHONE_STATE);
            }
        } else {
            // In case User Registers after Un-registering...
            //The Permission is already be granted.
            TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            imei = tManager.getDeviceId();
            new Reg().execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    imei = tManager.getDeviceId();
                    new Reg().execute();
                }
                break;
        }
    }

    public boolean validate() {
        String un_name = uname.getText().toString().trim();
        un_name = un_name.replaceAll(" ", "");
        if (un_name.length() == 0) {
            textInputLayout.setError(getString(R.string.empty_et_name));
            uname.requestFocus();
            return false;
        }
        if (un_name.length() < 3 || un_name.length() > 20) {
            textInputLayout.setError(getString(R.string.un_et_length));
            uname.requestFocus();
            return false;
        }
        if (!un_name.matches("[a-zA-Z_]{3,20}")) {
            textInputLayout.setError(getString(R.string.un_et_alphabet));
            uname.requestFocus();
            return false;
        }
        if (un_name.toLowerCase().contains("rjstar")) {
            textInputLayout.setError(getString(R.string.rj_error));
            uname.requestFocus();
            return false;
        }

        return true;
    }

    public void Show(boolean should) {
        if (should) {
            pd.setMessage(getString(R.string.reg_loader));
            pd.setCancelable(false);
            showing = true;
            pd.show();
        } else {
            if (pd != null) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                showing = false;
            }
        }
    }

    class Reg extends AsyncTask<Void, String, Void> {
        int success;

        @Override
        protected void onPreExecute() {
            Show(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            String token = Tools.getPrefs(Startup.this, "token");
            if (token == null || token.equals("")) {
                token = FirebaseInstanceId.getInstance().getToken();
            }
            //Log Your Token Here or Already in the FID
            try {
                if (token != null) {
                    JsonParser jsonParser = new JsonParser();
                    String[] data = new String[]{"REG_VER", imei};
                    JSONObject json = jsonParser.getJSONFromUrl(Tools.POST_URL, Tools.getJson(data, null));
                    String[] data_;
                    if (json != null) {
                        success = json.getInt("success");
                        if (success != 0) {
                            if (success == 4) {
                                name = json.getString("name");
                                publishProgress("FULL", name);
                                data_ = new String[]{"UPDATE", imei, token};
                                disp = name;
                            } else {
                                data_ = new String[]{"REG", disp, imei, token};
                            }
                            JSONObject object = jsonParser.getJSONFromUrl(Tools.POST_URL, Tools.getJson(data_, null));


                            if (object != null) {
                                int suc = object.getInt("success");
                                if (suc == 1) {
                                    found = true;
                                    Tools.storeBoolPrefs(Startup.this, "reg", true);
                                    ContentValues values = new ContentValues();
                                    values.put(FMContract.USER_IMEI, imei);
                                    values.put(FMContract.USER_NAME, disp);
                                    getContentResolver().insert(FMContract.UserData.CONTENT_URI, values);
                                } else if (suc == 10) {
                                    publishProgress("TAKEN", disp);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            switch (values[0]) {
                case "FULL":
                    Toast.makeText(Startup.this, getString(R.string.found_reg) + " " + values[1], Toast.LENGTH_LONG).show();
                    break;
                case "TAKEN":
                    Toast.makeText(Startup.this, "Name " + values[1] + " " + getString(R.string.taken_name), Toast.LENGTH_LONG).show();
                    break;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Show(false);
            if (found) {
                Toast.makeText(Startup.this, getString(R.string.reg_success), Toast.LENGTH_LONG).show();
                startActivity(new Intent(Startup.this, StarMain.class), bundle);
                startService(new Intent(Startup.this, SongFetch.class));
                finish();
            } else {
                Toast.makeText(Startup.this, getString(R.string.reg_error), Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("showing", showing);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pd != null) {
            if (pd.isShowing()) {
                pd.dismiss();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
