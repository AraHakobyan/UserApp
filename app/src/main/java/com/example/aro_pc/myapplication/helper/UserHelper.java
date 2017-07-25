package com.example.aro_pc.myapplication.helper;

import com.example.aro_pc.myapplication.model.UserModel;

/**
 * Created by Aro-PC on 7/24/2017.
 */

public class UserHelper {
    public static volatile UserHelper instance;
    public static synchronized UserHelper getInstance(){
        if (instance == null){
            instance = new UserHelper();
        }
        return instance;
    }

    private UserModel userModel;

    public UserModel getUserModel() {
        if (userModel == null){
            userModel = new UserModel();
        }
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }
}
