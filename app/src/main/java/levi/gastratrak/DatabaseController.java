package levi.gastratrak;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

class DatabaseController extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 10;

    // Database Name
    private static final String DATABASE_NAME = "pain_and_food_diary";

    // Contacts table name
    private static final String TABLE_PAIN = "pain";
    private static final String TABLE_FOOD = "food";
    private static final String TABLE_STOOL = "stool";

    // Pain Table Columns names
    private static final String KEY_PAIN_ID = "id";
    private static final String KEY_PAIN_TIME = "time"; //long - standard time format(milliseconds since 1970)
    private static final String KEY_PAIN_TOTAL = "TotalPain";
    private static final String KEY_PAIN_OTHER = "OtherPain";
    private static final String KEY_PAIN_UPPER = "UpperStomachPain";
    private static final String KEY_PAIN_LOWER = "LowerStomachPain";

    // Food Table Columns names
    private static final String KEY_FOOD_ID = "id";
    private static final String KEY_FOOD_TIME = "time"; //long - standard time format(milliseconds since 1970)
    private static final String KEY_FOOD_ITEM = "oldItem";

    // Stool Table Columns names
    private static final String KEY_STOOL_ID = "id";
    private static final String KEY_STOOL_TIME = "time"; //long - standard time format(milliseconds since 1970)
    private static final String KEY_STOOL_CONSISTENCY = "consistency";
    private static final String KEY_STOOL_WETNESS = "wetness";
    private static final String KEY_STOOL_DIFFICULTY = "difficulty";


    DatabaseController(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PAIN_TABLE =
                "CREATE TABLE " + TABLE_PAIN + "(" +
                        KEY_PAIN_ID + " INTEGER PRIMARY KEY," +
                        KEY_PAIN_TIME + " INTEGER, " +
                        KEY_PAIN_TOTAL + " INTEGER, " +
                        KEY_PAIN_OTHER + " INTEGER, " +
                        KEY_PAIN_UPPER + " INTEGER, " +
                        KEY_PAIN_LOWER + " INTEGER )";

        String CREATE_FOOD_TABLE =
                "CREATE TABLE " + TABLE_FOOD + " ( " +
                        KEY_FOOD_ID + " INTEGER PRIMARY KEY, " +
                        KEY_FOOD_TIME + " INTEGER, " +
                        KEY_FOOD_ITEM + " TEXT )";


        String CREATE_STOOL_TABLE =
                "CREATE TABLE " + TABLE_STOOL + "(" +
                        KEY_STOOL_ID + " INTEGER PRIMARY KEY, " +
                        KEY_STOOL_TIME + " INTEGER, " +
                        KEY_STOOL_CONSISTENCY + " INTEGER, " +
                        KEY_STOOL_WETNESS + " INTEGER, " +
                        KEY_STOOL_DIFFICULTY + " INTEGER )";

        db.execSQL(CREATE_PAIN_TABLE);
        db.execSQL(CREATE_FOOD_TABLE);
        db.execSQL(CREATE_STOOL_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Not currently saving old database on upgrade. Had some issues with debugging entries,
        //and being able to wipe the database by incrementing the version was helpful for quick testing

        // TODO remove dropping tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOOL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAIN);
        onCreate(db);
    }

    boolean addFoodItem(FoodItem item) {
        if(!item.getFoodItem().replaceAll("\\s","").isEmpty()) {
            try (SQLiteDatabase db = this.getWritableDatabase()) {
                ContentValues values = new ContentValues();
                values.put(KEY_FOOD_TIME, item.getFoodTime().getTimeInMillis()); // Time Consumed
                values.put(KEY_FOOD_ITEM, item.getFoodItem()); // Food Name
                db.insert(TABLE_FOOD, null, values);
            }
            return true;
        }else{
            return false;
        }
    }

    void removeFoodItem(FoodItem item) {

        try(SQLiteDatabase db = this.getWritableDatabase()) {
            db.delete(TABLE_FOOD,
                    KEY_FOOD_ITEM + " = "+item.getFoodItem()+" and " +
                    KEY_PAIN_TIME +" = "+String.valueOf(item.getFoodTime().getTimeInMillis())+" ", new String[]{});
        }
    }

    ArrayList<FoodItem> getAllFoodItems() {
        ArrayList<FoodItem> result = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_FOOD;
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            try (Cursor cursor = db.rawQuery(selectQuery, null)) {
                if (cursor.moveToFirst()) {
                    do {
                        Calendar time = Calendar.getInstance();
                        time.setTimeInMillis(cursor.getLong(1));
                        FoodItem item = new FoodItem(cursor.getString(2), time);
                        result.add(item);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            Log.e("all food items", "" + e);
        }

        return result;
    }

    ArrayList<FoodItem> getDateFoodItems(Calendar startCalendar, Calendar endCalendar) {
        ArrayList<FoodItem> result = new ArrayList<>();
        long startTimeLong = startCalendar.getTimeInMillis();
        long endTimeLong = endCalendar.getTimeInMillis();
        String selectQuery = "SELECT  * FROM " + TABLE_FOOD + " WHERE " + KEY_FOOD_TIME + " > " +
                startTimeLong + " AND " + KEY_FOOD_TIME + " < " + endTimeLong;

        try (SQLiteDatabase db = this.getReadableDatabase()) {
            try (Cursor cursor = db.rawQuery(selectQuery, null)) {
                if (cursor.moveToFirst()) {
                    do {
                        Calendar time = Calendar.getInstance();
                        time.setTimeInMillis(cursor.getLong(1));
                        FoodItem item = new FoodItem(cursor.getString(2), time);
                        result.add(item);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            Log.e("date food items", "" + e);
        }
        Collections.sort(result, (foodOne, foodTwo) -> {
            long t1 = foodOne.getFoodTime().getTimeInMillis();
            long t2 = foodTwo.getFoodTime().getTimeInMillis();
            return ((int) (t1 - t2));
        });
        return result;
    }


    void addPainRecording(PainItem item) {
        try(SQLiteDatabase db = this.getWritableDatabase()) {
            int[] painArray = item.getPainLevel();
            ContentValues values = new ContentValues();
            values.put(KEY_PAIN_TIME, item.getPainTime().getTime()); // Time recorded
            values.put(KEY_PAIN_TOTAL, painArray[0]);
            values.put(KEY_PAIN_OTHER, painArray[1]);
            values.put(KEY_PAIN_UPPER, painArray[2]);
            values.put(KEY_PAIN_LOWER, painArray[3]);
            db.insert(TABLE_PAIN, null, values);
        }
    }

    ArrayList<PainItem> getAllPainItems() {
        ArrayList<PainItem> result = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_PAIN;
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            try (Cursor cursor = db.rawQuery(selectQuery, null)) {
                if (cursor.moveToFirst()) {
                    do {
                        int[] painArray = new int[]{cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5)};
                        PainItem item = new PainItem(painArray, new Time(cursor.getLong(1)));
                        result.add(item);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            Log.e("all pain items", "" + e);
        }
        return result;
    }

    public ArrayList<PainItem> getDatePainItems(Calendar startCalendar, Calendar endCalendar) {
        ArrayList<PainItem> result = new ArrayList<>();
        Long startTimeLong = startCalendar.getTimeInMillis();
        Long endTimeLong = endCalendar.getTimeInMillis();
        String selectQuery = "SELECT  * FROM " + TABLE_PAIN + "WHERE " + KEY_PAIN_TIME + " > " +
                startTimeLong + " AND " + KEY_PAIN_TIME + " < " + endTimeLong;

        try (SQLiteDatabase db = this.getReadableDatabase()) {
            try (Cursor cursor = db.rawQuery(selectQuery, null)) {
                if (cursor.moveToFirst()) {
                    do {
                        int[] painArray = new int[]{cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5)};
                        PainItem item = new PainItem(painArray, new Time(cursor.getLong(1)));
                        result.add(item);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            Log.e("date pain items", "" + e);
        }

        return result;
    }


    void addStoolRecording(StoolItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        int[] stoolArray = item.getStoolArray();
        ContentValues values = new ContentValues();

        values.put(KEY_STOOL_CONSISTENCY, stoolArray[0]);
        values.put(KEY_STOOL_WETNESS, stoolArray[1]);
        values.put(KEY_STOOL_DIFFICULTY, stoolArray[2]);
        values.put(KEY_STOOL_TIME, item.getStoolTime().getTime());
        db.insert(TABLE_STOOL, null, values);
        db.close();
    }

    public ArrayList<StoolItem> getAllStoolItems() {
        ArrayList<StoolItem> result = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_STOOL;
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            try (Cursor cursor = db.rawQuery(selectQuery, null)) {
                if (cursor.moveToFirst()) {
                    do {
                        int[] stoolArray = new int[]{cursor.getInt(2), cursor.getInt(3), cursor.getInt(4)};
                        StoolItem item = new StoolItem(stoolArray, new Time(cursor.getLong(1)));
                        result.add(item);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            Log.e("all stool items", "" + e);
        }

        return result;
    }

    public ArrayList<StoolItem> getDateStoolItems(Calendar startCalendar, Calendar endCalendar) {
        ArrayList<StoolItem> result = new ArrayList<>();
        Long startTimeLong = startCalendar.getTimeInMillis();
        Long endTimeLong = endCalendar.getTimeInMillis();
        String selectQuery = "SELECT  * FROM " + TABLE_STOOL + "WHERE " + KEY_STOOL_TIME + " > " +
                startTimeLong + " AND " + KEY_STOOL_TIME + " < " + endTimeLong;
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            try (Cursor cursor = db.rawQuery(selectQuery, null)) {
                if (cursor.moveToFirst()) {
                    do {
                        int[] stoolArray = new int[]{cursor.getInt(2), cursor.getInt(3), cursor.getInt(4)};
                        StoolItem item = new StoolItem(stoolArray, new Time(cursor.getLong(1)));
                        result.add(item);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            Log.e("date stool items", "" + e);
        }

        return result;
    }
}