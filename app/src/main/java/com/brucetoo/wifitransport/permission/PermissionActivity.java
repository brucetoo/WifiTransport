package com.brucetoo.wifitransport.permission;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.brucetoo.wifitransport.R;

import java.util.List;

/**
 * Created by Bruce Too
 * On 7/21/16.
 * At 11:49
 */
public class PermissionActivity extends FragmentActivity implements PermissionsAdapter.PermissionCallbacks {

    private static final String TAG = PermissionActivity.class.getSimpleName();
    private static final int REQUEST_CAMERA_PERM = 123;
    private static final int REQUEST_CONTACTS_PERM = 124;
    private static final int REQUEST_ALL_PERM = 125;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        // Request one permission.
        findViewById(R.id.button_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCameraPerm();
            }
        });

        // Request two permissions.
        findViewById(R.id.button_location_and_wifi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLocationAndContactsPerm();
            }
        });

        findViewById(R.id.button_perm_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAllPerm();
            }
        });
    }

    @OnPermissionGranted(REQUEST_ALL_PERM)
    public void requestAllPerm() {

        //request one of permission in permission-group,then you get the whole permission in permission-group
        String[] allPerms = {Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS
                , Manifest.permission.WRITE_CONTACTS, Manifest.permission.GET_ACCOUNTS
                , Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                , Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE
                , Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG
                , Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS
                , Manifest.permission.READ_SMS, Manifest.permission.READ_EXTERNAL_STORAGE
                , Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        if (PermissionsAdapter.checkHasPermissions(this, allPerms)) {// Have permission

            Toast.makeText(this, "Had All Permissions", Toast.LENGTH_LONG).show();
        } else {// Request one permission
            PermissionsAdapter.requestPermissions(this, getString(R.string.rationale_all),
                    REQUEST_ALL_PERM, allPerms);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PermissionsAdapter.SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen
            Toast.makeText(this, "Returned from app settings", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // EasyPermissions handles the request result.
        PermissionsAdapter.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @OnPermissionGranted(REQUEST_CAMERA_PERM)
    public void requestCameraPerm() {
        if (PermissionsAdapter.checkHasPermissions(this, Manifest.permission.CAMERA)) {
            // Have permission
            Toast.makeText(this, "Had Camera Permission", Toast.LENGTH_LONG).show();
        } else {
            // Request one permission
            PermissionsAdapter.requestPermissions(this, getString(R.string.rationale_camera),
                    REQUEST_CAMERA_PERM, Manifest.permission.CAMERA);
        }
    }

    @OnPermissionGranted(REQUEST_CONTACTS_PERM)
    public void requestLocationAndContactsPerm() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS};
        if (PermissionsAdapter.checkHasPermissions(this, perms)) {
            // Have permissions, do the thing!
            Toast.makeText(this, "Had Location and Contacts Permissions", Toast.LENGTH_LONG).show();
        } else {
            // Ask for both permissions
            PermissionsAdapter.requestPermissions(this, getString(R.string.rationale_location_contacts),
                    REQUEST_CONTACTS_PERM, perms);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.i(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.i(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // Check whether the user denied permissions and checked NEVER ASK AGAIN.
        // This will display a dialog directing them to enable the permission in app settings.
        PermissionsAdapter.checkDeniedPermissionsNeverAskAgain(this,
                getString(R.string.rationale_ask_again),
                R.string.setting, R.string.cancel, null, perms);
    }
}
