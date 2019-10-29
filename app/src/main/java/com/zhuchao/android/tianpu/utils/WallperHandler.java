package com.zhuchao.android.tianpu.utils;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.zhuchao.android.tianpu.R;

import com.zhuchao.android.tianpu.R;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;

/**
 * Created by Oracle on 2018/1/13.
 */

public class WallperHandler {

    private static final String TAG = WallperHandler.class.getSimpleName();
    private boolean loadOk;
    private boolean loading;
    private List<String> imgUrls;
    private Context context;

    public WallperHandler(Context context) {
        loadOk = false;
        loading = false;
        this.context = context;
    }

    public void release() {
        context = null;
        if (imgUrls != null) {
            imgUrls.clear();
        }
        imgUrls = null;
        onWallperUpdateListener = null;
    }

    public void scanWallper() {
        if (!loading) {
            loading = true;
            new ScanTask().start();
        }
    }

    public List<String> getImgUrls() {
        return loadOk ? imgUrls : null;
    }

    public boolean dataOk() {
        return loadOk;
    }

    private String[] loadDef() {
        String[] imgArr = new String[]{
                String.format("res://com.zhuchao.android.tianpu/%d", R.drawable.iv7),
                String.format("res://com.zhuchao.android.tianpu/%d", R.drawable.ad2),
                String.format("res://com.zhuchao.android.tianpu/%d", R.drawable.ad3),
        };
        return imgArr;
    }

    class ScanTask extends Thread {

        @Override
        public void run() {

            String[] imgArr = null;

            ResponseBody responseBody = NetTool.pUrl("http://shangcheng.zdiptv.com:9500/appfile/glob/pic.json");

            String imgUrlStr = null;

            if (responseBody != null) {

                long fileSize = responseBody.contentLength();

                byte[] buffer = new byte[(int) fileSize];

                InputStream is = responseBody.byteStream();

                try {
                    is.read(buffer, 0, (int) fileSize);
                    imgUrlStr = new String(buffer, "GB2312");
//            String imgUrlStr =
//                    NetTool.getUrlStr("http://shangcheng.zdiptv.com:9500/appfile/glob/pic.json");
                    Log.d(TAG, "imgUrlStr = " + imgUrlStr);
                    if (!TextUtils.isEmpty(imgUrlStr)) {
                        imgArr = imgUrlStr.split("\n");
                        if (imgArr != null && imgArr.length > 0) {
                            loadOk = true;
                        }
                    }
                } catch (Exception e) {
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Exception e) {
                        }
                    }
                    if (responseBody != null) {
                        responseBody.close();
                    }
                }
            }

            if (!loadOk) {
                imgArr = loadDef();
            }
            int imgNum = imgArr.length;
            imgUrls = new ArrayList<>(imgNum);
            for (int i = 0; i < imgNum; i++) {
                imgUrls.add(imgArr[i].trim());
            }
            loadOk = true;
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (onWallperUpdateListener != null) {
                        onWallperUpdateListener.wallperUpdate();
                    }
                }
            });
            loading = false;
        }
    }

    public interface OnWallperUpdateListener {
        void wallperUpdate();
    }

    private OnWallperUpdateListener onWallperUpdateListener;

    public void setOnWallperUpdateListener(OnWallperUpdateListener listener) {
        onWallperUpdateListener = listener;
    }
}
