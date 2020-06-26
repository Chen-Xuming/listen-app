package com.example.xiong.myapplication.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class PermissionUtil implements EasyPermissions.PermissionCallbacks{

    private Activity mContext;

    private static final int PERMISSIONS = 100; //请求码

    // 必须动态获取的权限
    private String[] mPerms = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    public PermissionUtil(Activity activity){
        this.mContext = activity;
    }

    @AfterPermissionGranted(PERMISSIONS)
    public void requestPermission() {
        if (EasyPermissions.hasPermissions(mContext, mPerms)) {

        } else {
            EasyPermissions.requestPermissions(mContext, "获取读写内存和麦克风权限", PERMISSIONS, mPerms);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == PERMISSIONS) {

        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(mContext, perms)) {
            new AppSettingsDialog.Builder(mContext)
                    .setRationale("没有该权限，此应用程序可能无法正常工作。打开应用设置屏幕以修改应用权限")
                    .setTitle("必需权限")
                    .build()
                    .show();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //mContext.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
