package com.slambuddies.star15.acts;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.slambuddies.star15.R;
import com.slambuddies.star15.tools.Tools;

public class ReqDedAct extends Fragment implements Tools.Switcher {

    private static int FRAGMENT_CHOSEN = 1;
    FrameLayout frag_container;
    RequestAct request;
    Dedicate dedicate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            FRAGMENT_CHOSEN = savedInstanceState.getInt("fragment");
        }
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.req_ded, container, false);
        Tools.hiddenKeyboard(v);
        frag_container = (FrameLayout) v.findViewById(R.id.frag_cont);
        request = new RequestAct();
        request.setSwitcher(this);

        dedicate = new Dedicate();
        dedicate.setSwitcher(this);

        if (FRAGMENT_CHOSEN == 1) {
            onSwitch(R.id.req_switcher);
        } else {
            onSwitch(R.id.ded_switcher);
        }
        return v;
    }

    @Override
    public void onSwitch(int id) {
        switch (id) {
            case R.id.req_switcher:
                FRAGMENT_CHOSEN = 1;
                changeFrag(dedicate);
                break;
            case R.id.ded_switcher:
                FRAGMENT_CHOSEN = 2;
                changeFrag(request);
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("fragment", FRAGMENT_CHOSEN);
    }

    public void changeFrag(Fragment fragment) {
        getActivity().getSupportFragmentManager().beginTransaction().replace(frag_container.getId(), fragment).commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Tools.hiddenKeyboard(getView());
    }

}
