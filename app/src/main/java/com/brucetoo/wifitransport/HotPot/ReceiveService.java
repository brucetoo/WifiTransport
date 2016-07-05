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
        boolean isPortEnable = false;
        int finalPort = WifiManagerUtils.SERVER_CONNECT_PORT;
        int retryCount = 1;

        //check the port first,and retry count <= 5
        while (!isPortEnable && retryCount <= WifiManagerUtils.RETRY_COUNT) {
            try {
                serverSocket = new ServerSocket(finalPort);
                isPortEnable = true;
            } catch (IOException e) {
                finalPort += WifiManagerUtils.SERVER_PORT_RETRY_OFFSET;//if the port is not available,add 6 until ok
                if (retryCount++ > WifiManagerUtils.RETRY_COUNT) {
                    //create ServerSocket error
                    return;
                }
                e.printStackTrace();
            }
        }

        //create ServerSocket successfully
        try {
            while (true) {
                //block until client connect
                Log.i(TAG, "Wait for client to connect...");
                socket = serverSocket.accept();
                Log.i(TAG, "Start receive file...");

                //create a template file,when receive over,copy it to destination
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

                setResult(RECEIVE_SUCCESS);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, e.getMessage());
            setResult(RECEIVE_FAILED);
        } finally {
            try {
                Utils.closeSilently(in,out,socket);
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
