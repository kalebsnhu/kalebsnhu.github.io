package com.example.modulethreeassignmentkalebward;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag for debugging
    private static final String TAG = "DatabaseHelper";

    // Database Version and Name
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "inventory_manager.db";

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_INVENTORY = "inventory";
    private static final String TABLE_NOTIFICATIONS = "notifications";

    // Common column names
    private static final String COLUMN_ID = "id";

    // Users Table Columns
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // Inventory Table Columns
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_USER_ID = "user_id";

    // Notifications Table Columns
    private static final String COLUMN_ENABLED = "enabled";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_THRESHOLD = "threshold";

    // Table Create Statements
    // Users table create statement
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS
            + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USERNAME + " TEXT UNIQUE,"
            + COLUMN_PASSWORD + " TEXT" + ")";

    // Inventory table create statement
    private static final String CREATE_TABLE_INVENTORY = "CREATE TABLE " + TABLE_INVENTORY
            + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NAME + " TEXT,"
            + COLUMN_QUANTITY + " TEXT,"
            + COLUMN_DATE + " TEXT,"
            + COLUMN_USER_ID + " INTEGER,"
            + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")" + ")";

    // Notifications table create statement
    private static final String CREATE_TABLE_NOTIFICATIONS = "CREATE TABLE " + TABLE_NOTIFICATIONS
            + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ENABLED + " INTEGER DEFAULT 0,"
            + COLUMN_PHONE + " TEXT,"
            + COLUMN_THRESHOLD + " INTEGER DEFAULT 5,"
            + COLUMN_USER_ID + " INTEGER,"
            + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creating required tables
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_INVENTORY);
        db.execSQL(CREATE_TABLE_NOTIFICATIONS);

        Log.d(TAG, "Database tables created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // On upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Create new tables
        onCreate(db);

        Log.d(TAG, "Database upgraded from version " + oldVersion + " to " + newVersion);
    }

    public long addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        long userId = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, username);
            values.put(COLUMN_PASSWORD, password);

            // Insert row
            userId = db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_IGNORE);

            // Create notification setting for new user
            if (userId != -1) {
                ContentValues notifValues = new ContentValues();
                notifValues.put(COLUMN_USER_ID, userId);
                notifValues.put(COLUMN_ENABLED, 0); // Disabled by default
                db.insert(TABLE_NOTIFICATIONS, null, notifValues);

                Log.d(TAG, "New user created: " + username);
            } else {
                Log.d(TAG, "Failed to create user: " + username + ". Username may already exist.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding user: " + e.getMessage());
        }

        return userId;
    }

    public long authenticateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        long userId = -1;

        try {
            String[] columns = {COLUMN_ID, COLUMN_PASSWORD};
            String selection = COLUMN_USERNAME + " = ?";
            String[] selectionArgs = {username};

            Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int passwordIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
                int idIndex = cursor.getColumnIndex(COLUMN_ID);

                if (passwordIndex != -1 && idIndex != -1) {
                    String storedPassword = cursor.getString(passwordIndex);

                    // Check if password matches
                    if (password.equals(storedPassword)) {
                        userId = cursor.getLong(idIndex);
                        Log.d(TAG, "User authenticated: " + username);
                    } else {
                        Log.d(TAG, "Authentication failed: Incorrect password for " + username);
                    }
                }
                cursor.close();
            } else {
                Log.d(TAG, "Authentication failed: User not found - " + username);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error authenticating user: " + e.getMessage());
        }

        return userId;
    }

    public long addInventoryItem(String name, String quantity, String date, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long itemId = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, name);
            values.put(COLUMN_QUANTITY, quantity);
            values.put(COLUMN_DATE, date);
            values.put(COLUMN_USER_ID, userId);

            // Insert row
            itemId = db.insert(TABLE_INVENTORY, null, values);
            Log.d(TAG, "Added inventory item: " + name + " for user ID: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Error adding inventory item: " + e.getMessage());
        }

        return itemId;
    }

    public List<DataItem> getAllInventoryItems(long userId) {
        List<DataItem> items = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_INVENTORY
                + " WHERE " + COLUMN_USER_ID + " = ?"
                + " ORDER BY " + COLUMN_ID + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});

            if (cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(COLUMN_NAME);
                int quantityIndex = cursor.getColumnIndex(COLUMN_QUANTITY);
                int dateIndex = cursor.getColumnIndex(COLUMN_DATE);
                int idIndex = cursor.getColumnIndex(COLUMN_ID);

                // Check if all columns exist
                if (nameIndex != -1 && quantityIndex != -1 && dateIndex != -1 && idIndex != -1) {
                    do {
                        DataItem item = new DataItem(
                                cursor.getString(nameIndex),
                                cursor.getString(quantityIndex),
                                cursor.getString(dateIndex));
                        item.setId(cursor.getLong(idIndex));
                        items.add(item);
                    } while (cursor.moveToNext());
                }
            }
            Log.d(TAG, "Retrieved " + items.size() + " inventory items for user ID: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Error getting inventory items: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return items;
    }

    public int updateInventoryItem(long itemId, String name, String quantity, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, name);
            values.put(COLUMN_QUANTITY, quantity);
            values.put(COLUMN_DATE, date);

            // Update row
            rowsAffected = db.update(TABLE_INVENTORY, values, COLUMN_ID + " = ?",
                    new String[]{String.valueOf(itemId)});
            Log.d(TAG, "Updated inventory item ID: " + itemId);
        } catch (Exception e) {
            Log.e(TAG, "Error updating inventory item: " + e.getMessage());
        }

        return rowsAffected;
    }

    public int deleteInventoryItem(long itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;

        try {
            rowsAffected = db.delete(TABLE_INVENTORY, COLUMN_ID + " = ?",
                    new String[]{String.valueOf(itemId)});
            Log.d(TAG, "Deleted inventory item ID: " + itemId);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting inventory item: " + e.getMessage());
        }

        return rowsAffected;
    }

    public int saveNotificationSettings(long userId, boolean enabled, String phoneNumber, int threshold) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ENABLED, enabled ? 1 : 0);
            values.put(COLUMN_PHONE, phoneNumber);
            values.put(COLUMN_THRESHOLD, threshold);

            // Check if settings already exist for this user
            Cursor cursor = db.query(TABLE_NOTIFICATIONS, null, COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)}, null, null, null);

            if (cursor != null && cursor.getCount() > 0) {
                // Update existing settings
                rowsAffected = db.update(TABLE_NOTIFICATIONS, values, COLUMN_USER_ID + " = ?",
                        new String[]{String.valueOf(userId)});
            } else {
                // Create new settings
                values.put(COLUMN_USER_ID, userId);
                rowsAffected = (db.insert(TABLE_NOTIFICATIONS, null, values) != -1) ? 1 : 0;
            }

            if (cursor != null) {
                cursor.close();
            }

            Log.d(TAG, "Saved notification settings for user ID: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Error saving notification settings: " + e.getMessage());
        }

        return rowsAffected;
    }

    public Object[] getNotificationSettings(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Object[] settings = null;

        try {
            Cursor cursor = db.query(TABLE_NOTIFICATIONS, null, COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int enabledIndex = cursor.getColumnIndex(COLUMN_ENABLED);
                int phoneIndex = cursor.getColumnIndex(COLUMN_PHONE);
                int thresholdIndex = cursor.getColumnIndex(COLUMN_THRESHOLD);

                if (enabledIndex != -1 && phoneIndex != -1 && thresholdIndex != -1) {
                    boolean enabled = cursor.getInt(enabledIndex) == 1;
                    String phone = cursor.getString(phoneIndex);
                    int threshold = cursor.getInt(thresholdIndex);

                    settings = new Object[]{enabled, phone, threshold};
                }
                cursor.close();
            }

            Log.d(TAG, "Retrieved notification settings for user ID: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Error getting notification settings: " + e.getMessage());
        }

        return settings;
    }

    public String getUsernameById(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String username = null;

        try {
            Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USERNAME},
                    COLUMN_ID + " = ?", new String[]{String.valueOf(userId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME);
                if (usernameIndex != -1) {
                    username = cursor.getString(usernameIndex);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting username: " + e.getMessage());
        }

        return username;
    }

    public List<String> getItemsBelowThreshold(long userId, int threshold) {
        List<String> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            // Find items where quantity (converted to integer) is below threshold
            String query = "SELECT " + COLUMN_NAME + " FROM " + TABLE_INVENTORY
                    + " WHERE " + COLUMN_USER_ID + " = ? AND CAST("
                    + COLUMN_QUANTITY + " AS INTEGER) <= ?";

            Cursor cursor = db.rawQuery(query, new String[]{
                    String.valueOf(userId),
                    String.valueOf(threshold)
            });

            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(COLUMN_NAME);
                if (nameIndex != -1) {
                    do {
                        items.add(cursor.getString(nameIndex));
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }

            Log.d(TAG, "Found " + items.size() + " items below threshold for user ID: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Error checking items below threshold: " + e.getMessage());
        }

        return items;
    }
}