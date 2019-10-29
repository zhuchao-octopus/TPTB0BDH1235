package com.zhuchao.android.tianpu.utils;

import android.os.Handler;
import android.os.Looper;

import com.zhuchao.android.tianpu.utils.interceptors.HttpLoggingInterceptor;
import com.zhuchao.android.tianpu.BuildConfig;

import java.util.concurrent.TimeUnit;

import me.jessyan.progressmanager.ProgressManager;
import okhttp3.OkHttpClient;

/**
 * Created by ZTZ on 2018/3/20.
 */

public class HttpUtils {

    private OkHttpClient okHttpClient;

    Handler mHandler;

    private HttpUtils() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        okHttpClient = ProgressManager.getInstance().with(new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)).build();
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static HttpUtils getInstance() {
        return Holder.httpUtils;
    }

    private static class Holder {
        private static HttpUtils httpUtils = new HttpUtils();
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

}
