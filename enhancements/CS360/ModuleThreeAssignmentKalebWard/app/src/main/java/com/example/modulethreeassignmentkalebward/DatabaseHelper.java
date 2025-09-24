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

    private static final String TAG = "DatabaseHelper";

    private static final int DATABASE_VERSION = 2; 
    private static final String DATABASE_NAME = "inventory_manager.db";

    private static final String TABLE_USERS = "users";
    private static final String TABLE_INVENTORY = "inventory";
    private static final String TABLE_NOTIFICATIONS = "notifications";

    private static final String COLUMN_ID = "id";

    
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_DISPLAY_NAME = "display_name"; 
    private static final String COLUMN_EMAIL = "email"; 
    private static final String COLUMN_SECURITY_QUESTION = "security_question"; 
    private static final String COLUMN_SECURITY_ANSWER = "security_answer"; 
    private static final String COLUMN_THEME_PREFERENCE = "theme_preference"; 
    private static final String COLUMN_CREATED_DATE = "created_date"; 
    private static final String COLUMN_LAST_LOGIN = "last_login"; 

    
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_USER_ID = "user_id";

    
    private static final String COLUMN_ENABLED = "enabled";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_THRESHOLD = "threshold";

    
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS
            + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USERNAME + " TEXT UNIQUE,"
            + COLUMN_PASSWORD + " TEXT,"
            + COLUMN_DISPLAY_NAME + " TEXT,"
            + COLUMN_EMAIL + " TEXT,"
            + COLUMN_SECURITY_QUESTION + " TEXT,"
            + COLUMN_SECURITY_ANSWER + " TEXT,"
            + COLUMN_THEME_PREFERENCE + " TEXT DEFAULT 'light',"
            + COLUMN_CREATED_DATE + " TEXT,"
            + COLUMN_LAST_LOGIN + " TEXT" + ")";

    private static final String CREATE_TABLE_INVENTORY = "CREATE TABLE " + TABLE_INVENTORY
            + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NAME + " TEXT,"
            + COLUMN_QUANTITY + " TEXT,"
            + COLUMN_DATE + " TEXT,"
            + COLUMN_USER_ID + " INTEGER,"
            + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")" + ")";

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
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_INVENTORY);
        db.execSQL(CREATE_TABLE_NOTIFICATIONS);

        Log.d(TAG, "Database tables created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_DISPLAY_NAME + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_EMAIL + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_SECURITY_QUESTION + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_SECURITY_ANSWER + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_THEME_PREFERENCE + " TEXT DEFAULT 'light'");
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_CREATED_DATE + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_LAST_LOGIN + " TEXT");

                Log.d(TAG, "Database upgraded to version " + newVersion);
            } catch (Exception e) {
                Log.e(TAG, "Error upgrading database: " + e.getMessage());
                
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
                onCreate(db);
            }
        }
    }

    public long addUser(String username, String password) {
        return addUser(username, password, null, null, null, null, "light");
    }

    public long addUser(String username, String password, String displayName, String email,
                        String securityQuestion, String securityAnswer, String themePreference) {
        SQLiteDatabase db = this.getWritableDatabase();
        long userId = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, username);
            values.put(COLUMN_PASSWORD, password);
            values.put(COLUMN_DISPLAY_NAME, displayName != null ? displayName : username);
            values.put(COLUMN_EMAIL, email);
            values.put(COLUMN_SECURITY_QUESTION, securityQuestion);
            values.put(COLUMN_SECURITY_ANSWER, securityAnswer);
            values.put(COLUMN_THEME_PREFERENCE, themePreference != null ? themePreference : "light");
            values.put(COLUMN_CREATED_DATE, getCurrentDateTime());
            values.put(COLUMN_LAST_LOGIN, getCurrentDateTime());

            userId = db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_IGNORE);

            if (userId != -1) {
                ContentValues notifValues = new ContentValues();
                notifValues.put(COLUMN_USER_ID, userId);
                notifValues.put(COLUMN_ENABLED, 0);
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
        SQLiteDatabase db = this.getWritableDatabase();
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

                    if (password.equals(storedPassword)) {
                        userId = cursor.getLong(idIndex);

                        
                        ContentValues updateValues = new ContentValues();
                        updateValues.put(COLUMN_LAST_LOGIN, getCurrentDateTime());
                        db.update(TABLE_USERS, updateValues, COLUMN_ID + " = ?",
                                new String[]{String.valueOf(userId)});

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

    public boolean updatePassword(long userId, String oldPassword, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            
            Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_PASSWORD},
                    COLUMN_ID + " = ?", new String[]{String.valueOf(userId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int passwordIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
                if (passwordIndex != -1) {
                    String currentPassword = cursor.getString(passwordIndex);
                    cursor.close();

                    if (!oldPassword.equals(currentPassword)) {
                        Log.d(TAG, "Password update failed: Incorrect old password");
                        return false;
                    }

                    
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_PASSWORD, newPassword);

                    int rowsAffected = db.update(TABLE_USERS, values, COLUMN_ID + " = ?",
                            new String[]{String.valueOf(userId)});

                    Log.d(TAG, "Password updated for user ID: " + userId);
                    return rowsAffected > 0;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating password: " + e.getMessage());
        }

        return false;
    }

    public boolean resetPasswordWithSecurity(String username, String securityAnswer, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            String[] columns = {COLUMN_ID, COLUMN_SECURITY_ANSWER};
            String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_SECURITY_ANSWER + " IS NOT NULL";
            String[] selectionArgs = {username};

            Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(COLUMN_ID);
                int answerIndex = cursor.getColumnIndex(COLUMN_SECURITY_ANSWER);

                if (idIndex != -1 && answerIndex != -1) {
                    long userId = cursor.getLong(idIndex);
                    String storedAnswer = cursor.getString(answerIndex);
                    cursor.close();

                    if (securityAnswer.equalsIgnoreCase(storedAnswer)) {
                        ContentValues values = new ContentValues();
                        values.put(COLUMN_PASSWORD, newPassword);

                        int rowsAffected = db.update(TABLE_USERS, values, COLUMN_ID + " = ?",
                                new String[]{String.valueOf(userId)});

                        Log.d(TAG, "Password reset for user: " + username);
                        return rowsAffected > 0;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error resetting password: " + e.getMessage());
        }

        return false;
    }

    public UserProfile getUserProfile(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String[] columns = {COLUMN_USERNAME, COLUMN_DISPLAY_NAME, COLUMN_EMAIL,
                    COLUMN_SECURITY_QUESTION, COLUMN_THEME_PREFERENCE,
                    COLUMN_CREATED_DATE, COLUMN_LAST_LOGIN};
            String selection = COLUMN_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId)};

            Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                UserProfile profile = new UserProfile();
                profile.userId = userId;
                profile.username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
                profile.displayName = cursor.getString(cursor.getColumnIndex(COLUMN_DISPLAY_NAME));
                profile.email = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL));
                profile.securityQuestion = cursor.getString(cursor.getColumnIndex(COLUMN_SECURITY_QUESTION));
                profile.themePreference = cursor.getString(cursor.getColumnIndex(COLUMN_THEME_PREFERENCE));
                profile.createdDate = cursor.getString(cursor.getColumnIndex(COLUMN_CREATED_DATE));
                profile.lastLogin = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_LOGIN));

                cursor.close();
                return profile;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user profile: " + e.getMessage());
        }

        return null;
    }

    public boolean updateUserProfile(long userId, String displayName, String email,
                                     String securityQuestion, String securityAnswer, String themePreference) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            if (displayName != null) values.put(COLUMN_DISPLAY_NAME, displayName);
            if (email != null) values.put(COLUMN_EMAIL, email);
            if (securityQuestion != null) values.put(COLUMN_SECURITY_QUESTION, securityQuestion);
            if (securityAnswer != null) values.put(COLUMN_SECURITY_ANSWER, securityAnswer);
            if (themePreference != null) values.put(COLUMN_THEME_PREFERENCE, themePreference);

            int rowsAffected = db.update(TABLE_USERS, values, COLUMN_ID + " = ?",
                    new String[]{String.valueOf(userId)});

            Log.d(TAG, "User profile updated for ID: " + userId);
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating user profile: " + e.getMessage());
        }

        return false;
    }

    public String getSecurityQuestion(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String[] columns = {COLUMN_SECURITY_QUESTION};
            String selection = COLUMN_USERNAME + " = ?";
            String[] selectionArgs = {username};

            Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int questionIndex = cursor.getColumnIndex(COLUMN_SECURITY_QUESTION);
                if (questionIndex != -1) {
                    String question = cursor.getString(questionIndex);
                    cursor.close();
                    return question;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting security question: " + e.getMessage());
        }

        return null;
    }

    private String getCurrentDateTime() {
        return String.valueOf(System.currentTimeMillis());
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

            Cursor cursor = db.query(TABLE_NOTIFICATIONS, null, COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)}, null, null, null);

            if (cursor != null && cursor.getCount() > 0) {
                rowsAffected = db.update(TABLE_NOTIFICATIONS, values, COLUMN_USER_ID + " = ?",
                        new String[]{String.valueOf(userId)});
            } else {
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

    
    public static class UserProfile {
        public long userId;
        public String username;
        public String displayName;
        public String email;
        public String securityQuestion;
        public String themePreference;
        public String createdDate;
        public String lastLogin;
    }
}