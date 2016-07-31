package com.slambuddies.star15.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.slambuddies.star15.R;
import com.slambuddies.star15.acts.Startup;
import com.slambuddies.star15.backs.SongFetch;
import com.slambuddies.star15.backs.StreamService;
import com.slambuddies.star15.tools.Tools;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class FMWidgetProvider extends AppWidgetProvider {

    public static final String TAG = "WIDGET";
    public static boolean LARGE = false;
    private static boolean PLAY = false;
    public static int ERROR = 0;
    public static boolean should;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            setup(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        setup(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        should = Tools.loadPrefs(context, "album_art_key");
        String action = intent.getAction();
        final RemoteViews view = getViews(context);
        if (Tools.WIDGET_SERVICE.equals(action)) {
            //Called by Events that need Updating UI
            button(context, view);
            updateUITexts(context, view, ERROR);
        } else if (TAG.equals(action)) {
            //Self Click Listener Broadcast
            if (!PLAY) {
                if (Tools.isNetConn(context)) {
                    ERROR = 0;
                    Tools.storeBoolPrefs(context, "playing", true);
                    button(context, view);
                    context.startService(new Intent(context, StreamService.class));
                    context.startService(new Intent(context, SongFetch.class));
                    view.setTextViewText(R.id.songName, context.getString(R.string.loading_song_widget));
                    if (LARGE) {
                        view.setTextViewText(R.id.proName, context.getString(R.string.loading_song_widget));
                    }
                    view.setImageViewResource(R.id.widget_play, R.mipmap.ic_pause_white_48dp);
                    PLAY = true;
                    notifyWidget(context, view);
                } else {
                    Tools.storeBoolPrefs(context, "playing", false);
                    view.setTextViewText(R.id.songName, context.getString(R.string.net_error));
                    if (LARGE) {
                        view.setTextViewText(R.id.proName, context.getString(R.string.net_error));
                    }
                    notifyWidget(context, view);
                    PLAY = false;
                }
            } else {
                ERROR = 2;
                PLAY = false;
                //To change the button instantly
                view.setImageViewResource(R.id.widget_play, R.mipmap.ic_play_arrow_white_48dp);
                notifyWidget(context, view);
                context.stopService(new Intent(context, StreamService.class));
            }
        } else if (Tools.WIDGET_NET.equals(action)) {
            if (Tools.isNetConn(context)) {
                view.setTextViewText(R.id.songName, context.getString(R.string.current_song));
                if (LARGE) {
                    view.setTextViewText(R.id.proName, context.getString(R.string.current_pro));
                }
                notifyWidget(context, view);
            } else {
                view.setTextViewText(R.id.songName, context.getString(R.string.net_error));
                if (LARGE) {
                    view.setTextViewText(R.id.proName, context.getString(R.string.net_error));
                }
                notifyWidget(context, view);
            }
        }
    }

    private void albumCheck(final Context context, final RemoteViews view, String url) {
        if (LARGE) {
            if (should) {
                try {
                    Picasso.with(context).load(url).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            view.setImageViewBitmap(R.id.album_art_iv, bitmap);
                            notifyWidget(context, view);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });
                } catch (Exception e) {

                }

            }

        }
    }

    private void setup(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        int widgetHeight = Tools.getWidgetHeight(context, appWidgetManager, appWidgetId);
        int largeHeight = context.getResources().getDimensionPixelSize(R.dimen.widget_resize_height_large);
        int layoutId;
        if (widgetHeight >= largeHeight) {
            layoutId = R.layout.fm_widget_large;
            LARGE = true;
        } else {
            layoutId = R.layout.fm_widget;
            LARGE = false;
        }
        RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);

        Intent play_intent = new Intent(context, FMWidgetProvider.class).setAction(TAG);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(context, 0, play_intent, 0);
        views.setOnClickPendingIntent(R.id.widget_play, playPendingIntent);

        Intent launchIntent = new Intent(context, Startup.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
        if (Tools.loadPrefs(context, "playing")) {
            views.setImageViewResource(R.id.widget_play, R.mipmap.ic_pause_white_48dp);
        } else {
            views.setImageViewResource(R.id.widget_play, R.mipmap.ic_play_arrow_white_48dp);
        }

        views.setOnClickPendingIntent(R.id.widget, pendingIntent);
        button(context, views);
        updateUITexts(context, views, ERROR);
    }

    private void button(Context context, RemoteViews view) {
        if (Tools.loadPrefs(context, "playing")) {
            albumCheck(context, view, Tools.getPrefs(context, "album_art"));
            view.setImageViewResource(R.id.widget_play, R.mipmap.ic_pause_white_48dp);
            PLAY = true;
        } else {
            view.setImageViewResource(R.id.widget_play, R.mipmap.ic_play_arrow_white_48dp);
            PLAY = false;
        }
        notifyWidget(context, view);
    }

    private void updateUITexts(Context context, RemoteViews view, int error) {
        if (Tools.loadPrefs(context, "playing")) {
            String song = Tools.getPrefs(context, "song");
            String ProName = Tools.getPrefs(context, "pro");
            //ERROR 2 is ALL OK self closing error
            if (!song.equals("")) {
                view.setTextViewText(R.id.songName, song);
                if (LARGE) {
                    view.setTextViewText(R.id.proName, ProName);
                }
            } else {
                view.setTextViewText(R.id.songName, context.getString(R.string.current_song));
                if (LARGE) {
                    view.setTextViewText(R.id.proName, context.getString(R.string.current_pro));
                }
            }
        } else {
            if (error == 1) {
                //ERROR 1 is the main genuine Error Occurrence
                view.setTextViewText(R.id.songName, context.getString(R.string.song_error));
                if (LARGE) {
                    view.setTextViewText(R.id.proName, context.getString(R.string.gen_error));
                }
            } else {
                //ERROR 3 is a not live Error.
                if (ERROR == 3) {
                    context.stopService(new Intent(context, StreamService.class));
                    view.setTextViewText(R.id.songName, context.getString(R.string.fm_not_live));
                    if (LARGE) {
                        view.setTextViewText(R.id.proName, context.getString(R.string.current_pro));
                    }
                } else if (ERROR == 5) {
                    //ERROR 5 is Network Error Occurrence
                    view.setTextViewText(R.id.songName, context.getString(R.string.net_error));
                    if (LARGE) {
                        view.setTextViewText(R.id.proName, context.getString(R.string.net_error));
                    }
                }else {
                    view.setTextViewText(R.id.songName, context.getString(R.string.current_song));
                    if (LARGE) {
                        view.setTextViewText(R.id.proName, context.getString(R.string.current_pro));
                    }
                }
            }
        }
        notifyWidget(context, view);

    }

    private void notifyWidget(Context context, RemoteViews views) {
        ComponentName name = new ComponentName(context, FMWidgetProvider.class);
        (AppWidgetManager.getInstance(context)).updateAppWidget(name, views);
    }

    private RemoteViews getViews(Context context) {
        RemoteViews views;
        if (LARGE) {
            views = new RemoteViews(context.getPackageName(), R.layout.fm_widget_large);
        } else {
            views = new RemoteViews(context.getPackageName(), R.layout.fm_widget);
        }
        return views;
    }
}
