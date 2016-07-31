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
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.slambuddies.star15.R;
import com.slambuddies.star15.backs.PostService;
import com.slambuddies.star15.datab.FMContract;
import com.slambuddies.star15.tools.Tools;

import java.util.Calendar;
import java.util.Locale;

public class RequestAct extends Fragment {

    EditText SongName, MovieName;
    MultiAutoCompleteTextView dedBudName;
    Button req, switch_b;

    String post_song_name, post_movieName, post_dedBudName, Name;
    TextInputLayout sname, alname, budname;
    Tools.Switcher switcher;
    String[] data, db, names;
    public static boolean alive = false, showing = false;
    ProgressDialog pd;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().registerReceiver(receiver, new IntentFilter(Tools.REQ));
        if (savedInstanceState != null) {
            showing = savedInstanceState.getBoolean("showing");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.req_frag, container, false);
        SongName = (EditText) view.findViewById(R.id.songName);
        MovieName = (EditText) view.findViewById(R.id.albumName);
        dedBudName = (MultiAutoCompleteTextView) view.findViewById(R.id.dedName);
        dedBudName.setThreshold(1);
        sname = (TextInputLayout) view.findViewById(R.id.songNameLayout);
        alname = (TextInputLayout) view.findViewById(R.id.albumNameLayout);
        budname = (TextInputLayout) view.findViewById(R.id.dedNameLayout);
        dedBudName.setTokenizer(new Tools.SpaceTokenizer());
        getActivity().getWindow().setBackgroundDrawableResource(R.mipmap.back_26);
        req = (Button) view.findViewById(R.id.reqB);
        switch_b = (Button) view.findViewById(R.id.req_switcher);
        db = Tools.getSelf(getActivity().getContentResolver().query(FMContract.UserData.CONTENT_URI, null, null, null, null));
        Name = db[0];
        names = Tools.setAdapter(getActivity());
        alive = true;
        pd = new ProgressDialog(getActivity());
        if (names != null) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, names);
            dedBudName.setAdapter(arrayAdapter);
        }
        if (showing) {
            StarMain.Show("", getActivity().getString(R.string.req_should));
        }
        req.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (validator()) {
                            if (Tools.isNetConn(getActivity())) {
                                int mentioned = 0;
                                String[] mentions = null;
                                if (!post_dedBudName.equals("NA")) {
                                    mentions = Tools.Name(getActivity(), post_dedBudName);
                                    if (mentions != null) {
                                        mentioned = 1;
                                    }
                                }
                                Show(true);
                                Intent serv = new Intent(getActivity(), PostService.class);
                                data = new String[]{"REQ", Name, post_dedBudName, post_song_name, post_movieName, db[1], String.valueOf(mentioned)};
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
                }
        );

        switch_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switcher.onSwitch(v.getId());
            }
        });

        return view;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String from = intent.getStringExtra("from");
            if (from.equals("SERV")) {
                Show(false);
                int suc = intent.getIntExtra("Success", 0);
                if (suc != 0) {
                    Toast.makeText(context, context.getString(R.string.req_sent), Toast.LENGTH_LONG).show();
                    if (alive) {
                        clear();
                    }
                    if (!intent.getBooleanExtra("request", true)) {
                        if (StarMain.alive) {
                            StarMain.Show("", context.getString(R.string.fm_not_live_req_error));
                        }
                    }

                } else {
                    Toast.makeText(context, context.getString(R.string.req_error), Toast.LENGTH_LONG).show();
                }
            } else if (from.equals(Tools.BUDS) || from.equals("REMOVE")) {
                names = Tools.setAdapter(getActivity());
                if (names != null) {
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, names);
                    if (alive) {
                        dedBudName.setAdapter(arrayAdapter);
                    }
                }
            }
        }
    };

    public void clear() {
        SongName.setText("");
        MovieName.setText("");
        dedBudName.setText("");
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

    public boolean validator() {
        post_song_name = SongName.getText().toString().trim();
        post_movieName = MovieName.getText().toString().trim();
        post_dedBudName = dedBudName.getText().toString().trim();

        if (post_dedBudName.length() == 0 || post_dedBudName.isEmpty()) {
            post_dedBudName = "NA";
        }
        if (post_song_name.length() == 0 || post_song_name.isEmpty()) {
            sname.setError(getString(R.string.empty_et));
            return false;
        } else if (post_movieName.length() == 0 || post_movieName.isEmpty()) {
            alname.setError(getString(R.string.empty_et));
            return false;
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("showing", showing);
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
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(receiver);
    }
}
