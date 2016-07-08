package com.brucetoo.wifitransport.HotPot.image;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruce Too
 * On 7/8/16.
 * At 14:51
 */
public class ImageLoaderCallBack implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media._ID};

    /**
     * Search all SD card
     */
    public static final int LOADER_ALL = 0;
    /**
     * Pass exact path location
     */
    public static final int LOADER_CATEGORY = 1;
    /**
     * Use when id = {@link #LOADER_CATEGORY},and pass exact path
     */
    public static final String BUNDLE_ARG_PATH = "arg_path";

    private Context mContext;
    private boolean mIsDirectoryGenerated;
    private ArrayList<Directory> mResultDirectory = new ArrayList<>();
    private OnLoadCallbackListener mOnLoadCallbackListener;

    public ImageLoaderCallBack(Context context, OnLoadCallbackListener onLoadCallbackListener) {
        this.mContext = context;
        this.mOnLoadCallbackListener = onLoadCallbackListener;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        CursorLoader cursorLoader = null;
        if (id == LOADER_CATEGORY) {
            if (args == null) {
                throw new IllegalArgumentException("when choose LOADER_CATEGORY,bundle can not be null");
            }
        }

        if (id == LOADER_ALL) {
            cursorLoader = new CursorLoader(mContext,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                    IMAGE_PROJECTION[4] + ">0 AND " + IMAGE_PROJECTION[3] + "=? OR " + IMAGE_PROJECTION[3] + "=? ",
                    new String[]{"image/jpeg", "image/png"}, IMAGE_PROJECTION[2] + " DESC");
        } else if (id == LOADER_CATEGORY) {
            cursorLoader = new CursorLoader(mContext,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                    IMAGE_PROJECTION[4] + ">0 AND " + IMAGE_PROJECTION[0] + " like '%" + args.getString(BUNDLE_ARG_PATH) + "%'",
                    null, IMAGE_PROJECTION[2] + " DESC");
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null) {
            if (data.getCount() > 0) {
                List<Image> images = new ArrayList<>();
                data.moveToFirst();
                do {
                    String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                    String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                    long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                    //if file not exist
                    if (!fileExist(path)) {
                        continue;
                    }
                    Image image = null;
                    if (!TextUtils.isEmpty(name)) {
                        image = new Image(path, name, dateTime);
                        images.add(image);
                    }
                    if (!mIsDirectoryGenerated) {
                        // get all directory data
                        File dirFile = new File(path).getParentFile();
                        if (dirFile != null && dirFile.exists()) {
                            String fp = dirFile.getAbsolutePath();
                            Directory d = getDirectoryByPath(fp);
                            if (d == null) {
                                Directory directory = new Directory();
                                directory.name = dirFile.getName();
                                directory.path = fp;
                                directory.image = image;
                                List<Image> imageList = new ArrayList<>();
                                imageList.add(image);
                                directory.images = imageList;
                                mResultDirectory.add(directory);
                            } else {
                                d.images.add(image);
                            }
                        }
                    }

                } while (data.moveToNext());

                if (!mIsDirectoryGenerated) {
                    if (mOnLoadCallbackListener != null) {
                        mOnLoadCallbackListener.onLoadCallback(mResultDirectory);
                    }
                    mIsDirectoryGenerated = true;
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private boolean fileExist(String path) {
        if (!TextUtils.isEmpty(path)) {
            return new File(path).exists();
        }
        return false;
    }

    private Directory getDirectoryByPath(String path) {

        if (mResultDirectory != null) {
            for (Directory directory : mResultDirectory) {
                if (TextUtils.equals(directory.path, path)) {
                    return directory;
                }
            }
        }
        return null;
    }

    public interface OnLoadCallbackListener {

        void onLoadCallback(ArrayList<Directory> directories);
    }
}
