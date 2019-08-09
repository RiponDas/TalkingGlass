package com.jiangdg.usbcamera.application;

import android.app.Application;

import com.jiangdg.usbcamera.UVCCameraHelper;
import com.jiangdg.usbcamera.utils.CrashHandler;

import java.io.File;


public class MyApplication extends Application {
    private CrashHandler mCrashHandler;
    // File Directory in sd card
    public static final String DIRECTORY_NAME = "USBCamera";

    @Override
    public void onCreate() {
        super.onCreate();
        mCrashHandler = CrashHandler.getInstance();
        mCrashHandler.init(getApplicationContext(), getClass());
    }

    public static String getAppFolder(){
        File folder = new File(UVCCameraHelper.ROOT_PATH + MyApplication.DIRECTORY_NAME +"/");
        if(!folder.exists()){
            folder.mkdirs();
        }
        return folder.getAbsolutePath();
    }

    public static String getPictureFolder(){
        File folder = new File(UVCCameraHelper.ROOT_PATH + MyApplication.DIRECTORY_NAME +"/images/");
        if(!folder.exists()){
            folder.mkdirs();
        }
        return folder.getAbsolutePath();
    }

    public static String getVideoFolder(){
        File folder = new File(UVCCameraHelper.ROOT_PATH + MyApplication.DIRECTORY_NAME +"/videos/");
        if(!folder.exists()){
            folder.mkdirs();
        }
        return folder.getAbsolutePath();
    }
}
