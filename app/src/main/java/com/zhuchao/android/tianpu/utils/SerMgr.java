package com.zhuchao.android.tianpu.utils;

import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhuchao.android.tianpu.data.ExtensionCls;
import com.zhuchao.android.tianpu.data.json.regoem.RegOemCls;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Oracle on 2017/12/13.
 */
public class SerMgr {

    private static final String TAG = SerMgr.class.getSimpleName();
    private static SerMgr ins;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final String host = "http://101.132.77.226:8066";
    private static final String externsionFmt = "%s/v1/extension?app_id=%s&sn=%s";
    private static final String regFmt = "%s/v1/entry?app_id=%s&sn=%s%s%s";

    private SerMgr() {}

    public static SerMgr getIns() {
        if (ins == null) {
            ins = new SerMgr();
        }
        return ins;
    }

    public void checkExtension(String appId) {
        executorService.execute(new ExtensionTask(appId));
    }

    public void regOem(String appId, String oldUserId) {
        executorService.execute(new RegOemTask(appId, oldUserId));
    }

    class RegOemTask implements Runnable {

        String appId, oldUserId;

        public RegOemTask(String appId, String oldUserId) {
            this.appId = appId;
            this.oldUserId = oldUserId;
        }

        @Override
        public void run() {

            String url = null;
            String sn = PasswordUtil.generate(NetTool.getMac());
            if (TextUtils.isEmpty(oldUserId)) {
                url = String.format(regFmt, host, appId, sn, "", "");
            } else {
                url = String.format(regFmt, host, appId, sn, "&reference=", oldUserId);
            }
            String val = NetTool.getUrlStr(url);
            if (!TextUtils.isEmpty(val)) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    RegOemCls regOemCls = objectMapper.readValue(val, RegOemCls.class);
                    Log.d(TAG, regOemCls.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ExtensionTask implements Runnable {

        String appId;

        public ExtensionTask(String appId) {
            this.appId = appId;
        }

        @Override
        public void run() {

            String sn = PasswordUtil.generate(NetTool.getMac());
            String url = String.format(externsionFmt, host, appId, sn);
            String val = NetTool.getUrlStr(url);
            if (!TextUtils.isEmpty(val)) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    ExtensionCls extensionCls = objectMapper.readValue(val, ExtensionCls.class);
                    Log.d(TAG, extensionCls.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
