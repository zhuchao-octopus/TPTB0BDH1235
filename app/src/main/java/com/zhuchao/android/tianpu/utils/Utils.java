package com.zhuchao.android.tianpu.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Locale;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;

public class Utils {

    /**
     * 获取SDK版本
     */
    public static int getSDKVersion() {
        int version = 0;
        try {
            version = Integer.valueOf(android.os.Build.VERSION.SDK);
        } catch (NumberFormatException e) {
        }
        return version;
    }

    public static void uninstallApp(Context context, String packageName) {
        Uri uri = Uri.parse(String.format("package:%s", packageName));
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static int getVerCode() {
        try {
            return MyApplication.ctx().getPackageManager().
                    getPackageInfo(MyApplication.ctx().getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getRootPath() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            return MyApplication.ctx().getFilesDir().getAbsolutePath();
        }
    }

    /**
     * 判断系统时间是否为24小时制
     * @param ctx
     * @return
     */
    public static boolean isTime24(Context ctx) {
        ContentResolver cv = ctx.getContentResolver();
        String strTimeFormat = android.provider.Settings.System.getString(cv,
                android.provider.Settings.System.TIME_12_24);
        if (strTimeFormat != null && strTimeFormat.equals("24")) {//strTimeFormat某些rom12小时制时会返回null
            return true;
        } else return false;

    }

}
