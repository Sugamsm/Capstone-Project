package com.slambuddies.star15.datab;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.slambuddies.star15.R;
import com.slambuddies.star15.infos.RDInfo;
import com.slambuddies.star15.tools.Tools;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.MyViewHolder> {


    Context context;
    List<RDInfo> rd = Collections.emptyList();
    LayoutInflater inflater;

    public InfoAdapter(Context context, List<RDInfo> rd) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.rd = rd;
    }


    @Override
    public int getItemViewType(int position) {

        RDInfo rdInfo = rd.get(position);
        if (position == 0) {
            if (rdInfo.getType() == 1) {
                return 1;
            } else {
                return 2;
            }
        } else {
            if (rdInfo.getType() == 1) {
                if (!CheckDate(Tools.getDateParser(rdInfo.getDay()), position)) {
                    return 3;
                } else {
                    return 4;
                }
            } else {
                if (!CheckDate(Tools.getDateParser(rdInfo.getDay()), position)) {
                    return 5;
                } else {
                    return 6;
                }
            }
        }
    }

    public void add(List<RDInfo> mList) {
        this.rd = mList;
        notifyDataSetChanged();
    }

    public boolean CheckDate(int date, int pos) {
        int pre;
        RDInfo info = rd.get(pos - 1);
        pre = Tools.getDateParser(info.getDay());
        if (pre == date) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        MyViewHolder holder;
        if (viewType == 1 || viewType == 3 || viewType == 4) {
            view = inflater.inflate(R.layout.single_req, parent, false);
            holder = new MyViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.single_ded, parent, false);
            holder = new MyViewHolder(view);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        int type = getItemViewType(position);

        RDInfo re = rd.get(position);
        if (type == 1 || type == 2 || type == 3 || type == 5) {
            holder.layout.setVisibility(View.VISIBLE);
            holder.date_head.setText(Tools.getTodays(re.getDay()));
        }
        holder.buddy.setText(re.getName());
        holder.time.setText(re.getDate());
        String forB = re.getBname();
        if (type == 4 || type == 1 || type == 3) {
            String entire;
            if (forB.equals("NA")) {
                entire = String.format(Locale.getDefault(), context.getString(R.string.reqs), re.getSong());
                holder.extras.setText(entire);
            } else {
                holder.extras.setText(MsgSpan(String.format(Locale.getDefault(), context.getString(R.string.reqs_single), re.getSong(), forB), "#3F51B5"));
            }

        } else if (type == 6 || type == 2 || type == 5) {
            holder.extras.setText(MsgSpan(String.format(Locale.getDefault(), context.getString(R.string.deds), forB), "#448AFF"));
        }
    }

    public SpannableString MsgSpan(String Msg, String color) {
        int len = Msg.length();
        int cur = 0;
        int end;
        SpannableString span = new SpannableString(Msg);
        while (len > 0) {
            if (Msg.charAt(cur) == '@') {

                String in = Msg.substring(cur, Msg.length());
                if (in.contains(" ")) {
                    end = cur + in.indexOf(" ");
                } else {
                    end = cur + in.length();
                }
                span.setSpan(new ForegroundColorSpan(Color.parseColor(color)), cur, end, 0);
                span.setSpan(new StyleSpan(Typeface.BOLD), cur, end, 0);
                span.setSpan(new UnderlineSpan(), cur, end, 0);
            }
            cur++;

            len--;
        }
        return span;
    }

    @Override
    public int getItemCount() {
        return rd.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout layout, rLayout;
        TextView buddy, extras, date_head, time;

        public MyViewHolder(View itemView) {
            super(itemView);
            time = (TextView) itemView.findViewById(R.id.stamp);
            buddy = (TextView) itemView.findViewById(R.id.bNa);
            date_head = (TextView) itemView.findViewById(R.id.extras);
            date_head = (TextView) itemView.findViewById(R.id.date);
            layout = (RelativeLayout) itemView.findViewById(R.id.date_cont);
            rLayout = (RelativeLayout) itemView.findViewById(R.id.RelativeLayout1);
            extras = (TextView) itemView.findViewById(R.id.extras);
        }

    }
}
