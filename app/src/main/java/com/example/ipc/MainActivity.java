package com.example.ipc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.mtp.MtpConstants;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    // 服务端Messenger
    private Messenger mServerMessenger;
    // 服务端连接状态
    private boolean mIsBound = false;
    // 绑定服务端
    private Button message_0;
    private Button aidl_0;
    private IBookManager mRemoteBookManager;

    private Button contentProvider_0;
    private Button contentprovier_1;



    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServerMessenger = new Messenger(service);
            Message message = new Message();
            message.what = 1;
            Bundle bundle = new Bundle();
            bundle.putString("data","你好啊");
            message.setData(bundle);
            message.replyTo = mGetReplyMessenger;
            try {
                mServerMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private IOnNewBookArrivedListener mIOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {
        @Override
        public void onNewBookArrived(Book book) throws RemoteException {
            mHandler.obtainMessage(555,book).sendToTarget();
        }
    };
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 555:
                    Log.d("TAG","receive new book:"+msg.obj);
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        message_0 = findViewById(R.id.message_0);
        aidl_0 = findViewById(R.id.aidl_0);
        contentProvider_0 = findViewById(R.id.contentprovider_0);
        contentprovier_1 = findViewById(R.id.contentprovier_1);
        // 绑定服务端
        if(!mIsBound) {

            message_0.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mIsBound = true;
                    Intent intent = new Intent(MainActivity.this, MyService.class);
                    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                }
            });
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                contentProvider_0.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.parse("content://com.example.ipc.BookProvider");
                        getContentResolver().query(uri,null,null,null,null);
                        getContentResolver().query(uri,null,null,null,null);
                        getContentResolver().query(uri,null,null,null,null);

                    }
                });
            }
        });
        thread.start();

        aidl_0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsBound = true;
                Intent intent = new Intent(MainActivity.this,BookManagerService.class);
                bindService(intent,mConnection1,BIND_AUTO_CREATE);
            }
        });

        contentprovier_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri bookUri = Uri.parse("content://com.example.ipc.provider/book");
                ContentValues values = new ContentValues();
                values.put("_id",6);
                values.put("name","程序设计的艺术");
                getContentResolver().insert(bookUri,values);
                Cursor bookCursor = getContentResolver().query(bookUri,new String[]{"_id","name"},null,null,null);

                while(bookCursor.moveToNext()){
                   Book book = new Book(bookCursor.getString(1),bookCursor.getInt(0));
                   Log.d("TAG","query book:"+book.toString());
                }
                bookCursor.close();


                Uri userUri = Uri.parse("content://com.example.ipc.provider/user");
                Cursor userCursor = getContentResolver().query(userUri,new String[]{"_id","name","sex"},null,null,null);
                while(userCursor.moveToNext()){
                    User user = new User();
                    user.userName = userCursor.getString(1);
                    user.userId = userCursor.getInt(0);
                    user.isMale = userCursor.getInt(2) ==1;
                    Log.d("TAG","query user:"+user.toString());
                }
                userCursor.close();
            }
        });





    }
    private ServiceConnection mConnection1 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
           new Thread(new Runnable() {
               @Override
               public void run() {
                   IBookManager iBookManager = IBookManager.Stub.asInterface(service);
                   try {
                       mRemoteBookManager = iBookManager;
                       List<Book>list = iBookManager.getBookList();
                       Log.d("TAG","query book list,list type:"+list.getClass().getCanonicalName());
                       Book book = new Book("好好学习",3);
                       iBookManager.addBook(book);
                       Log.d("TAG","Now add book named"+"   "+book.getBookName());
                       Log.d("TAG","添加后");
                       List<Book>newList = iBookManager.getBookList();
                       for(int i = 0;i<newList.size();i++){
                           Log.d("TAG","客户端中"+" "+newList.get(i).getBookName()+"  "+newList.get(i).getBookNum());

                       }
                       iBookManager.registerListener(mIOnNewBookArrivedListener);

                   } catch (RemoteException e) {
                       e.printStackTrace();
                   }
               }
           }).start();

        }



        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解绑服务端
        if(mRemoteBookManager!=null&&mRemoteBookManager.asBinder().isBinderAlive()){
            Log.d("TAG","unregister listener:"+mIOnNewBookArrivedListener);
            try {
                mRemoteBookManager.unregisterListener(mIOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
    }
    private Messenger mGetReplyMessenger = new Messenger(new MessengerHandle());
    private static class MessengerHandle extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 2:
                    Log.d("TAG",msg.getData().getString("TAG").toString());
                    break;
            }
        }
    }
}