package com.example.myapplication.Profile;



import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//1
public class ProfileDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FitnessApp.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_NAME = "UserProfile";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_WEIGHT = "weight";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_SEX = "sex";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_WEIGHT + " REAL, " +
                    COLUMN_AGE + " INTEGER, " +
                    COLUMN_HEIGHT + " REAL, " +
                    COLUMN_IMAGE + " BLOB, " +
                    COLUMN_SEX + " TEXT)";

    public ProfileDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public long insertUserProfile(UserProfile userProfile) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, userProfile.getName());
        values.put(COLUMN_WEIGHT, userProfile.getWeight());
        values.put(COLUMN_AGE, userProfile.getAge());
        values.put(COLUMN_HEIGHT, userProfile.getHeight());
        values.put(COLUMN_IMAGE, userProfile.getImage());
        values.put(COLUMN_SEX, userProfile.getSex());

        long newRowId = db.insert(TABLE_NAME, null, values);
        db.close(); // Close the database
        return newRowId;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public UserProfile getUserProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " DESC LIMIT 1";
        System.out.println(selectQuery);
        Cursor cursor = db.rawQuery(selectQuery, null);

        UserProfile userProfile = null;
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
            float weight = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT));
            int age = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AGE));
            float height = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_HEIGHT));
            byte[] image = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_IMAGE));
            String sex = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SEX));

            userProfile = new UserProfile(name, weight, age, height, image,sex);
        }
        cursor.close();
        db.close();
        return userProfile;
    }
}

