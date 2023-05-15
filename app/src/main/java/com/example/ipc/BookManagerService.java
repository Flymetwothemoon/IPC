package com.example.ipc;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class BookManagerService extends Service {
    public BookManagerService() {
    }
    private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(false);
    private CopyOnWriteArrayList<Book>mBookList = new CopyOnWriteArrayList<Book>();
    private RemoteCallbackList<IOnNewBookArrivedListener> mListenerList = new RemoteCallbackList<IOnNewBookArrivedListener>();
    private Binder mBinder = new IBookManager.Stub() {

        @Override
        public List<Book> getBookList() throws RemoteException {
            SystemClock.sleep(5000);
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
//            if(!mListenerList.contains(listener)){
//                mListenerList.add(listener);
//            }
//            else{
//                Log.d("TAG","already exist");
//            }
//            Log.d("TAG","registerListener,size:"+mListenerList.size());
            mListenerList.register(listener);
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
//            if(mListenerList.contains(listener)){
//                mListenerList.remove(listener);
//                Log.d("TAG","unregister listener succeed");
//            }
//            else {
//                Log.d("TAG","not found,can not unregister");
//            }
//            Log.d("TAG","unregisterListener,size:"+mListenerList.size());
            mListenerList.unregister(listener);

        }
    };
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
       return mBinder;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book("Android",1));
        mBookList.add(new Book("不想上班",2));
        new Thread(new ServiceWorker()).start();

    }
    private class ServiceWorker implements Runnable{
        @Override
        public void run() {
            while(!mIsServiceDestroyed.get()){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int bookId = mBookList.size()+1;

                Book newBook = new Book("new book#"+bookId,bookId);
                try {
                    onNewBookArrived(newBook);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }
    }
    private void onNewBookArrived(Book book) throws RemoteException {
        mBookList.add(book);
        final int N = mListenerList.beginBroadcast();
        for(int i = 0;i<N;i++){
            IOnNewBookArrivedListener l = mListenerList.getBroadcastItem(i);
            if(l!=null){
                l.onNewBookArrived(book);
            }
        }
        mListenerList.finishBroadcast();
//        for(int i =0;i<mListenerList.size();i++){
//            IOnNewBookArrivedListener listener = mListenerList.get(i);
//            Log.d("TAG","onNewBookArrived,notify listener:"+listener);
//            listener.onNewBookArrived(book);
//        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsServiceDestroyed.set(true);
    }
}