package com.slambuddies.star15.acts;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.slambuddies.star15.R;
import com.slambuddies.star15.backs.PostService;
import com.slambuddies.star15.datab.FMContract;
import com.slambuddies.star15.tools.Tools;

import java.util.Calendar;
import java.util.Locale;

public class Dedicate extends Fragment {
    MultiAutoCompleteTextView cdBud;
    TextInputLayout bud;
    Button slamB, switch_b;
    String post_BuddyName;
    ProgressDialog pd;
    Tools.Switcher switcher;
    public static boolean alive = false, showing = false;
    String[] data, db, names;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().registerReceiver(receiver, new IntentFilter(Tools.DED));
        if (savedInstanceState != null) {
            showing = savedInstanceState.getBoolean("showing");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dedicate_frag, container, false);
        alive = true;
        Tools.hiddenKeyboard(view);
        getActivity().getWindow().setBackgroundDrawableResource(R.mipmap.back_15);
        cdBud = (MultiAutoCompleteTextView) view.findViewById(R.id.cDedBudName);
        cdBud.setThreshold(1);
        cdBud.setTokenizer(new Tools.SpaceTokenizer());
        bud = (TextInputLayout) view.findViewById(R.id.dedBudLayout);
        slamB = (Button) view.findViewById(R.id.dedSub);
        pd = new ProgressDialog(getActivity());
        switch_b = (Button) view.findViewById(R.id.ded_switcher);
        db = Tools.getSelf(getActivity().getContentResolver().query(FMContract.UserData.CONTENT_URI, null, null, null, null));
        names = Tools.setAdapter(getActivity());
        if (names != null) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, names);
            cdBud.setAdapter(arrayAdapter);
        }

        if (showing) {
            StarMain.Show("", getActivity().getString(R.string.ded_should));
        }

        slamB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                post_BuddyName = cdBud.getText().toString().trim();
                if (post_BuddyName.length() == 0 || post_BuddyName.isEmpty()) {
                    bud.setError(getString(R.string.empty_et));
                } else {
                    if (Tools.isNetConn(getActivity())) {
                        int mentioned = 0;
                        String[] mentions = Tools.Name(getActivity(), post_BuddyName);

                        if (mentions != null) {
                            mentioned = 1;
                        }
                        Show(true);
                        Intent serv = new Intent(getActivity(), PostService.class);
                        data = new String[]{"DED", db[0], post_BuddyName, db[1], String.valueOf(mentioned)};
                        serv.putExtra("data", Tools.getJson(data, mentions));
                        String date = Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " Q" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "Q";
                        serv.putExtra("date", date);

                        getActivity().startService(serv);
                    } else {
                        Toast.makeText(getActivity(),
                                getString(R.string.net_error),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        switch_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switcher.onSwitch(v.getId());
            }
        });

        return view;

    }


    public void Show(boolean run) {
        if (pd != null) {
            if (run) {
                showing = true;
                pd.setMessage(getString(R.string.req_msg));
                pd.setCancelable(false);
                pd.show();
            } else {
                showing = false;
                pd.dismiss();
            }
        } else {
            showing = false;
        }
    }


    public void setSwitcher(Tools.Switcher switcher) {
        this.switcher = switcher;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String from = intent.getStringExtra("from");
            Show(false);
            if (from.equals("SERV")) {
                int suc = intent.getIntExtra("Success", 0);
                if (suc != 0) {
                    Toast.makeText(context, context.getString(R.string.ded_sent), Toast.LENGTH_LONG).show();
                    if (alive) {
                        clear();
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.ded_error), Toast.LENGTH_LONG).show();
                }
            } else if (from.equals(Tools.BUDS)) {
                names = Tools.setAdapter(context);
                if (names != null) {
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, names);
                    if (alive) {
                        cdBud.setAdapter(arrayAdapter);
                    }
                }
            }
        }
    };


    public void clear() {
        cdBud.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        alive = true;
        super.onResume();
    }

    @Override
    public void onPause() {
        alive = false;

        if (pd != null) {
            if (pd.isShowing()) {
                pd.dismiss();
            }
        }

        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("showing", showing);
    }
}
