package com.kumar.ak.arpit.mydecks.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DecksDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "userdecks.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 2;

    private static final String DECKS_TABLE = "CREATE TABLE " + DecksContract.DecksEntry.TABLE_NAME + " ("
            + DecksContract.DecksEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DecksContract.DecksEntry.COLUMN_DECK_STRING + " TEXT, "
            + DecksContract.DecksEntry.COLUMN_DECK_FORMAT + " TEXT, "
            + DecksContract.DecksEntry.COLUMN_DECK_CLASS + " TEXT, "
            + DecksContract.DecksEntry.COLUMN_DECK_NAME + " TEXT, "
            + DecksContract.DecksEntry.IS_FAVORITE + " INTEGER DEFAULT 0);";

    public DecksDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create the Cards Table
        db.execSQL(DECKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1) {
            db.execSQL(DECKS_TABLE);
        }
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + DecksContract.DecksEntry.TABLE_NAME
                    + " ADD COLUMN " + DecksContract.DecksEntry.IS_FAVORITE + " INTEGER DEFAULT 0");
        }
    }
}

