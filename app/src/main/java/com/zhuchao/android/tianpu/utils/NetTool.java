package com.zhuchao.android.tianpu.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Oracle on 2017/12/2.
 */

public class NetTool {

    private static final OkHttpClient okHttpClient;
    private static final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    private static String mac;

    static {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10L, TimeUnit.SECONDS)
                .writeTimeout(10L, TimeUnit.SECONDS)
                .readTimeout(10L, TimeUnit.SECONDS)
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(url.host(), cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url.host());
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                })
                .build();
    }

    public static ResponseBody pUrl(String url) {
        Request request = new Request.Builder()
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 7.1.2; Nexus 7 Build/N2G47E) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.83 Safari/537.36")
//				.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
//				.addHeader("Accept-Encoding", "gzip, deflate, sdch")
//				.addHeader("Accept-Language", "zh-CN,zh;q=0.8")
//				.addHeader("Connection", "keep-alive")
                .url(url)
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getUrlStr(String url) {
        ResponseBody responseBody = pUrl(url);
        try {
            String val = responseBody.string();
            return val;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (responseBody != null) {
                responseBody.close();
            }
        }
    }

    public static void setMac(String mac) {
        NetTool.mac = mac;
    }

    public static String getMac() {
        return NetTool.mac;
    }

    private Context context;
    private ConnectivityManager connectivityManager;
    private WifiManager wifiManager;

    public NetTool(Context context) {
        this.context = context;
        connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        handleNetScan();
    }

    public void registerNetReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        context.registerReceiver(netReceiver, intentFilter);
    }

    public void unRegisterNetReceiver() {
        context.unregisterReceiver(netReceiver);
    }

    private BroadcastReceiver netReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                handleNetScan();
            } else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
                updateWiFiStrength();
            }
        }
    };

    public interface OnNetListener {
        void onNetState(boolean isConnected, int type);
        void wifiLevel(int level);
    }

    private OnNetListener listener;

    public void setOnNetListener(OnNetListener listener) {
        this.listener = listener;
    }

    private int netType = -1;
    private boolean netState = false;

    private synchronized void handleNetScan() {

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            netState = networkInfo.isConnected();
            netType = networkInfo.getType();
        }

        //((Activity) context).runOnUiThread(new Runnable() {
        //    @Override
        //    public void run() {
                if (listener != null) {
                    listener.onNetState(netState, netType);
                }
        //    }
        //});
    }

    private synchronized void updateWiFiStrength() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getBSSID() != null) {
            final int strength = wifiManager.calculateSignalLevel(wifiInfo.getRssi(), 4);
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.wifiLevel(strength);
                    }
                }
            });
        }
    }

    public void release() {
        context = null;
        connectivityManager = null;
        wifiManager = null;
    }

    public static boolean isNetworkOK() {

        ConnectivityManager connectivityManager = (ConnectivityManager) AppMain.ctx()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (null != netInfo) {
            if(netInfo.isConnected()) {
                return true;
            }
        }
        return false;
    }
}
