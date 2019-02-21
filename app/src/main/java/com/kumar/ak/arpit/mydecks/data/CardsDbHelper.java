package com.kumar.ak.arpit.mydecks.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CardsDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "deckbox.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    private static final String CARDS_TABLE = "CREATE TABLE " + DecksContract.CardsEntry.TABLE_NAME + " ("
            + DecksContract.CardsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DecksContract.CardsEntry.COLUMN_CARD_DBFID + " INTEGER, "
            + DecksContract.CardsEntry.COLUMN_CARD_NAME + " TEXT, "
            + DecksContract.CardsEntry.COLUMN_CARD_COST + " INTEGER, "
            + DecksContract.CardsEntry.COLUMN_CARD_RARITY + " INTEGER, "
            + DecksContract.CardsEntry.COLUMN_CARD_TYPE + " INTEGER, "
            + DecksContract.CardsEntry.COLUMN_CARD_ID + " TEXT);";

    public CardsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
