package com.brucetoo.wifitransport.HotPot;


import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;

import com.brucetoo.wifitransport.WifiMsg;
import com.brucetoo.wifitransport.materialfilepicker.utils.FileTypeUtils;
import com.google.protobuf.ByteString;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Bruce Too
 * On 6/29/16.
 * At 17:50
 * This service use to send data
 *
 * @see HotpotActivity#sendFile(ClientScanResult) server -> client
 * @see HotpotActivity#sendFile(View)  client -> server
 */
public class TransferService extends IntentService {

    private static final String TAG = "TransferService";
    public static final String INTENT_FILE_TO_SEND = "file_to_send";
    public static final String INTENT_SERVER_PORT = "server_port";
    public static final String INTENT_SERVER_IP = "server_ip";
    public static final String INTENT_SEND_RESULT = "send_result";
    public static final int TIME_OUT = 5000;

    private ResultReceiver mSendResult;

    public TransferService() {
        super("ClientService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        mSendResult = intent.getParcelableExtra(INTENT_SEND_RESULT);
        String serverIp = intent.getStringExtra(INTENT_SERVER_IP);
        String fileToSend = intent.getStringExtra(INTENT_FILE_TO_SEND);

        Socket clientSocket = null;
        OutputStream out = null;
        InputStream in = null;

        clientSocket = new Socket();
        boolean isPortEnable = false;
        int finalPort = WifiManagerUtils.SERVER_CONNECT_PORT;
        int retryCount = 1;


        //retry 5 times connect to server
        while (!isPortEnable && retryCount <= WifiManagerUtils.RETRY_COUNT) {
            try {
                /**
                 * If {@code localAddr} is set to {@code null},
                 * this socket will be bound to an available local address on
                 * any free port.
                 */
                clientSocket.bind(null);
                //TIME_OUT the connection is refused or server doesn't exist.
                clientSocket.connect((new InetSocketAddress(serverIp, finalPort)), TIME_OUT);
                isPortEnable = true;
            } catch (IOException e) {
                finalPort += WifiManagerUtils.SERVER_PORT_RETRY_OFFSET;//if the port is not available,add 6 until ok
                if (retryCount++ > WifiManagerUtils.RETRY_COUNT) {
                    //connect server error
                    return;
                }
                e.printStackTrace();
            }
        }

        //connect to server successfully
        try {

            out = clientSocket.getOutputStream();

            Log.i(TAG, "Start send file: " + fileToSend);

            File file = new File(fileToSend);
            in = new BufferedInputStream(new FileInputStream(fileToSend));
            WifiMsg.WifiData wifiData = WifiMsg.WifiData.newBuilder()
                    .setId(1)
                    .setSuffix(FileTypeUtils.getExtension(file.getName()))
                    .setFileSize(file.getTotalSpace())
                    .setData(ByteString.readFrom(in))
                    .build();
            wifiData.writeTo(out);

            Log.i(TAG, "File send complete, sent file: " + fileToSend);
            setResult(ReceiveService.RECEIVE_SUCCESS);
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
            setResult(ReceiveService.RECEIVE_FAILED);
        } finally {
            Utils.closeSilently(in, out, clientSocket);
        }

    }

    private void setResult(int state) {
        Bundle bundle = new Bundle();
        bundle.putInt(ReceiveService.BUNDLE_RECEIVE_STATE, state);
        mSendResult.send(WifiManagerUtils.SERVER_CONNECT_PORT, bundle);
    }

}