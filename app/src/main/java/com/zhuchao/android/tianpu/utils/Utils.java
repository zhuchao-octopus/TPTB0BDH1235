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

    public static String getDevID() {
        String devID = getLanMac();
        if (TextUtils.isEmpty(devID)) {
            devID = getWifiMac();
        }
//        if (BuildConfig.DEBUG) return "28:07:0D:00:40:BD";
        return devID;
    }

    public static String getLanMac() {

        String mac = null;

        try {

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                String name = networkInterface.getName();
                byte[] addr = networkInterface.getHardwareAddress();
                if ((addr == null) || (addr.length == 0)) {
                    continue;
                }
                StringBuilder buffer = new StringBuilder();
                for (byte b : addr) {
                    buffer.append(String.format("%02X:", b));
                }
                if (buffer.length() > 0) {
                    buffer.deleteCharAt(buffer.length() - 1);
                }

                mac = buffer.toString().toLowerCase(Locale.ENGLISH);

                if (name.startsWith("eth")) {
                    if (!jyMac(mac)) {
//						MLog.e(TAG, "mac format err");
                        mac = "00:00:00:00:00:00";
                    }
                    return mac;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "");
        }

        return "00:00:00:00:00:00";
    }

    public static String getWifiMac() {

        WifiManager wifiManager = (WifiManager) AppMain.ctx().getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        boolean wifiInitState = wifiManager.isWifiEnabled();

        String mac = null;

        try {

            if (!wifiInitState) {
                boolean openWifi = wifiManager.setWifiEnabled(true);
//				if(openWifi) {
//					MLog.e(TAG, "open wifi OK");
//				} else {
//					MLog.e(TAG, "open wifi no OK");
//				}
            }

            for (int i = 0; i < 10; i++) {
                if (wifiManager.isWifiEnabled()) {
//					MLog.e(TAG, "wifi state OK");
                    break;
                }
//				else {
//					MLog.e(TAG, "wifi state no OK");
//				}
                Thread.sleep(1000);
            }

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                String name = networkInterface.getName();
                byte[] addr = networkInterface.getHardwareAddress();
                if ((addr == null) || (addr.length == 0)) {
                    continue;
                }
                StringBuilder buffer = new StringBuilder();
                for (byte b : addr) {
                    buffer.append(String.format("%02X:", b));
                }
                if (buffer.length() > 0) {
                    buffer.deleteCharAt(buffer.length() - 1);
                }

                mac = buffer.toString().toLowerCase(Locale.ENGLISH);

                if (name.startsWith("wlan")) {
                    if (!jyMac(mac)) {
                        mac = "00:00:00:00:00:00";
                    }
                    return mac;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "");
        } finally {
            if (!wifiInitState) {
                Log.d(TAG, "wifi close");
                wifiManager.setWifiEnabled(false);
            }
        }

        return "00:00:00:00:00:00";
    }

    private static boolean jyMac(String mac) {

        if (TextUtils.isEmpty(mac)) {
            return false;
        }

        /**
         * ^ 表示行的开始
         * $ 表示行的结束
         * */
        String patternMac = "^[a-f0-9]{2}(:[a-f0-9]{2}){5}$";

        return Pattern.compile(patternMac).matcher(mac).find();
    }

    public static int getVerCode() {
        try {
            return AppMain.ctx().getPackageManager().
                    getPackageInfo(AppMain.ctx().getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getRootPath() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            return AppMain.ctx().getFilesDir().getAbsolutePath();
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
