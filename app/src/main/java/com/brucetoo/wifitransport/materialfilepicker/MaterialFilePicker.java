package com.brucetoo.wifitransport.materialfilepicker;

import android.app.Activity;
import android.content.Intent;

import com.brucetoo.wifitransport.materialfilepicker.filter.CompositeFilter;
import com.brucetoo.wifitransport.materialfilepicker.filter.HiddenFilter;
import com.brucetoo.wifitransport.materialfilepicker.filter.PatternFilter;
import com.brucetoo.wifitransport.materialfilepicker.ui.FilePickerActivity;

import java.io.FileFilter;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by Dimorinny on 25.02.16.
 */
public class MaterialFilePicker {
    private Activity mActivity;
    private boolean mFromFragment;
    private Integer mRequestCode;
    private Pattern mFileFilter;
    private Boolean mDirectoriesFilter = false;
    private String mRootPath;
    private String mCurrentPath;
    private Boolean mShowHidden = false;

    public MaterialFilePicker() {}

    public MaterialFilePicker withActivity(Activity activity) {
        mActivity = activity;
        mFromFragment = false;
        return this;
    }

    public MaterialFilePicker withFragment(Activity activity) {
        mActivity = activity;
        mFromFragment = true;
        return this;
    }


    public MaterialFilePicker withRequestCode(int requestCode) {
        mRequestCode = requestCode;
        return this;
    }

    public MaterialFilePicker withFilter(Pattern pattern) {
        mFileFilter = pattern;
        return this;
    }

    public MaterialFilePicker withFilterDirectories(boolean directoriesFilter) {
        mDirectoriesFilter = directoriesFilter;
        return this;
    }

    public MaterialFilePicker withRootPath(String rootPath) {
        mRootPath = rootPath;
        return this;
    }

    public MaterialFilePicker withPath(String path) {
        mCurrentPath = path;
        return this;
    }

    public MaterialFilePicker withHiddenFiles(boolean show) {
        mShowHidden = show;
        return this;
    }

    private CompositeFilter getFilter() {
        ArrayList<FileFilter> filters = new ArrayList<>();

        if (!mShowHidden) {
            filters.add(new HiddenFilter());
        }

        if (mFileFilter != null) {
            filters.add(new PatternFilter(mFileFilter, mDirectoriesFilter));
        }

        return new CompositeFilter(filters);
    }

    public Intent start() {
        if (mActivity == null) {
            throw new RuntimeException("You must pass activity by calling withActivity method");
        }

        if (mRequestCode == null) {
            throw new RuntimeException("You must pass request code by calling withRequestCode method");
        }

        CompositeFilter filter = getFilter();

        Intent intent = new Intent(mActivity, FilePickerActivity.class);
        intent.putExtra(FilePickerActivity.ARG_FILTER, filter);

        if (mRootPath != null) {
            intent.putExtra(FilePickerActivity.ARG_START_PATH, mRootPath);
        }

        if (mCurrentPath != null) {
            intent.putExtra(FilePickerActivity.ARG_CURRENT_PATH, mCurrentPath);
        }

        if(!mFromFragment) {
            mActivity.startActivityForResult(intent, mRequestCode);
            return null;
        }else {
            return intent;
        }
    }
}
