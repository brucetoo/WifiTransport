package com.brucetoo.wifitransport.HotPot.image;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;
import android.widget.ListView;

import com.brucetoo.wifitransport.R;
import com.bumptech.glide.Glide;
import com.joanzapata.android.BaseAdapterHelper;
import com.joanzapata.android.QuickAdapter;

import java.util.ArrayList;

/**
 * Created by Bruce Too
 * On 7/8/16.
 * At 15:15
 */
public class ImageActivity extends FragmentActivity {

    ListView mListView;
    QuickAdapter<Directory> mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        mListView = (ListView) findViewById(R.id.image_list);
        mAdapter = new QuickAdapter<Directory>(this, R.layout.list_image) {
            @Override
            protected void convert(BaseAdapterHelper helper, Directory item) {
                helper.setText(R.id.text_name, "Image Name:" + item.name);
                helper.setText(R.id.text_path, "Image Path:" + item.path);
                helper.setText(R.id.text_count, "Image Count:" + item.images.size() + "");
                Glide.with(ImageActivity.this).load(item.image.path).into((ImageView) helper.getView(R.id.image_icon));
            }
        };
        mListView.setAdapter(mAdapter);

        getLoaderManager().restartLoader(ImageLoaderCallBack.LOADER_ALL, null, new ImageLoaderCallBack(this, new ImageLoaderCallBack.OnLoadCallbackListener() {
            @Override
            public void onLoadCallback(ArrayList<Directory> directories) {
                mAdapter.replaceAll(directories);
            }
        }));

    }
}
