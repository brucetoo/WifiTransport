package com.brucetoo.wifitransport.HotPot;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brucetoo.wifitransport.HotPot.image.ImageActivity;
import com.brucetoo.wifitransport.R;

/**
 * Created by Bruce Too
 * On 7/7/16.
 * At 14:32
 */
public class EnterFragment extends Fragment {

    public static EnterFragment newInstance() {

        Bundle args = new Bundle();

        EnterFragment fragment = new EnterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter, container, false);
        view.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HotpotReleaseActivity) getActivity()).replaceFragment(SenderFragment.newInstance());
            }
        });

        view.findViewById(R.id.btn_receive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HotpotReleaseActivity) getActivity()).replaceFragment(ReceiverFragment.newInstance());
            }
        });

        view.findViewById(R.id.btn_browse_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ImageActivity.class));
            }
        });

        view.findViewById(R.id.btn_scan_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HotpotReleaseActivity) getActivity()).replaceFragment(ScanFragment.newInstance());
            }
        });
        return view;
    }
}
