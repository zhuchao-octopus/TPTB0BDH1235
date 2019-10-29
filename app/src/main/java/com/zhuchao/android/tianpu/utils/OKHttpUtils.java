package com.zhuchao.android.tianpu.utils;

import com.zhuchao.android.tianpu.data.StatusNetVal;
import com.zhuchao.android.tianpu.utils.interceptors.HttpLoggingInterceptor;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Oracle on 2018/1/2.
 */

public class OKHttpUtils {

    private static OkHttpClient ohc;

    private static HashMap<String, List<Cookie>> cookieStore;

    private static CacheControl cacheControl;

    private static final int HTTP_CACHE_SIZE = 2 * 1024 * 1024;

    private static OKHttpUtils okHttpUtils;

    private OKHttpUtils() {
        cookieStore = new HashMap<>();
        initNetTools();
    }

    public static OKHttpUtils getIns() {
        if (okHttpUtils == null) {
            synchronized (OKHttpUtils.class) {
                if (okHttpUtils == null) {
                    okHttpUtils = new OKHttpUtils();
                }
            }
        }
        return okHttpUtils;
    }

    private void initNetTools() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        Cache cache = new Cache(new File(Utils.getRootPath()), HTTP_CACHE_SIZE);
        cacheControl = new CacheControl.Builder()
                .maxAge(0, TimeUnit.SECONDS)
//                .maxStale(365, TimeUnit.DAYS)
                .build();
        ohc = new OkHttpClient.Builder()
                .cache(cache)
//                .connectTimeout(30L, TimeUnit.SECONDS)
//                .writeTimeout(30L, TimeUnit.SECONDS)
//                .readTimeout(30L, TimeUnit.SECONDS)
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
//                .addNetworkInterceptor(logging)
                .build();
    }

    private Response pUrl(String url) {
        Request request = new Request.Builder()
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 7.1.2; Nexus 7 Build/N2G47E) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.83 Safari/537.36")
                .cacheControl(cacheControl)
                .url(url)
                .build();
        try {
            return ohc.newCall(request).execute();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getUrlStr(String url) {
        Response response = pUrl(url);
        ResponseBody body = null;
        try {
            body = response.body();
            String val = body.string();
            return val;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (body != null) {
                body.close();
            }
        }
    }

    public boolean testHostUrlOK(String url) {
        Response response = pUrl(url);
        boolean val = false;
        try {
            if (response.isSuccessful() &&
                    response.networkResponse() != null &&
                    response.networkResponse().code() == HttpURLConnection.HTTP_OK) {
                val = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            val = false;
        }
        return val;
    }

    public StatusNetVal getUrlStrForModified(String url) {
        Response response = pUrl(url);
        ResponseBody body = null;
        StatusNetVal statusNetVal= new StatusNetVal();
        try {
            if (response.isSuccessful() &&
                    response.networkResponse() != null &&
                    response.networkResponse().code() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                statusNetVal.setModified(false);
            } else {
                statusNetVal.setModified(true);
            }
            body = response.body();
            statusNetVal.setNetVal(body.string());
            return statusNetVal;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (body != null) {
                body.close();
            }
        }
    }

    public StatusNetVal getUrlISForModified(String url) {
        Response response = pUrl(url);
        ResponseBody body = null;
        StatusNetVal statusNetVal= new StatusNetVal();
        try {
            if (response.isSuccessful() &&
                    response.networkResponse() != null &&
                    response.networkResponse().code() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                statusNetVal.setModified(false);
            } else {
                statusNetVal.setModified(true);
            }
            body = response.body();
            statusNetVal.setNetVal(body.byteStream());
            return statusNetVal;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
