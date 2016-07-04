package com.brucetoo.wifitransport.HotPot;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by Bruce Too
 * On 6/30/16.
 * At 16:29
 */
public class UploadService extends IntentService {

    public static final int PORT = 7878;
    private static final String TAG = "UploadService";
    public static final String SERVER_IP = "server_ip";

    public UploadService() {
        super("UploadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(PORT);
            while (true) {
                //block until client connect
                Log.i(TAG, "Wait for client to connect...");
                socket = serverSocket.accept();
                Log.i(TAG, "Client connect successfully");

                String fileToSend = Utils.getInstalledAppPathList(getApplicationContext()).get(0).sourceDir;
                UploadFileThread thread = new UploadFileThread(socket, fileToSend);
                thread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, e.getMessage());
        }
    }

    class UploadFileThread extends Thread {

        private static final int BUFFER_SIZE = 4096;
        private Socket socket;
        private byte[] buf = new byte[BUFFER_SIZE];
        private OutputStream out;
        private InputStream is;
        private String fileToSend;

        public UploadFileThread(Socket socket, String fileToSend) {
            this.socket = socket;
            this.fileToSend = fileToSend;
        }

        public void run() {
            int numberRead = 0;
            try {
                out = socket.getOutputStream();
                is = socket.getInputStream();
                numberRead = is.read(buf, 0, BUFFER_SIZE);
                Log.i(TAG, "request read size:" + numberRead);

                //no request at all
                if (numberRead < 0)
                    return;

                File file = new File(fileToSend);

                out.write("HTTP/1.1 200 OK\r\n".getBytes());
                out.write("Accept-Ranges: bytes\r\n".getBytes());
                out.write(("Content-Length: " + file.length() + "\r\n").getBytes());
                out.write("Content-Type: application/octet-stream\r\n".getBytes());
                out.write(("Content-Disposition: attachment; filename=\"" + file.getName() + "\"\r\n").getBytes());
                out.write("\r\n".getBytes());//header and data must be separate
                copy(file, out);
                Log.i(TAG, "File upload completed!");

                socket.close();
            } catch (SocketException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void copy(File src, OutputStream out) throws IOException {
        InputStream in = new FileInputStream(src);
        byte[] buf = new byte[4096];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}
