package com.example.minxuan.socialprojectv2.MQTT;

/**
 * Created by RMO on 2017/6/16.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.minxuan.socialprojectv2.R;
/**
 * Fragment for the subscribe pane for the client
 *
 */
public class SubscribeFragment extends Fragment {

    /**
     * @see Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return LayoutInflater.from(getActivity()).inflate(R.layout.activity_subscribe, null);

    }

}
