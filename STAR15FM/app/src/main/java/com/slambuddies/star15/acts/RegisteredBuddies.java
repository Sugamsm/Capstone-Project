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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slambuddies.star15.R;
import com.slambuddies.star15.datab.BudsAdapter;
import com.slambuddies.star15.datab.FMContract;
import com.slambuddies.star15.infos.BudsInfo;
import com.slambuddies.star15.tools.Tools;

import java.util.ArrayList;
import java.util.List;

public class RegisteredBuddies extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int BUDS_LOADER = 3;
    public static boolean alive = true;
    TextView tv;
    RecyclerView rcv;
    List<BudsInfo> mList;
    BudsAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().registerReceiver(receiver, new IntentFilter(Tools.BUDS));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        alive = true;
        View view = inflater.inflate(R.layout.buddy_list, container, false);
        tv = (TextView) view.findViewById(R.id.empty);
        rcv = (RecyclerView) view.findViewById(R.id.buddies);
        rcv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mList = new ArrayList<>();
        checker();
        adapter = new BudsAdapter(getActivity(), mList);
        rcv.setAdapter(adapter);
        return view;
    }

    public boolean checker() {
        if (mList.size() == 0 || mList.isEmpty()) {
            rcv.setVisibility(View.GONE);
            tv.setVisibility(View.VISIBLE);
            return false;
        } else {
            rcv.setVisibility(View.VISIBLE);
            tv.setVisibility(View.GONE);
            return true;
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String from = intent.getStringExtra("from");
            if (from.equals("NEW") || from.equals("DB_DEL") || from.equals("ALL")) {
                getLoaderManager().restartLoader(BUDS_LOADER, null, RegisteredBuddies.this);
            }
        }
    };

    @Override
    public void onResume() {
        alive = true;
        getLoaderManager().initLoader(BUDS_LOADER, null, this);
        super.onResume();
    }

    @Override
    public void onPause() {
        alive = false;
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), FMContract.BudsData.CONTENT_URI,
                null,
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
                    BudsInfo current = new BudsInfo();
                    current.setName(cursor.getString(1));
                    current.setDate(cursor.getString(2));
                    mList.add(current);
                } while (cursor.moveToNext());
            }
        }
        if (alive) {
            checker();
            adapter.add(mList);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}