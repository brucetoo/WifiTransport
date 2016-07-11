package com.brucetoo.wifitransport.HotPot;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brucetoo.wifitransport.HotPot.widget.ScanDeviceView;

/**
 * Created by Bruce Too
 * On 7/7/16.
 * At 14:32
 */
public class ScanFragment extends Fragment {

    public static ScanFragment newInstance() {

        Bundle args = new Bundle();

        ScanFragment fragment = new ScanFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScanDeviceView view = new ScanDeviceView(container.getContext());
        return view;
    }
}
