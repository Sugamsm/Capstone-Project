package com.slambuddies.star15.datab;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slambuddies.star15.R;
import com.slambuddies.star15.infos.BudsInfo;

import java.util.Collections;
import java.util.List;

public class BudsAdapter extends RecyclerView.Adapter<BudsAdapter.MyViewHolder> {
    Context context;
    LayoutInflater inflater;
    List<BudsInfo> mList = Collections.emptyList();

    public BudsAdapter(Context context, List<BudsInfo> mList) {
        this.context = context;
        this.mList = mList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(inflater.inflate(R.layout.single_bud, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        BudsInfo info = mList.get(position);
        holder.buddy.setText(info.getName());
        holder.time.setText(info.getDate());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void add(List<BudsInfo> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView time, buddy;

        public MyViewHolder(View itemView) {
            super(itemView);

            time = (TextView) itemView.findViewById(R.id.stamp);
            buddy = (TextView) itemView.findViewById(R.id.bNa);
        }
    }
}
