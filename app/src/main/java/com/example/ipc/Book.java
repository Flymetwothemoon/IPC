package com.example.ipc;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Book implements Parcelable {
    public int getBookNum() {
        return BookNum;
    }

    private int BookNum;

    public String getBookName() {
        return BookName;
    }

    private String BookName;
    public Book(String BookName,int BookNum) {
        this.BookName = BookName;
        this.BookNum = BookNum;
    }

    protected Book(Parcel in) {
        BookNum = in.readInt();
        BookName = in.readString();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(BookNum);
        dest.writeString(BookName);
    }
}
