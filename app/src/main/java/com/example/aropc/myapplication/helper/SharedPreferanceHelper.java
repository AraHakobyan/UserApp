package com.example.aropc.myapplication.helper;

import android.content.SharedPreferences;

/**
 * Created by Aro-PC on 7/27/2017.
 */

public class SharedPreferanceHelper {
    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public void setSharedPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public SharedPreferences.Editor getEditor() {
        return editor;
    }

    public void setEditor(SharedPreferences.Editor editor) {
        this.editor = editor;
    }

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    public static volatile SharedPreferanceHelper instance;
    public static synchronized SharedPreferanceHelper getInstance(){
        if (instance == null){
            instance = new SharedPreferanceHelper();


        }
        return instance;
    }
    public void saveString(String key, String value){
        editor.putString(key, value);
        editor.commit();
    }

    public String getString(String key, String def){
        return sharedPreferences.getString(key,def);
    }

    public void saveBoolean(String key, boolean value){
        editor.putBoolean(key, value);
        editor.commit();
    }
    public Boolean getBoolean(String key, boolean def){
        return sharedPreferences.getBoolean(key,def);
    }
}
