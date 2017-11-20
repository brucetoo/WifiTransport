package com.brucetoo.wifitransport.HotPot;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Bruce Too
 * On 6/30/16.
 * At 16:29
 * This is used in receiving data
 * if want device has receiving function,we need start thi service
 * in {@link android.app.Activity#onCreate(Bundle)} or other start callback
 */
public class ReceiveService extends IntentService {

    public static final String LOCATION = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String TAG = "ReceiveService";
    public static final String INTENT_RECEIVER_RESULT = "receiver_result";
    public static final String BUNDLE_RECEIVE_STATE = "send_state";
    public static final int RECEIVE_SUCCESS = 0;
    public static final int RECEIVE_FAILED = 1;

    private ResultReceiver mServerResult;

    public ReceiveService() {
        super("ReceiveService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        mServerResult = intent.getParcelableExtra(INTENT_RECEIVER_RESULT);

        ServerSocket serverSocket = null;
        Socket socket = null;
        InputStream in = null;
        OutputStream out = null;

        try {
            serverSocket = new ServerSocket(WifiManagerUtils.SERVER_CONNECT_PORT);
            while (true) {
                //block until client connect
                Log.i(TAG, "Wait for client to connect...");
                socket = serverSocket.accept();
                Log.i(TAG, "Start receive file...");

                String savedAs = "/WIFI_" + System.currentTimeMillis();
                File file = new File(LOCATION, savedAs);

                byte[] buffer = new byte[4096];
                in = socket.getInputStream();
                out = new BufferedOutputStream(new FileOutputStream(file));
                //read bytes
                int count;
                while ((count = in.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                    out.flush();
                }
                Log.i(TAG, "File receive complete, saved as: " + savedAs);

                setResult(RECEIVE_SUCCESS);//call this in while(true) has problem

            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, e.getMessage());
            setResult(RECEIVE_FAILED);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (socket != null) {
                    socket.close();
                }
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, e.getMessage());
            }

        }
    }

    private void setResult(int state) {
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_RECEIVE_STATE, state);
        if (mServerResult != null)
            mServerResult.send(WifiManagerUtils.SERVER_CONNECT_PORT, bundle);
    }

}
