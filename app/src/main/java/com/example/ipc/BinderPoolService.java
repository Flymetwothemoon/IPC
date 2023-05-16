package com.example.ipc;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

public class BinderPoolService extends Service {
    private Binder mBinderPool = new BinderPoolImpl();
    public BinderPoolService() {
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return mBinderPool;
    }


    //
    public static class SecurityCenterImpl extends ISecurityCenter.Stub{
        //加密
        private static final char SECRET_CODE = '^';
        @Override
        public String encrypt(String content) throws RemoteException {
            char[]chars = content.toCharArray();
            for(int i = 0;i<chars.length;i++){
                chars[i]^= SECRET_CODE;
            }
            return new String(chars);
        }
        //解密
        @Override
        public String decrypt(String password) throws RemoteException {
            return encrypt(password);
        }
    }



    public static class ComputeImpl extends ICompute.Stub{
        @Override
        public int add(int a, int b) throws RemoteException {
            return a+b;
        }
    }



    public class BinderPoolImpl extends IBinderPool.Stub{

        @Override
        public IBinder queryBinder(int binderCode) throws RemoteException {
            IBinder binder = null;
            switch (binderCode){
                case 0:
                    binder = new SecurityCenterImpl();
                    break;
                case 1:
                    binder = new ComputeImpl();
                    break;
                default:
                    break;
            }
            return binder;
        }
    }
}