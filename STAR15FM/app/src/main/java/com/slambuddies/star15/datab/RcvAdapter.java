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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.slambuddies.star15.R;
import com.slambuddies.star15.infos.ChatInfo;
import com.slambuddies.star15.tools.Tools;

import java.util.Collections;
import java.util.List;

public class RcvAdapter extends RecyclerView.Adapter<RcvAdapter.MyViewHolder> {

    List<ChatInfo> data = Collections.emptyList();
    private LayoutInflater inflater;
    Context context;
    String name;
    boolean must;
    RelativeLayout layout;
    onLongClickListenerChatItem longClickListenerChatItem;

    public RcvAdapter(Context context, List<ChatInfo> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
        name = Tools.getSelf(context.getContentResolver().query(FMContract.UserData.CONTENT_URI, null, null, null, null))[0];
    }

    @Override
    public int getItemViewType(int position) {
        ChatInfo info = data.get(position);
        if (position == 0) {
            if (info.getName().equals(name)) {
                return 1;
            } else {
                return 2;
            }

        } else {
            if (info.getName().equals(name)) {
                if (!SameDate(Tools.getDateParser(info.getDay()), position)) {
                    return 3;
                } else {
                    return 4;
                }
            } else {
                if (!SameDate(Tools.getDateParser(info.getDay()), position)) {
                    return 5;
                } else {
                    return 6;
                }
            }
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        MyViewHolder holder;
        if (viewType == 1 || viewType == 3 || viewType == 4) {
            view = inflater.inflate(R.layout.slam_to, parent, false);
            holder = new MyViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.slam_from, parent, false);
            holder = new MyViewHolder(view);
        }

        return holder;
    }

    public boolean SameDate(int date, int pos) {
        int pre;
        ChatInfo info = data.get(pos - 1);
        pre = Tools.getDateParser(info.getDay());
        if (pre == date) {
            return true;
        } else {
            return false;
        }

    }


    public void setLongClickListenerChatItem(onLongClickListenerChatItem longClickListenerChatItem) {
        this.longClickListenerChatItem = longClickListenerChatItem;
    }

    public SpannableString MsgSpan(String Msg, String color) {
        int len = Msg.length();
        int cur = 0;
        int end;
        SpannableString span = new SpannableString(Msg);
        while (len > 0) {
            if (Msg.charAt(cur) == '#' || Msg.charAt(cur) == '@') {

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
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ChatInfo current = data.get(position);
        int type = getItemViewType(position);
        if (type == 1 || type == 2 || type == 3 || type == 5) {
            holder.relativeLayout.setVisibility(View.VISIBLE);
            holder.date_display.setText(Tools.getTodays(current.getDay()));
        }

        holder.name.setText(current.getName());
        holder.when.setText(current.getDate());
        if (current.getName().equals(name)) {
            holder.msg.setText(MsgSpan(current.getMsg(), "#FF4081"));
            holder.img.setVisibility(View.VISIBLE);
            if (current.getModify() == 2) {
                holder.img.setImageResource(R.mipmap.done);
            }
        } else {
            holder.msg.setText(MsgSpan(current.getMsg(), "#3454D1"));
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void add(List<ChatInfo> mList) {
        this.data = mList;
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        TextView name, msg, when, date_display;
        ImageView img;
        RelativeLayout relativeLayout;

        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.buddyName);
            msg = (TextView) itemView.findViewById(R.id.msgText);
            when = (TextView) itemView.findViewById(R.id.stamp);
            date_display = (TextView) itemView.findViewById(R.id.date);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.date_cont);
            layout = (RelativeLayout) itemView.findViewById(R.id.rLayout);
            img = (ImageView) itemView.findViewById(R.id.status);

            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            longClickListenerChatItem.onLongClick(getAdapterPosition());
            return true;
        }
    }

    public interface onLongClickListenerChatItem {
        void onLongClick(int pos);
    }
}
