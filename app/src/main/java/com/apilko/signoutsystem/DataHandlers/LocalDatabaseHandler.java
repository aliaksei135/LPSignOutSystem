package com.apilko.signoutsystem.DataHandlers;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Keep;
import android.util.Log;

import java.util.Arrays;

@Keep
public class LocalDatabaseHandler extends SQLiteOpenHelper {

    private static final String TABLE_PEOPLE = "people"; //SQL table of people and their properties
    private static final String COLUMN_ID = "id"; //Integer, A unique identifier for each person **PRIMARY KEY**
    private static final String COLUMN_NAME = "name"; //String, Persons full name
    private static final String COLUMN_NATIVE_HOUSE = "home_house"; //String, Persons home house
    private static final String COLUMN_STATE = "state"; //Integer (Boolean), 1 (True)=In House, else 0 (false) = Out of House
    private static final String COLUMN_WHEREABOUTS = "whereabouts"; //String, shows the persons whereabouts
    private static final String COLUMN_TAG_ID = "tag_id"; //BLOB (Byte[]), the ID of the persons NFC Tag
    private static final String COLUMN_BIO_IMAGE = "bio_image"; //BLOB (Byte[]) , the image of the persons fingerprint /////// Could be useful: http://stackoverflow.com/questions/7331310/how-to-store-image-as-blob-in-sqlite-how-to-retrieve-it
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "data.db";
    private static LocalDatabaseHandler ourInstance;

    private LocalDatabaseHandler(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static LocalDatabaseHandler getInstance(Context context) {

        if (ourInstance == null) {
            ourInstance = new LocalDatabaseHandler(context);
            return ourInstance;
        } else {
            return ourInstance;
        }
    }

    public byte[] getBioImage(long id) {

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT  FROM " + TABLE_PEOPLE + " WHERE " + COLUMN_ID + " = " + id + ";", null);
        db.close();
        cursor.close();
        //Column 7 is the Bio image which we want (cursor is zero indexed)
        return cursor.getBlob(6);
    }

    public long getRecordNum() {

        SQLiteDatabase db = getReadableDatabase();
        long recordNum = DatabaseUtils.longForQuery(db, "SELECT COUNT (*) FROM " + TABLE_PEOPLE + ";", null);
        db.close();
        return recordNum;
    }

    public String getName(long id) {

        SQLiteDatabase db = getReadableDatabase();
        String name = DatabaseUtils.stringForQuery(db, "SELECT " + COLUMN_NAME + " FROM " + TABLE_PEOPLE + " WHERE " + COLUMN_ID + " = " + id + ";", null);
        db.close();
        return name;
    }

    public String getWhereabouts(long id) {

        SQLiteDatabase db = getReadableDatabase();
        String whereabouts = DatabaseUtils.stringForQuery(db, "SELECT " + COLUMN_WHEREABOUTS + " FROM " + TABLE_PEOPLE + " WHERE " + COLUMN_ID + " = '" + id + "';", null);
        db.close();
        return whereabouts;
    }

    public String getWhereabouts(String name) {

        SQLiteDatabase db = getReadableDatabase();
        String whereabouts = DatabaseUtils.stringForQuery(db, "SELECT " + COLUMN_WHEREABOUTS + " FROM " + TABLE_PEOPLE + " WHERE " + COLUMN_NAME + " = '" + name + "';", null);
        db.close();
        return whereabouts;
    }

    public void addNewRecord(String name, String house, byte[] ID, boolean isNFC) {
        //Add new data to buffer
        ContentValues values = new ContentValues();
        if (isNFC) {
            values.put(COLUMN_TAG_ID, ID);
        } else {
            values.put(COLUMN_BIO_IMAGE, ID);
        }
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_STATE, 1);
        values.put(COLUMN_NATIVE_HOUSE, house);
        values.put(COLUMN_WHEREABOUTS, "Signed In");

        //Push values to db
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_PEOPLE, null, values);
        db.close();
    }

    public void updateID(String name, Object newID, boolean isNFC) throws IllegalArgumentException {

        if (findRecord(name, null)) {
            SQLiteDatabase db = getWritableDatabase();
            if (isNFC) {
                String query = "UPDATE " + TABLE_PEOPLE + " SET " + COLUMN_TAG_ID + newID + " WHERE " + COLUMN_NAME + " = '" + name + "';";
                db.execSQL(query);
            } else {
                String query = "UPDATE " + TABLE_PEOPLE + " SET " + COLUMN_BIO_IMAGE + newID + " WHERE " + COLUMN_NAME + " = '" + name + "';";
                db.execSQL(query);
            }
            db.close();
        } else {
            Log.e("LclDB", "Can't update non existing ID record");
            throw new IllegalArgumentException("Can't update non existing ID record");
        }
    }

    private boolean findRecord(String field, Object key) {

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor;
        if (key == null) {
            String query = "SELECT * FROM " + TABLE_PEOPLE + " WHERE " + COLUMN_NAME + " = '" + field + "';";
            cursor = db.rawQuery(query, null);
        } else {
            String query = "SELECT * FROM " + TABLE_PEOPLE + " WHERE " + field + " = '" + key + "';";
            cursor = db.rawQuery(query, null);
        }
        if (cursor.getCount() <= 0) {
            cursor.close();
            db.close();
            return false;
        } else {
            cursor.close();
            db.close();
            return true;
        }
    }

    public void updateLocation(String name, String location) throws IllegalArgumentException {

        if (findRecord(name, null)) {
            SQLiteDatabase db = getWritableDatabase();

            if (location.equals("Study Period") || location.equals("Signed In")) {
                String query = "UPDATE " + TABLE_PEOPLE + " SET " + COLUMN_WHEREABOUTS + " = '" + location + "' ," + COLUMN_STATE + " = '" + 1 + "' WHERE " + COLUMN_NAME + " = '" + name + "';";
                db.execSQL(query);
            } else {
                String query = "UPDATE " + TABLE_PEOPLE + " SET " + COLUMN_WHEREABOUTS + " = '" + location + "' ," + COLUMN_STATE + " = '" + 0 + "' WHERE " + COLUMN_NAME + " = '" + name + "';";
                db.execSQL(query);
            }
            db.close();

        } else {
            Log.e("LclDB", "Can't update non existing Location record");
            throw new IllegalArgumentException("Can't update non existing Location record!");
        }
    }

    public void deleteRecord(byte[] tagID) {

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PEOPLE + " WHERE " + COLUMN_TAG_ID + " = '" + Arrays.toString(tagID) + "';");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String query = "CREATE TABLE " + TABLE_PEOPLE + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                COLUMN_NAME + " TEXT" + "," +
                COLUMN_NATIVE_HOUSE + " TEXT" + "," +
                COLUMN_STATE + " INTEGER" + "," +
                COLUMN_WHEREABOUTS + " TEXT" + "," +
                COLUMN_TAG_ID + " BLOB" + "," +
                COLUMN_BIO_IMAGE + " BLOB" +
                " )";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Delete current table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PEOPLE + ";");
        //Rebuild table
        onCreate(db);
    }


}
