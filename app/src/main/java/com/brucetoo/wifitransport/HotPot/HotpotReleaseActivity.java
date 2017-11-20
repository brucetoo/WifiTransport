package com.brucetoo.wifitransport.HotPot;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.brucetoo.wifitransport.HotPot.video.WebFragment;
import com.brucetoo.wifitransport.R;

import java.util.List;

/**
 * Created by Bruce Too
 * On 7/7/16.
 * At 10:42
 */
public class HotpotReleaseActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotpot_release);

        replaceFragment(EnterFragment.newInstance());
    }

    public void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (fragment instanceof EnterFragment) {
            transaction.replace(R.id.layout_container, fragment)
                    .commit();
        } else {
            transaction.replace(R.id.layout_container, fragment)
                    .addToBackStack(fragment.getClass().getSimpleName())
                    .commit();
        }
    }



    @Override
    public void onBackPressed() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if(fragments.size() >= 2){
            for (Fragment f : fragments) {
                if (f instanceof WebFragment){
                    if(((WebFragment) f).canGoBack()){
                        ((WebFragment) f).goBack();
                    }else {
                        super.onBackPressed();
                    }
                }
            }
        }else {
            super.onBackPressed();
        }

    }

//    @Override
//        public boolean onKeyDown(int keyCode, KeyEvent event) {
//            if ((keyCode == KeyEvent.KEYCODE_BACK) && getFragmentManager().f) {
//                webView.goBack();
//                return true;
//            }
//            return super.onKeyDown(keyCode, event);
//        }
}
