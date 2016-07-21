package com.brucetoo.wifitransport.permission;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.brucetoo.wifitransport.R;

import java.util.List;

/**
 * Created by Bruce Too
 * On 7/21/16.
 * At 11:50
 */
public class PermissionFragment extends Fragment implements
        PermissionsAdapter.PermissionCallbacks {

    private static final String TAG = PermissionFragment.class.getSimpleName();
    private static final int REQUEST_SMS_PERM = 122;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_permission, container);

        v.findViewById(R.id.button_sms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSMSPerm();
            }
        });

        return v;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Let EasyPermissions handles the request result.
        PermissionsAdapter.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    //Handle back from setting screen if your changed permission or not
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PermissionsAdapter.SETTINGS_REQ_CODE) {
            boolean hasReadSmsPermission = PermissionsAdapter.checkHasPermissions(getContext(),
                    Manifest.permission.READ_SMS);
            String hasReadSmsPermissionText = "Has read sms permission: " + hasReadSmsPermission;

            Toast.makeText(getContext(), hasReadSmsPermissionText, Toast.LENGTH_SHORT).show();
        }
    }

    @OnPermissionGranted(REQUEST_SMS_PERM)
    private void requestSMSPerm() {
        if (PermissionsAdapter.checkHasPermissions(getContext(), Manifest.permission.READ_SMS)) {
            // Have permission
            Toast.makeText(getActivity(), "Had SMS Permission", Toast.LENGTH_LONG).show();
        } else {
            // Request one permission
            PermissionsAdapter.requestPermissions(this, getString(R.string.rationale_sms),
                    REQUEST_SMS_PERM, Manifest.permission.READ_SMS);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.i(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // Handle negative button on click listener
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(),"Setting Dialog Cancel!", Toast.LENGTH_SHORT).show();
            }
        };

        // If the user denied permissions and checked NEVER ASK AGAIN.
        // This will display a dialog directing them to enable the permission in app settings.
        PermissionsAdapter.checkDeniedPermissionsNeverAskAgain(this,
                getString(R.string.rationale_ask_again),
                R.string.setting, R.string.cancel, onClickListener, perms);
    }
}
