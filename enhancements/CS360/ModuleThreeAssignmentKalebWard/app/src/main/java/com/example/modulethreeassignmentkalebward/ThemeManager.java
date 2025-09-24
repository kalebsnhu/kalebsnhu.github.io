package com.example.modulethreeassignmentkalebward;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

/*
 * Class: ThemeManager:
 * Description: This class is used for the theme manager of the Android application, this allows for
 * themes to be saved and to be set to light or dark theme. This class also allows to receive and GET
 * the saved theme and the theme selected previously. We are able to switch between dark or light
 * mode, you can also select the SYSTEM theme which means when the phone is in dark mode it will grab
 * the dark theme and when it's in light mode it will grab light mode for the application
 */

public class ThemeManager {
    private static final String PREF_NAME = "user_session";
    private static final String THEME_PREF = "app_theme";
    private static final String THEME_LIGHT = "light";
    private static final String THEME_DARK = "dark";
    private static final String THEME_SYSTEM = "system";

    public static void applyTheme(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String theme = preferences.getString(THEME_PREF, THEME_SYSTEM);

        switch (theme) {
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public static void saveTheme(Context context, String theme) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(THEME_PREF, theme);
        editor.apply();

        applyTheme(context);
    }

    public static String getCurrentTheme(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(THEME_PREF, THEME_SYSTEM);
    }
}
