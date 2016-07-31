package com.slambuddies.star15.acts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slambuddies.star15.R;
import com.slambuddies.star15.datab.FMContract;
import com.slambuddies.star15.datab.InfoAdapter;
import com.slambuddies.star15.infos.RDInfo;
import com.slambuddies.star15.tools.Tools;

import java.util.ArrayList;
import java.util.List;

public class LiveReqs extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int RD_LOADER = 2;
    TextView tv;
    RecyclerView lv;
    TextView tag;
    public static boolean alive = false;
    List<RDInfo> mList;
    InfoAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().registerReceiver(receiver, new IntentFilter(Tools.RDI));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        alive = true;
        View view = inflater.inflate(R.layout.r_d_act, container, false);
        tv = (TextView) view.findViewById(R.id.empty_list_msg);
        lv = (RecyclerView) view.findViewById(R.id.actList);
        lv.setLayoutManager(new LinearLayoutManager(getContext()));
        mList = new ArrayList<>();
        adapter = new InfoAdapter(getContext(), mList);
        lv.setAdapter(adapter);
        tag = (TextView) view.findViewById(R.id.head);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.chat, menu);
        MenuItem item = menu.findItem(R.id.delete_chat);
        item.setTitle(getActivity().getString(R.string.clear_acts));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_chat) {
            getActivity().getContentResolver().delete(FMContract.RDData.CONTENT_URI, null, null);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        alive = true;
        getLoaderManager().initLoader(RD_LOADER, null, this);
        super.onResume();
    }


    @Override
    public void onPause() {
        alive = false;
        super.onPause();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String from = intent.getStringExtra("from");
            if (from.equals("NEW") || (from.equals("DB_DEL"))) {
                getLoaderManager().restartLoader(RD_LOADER, null, LiveReqs.this);
            }
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), FMContract.RDData.CONTENT_URI, null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mList = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    RDInfo current = new RDInfo();
                    current.setName(cursor.getString(2));
                    Log.i("Load", "From Buddy " + cursor.getString(2));
                    current.setSong(cursor.getString(3));
                    current.setAlbum(cursor.getString(4));
                    current.setBname(cursor.getString(5));
                    current.setDate(cursor.getString(6));
                    current.setDay(cursor.getString(7));
                    current.setType(cursor.getInt(1));
                    mList.add(current);
                } while (cursor.moveToNext());
            }
        }
        if (alive) {
            Log.i("Load", "Is Visible and Loading... Updating UI");
            adapter.add(mList);
            if (mList.size() == 0 || mList.isEmpty()) {
                lv.setVisibility(View.GONE);
                tv.setVisibility(View.VISIBLE);
            } else {
                tv.setVisibility(View.GONE);
                lv.setVisibility(View.VISIBLE);
            }
        } else {
            Log.i("Load", "No Alive");
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(receiver);
    }
}
