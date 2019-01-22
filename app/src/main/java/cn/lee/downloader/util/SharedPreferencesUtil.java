package cn.lee.downloader.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * @author Lee
 * @Title: {Title}
 * @Description:{}
 * @date 2017/9/25
 */
public class SharedPreferencesUtil {
    private SharedPreferences mSharedPreferences;

    public SharedPreferencesUtil(Context context) {
        this.mSharedPreferences = null;
        if (context != null) {
            this.mSharedPreferences = context.getSharedPreferences("cn.lee.download", 0);
        }
    }

    public void putString(String str, String str2) {
        if (this.mSharedPreferences != null) {
            Editor edit = this.mSharedPreferences.edit();
            edit.putString(str, str2);
            edit.commit();
        }
    }

    public String getString(String str) {
        if (this.mSharedPreferences == null) {
            return null;
        }
        return this.mSharedPreferences.getString(str, "");
    }

    public void putInt(String str, int i) {
        if (this.mSharedPreferences != null) {
            Editor edit = this.mSharedPreferences.edit();
            edit.putInt(str, i);
            edit.commit();
        }
    }

    public int getInt(String str, int i) {
        return this.mSharedPreferences == null ? i : this.mSharedPreferences.getInt(str, i);
    }

    public void putBoolean(String str, boolean z) {
        if (this.mSharedPreferences != null) {
            Editor edit = this.mSharedPreferences.edit();
            edit.putBoolean(str, z);
            edit.commit();
        }
    }

    public boolean getBoolean(String str, boolean defaultBoolean) {
        if (this.mSharedPreferences != null) {
            return this.mSharedPreferences.getBoolean(str, defaultBoolean);
        }
        return false;
    }

    public void remove(String str) {
        if (this.mSharedPreferences != null) {
            Editor edit = mSharedPreferences.edit();
            edit.remove(str);
            edit.commit();
        }
    }

    public void setOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        this.mSharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

}
