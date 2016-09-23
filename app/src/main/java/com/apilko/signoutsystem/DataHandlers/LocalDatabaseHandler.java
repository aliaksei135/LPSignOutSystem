package com.apilko.signoutsystem.DataHandlers;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Keep;
import android.util.Log;

@Keep
public class LocalDatabaseHandler extends SQLiteOpenHelper {

    public static final int GROVE_VISITOR = 1;
    public static final int FIELD_VISITOR = 2;
    public static final int RECKITT_VISITOR = 3;
    public static final int SCHOOL_VISITOR = 4;
    public static final int FRYER_VISITOR = 5;

    //SQL table for each year
    private static final String TABLE_Y13 = "year13";
    private static final String TABLE_Y12 = "year12";
    private static final String TABLE_Y11 = "year11";
    private static final String TABLE_Y10 = "year10";
    private static final String TABLE_Y9 = "year9";

    //Fryer only SQL tables
    private static final String TABLE_Y8 = "year8";
    private static final String TABLE_Y7 = "year7";

    //Grove visitor tables
    private static final String TABLE_GROVE_VISITOR = "visitorGrove";
    private static final String TABLE_FIELD_VISITOR = "visitorField";
    private static final String TABLE_RECKITT_VISITOR = "visitorReckitt";
    private static final String TABLE_FRYER_VISITOR = "visitorFryer";

    //Same columns for all tables
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

        //This is weird as onCreate should be called automagically
        onCreate(getWritableDatabase());
    }

    public static LocalDatabaseHandler getInstance(Context context) {

        if (ourInstance == null) {
            ourInstance = new LocalDatabaseHandler(context);
            return ourInstance;
        } else {
            return ourInstance;
        }
    }

    public byte[] getBioImage(long id, int year) {

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT  FROM " + getYearTable(year) + " WHERE " + COLUMN_ID + " = " + id + ";", null);
        db.close();
        cursor.close();
        //Column 7 is the Bio image which we want (cursor is zero indexed)
        return cursor.getBlob(6);
    }

    public long getRecordNum(int year) {

        SQLiteDatabase db = getReadableDatabase();
        long recordNum = DatabaseUtils.longForQuery(db, "SELECT COUNT (*) FROM " + getYearTable(year) + ";", null);
        db.close();
        return recordNum;
    }

    public String getName(long id, int year) {

        SQLiteDatabase db = getReadableDatabase();
        String name = DatabaseUtils.stringForQuery(db, "SELECT " + COLUMN_NAME + " FROM " + getYearTable(year) + " WHERE " + COLUMN_ID + " = " + id + ";", null);
        db.close();
        return name;
    }

    public String getWhereabouts(long id, int year) {

        SQLiteDatabase db = getReadableDatabase();
        String whereabouts = DatabaseUtils.stringForQuery(db, "SELECT " + COLUMN_WHEREABOUTS + " FROM " + getYearTable(year) + " WHERE " + COLUMN_ID + " = '" + id + "';", null);
        db.close();
        return whereabouts;
    }

    public String getWhereabouts(String name, int year) {

        SQLiteDatabase db = getReadableDatabase();
        String whereabouts = DatabaseUtils.stringForQuery(db, "SELECT " + COLUMN_WHEREABOUTS + " FROM " + getYearTable(year) + " WHERE " + COLUMN_NAME + " = '" + name + "';", null);
        db.close();
        return whereabouts;
    }

    public void addNewRecord(String name, String house, int year, byte[] ID, boolean isNFC) {
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
        String table = getYearTable(year);
        SQLiteDatabase db = getWritableDatabase();
        db.insert(table, null, values);
        db.close();
    }

    public void updateID(String name, Object newID, boolean isNFC, int year) throws IllegalArgumentException {

        if (findRecord(name, null, year)) {
            SQLiteDatabase db = getWritableDatabase();
            String table = getYearTable(year);
            if (isNFC) {
                String query = "UPDATE " + table + " SET " + COLUMN_TAG_ID + newID + " WHERE " + COLUMN_NAME + " = '" + name + "';";
                db.execSQL(query);
            } else {
                String query = "UPDATE " + table + " SET " + COLUMN_BIO_IMAGE + newID + " WHERE " + COLUMN_NAME + " = '" + name + "';";
                db.execSQL(query);
            }
            db.close();
        } else {
            Log.e("LclDB", "Can't update non existing ID record");
            throw new IllegalArgumentException("Can't update non existing ID record");
        }
    }

    private boolean findRecord(String field, Object key, int year) {

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor;
        String table = getYearTable(year);
        if (key == null) {
            String query = "SELECT * FROM " + table + " WHERE " + COLUMN_NAME + " = '" + field + "';";
            cursor = db.rawQuery(query, null);
        } else {
            String query = "SELECT * FROM " + table + " WHERE " + field + " = '" + key + "';";
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

    public void updateLocation(String name, String location, int year) throws IllegalArgumentException {

        if (findRecord(name, null, year)) {
            SQLiteDatabase db = getWritableDatabase();
            String table = getYearTable(year);

            if (location.equals("Study Period") || location.equals("Signed In")) {
                String query = "UPDATE " + table + " SET " + COLUMN_WHEREABOUTS + " = '" + location + "' ," + COLUMN_STATE + " = '" + 1 + "' WHERE " + COLUMN_NAME + " = '" + name + "';";
                db.execSQL(query);
            } else {
                String query = "UPDATE " + table + " SET " + COLUMN_WHEREABOUTS + " = '" + location + "' ," + COLUMN_STATE + " = '" + 0 + "' WHERE " + COLUMN_NAME + " = '" + name + "';";
                db.execSQL(query);
            }
            db.close();

        } else {
            Log.e("LclDB", "Can't update non existing Location record");
            throw new IllegalArgumentException("Can't update non existing Location record!");
        }
    }

    public void deleteRecord(byte[] tagID, int year) {

        SQLiteDatabase db = getWritableDatabase();
        String table = getYearTable(year);
        db.execSQL("DELETE FROM " + table + " WHERE " + COLUMN_TAG_ID + " = '" + tagID + "';");
    }

    private String getYearTable(int year) {

        switch (year) {
            case 7:
                return TABLE_Y7;
            case 8:
                return TABLE_Y8;
            case 9:
                return TABLE_Y9;
            case 10:
                return TABLE_Y10;
            case 11:
                return TABLE_Y11;
            case 12:
                return TABLE_Y12;
            case 13:
                return TABLE_Y13;
            case GROVE_VISITOR:
                return TABLE_GROVE_VISITOR;
            case FIELD_VISITOR:
                return TABLE_FIELD_VISITOR;
            case RECKITT_VISITOR:
                return TABLE_RECKITT_VISITOR;
            case FRYER_VISITOR:
                return TABLE_FRYER_VISITOR;
            default:
                return TABLE_Y13;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String query;

//DEBUG only table
//        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_PEOPLE + "(" +
//                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
//                COLUMN_NAME + " TEXT" + "," +
//                COLUMN_NATIVE_HOUSE + " TEXT" + "," +
//                COLUMN_STATE + " INTEGER" + "," +
//                COLUMN_WHEREABOUTS + " TEXT" + "," +
//                COLUMN_TAG_ID + " BLOB" + "," +
//                COLUMN_BIO_IMAGE + " BLOB" +
//                " )";
//
//        db.execSQL(query);

        //Fryer only tables
/*        query = "CREATE TABLE IF NOT EXISTS " + TABLE_Y7 + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                COLUMN_NAME + " TEXT" + "," +
                COLUMN_NATIVE_HOUSE + " TEXT" + "," +
                COLUMN_STATE + " INTEGER" + "," +
                COLUMN_WHEREABOUTS + " TEXT" + "," +
                COLUMN_TAG_ID + " BLOB" + "," +
                COLUMN_BIO_IMAGE + " BLOB" +
                " )";

        db.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + TABLE_Y8 + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                COLUMN_NAME + " TEXT" + "," +
                COLUMN_NATIVE_HOUSE + " TEXT" + "," +
                COLUMN_STATE + " INTEGER" + "," +
                COLUMN_WHEREABOUTS + " TEXT" + "," +
                COLUMN_TAG_ID + " BLOB" + "," +
                COLUMN_BIO_IMAGE + " BLOB" +
                " )";

        db.execSQL(query);*/

        query = "CREATE TABLE IF NOT EXISTS " + TABLE_Y9 + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                COLUMN_NAME + " TEXT" + "," +
                COLUMN_NATIVE_HOUSE + " TEXT" + "," +
                COLUMN_STATE + " INTEGER" + "," +
                COLUMN_WHEREABOUTS + " TEXT" + "," +
                COLUMN_TAG_ID + " BLOB" + "," +
                COLUMN_BIO_IMAGE + " BLOB" +
                " )";

        db.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + TABLE_Y10 + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                COLUMN_NAME + " TEXT" + "," +
                COLUMN_NATIVE_HOUSE + " TEXT" + "," +
                COLUMN_STATE + " INTEGER" + "," +
                COLUMN_WHEREABOUTS + " TEXT" + "," +
                COLUMN_TAG_ID + " BLOB" + "," +
                COLUMN_BIO_IMAGE + " BLOB" +
                " )";

        db.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + TABLE_Y11 + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                COLUMN_NAME + " TEXT" + "," +
                COLUMN_NATIVE_HOUSE + " TEXT" + "," +
                COLUMN_STATE + " INTEGER" + "," +
                COLUMN_WHEREABOUTS + " TEXT" + "," +
                COLUMN_TAG_ID + " BLOB" + "," +
                COLUMN_BIO_IMAGE + " BLOB" +
                " )";

        db.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + TABLE_Y12 + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                COLUMN_NAME + " TEXT" + "," +
                COLUMN_NATIVE_HOUSE + " TEXT" + "," +
                COLUMN_STATE + " INTEGER" + "," +
                COLUMN_WHEREABOUTS + " TEXT" + "," +
                COLUMN_TAG_ID + " BLOB" + "," +
                COLUMN_BIO_IMAGE + " BLOB" +
                " )";

        db.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + TABLE_Y13 + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                COLUMN_NAME + " TEXT" + "," +
                COLUMN_NATIVE_HOUSE + " TEXT" + "," +
                COLUMN_STATE + " INTEGER" + "," +
                COLUMN_WHEREABOUTS + " TEXT" + "," +
                COLUMN_TAG_ID + " BLOB" + "," +
                COLUMN_BIO_IMAGE + " BLOB" +
                " )";

        db.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + TABLE_GROVE_VISITOR + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                COLUMN_NAME + " TEXT" + "," +
                COLUMN_NATIVE_HOUSE + " TEXT" + "," +
                COLUMN_STATE + " INTEGER" + "," +
                COLUMN_WHEREABOUTS + " TEXT" + "," +
                COLUMN_TAG_ID + " BLOB" + "," +
                COLUMN_BIO_IMAGE + " BLOB" +
                " )";

        db.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + TABLE_FIELD_VISITOR + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                COLUMN_NAME + " TEXT" + "," +
                COLUMN_NATIVE_HOUSE + " TEXT" + "," +
                COLUMN_STATE + " INTEGER" + "," +
                COLUMN_WHEREABOUTS + " TEXT" + "," +
                COLUMN_TAG_ID + " BLOB" + "," +
                COLUMN_BIO_IMAGE + " BLOB" +
                " )";

        db.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + TABLE_RECKITT_VISITOR + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                COLUMN_NAME + " TEXT" + "," +
                COLUMN_NATIVE_HOUSE + " TEXT" + "," +
                COLUMN_STATE + " INTEGER" + "," +
                COLUMN_WHEREABOUTS + " TEXT" + "," +
                COLUMN_TAG_ID + " BLOB" + "," +
                COLUMN_BIO_IMAGE + " BLOB" +
                " )";

        db.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + TABLE_FRYER_VISITOR + "(" +
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
        //Delete current tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Y7 + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Y8 + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Y9 + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Y10 + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Y11 + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Y12 + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Y13 + ";");
        //Rebuild tables
        onCreate(db);
    }


}
