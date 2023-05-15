package com.example.ipc;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class provider extends ContentProvider {
//    private static final String TAG = "BookProvider";
    private Context mContext;
    private SQLiteDatabase mDb;
    public static final String AUTHORITY = "com.example.ipc.provider";
    public static final Uri BOOK_CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/book");
    public static final Uri USER_CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/user");
    public static final int BOOK_URI_CODE = 0;
    public static final int USER_URI_CODE = 1;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY,"book",BOOK_URI_CODE);
        sUriMatcher.addURI(AUTHORITY,"user",USER_URI_CODE);
    }

    private String getTableName(Uri uri){
        String tableName = null;
        switch (sUriMatcher.match(uri)){
            case BOOK_URI_CODE:
                tableName = DbOpenHelper.BOOK_TABLE_NAME;
                break;
            case USER_URI_CODE:
                tableName = DbOpenHelper.USER_TABLE_NAME;
                break;
            default:break;
        }
        return tableName;
    }
    @Override
    public boolean onCreate() {
        Log.d("TAG","onCreate,current thread:"+Thread.currentThread().getName());
        mContext = getContext();
        initProviderData();
        return true;
    }
    private void initProviderData(){
        mDb = new DbOpenHelper(mContext,AUTHORITY,null,1).getWritableDatabase();
        mDb.execSQL("delete from "+DbOpenHelper.BOOK_TABLE_NAME);
        mDb.execSQL("delete from "+DbOpenHelper.USER_TABLE_NAME);
        mDb.execSQL("insert into book values (3,'Android');");
        mDb.execSQL("insert into book values (4,'iOS');");
        mDb.execSQL("insert into book values (5,'HarmonyOs');");
        mDb.execSQL("insert into user values (1,'jack',1);");
        mDb.execSQL("insert into user values (2,'hi',0);");
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Log.d("TAG","query,current thread:"+Thread.currentThread().getName());
        String table = getTableName(uri);
        if(table==null){
            throw new IllegalArgumentException("Unsupport URI:"+uri);
        }
        return mDb.query(table,projection,selection,selectionArgs,null,null,sortOrder,null);

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        Log.d("TAG","getType");
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Log.d("TAG","insert");
        String table =getTableName(uri);
        if(table==null){
            throw new IllegalArgumentException("Unsupport URI:"+uri);
        }
        mDb.insert(table,null,values);
        mContext.getContentResolver().notifyChange(uri,null);
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.d("TAG","delete");
        String table =getTableName(uri);
        if(table==null){
            throw new IllegalArgumentException("Unsupport URI:"+uri);
        }
        int count =mDb.delete(table,selection,selectionArgs);
        if(count>0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.d("TAG","update");
        String table = getTableName(uri);
        if(table==null){
            throw new IllegalArgumentException("Unsupport URI:"+uri);
        }
        int row = mDb.update(table,values,selection,selectionArgs);
        if(row>0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return row;
    }
}
