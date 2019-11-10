package com.zhuchao.android.tianpu.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;

import com.zhuchao.android.tianpu.R;
import com.zhuchao.android.tianpu.databinding.ActivityMyApplicationBinding;
import com.zhuchao.android.tianpu.utils.AppListHandler;
import com.zhuchao.android.tianpu.utils.ShareAdapter;

import com.zhuchao.android.tianpu.adapter.AppAdapter;
import com.zhuchao.android.tianpu.data.App;

import com.zhuchao.android.tianpu.utils.PageType;
import com.zhuchao.android.tianpu.utils.Utils;

/**
 * Created by Oracle on 2017/12/1.
 */

public class AppsActivity extends Activity {

    private static final String TAG = AppsActivity.class.getSimpleName();
    private ActivityMyApplicationBinding binding;
    private AppListHandler appListHandler;
    private AppAdapter appAdapter;
    private PageType pageType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_application);
        String pageTypeStr = getIntent().getStringExtra("type");
        pageType = TextUtils.isEmpty(pageTypeStr) ?
                PageType.MY_APP_TYPE : PageType.valueOf(pageTypeStr);
        appListHandler = new AppListHandler(AppsActivity.this, pageType);
        appAdapter = new AppAdapter(this);
        appListHandler.setOnScanListener(new AppListHandler.OnScanListener() {
            @Override
            public void onResponse(SparseArray<App> apps) {
                appAdapter.setApps(apps);
                binding.allapps.setAdapter(appAdapter);
            }
        });
        binding.allapps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                App app = (App) appAdapter.getItem(position);
                if (app != null) {
                    String packageName = app.getPackageName();
                    if (!TextUtils.isEmpty(packageName)) {
                        appListHandler.launchApp(packageName);
                        if (!"com.android.music".equals(packageName)){
                            ShareAdapter.getInstance().saveStr("last_app", packageName);
                        }
                    }
                }
            }
        });
        binding.allapps.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                App app = (App) appAdapter.getItem(position);
                if (app != null) {
                    String packageName = app.getPackageName();
                    if (!TextUtils.isEmpty(packageName)) {
                        Utils.uninstallApp(AppsActivity.this, packageName);
                    }
                }
                return true;
            }
        });


        switch (pageType) {
            case RECENT_TYPE:
                appListHandler.scanRecent();
                break;
            case MY_APP_TYPE:
                appListHandler.scan();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.e("app","onKeyDown>>>>>event="+event);
        switch (keyCode){
            case KeyEvent.KEYCODE_F11:    //天普遥控器的设置键
                Intent in = new Intent();
                in.setClassName("com.android.settings","com.android.settings.Settings");
                startActivity(in);
                break;
            case KeyEvent.KEYCODE_G:      //天普遥控器的USB键
                appListHandler.launchApp("com.android.music");
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        switch (pageType) {
//            case RECENT_TYPE:
//                appListHandler.scanRecent();
//                break;
//            case MY_APP_TYPE:
//                appListHandler.scan();
//                break;
//        }
        appListHandler.regAppReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        appListHandler.unRegAppReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        appListHandler.release();
        appListHandler.setOnScanListener(null);
        appAdapter.release();
        appListHandler = null;
        appAdapter = null;
        binding = null;
    }

    public static void lunchAppsActivity(Context context, PageType pageType) {
        Intent intent = new Intent(context, AppsActivity.class);
        intent.putExtra("type", pageType.name());
        context.startActivity(intent);
    }
}
