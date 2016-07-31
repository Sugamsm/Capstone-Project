package com.slambuddies.star15.acts;


import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.slambuddies.star15.R;
import com.slambuddies.star15.backs.LiveService;
import com.slambuddies.star15.backs.PostService;
import com.slambuddies.star15.backs.SongFetch;
import com.slambuddies.star15.datab.FMContract;
import com.slambuddies.star15.tools.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class StarMain extends AppCompatActivity {
    static CoordinatorLayout coordinatorLayout;
    Toolbar toolbar;
    TabLayout tabLayout;
    AppBarLayout appBarLayout;
    ViewPager vp;
    public static ArrayList<String> names = null;
    ProgressDialog pd;
    public static ArrayList<Fragment> fragments = null;
    public static boolean alive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star_main);
        alive = true;
        registerReceiver(receiver, new IntentFilter(Tools.DEL));
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.c_layout);
        appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        vp = (ViewPager) findViewById(R.id.pager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide slide = new Slide(Gravity.LEFT);
            slide.setDuration(250);
            slide.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.interpolator.linear_out_slow_in));
            getWindow().setExitTransition(slide);
        }

        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Tools.hiddenKeyboard(vp.getRootView());
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        setTabs();
        pd = new ProgressDialog(this);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(vp);
        vp.setCurrentItem(1);
        if (!Tools.loadPrefs(this, "reg_buds")) {
            startService(new Intent(this, LiveService.class));
        }
    }

    public void setTabs() {
        names = new ArrayList<String>();
        fragments = new ArrayList<Fragment>();
        names.add(getString(R.string.slam_live));
        names.add(getString(R.string.fm_live));
        names.add(getString(R.string.live_acts));
        names.add(getString(R.string.act_head));
        names.add(getString(R.string.buds_live));

        fragments.add(new SlamLiveChat());
        fragments.add(new ListenAct());
        fragments.add(new LiveReqs());
        fragments.add(new ReqDedAct());
        fragments.add(new RegisteredBuddies());

        if (Tools.checkRtl(this)) {
            Collections.reverse(Arrays.asList(names));
            Collections.reverse(Arrays.asList(fragments));
        }
        ViewAdapter adapter = new ViewAdapter(getSupportFragmentManager());
        vp.setAdapter(adapter);
    }

    public static void Show(String from, String msg) {
        if (alive) {
            SpannableString s;
            if (!from.equals("")) {
                s = new SpannableString(msg);
                s.setSpan(new ForegroundColorSpan(Color.parseColor("#448AFF")), 0, from.length(), 0);
                s.setSpan(new UnderlineSpan(), 0, from.length(), 0);
                s.setSpan(new StyleSpan(Typeface.BOLD), 0, from.length(), 0);
            } else {
                s = new SpannableString(msg);
            }


            Snackbar snackbar = Snackbar.make(coordinatorLayout, s, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.delete_account:
                confirm().show();
                return true;

            case R.id.settings:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(this, Settings.class), ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else {
                    startActivity(new Intent(this, Settings.class));
                }
                return true;

            case R.id.refresh:
                startService(new Intent(this, SongFetch.class).putExtra("refresh", true));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("from").equals("SERV")) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                int success = intent.getIntExtra("Success", 0);
                if (success != 0) {
                    Toast.makeText(StarMain.this, getString(R.string.success_delete), Toast.LENGTH_LONG).show();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    preferences.edit().clear().apply();
                    getContentResolver().delete(FMContract.UserData.CONTENT_URI, null, null);
                    getContentResolver().delete(FMContract.BudsData.CONTENT_URI, null, null);
                    getContentResolver().delete(FMContract.ChatData.CONTENT_URI, null, null);
                    getContentResolver().delete(FMContract.RDData.CONTENT_URI, null, null);
                    Tools.resetDB(context);
                    Intent intent1 = new Intent(StarMain.this, Startup.class);
                    startActivity(intent1);
                    finish();
                } else {
                    Toast.makeText(StarMain.this, getString(R.string.error_delete), Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    public AlertDialog confirm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.del_confirm));
        builder.setPositiveButton(getString(R.string.yep), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new DeleteAcc().execute();
            }
        });
        builder.setNegativeButton(getString(R.string.no_del), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        return dialog;
    }

    class DeleteAcc extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            pd.setMessage(getString(R.string.deleting));
            pd.setCancelable(false);
            pd.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Intent intent = new Intent(StarMain.this, PostService.class);
            String[] data = new String[]{"DEL", Tools.getSelf(StarMain.this.getContentResolver().query(FMContract.UserData.CONTENT_URI, null, null, null, null))[1]};
            intent.putExtra("data", Tools.getJson(data, null));
            startService(intent);
            return null;
        }
    }


    @Override
    protected void onResume() {
        alive = true;
        registerReceiver(receiver, new IntentFilter(Tools.DEL));
        super.onResume();
    }

    @Override
    protected void onPause() {
        alive = false;
        unregisterReceiver(receiver);
        super.onPause();
    }

    class ViewAdapter extends FragmentPagerAdapter {

        public ViewAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return names.get(position);
        }

        @Override
        public int getCount() {
            return 5;
        }


    }


}