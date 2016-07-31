package com.slambuddies.star15.acts;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.slambuddies.star15.R;
import com.slambuddies.star15.backs.SongFetch;
import com.slambuddies.star15.backs.StreamService;
import com.slambuddies.star15.datab.FMContract;
import com.slambuddies.star15.tools.Tools;
import com.slambuddies.star15.widget.FMWidgetProvider;
import com.squareup.picasso.Picasso;

public class ListenAct extends Fragment {

    TextView mhead, current, uName;
    ImageButton play;
    ImageView albumArt;
    TextView SongName, listeners;
    public static boolean showing = false;
    public static boolean isAlive = false;
    ProgressDialog pd;
    FrameLayout back;
    boolean self = false;
    static int rid;
    String songtitle, listenerNum, ProName;
    Intent startServ;

    int[] ids = new int[]{R.mipmap.back_1, R.mipmap.back_2, R.mipmap.back_3, R.mipmap.back_4, R.mipmap.back_5, R.mipmap.back_6, R.mipmap.back_7, R.mipmap.back_8, R.mipmap.back_9, R.mipmap.back_10, R.mipmap.back_11, R.mipmap.back_12, R.mipmap.back_13, R.mipmap.back_14, R.mipmap.back_15, R.mipmap.back_16, R.mipmap.back_17, R.mipmap.back_18, R.mipmap.back_19, R.mipmap.back_20, R.mipmap.back_21, R.mipmap.back_22, R.mipmap.back_23, R.mipmap.back_24, R.mipmap.back_25, R.mipmap.back_26, R.mipmap.back_27, R.mipmap.back_28, R.mipmap.back_29, R.mipmap.back_30, R.mipmap.back_31};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().registerReceiver(br, new IntentFilter(Tools.SONG));
        rid = Tools.getRandomInteger(30, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isAlive = true;
        View view = inflater.inflate(R.layout.fm_live, container, false);
        mhead = (TextView) view.findViewById(R.id.mhead);
        mhead.setSelected(true);
        uName = (TextView) view.findViewById(R.id.uName);
        String s = "Welcome " + Tools.getSelf(getActivity().getContentResolver().query(FMContract.UserData.CONTENT_URI, null, null, null, null))[0] + "!";
        uName.setText(s);
        back = (FrameLayout) view.findViewById(R.id.album_art_frame);
        albumArt = (ImageView) view.findViewById(R.id.album_art_iv_fm_live);
        SongName = (TextView) view.findViewById(R.id.songName);
        SongName.setSelected(true);
        current = (TextView) view.findViewById(R.id.proName);
        listeners = (TextView) view.findViewById(R.id.listeners);
        play = (ImageButton) view.findViewById(R.id.plyB);
        startServ = new Intent(getActivity(), StreamService.class);
        if (savedInstanceState != null) {
            showing = savedInstanceState.getBoolean("show");
            if (showing) {
                pdSet();
            }
        }
        play.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!Tools.loadPrefs(getActivity(), "playing")) {
                    if (Tools.isNetConn(getActivity())) {


                        // Setting Playing Flag for the UI's to behave in order.
                        Tools.storeBoolPrefs(getActivity(), "playing", true);
                        //This variable is to allow loading, if Play button Clicked
                        //then song_details should load. (As the SongFetch is Called At Startup also).
                        //and as it sends a broadcast to this fragment, sp must it load details, or it just received
                        //data from FNotifs and if currently user is not engaged.
                        Tools.storeBoolPrefs(getActivity(), "song_details_load", true);
                        pdSet();
                        //To check if user did clicked the play pause button, setting to false to make sure
                        //that when the media player will stop playing which activity is responsible to do so
                        //as always in order to update the UI
                        self = false;
                        update(getActivity().getString(R.string.loading_song_widget), getActivity().getString(R.string.loading_song_widget), getActivity().getString(R.string.loading_song_widget));
                        getActivity().startService(new Intent(getActivity(), SongFetch.class));
                        getActivity().startService(startServ);

                    } else {
                        Toast.makeText(getActivity(), getString(R.string.net_error),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    //to ensure user stopped activity and Service didn't ended Unusually.
                    self = true;
                    update(getActivity().getString(R.string.current_pro), getActivity().getString(R.string.rap), getActivity().getString(R.string.current_song));
                    setDraw(getActivity(), R.mipmap.play2);
                    FMWidgetProvider.ERROR = 2;
                    getActivity().stopService(startServ);
                }

            }
        });
        return view;

    }

    public void pdSet() {
        pd = new ProgressDialog(getActivity());
        pd.setMessage(getString(R.string.main_load));
        pd.setCancelable(false);
        pd.setButton(DialogInterface.BUTTON_NEGATIVE,
                "Cancel", new OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {
                        pd.cancel();
                        showing = false;
                    }
                });
        pd.show();
        showing = true;
        pd.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                getActivity().stopService(startServ);
                setDraw(getActivity(), R.mipmap.play2);

            }
        });
    }

    public void Check() {
        songtitle = Tools.getPrefs(getActivity(), "song");
        listenerNum = Tools.getPrefs(getActivity(), "buddies");
        ProName = Tools.getPrefs(getActivity(), "pro");
        String album = Tools.getPrefs(getActivity(), "album_art");
        if (!songtitle.equals("") || !listenerNum.equals("") || !ProName.equals("")) {
            update(ProName, listenerNum, songtitle);
        }

        if (Tools.loadPrefs(getActivity(), "album_art_key")) {
            setAlbumArt(album, 0);
        }
    }

    public void update(String pro, String num, String song) {
        if (isAlive) {
            int buds_num;
            try {
                buds_num = Integer.parseInt(num);
            } catch (Exception e) {
                buds_num = -1;
            }
            current.setText(pro);
            if (buds_num != -1) {
                listeners.setText(buds_num + " " + getActivity().getString(R.string.listeners_tuned));
            } else {
                listeners.setText(num);
            }
            SongName.setText(song);
        }
    }

    public void setDraw(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            play.setImageDrawable(getResources().getDrawable(id, context.getTheme()));
        } else {
            play.setImageDrawable(getResources().getDrawable(id));
        }
    }


    private BroadcastReceiver br = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String from = intent.getStringExtra("from");
            if (from.equals("SERVICE")) {
                if (pd != null) {
                    showing = false;
                    if (pd.isShowing()) {
                        pd.dismiss();
                    }
                }
                if (Tools.loadPrefs(context, "playing")) {
                    setDraw(getActivity(), R.mipmap.pause2);
                } else {
                    if (!self) {
                        if (FMWidgetProvider.ERROR == 1) {
                            update(getActivity().getString(R.string.gen_error), getActivity().getString(R.string.gen_error), getActivity().getString(R.string.song_error));
                        } else if (FMWidgetProvider.ERROR == 3) {
                            update(getActivity().getString(R.string.current_pro), getActivity().getString(R.string.rap), getActivity().getString(R.string.fm_not_live));
                        } else if (FMWidgetProvider.ERROR == 5) {
                            update(getActivity().getString(R.string.net_error), getActivity().getString(R.string.net_error), getActivity().getString(R.string.net_error));
                        } else {
                            update(getActivity().getString(R.string.current_pro), getActivity().getString(R.string.rap), getActivity().getString(R.string.current_song));
                        }

                    }
                    setAlbumArt("", ids[rid]);
                    setDraw(getActivity(), R.mipmap.play2);

                }
            } else if (from.equals("SNU_FETCH")) {
                boolean url_change = intent.getBooleanExtra("url_change_restart", false);
                if (url_change) {
                    if (isAlive) {
                        pdSet();
                        setDraw(context, R.mipmap.pause2);
                    }
                }
                getStatus();
            } else if (from.equals("SNU_NO_CHANGE")) {
                update(getString(R.string.current_pro), getString(R.string.rap), getString(R.string.current_song));

            } else if (from.equals("SNU_ERROR")) {
                update(context.getString(R.string.gen_error), context.getString(R.string.gen_error), context.getString(R.string.song_error));

            } else if (from.equals(Tools.BUDS)) {
                //If username updated
                if (isAlive) {
                    uName.setText("Welcome " + Tools.getSelf(context.getContentResolver().query(FMContract.UserData.CONTENT_URI, null, null, null, null))[0] + "!");
                }
            }

        }
    };

    private void getStatus() {
        String live = Tools.getPrefs(getActivity(), "live");
        try {
            if (!live.equals("")) {
                int status = Integer.valueOf(live);
                if (status == 0) {
                    self = true;
                    FMWidgetProvider.ERROR = 3;
                    getActivity().stopService(startServ);
                    SongName.setText(getActivity().getString(R.string.fm_not_live));
                    if (StarMain.alive) {
                        StarMain.Show("", getActivity().getString(R.string.fm_not_live));
                    }
                    current.setText(getActivity().getString(R.string.current_pro));
                    listeners.setText(getActivity().getString(R.string.rap));
                    setAlbumArt("", ids[rid]);
                } else {
                    Check();
                }
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("show", showing);
    }

    public void setAlbumArt(String url, int id) {
        try {
            if (id != 0) {
                Picasso.with(getActivity()).load(id).noFade().into(albumArt);
            } else {
                Picasso.with(getActivity()).load(url).error(ids[rid]).into(albumArt);
            }
        } catch (Exception e) {
            if (StarMain.alive) {
                StarMain.Show("", getActivity().getString(R.string.album_art_error));
            }
        }
    }

    @Override
    public void onResume() {
        isAlive = true;
        uName.setText("Welcome " + Tools.getSelf(getActivity().getContentResolver().query(FMContract.UserData.CONTENT_URI, null, null, null, null))[0] + "!");
        if (Tools.loadPrefs(getActivity(), "playing")) {
            update(getActivity().getString(R.string.loading_song_widget), getActivity().getString(R.string.loading_song_widget), getActivity().getString(R.string.loading_song_widget));
            Check();
            getStatus();
            setDraw(getActivity(), R.mipmap.pause2);

        } else {
            setAlbumArt("", ids[rid]);
            setDraw(getActivity(), R.mipmap.play2);
        }

        super.onResume();
    }


    @Override
    public void onPause() {
        isAlive = false;
        if (pd != null) {
            //To prevent the Window Leak Error.
            if (pd.isShowing()) {
                pd.dismiss();
            }
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Tools.hiddenKeyboard(getView());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(br);
    }
}
