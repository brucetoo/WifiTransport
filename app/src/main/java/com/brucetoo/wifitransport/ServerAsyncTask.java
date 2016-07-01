package com.brucetoo.wifitransport;

import android.content.Context;
import android.os.AsyncTask;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

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
 * At 13:36
 */
public class ServerAsyncTask extends AsyncTask<Void, Void, String> {

    private final Context mContext;
    private ResultReceiver mServerResult;

    public ServerAsyncTask(Context context, ResultReceiver mServerResult) {
        this.mContext = context;
        this.mServerResult = mServerResult;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... params) {
        ServerSocket serverSocket = null;
        Socket socket = null;
        InputStream in = null;
        OutputStream out = null;

        try {
            serverSocket = new ServerSocket(MainActivity.PORT);
            //block until client connect
            Log.i(MainActivity.TAG, "Wait for client to connect...");
            socket = serverSocket.accept();
            Log.i(MainActivity.TAG, "Start receive file...");

            String savedAs = "/WIFI_" + System.currentTimeMillis();
            File file = new File(MainActivity.LOCATION, savedAs);

            byte[] buffer = new byte[4096];
            in = socket.getInputStream();
            out = new BufferedOutputStream(new FileOutputStream(file));
            //read bytes
            int count;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
                out.flush();
            }
            Log.i(MainActivity.TAG, "File receive complete, saved as: " + savedAs);
            return savedAs;
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(MainActivity.TAG, e.getMessage());
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
                Log.i(MainActivity.TAG, e.getMessage());
            }

        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            mServerResult.send(MainActivity.PORT, null);
            Toast.makeText(mContext, "File save in : " + result, Toast.LENGTH_SHORT).show();
            Log.i(MainActivity.TAG, "File save in : " + result);
//            Intent intent = new Intent();
//            intent.setAction(Intent.ACTION_VIEW);
//            intent.setDataAndType(Uri.parse("file://" + result), "image/*");
//            context.startActivity(intent);
        }
    }
}
