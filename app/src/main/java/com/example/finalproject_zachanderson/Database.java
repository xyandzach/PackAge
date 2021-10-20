package com.example.finalproject_zachanderson;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
    //constructor builds "db" database
    public Database(Context context) {
        super(context, "db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create singular table called "savedOrders" with singular column named "text", used to store tracking numbers for "My Orders" page
        db.execSQL("create table savedOrders (number text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //never used
    }
}