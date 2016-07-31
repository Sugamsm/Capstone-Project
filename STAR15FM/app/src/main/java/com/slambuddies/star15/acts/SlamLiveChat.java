package com.slambuddies.star15.acts;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.slambuddies.star15.R;
import com.slambuddies.star15.backs.PostService;
import com.slambuddies.star15.datab.FMContract;
import com.slambuddies.star15.datab.RcvAdapter;
import com.slambuddies.star15.infos.ChatInfo;
import com.slambuddies.star15.tools.Tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SlamLiveChat extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>, RcvAdapter.onLongClickListenerChatItem {

    private static final int CHAT_LOADER = 1;
    MultiAutoCompleteTextView et2;
    ImageButton send;
    public static boolean alive = false;
    static boolean first = false;
    public static final String NAME_ACT = "SLAM";
    List<ChatInfo> mList;
    RcvAdapter adapter;
    String send_name, send_msg, time, date;
    String[] names, dat;
    RecyclerView lv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        getActivity().registerReceiver(receiver, new IntentFilter(Tools.SLAM));
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_chat:
                getActivity().getContentResolver().delete(FMContract.ChatData.CONTENT_URI, null, null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.slam_chat_new, container, false);
        alive = true;
        first = true;
        mList = new ArrayList<>();
        lv = (RecyclerView) view.findViewById(R.id.chatList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        lv.setLayoutManager(layoutManager);
        et2 = (MultiAutoCompleteTextView) view.findViewById(R.id.Msg);
        et2.setThreshold(1);
        et2.addTextChangedListener(watcher);
        dat = Tools.getSelf(getActivity().getContentResolver().query(FMContract.UserData.CONTENT_URI, null, null, null, null));
        send = (ImageButton) view.findViewById(R.id.sButton);
        send.setOnClickListener(this);
        names = Tools.setAdapter(getActivity());
        if (names != null) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, names);
            et2.setAdapter(arrayAdapter);
            et2.setTokenizer(new Tools.SpaceTokenizer());
        }

        adapter = new RcvAdapter(getContext(), mList);
        adapter.setLongClickListenerChatItem(this);
        lv.setAdapter(adapter);
        return view;
    }


    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String all = s.toString();
            int len = all.length();
            int cur = 0;
            int end;
            while (len > 0) {
                if (all.charAt(cur) == '#' || all.charAt(cur) == '@') {
                    Spannable span = s;

                    String in = all.substring(cur, all.length());
                    if (in.contains(" ")) {
                        end = cur + in.indexOf(" ");
                    } else {
                        end = cur + in.length();
                    }
                    span.setSpan(new ForegroundColorSpan(Color.BLUE), cur, end, 0);
                }
                cur++;

                len--;
            }
        }
    };


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        alive = true;
        //Setting window background in order to prevent compression of background image when keyboard
        //is displayed.
        getActivity().getWindow().setBackgroundDrawableResource(R.mipmap.back_29);
        getLoaderManager().initLoader(CHAT_LOADER, null, this);
        super.onResume();
    }

    @Override
    public void onPause() {
        alive = false;
        super.onPause();
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(receiver);
        super.onDestroy();
    }

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String from = intent.getStringExtra("from");
            switch (from) {

                case "SERV":
                    int success = intent.getIntExtra("Success", 0);
                    if (success != 0) {
                        getLoaderManager().restartLoader(CHAT_LOADER, null, SlamLiveChat.this);
                    } else {
                        if (StarMain.alive) {
                            StarMain.Show("", context.getString(R.string.msg_send_error));
                        }
                    }
                    break;

                case "NEW":
                case "DB_DEL":
                    getLoaderManager().restartLoader(CHAT_LOADER, null, SlamLiveChat.this);
                    break;

                case Tools.BUDS:
                    names = Tools.setAdapter(context);
                    if (names != null) {
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, names);
                        et2.setAdapter(arrayAdapter);
                        et2.setTokenizer(new Tools.SpaceTokenizer());
                    }
                    break;
            }
        }
    };

    public void update(ChatInfo info) {
        mList.add(info);
        adapter.add(mList);
    }


    @Override
    public void onClick(View v) {
        if (Tools.isNetConn(getActivity())) {
            send_msg = et2.getText().toString().trim();

            send_name = dat[0];

            int mentioned = 0;

            if (!send_msg.isEmpty() && send_msg.length() > 0) {
                String[] mentions = Tools.Name(getActivity(), send_msg);

                if (mentions != null) {
                    mentioned = 1;
                }

                Intent intent = new Intent(getContext(), PostService.class);
                String[] data = new String[]{NAME_ACT, send_name, send_msg, dat[1], String.valueOf(mentioned)};
                intent.putExtra("data", Tools.getJson(data, mentions));
                ChatInfo info = new ChatInfo();
                info.setMsg(send_msg);
                info.setName(send_name);
                info.setModify(1);
                Calendar cal = Calendar.getInstance();
                String day = "Q" + Tools.getToday() + "Q";
                date = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + day;
                time = Tools.getFullTime();
                info.setDate(time);
                info.setDay(date);

                ContentValues values = new ContentValues();
                values.put(FMContract.BUDDY_NAME, send_name);
                values.put(FMContract.BUDDY_MSG, send_msg);
                values.put(FMContract.MSG_STATUS, 1);
                values.put(FMContract.DATE_TIME, Tools.getFullTime());
                values.put(FMContract.DATE, date);
                Uri uri = getActivity().getContentResolver().insert(FMContract.ChatData.CONTENT_URI, values);
                String u = "" + uri;
                String sub = u.substring(u.lastIndexOf("/") + 1, u.length());
                int i = Integer.valueOf(sub);
                info.setMsg_id(i);
                intent.putExtra("msg_id", i);
                update(info);
                et2.setText("");
                lv.smoothScrollToPosition(mList.size() - 1);
                getActivity().startService(intent);
            } else {
                Toast.makeText(getContext(), getString(R.string.msg_error), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getContext(), getString(R.string.net_error), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), FMContract.ChatData.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        mList = new ArrayList<>();
        if (data != null) {
            if (data.moveToFirst()) {
                do {
                    ChatInfo current = new ChatInfo();
                    current.setName(data.getString(1));
                    current.setMsg(data.getString(2));
                    current.setModify(data.getInt(3));
                    current.setDate(data.getString(4));
                    current.setDay(data.getString(5));
                    current.setMsg_id(data.getInt(0));
                    mList.add(current);
                } while (data.moveToNext());
            }
            if (alive) {
                adapter.add(mList);
                if (mList.size() != 0) {
                    if (first) {
                        lv.smoothScrollToPosition(mList.size() - 1);
                        first = false;
                    }
                }
            }
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }


    @Override
    public void onLongClick(int pos) {
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice);
        arrayAdapter.addAll(getResources().getStringArray(R.array.dialog_choices));
        if (mList != null && !mList.isEmpty()) {
            final ChatInfo current = mList.get(pos);
            final int stats = current.getModify();
            final int id = current.getMsg_id();
            final String msg = current.getMsg();
            if (stats != 0) {
                if (stats == 1) {
                    arrayAdapter.add(getString(R.string.r_send));
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.msg_dialog_title));
                builder.setIcon(R.mipmap.star15_logo_final_save);
                builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (arrayAdapter.getItem(which)) {
                            case "Delete":
                                getActivity().getContentResolver().delete(FMContract.ChatData.CONTENT_URI, FMContract.MSG_ID + "=?", new String[]{"" + id});
                                break;
                            case "Status":
                                if (StarMain.alive) {
                                    if (stats == 2) {
                                        StarMain.Show("", String.format(Locale.US, getString(R.string.success_status_display), msg));
                                    } else if (stats == 1) {
                                        StarMain.Show("", String.format(Locale.US, getString(R.string.failed_status_display), msg));
                                    }
                                }
                                break;
                            case "Re-Send":
                                int mentioned = 0;
                                Intent intent = new Intent(getActivity(), PostService.class);
                                String[] mentions = Tools.Name(getActivity(), msg);
                                if (mentions != null) {
                                    mentioned = 1;
                                }
                                String[] data = new String[]{NAME_ACT, current.getName(), msg, dat[1], String.valueOf(mentioned)};
                                intent.putExtra("data", Tools.getJson(data, mentions));
                                intent.putExtra("msg_id", id);
                                getActivity().startService(intent);
                                break;
                        }
                    }
                });
                builder.create().show();
            }
        }
    }
}
