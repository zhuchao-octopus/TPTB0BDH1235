package com.zhuchao.android.tianpu.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.zhuchao.android.tianpu.R;
import com.zhuchao.android.tianpu.data.App;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Oracle on 2017/12/1.
 */

public class AppListHandler {

    private static final String TAG = AppListHandler.class.getSimpleName();
    private Context context;
    private static final String recentAppKey = "recent_apps";
    private static final String lastAppKey = "last_app";

    public SparseArray<App> saAPP = new SparseArray<>();

    public static final String CLEAR_ACTION = "com.zhuchao.android.tianpu.action.clear";
    public static final String ADD_ACTION = "com.zhuchao.android.tianpu.action.add";
    public static final String SEND_APP = "send_app";
    private PageType type;
    private PageType oldType;
    private ExecutorService scanExecutorService;
    private PackageManager packageManager;
    //public String settings;

    public AppListHandler(Context context, PageType type) {
        this.context = context;
        this.type = type;
        this.oldType = this.type;
        this.packageManager = this.context.getPackageManager();
        scanExecutorService = Executors.newSingleThreadExecutor();
    }

    public void changePageType(PageType pageType) {
        oldType = type;
        type = pageType;
    }

    public void resetPageType() {
        type = oldType;
    }

    public void scan() {
        scanExecutorService.submit(new ScanTask());
    }

    public void scanHome() {
        scanExecutorService.submit(new ScanHomeTask(false));
    }

    public void scanRecent() {
        scanExecutorService.submit(new ScanRecentTask());
    }

    public void scanBottom() {
        scanExecutorService.submit(new ScanBottomTask());
    }

    public void release() {
        context = null;
        scanExecutorService.shutdownNow();
        scanExecutorService = null;
    }

    public void setOnRecentListener(OnRecentListener onRecentListener) {
        this.onRecentListener = onRecentListener;
    }

    public void setOnBottomListener(OnBottomListener onBottomListener) {
        this.onBottomListener = onBottomListener;
    }

    //通过回调返回有效的安装应用列表,用于添加应用的列表页
    class ScanTask implements Runnable {

        @Override
        public void run() {

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> scanVals = packageManager.queryIntentActivities(intent, 0);

            List<ResolveInfo> scanValsTmp = new LinkedList<ResolveInfo>();//能打开的应用列表
            for (int i = 0; i < scanVals.size(); i++) {
                ResolveInfo resolveInfo = scanVals.get(i);
                if (!isPreInstallApp(resolveInfo.activityInfo.packageName)) {
                    scanValsTmp.add(resolveInfo);
                }
            }

            int size = scanValsTmp.size();
            final SparseArray<App> list = new SparseArray<>();//能打开的封装后的应用列表
            for (int i = 0; i < size; i++) {
                ResolveInfo resolveInfo = scanValsTmp.get(i);
                App app = new App();
                app.setName(resolveInfo.loadLabel(packageManager).toString());
                app.setPackageName(resolveInfo.activityInfo.packageName);
                app.setIcon(resolveInfo.loadIcon(packageManager));
//                    Log.e("Tag","app="+app);
                //获取设置的包名
                //if (app.getName().equals("设置") || app.getName().equals("Settings") || app.getName().equals("settings") || app.getName().equals("Thiết lập")) {
                //    settings = app.getPackageName();
                //    Log.d("Tag", "settings=" + settings);
                //}
                list.put(i, app);
                saAPP = list;
                //Log.d(TAG, app.toString());
            }
            if (listener != null&&context!=null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onResponse(list);
                        }
                    }
                });
            }
        }
    }

    private boolean isPreInstallApp(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return true;
        }
//        if (packageName.equals("com.android.vending")) {
//            Log.d(TAG,"过滤谷歌商店="+packageName);
//            return true;
//        }
        if (packageName.equals("com.zhuchao.android.tianpu")) {
            //隐藏自己
            return true;
        }
        if (packageName.equals("com.wxs.scanner")) {
            //隐藏测试工具
            return true;
        }
        if (packageName.equals("com.iflytek.xiri")) {
            //隐藏讯飞语点
            return true;
        }
        if (packageName.equals("com.softwinner.dragonbox")) {
            //隐藏DragonBox
            return true;
        }
        if (packageName.equals("com.android.camera2")) {
            //隐藏相机
            return true;
        }
        return false;
    }

    class ScanHomeTask implements Runnable {

        boolean isOnlyRecent = false;

        public ScanHomeTask(boolean isOnlyRecent) {
            this.isOnlyRecent = isOnlyRecent;
        }

        @Override
        public void run() {

            boolean listenerOk = addRemoeveListener != null;
            ShareAdapter shareAdapter = ShareAdapter.getInstance();

            if (!isOnlyRecent) {
                int[] ids = new int[]{
                        R.id.fl2
                };
                App clearApp = new App();
                App app = null;
                clearApp.setIcon(MyApplication.res().getDrawable(R.drawable.add));
                clearApp.setName(MyApplication.res().getString(R.string.please_add));
                int idNum = ids.length;
                for (int i = 0; i < idNum; i++) {
                    final int idI = ids[i];
                    Log.d(TAG, "idI = " + idI);
                    String idStr = String.valueOf(idI);
                    String packageName = shareAdapter.getStr(idStr);
                    Log.d(TAG, "ScanHomeTask.idStr = " + idStr);
                    Log.d(TAG, "ScanHomeTask.packageName " + packageName);
                    if (listenerOk) {
                        if (TextUtils.isEmpty(packageName)) {
                            app = clearApp;
                        } else {
                            try {
                                PackageInfo packageInfo
                                        = packageManager.getPackageInfo(packageName, 0);
                                Drawable drawable
                                        = packageInfo.applicationInfo.loadIcon(packageManager);
                                String title
                                        = packageInfo.applicationInfo.loadLabel(packageManager).toString();
                                app = new App();
                                app.setName(title);
                                app.setIcon(drawable);
                                app.setPackageName(packageName);
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                                app = clearApp;
                            }
                        }
                        Log.d(TAG, app.toString());
                        final App sendApp = app;
                        if (listenerOk) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "idI=" + idI + "  sendApp=" + sendApp);
                                    addRemoeveListener.addRemove(idI, sendApp);
                                }
                            });
                        }
                    }
                }
            }
//            String recentAppsStr = shareAdapter.getStr(recentAppKey);
//            if (!TextUtils.isEmpty(recentAppsStr)) {
//                final int[] recentIds = new int[]{
//                        R.id.recent_iv1, R.id.recent_iv2, R.id.recent_iv3,
//                        R.id.recent_iv4
//                };
//                String[] recentApps = recentAppsStr.split(";");
//                int recentIdNum = recentIds.length;
//                int recentAppNum = recentApps.length;
//                App recentApp = null;
//                for (int idIndex = 0, appIndex = 0; idIndex < recentIdNum; appIndex++) {
//
//                    final int vId = recentIds[idIndex];
//                    recentApp = new App();
//
//                    if (idIndex < recentAppNum) {
//
//                        try {
//                            String packageName = recentApps[appIndex];
//                            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
//                            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
//                            recentApp.setIcon(applicationInfo.loadIcon(packageManager));
//                        } catch (PackageManager.NameNotFoundException e) {
//                            e.printStackTrace();
//                            continue;
//                        }
//                    }
//
//                    idIndex++;
//
//                    final App sendRecentApp = recentApp;
//                    if (listenerOk) {
//                        ((Activity) context).runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                addRemoeveListener.addRemove(vId, sendRecentApp);
//                            }
//                        });
//                    }
//                }
//            }
        }
    }

    public void regAppReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addAction(ADD_ACTION);
        intentFilter.addAction(CLEAR_ACTION);
        intentFilter.addDataScheme("package");
        context.registerReceiver(appReceiver, intentFilter);
    }

    public void unRegAppReceiver() {
        context.unregisterReceiver(appReceiver);
    }

    private BroadcastReceiver appReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.e("tag", String.format("appReceiver %s", intent.getAction()));
//            Toast.makeText(context,
//                    String.format("appReceiver %s", intent.getAction()), Toast.LENGTH_SHORT).show();

            String action = intent.getAction();
            if (action.equals(CLEAR_ACTION)) {
                int vId = intent.getIntExtra("vId", -1);
                if (vId >= 0) {
                    App clearApp = new App();
                    clearApp.setIcon(MyApplication.res().getDrawable(R.drawable.add));
                    if (onBottomListener != null) {
                        onBottomListener.updateBottom(vId, clearApp);
                    }
                    if (addRemoeveListener != null) {
                        clearApp.setName(MyApplication.res().getString(R.string.please_add));
                        addRemoeveListener.addRemove(vId, clearApp);
                    }
                    ShareAdapter.getInstance().remove(String.valueOf(vId));
                }
            } else if (action.equals(ADD_ACTION)) {
                App app = intent.getParcelableExtra(SEND_APP);
                Log.d(TAG, "recv send app " + app);
                if (app != null) {
                    if (addRemoeveListener != null) {
                        int vId = intent.getIntExtra("vId", -1);
                        Log.d(TAG, "recv send app " + vId);
                        if (vId >= 0) {
                            addRemoeveListener.addRemove(vId, app);
                            addAppToShort(app.getPackageName(), vId);
                        }
                    }
                }
            } else {
                switch (type) {
                    case HOME_TYPE:
                        scanHome();
                        break;
                    case MY_APP_TYPE:
                        scan();
                        break;
                    case BOTTOM_TYPE:
                        scanBottom();
                        break;
                    case RECENT_TYPE:
                        scanRecent();
                        break;
                    case HOME_BOTTOM_TYPE:
                        scanHome();
                        scanBottom();
                        break;
                }
            }
        }
    };

    public void addAppToShort(String packageName, int vId) {
        ShareAdapter.getInstance().saveStr(String.valueOf(vId), packageName);
    }

    public void launchApp(String packageName) {

        if (TextUtils.isEmpty(packageName)) {
            return;
        }

        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            scanExecutorService.submit(new SaveRecentTask(packageName));
            Log.d(TAG, "launchApp---->" + packageName);
        } else {
            Log.d(TAG, "launchApp---->" + packageName + "应用不存在");
        }
    }

    /**
     * 保存最近打开的app
     */
    class SaveRecentTask implements Runnable {

        String packageName = null;

        public SaveRecentTask(String packageName) {
            this.packageName = packageName;
        }

        @Override
        public void run() {
            ShareAdapter shareAdapter = ShareAdapter.getInstance();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(packageName).append(";");
            String recentAppsStr = shareAdapter.getStr(recentAppKey);
            if (!TextUtils.isEmpty(recentAppsStr)) {
                String[] recentAppArr = recentAppsStr.split(";");
                for (String recentApp : recentAppArr) {
                    if (!recentApp.equals(packageName)) {
                        stringBuilder.append(recentApp).append(";");
                    }
                }
            }
            shareAdapter.saveStr(recentAppKey, stringBuilder.toString());
            scanExecutorService.submit(new ScanHomeTask(true));
        }
    }

    class ScanRecentTask implements Runnable {

        @Override
        public void run() {

            StringBuilder stringBuilder = new StringBuilder();
            String recentAppsStr = ShareAdapter.getInstance().getStr(recentAppKey);
            String[] recentAppArr = !TextUtils.isEmpty(recentAppsStr) ?
                    recentAppsStr.split(";") : new String[0];

            int recentAppNum = recentAppArr.length;
            final SparseArray<App> list = new SparseArray<>();
            for (int recentIndex = 0, appIndex = 0; recentIndex < recentAppNum;
                 recentIndex++) {

                App recentAppObj = new App();

                try {
                    String packageName = recentAppArr[recentIndex];
                    Log.d(TAG, "ScanRecentTask " + packageName);
                    PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                    ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                    recentAppObj.setName(applicationInfo.loadLabel(packageManager).toString());
                    recentAppObj.setIcon(applicationInfo.loadIcon(packageManager));
                    recentAppObj.setPackageName(applicationInfo.packageName);
                    list.put(appIndex++, recentAppObj);
                    stringBuilder.append(packageName).append(";");
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    continue;
                }
            }

            if (listener != null && context != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onResponse(list);
                        }
                    }
                });
            }
            //最后使用的一个app
            String str = stringBuilder.toString().split(";")[0];
            if (!"com.android.music".equals(str)) {
                ShareAdapter.getInstance().saveStr(lastAppKey, str);
            }

            //最近使用
            ShareAdapter.getInstance().saveStr(recentAppKey, stringBuilder.toString());
//            Log.e("tag","str="+str+"    stringBuilder="+stringBuilder.toString());

        }
    }

    class ScanBottomTask implements Runnable {

        @Override
        public void run() {

            int[] vIds = new int[]{
                    R.id.pull_tag_9301, R.id.pull_tag_9302, R.id.pull_tag_9303, R.id.pull_tag_9304,
                    R.id.pull_tag_9305, R.id.pull_tag_9306, R.id.pull_tag_9307, R.id.pull_tag_9308,
                    R.id.pull_tag_9309, R.id.pull_tag_9310, R.id.pull_tag_9311, R.id.pull_tag_9312,
                    R.id.pull_tag_9313, R.id.pull_tag_9314, R.id.pull_tag_9315, R.id.pull_tag_9316
            };

            if (onBottomListener != null) {

                int vIdNum = vIds.length;
                for (int vIdIndex = 0; vIdIndex < vIdNum; vIdIndex++) {

                    final int idI = vIds[vIdIndex];
                    final App bottomAppObj = new App();
                    final String idStr = String.valueOf(idI);

                    try {
                        String packageName = ShareAdapter.getInstance().getStr(idStr);
                        Log.d(TAG, "ScanBottomTask " + packageName);
                        PackageInfo packageInfo
                                = packageManager.getPackageInfo(packageName, 0);
                        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                        bottomAppObj.setIcon(applicationInfo.loadIcon(packageManager));
                        bottomAppObj.setPackageName(applicationInfo.packageName);
                    } catch (PackageManager.NameNotFoundException e) {
                    }

                    Log.d(TAG, "ScanBottomTask " + bottomAppObj);
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onBottomListener.updateBottom(idI, bottomAppObj);
                        }
                    });
                }
            }
        }
    }

    public interface OnScanListener {
        void onResponse(SparseArray<App> apps);
    }

    public interface OnAddRemoeveListener {
        void addRemove(int id, App app);
    }

    public interface OnRecentListener {
        void updateRecent(int vId, App app);
    }

    public interface OnBottomListener {
        void updateBottom(int vId, App app);
    }

    private OnScanListener listener;
    private OnAddRemoeveListener addRemoeveListener;
    private OnRecentListener onRecentListener;
    private OnBottomListener onBottomListener;

    public void setOnScanListener(OnScanListener listener) {
        this.listener = listener;
    }

    public void setAddRemoeveListener(OnAddRemoeveListener listener) {
        this.addRemoeveListener = listener;
    }
}
