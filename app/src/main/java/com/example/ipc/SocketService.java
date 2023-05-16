package com.example.ipc;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketService extends Service {
    private ServerThread serverThread;

    public SocketService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (serverThread != null) {
            serverThread.stopServer();
            serverThread = null;
        }
    }
    private class ServerThread extends Thread {
        private ServerSocket serverSocket;
        private boolean isRunning = true;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                System.out.println("Server started and waiting for client connection...");

                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected.");

                    // 处理客户端连接的逻辑，参考上述服务端代码的处理逻辑

                    clientSocket.close();
                }

                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void stopServer() {
            isRunning = false;
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}