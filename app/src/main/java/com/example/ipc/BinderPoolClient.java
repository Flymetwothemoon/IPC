package com.example.ipc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.concurrent.CountDownLatch;

public class BinderPoolClient {
    private Context mContext;
    private IBinderPool mIBinderPool;
    private static volatile BinderPoolClient sInstance;
    private CountDownLatch mConnectBinderPoolCountDownLatch;

    public BinderPoolClient(Context context) {
        mContext = context.getApplicationContext();
        connectBinderPoolService();
    }

    public static BinderPoolClient getInstance(Context context){
        if(sInstance==null){
            synchronized (BinderPoolClient.class) {
                if(sInstance==null) {
                    sInstance = new BinderPoolClient(context);
                }
            }
        }
        return sInstance;
    }


    private synchronized void connectBinderPoolService(){
        mConnectBinderPoolCountDownLatch = new CountDownLatch(1);
        Intent service = new Intent(mContext,BinderPoolService.class);

        mContext.bindService(service,mBinderPoolConnection,Context.BIND_AUTO_CREATE);

        try {
            mConnectBinderPoolCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection mBinderPoolConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIBinderPool = IBinderPool.Stub.asInterface(service);

            try {
                mIBinderPool.asBinder().linkToDeath(mBinderPoolDeathRecipient,0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }


            mConnectBinderPoolCountDownLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private IBinder.DeathRecipient mBinderPoolDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d("TAG","binder died");
            mIBinderPool.asBinder().unlinkToDeath(mBinderPoolDeathRecipient,0);
            mIBinderPool = null;
            connectBinderPoolService();
        }
    };
    public synchronized IBinder queryBinder(int binderCode){
        IBinder iBinder = null;


            try {
                iBinder = mIBinderPool.queryBinder(binderCode);

            } catch (RemoteException e) {
                e.printStackTrace();

        }
        return iBinder;
    }


}
