package com.example.minxuan.socialprojectv2;

import android.util.Log;

/**
 * Created by MinXuan on 2017/6/7.
 */

public class AccountHandler {

    public static String adminPassword = "ciisciis";
    public static String account;
    public static String password;
    public static String firstName;
    public static String lastName;
    public static String phoneNumber;
    public static String email;
    public static String IP;

    public static void print(){
        Log.e("AccountData", account + ", " + password + ", " + firstName + ", " + lastName + ", " + phoneNumber + ", " + email);
    }
}
