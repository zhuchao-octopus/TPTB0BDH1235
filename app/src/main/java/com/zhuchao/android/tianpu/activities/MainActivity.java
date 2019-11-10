package com.zhuchao.android.tianpu.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.stx.xhb.xbanner.XBanner;
import com.stx.xhb.xbanner.transformers.Transformer;
import com.zhuchao.android.callbackevent.NormalRequestCallBack;
import com.zhuchao.android.databaseutil.SPreference;
import com.zhuchao.android.netutil.NetUtils;
import com.zhuchao.android.netutil.NetUtils.NetChangedCallBack;
import com.zhuchao.android.netutil.OkHttpUtils;
import com.zhuchao.android.tianpu.BuildConfig;
import com.zhuchao.android.tianpu.R;
import com.zhuchao.android.tianpu.bridge.SelEffectBridge;
import com.zhuchao.android.tianpu.data.App;
import com.zhuchao.android.tianpu.data.PackageName;
import com.zhuchao.android.tianpu.data.PreInstallApkPath;
import com.zhuchao.android.tianpu.data.json.regoem.CheckMacBean;
import com.zhuchao.android.tianpu.data.json.regoem.Recommend3Bean;
import com.zhuchao.android.tianpu.data.json.regoem.RecommendBean;
import com.zhuchao.android.tianpu.data.json.regoem.RecommendbgBean;
import com.zhuchao.android.tianpu.data.json.regoem.RecommendlogoBean;
import com.zhuchao.android.tianpu.data.json.regoem.RecommendmarqueeBean;
import com.zhuchao.android.tianpu.data.json.regoem.RecommendversionBean;
import com.zhuchao.android.tianpu.data.json.regoem.RemoveAppBean;
import com.zhuchao.android.tianpu.databinding.ActivityMainBinding;
import com.zhuchao.android.tianpu.services.MyService;
import com.zhuchao.android.tianpu.services.iflytekService;
import com.zhuchao.android.tianpu.utils.AppListHandler;
import com.zhuchao.android.tianpu.utils.AppsManager;
import com.zhuchao.android.tianpu.utils.GlideMgr;
import com.zhuchao.android.tianpu.utils.MySerialPort;
import com.zhuchao.android.tianpu.utils.TimeHandler;
import com.zhuchao.android.tianpu.utils.WallperHandler;
import com.zhuchao.android.tianpu.views.dialogs.BottomAppDialog;
import com.zhuchao.android.tianpu.views.dialogs.HomeAppDialog;
import com.zhuchao.android.tianpu.views.dialogs.HomeAppsDialog;
import com.zhuchao.android.tianpu.views.dialogs.Mac_Dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.ProgressManager;
import me.jessyan.progressmanager.body.ProgressInfo;

import static android.view.MotionEvent.ACTION_UP;
import static com.zhuchao.android.tianpu.utils.PageType.HOME_TYPE;
import static com.zhuchao.android.tianpu.utils.PageType.MY_APP_TYPE;
import static com.zhuchao.android.tianpu.utils.PageType.RECENT_TYPE;

public class MainActivity extends Activity implements OnTouchListener, OnGlobalFocusChangeListener, NetChangedCallBack,
        View.OnClickListener, TimeHandler.OnTimeDateListener,
        AppListHandler.OnScanListener, AppListHandler.OnAddRemoeveListener,
        View.OnKeyListener, AppListHandler.OnBottomListener, WallperHandler.OnWallperUpdateListener, ServiceConnection {

    public static final String bootVideo = "/system/media/boot.mp4";
    public static final String newBootVideo = "/system/media/new_boot.zip";
    private static final String TAG = "MainActivity";
    private static final String StartDragonTest = "1379";//测试
    private static final String StartDragonAging = "2379";//老化
    private static final String versionInfo = "3379";//版本信息
    private static HomeWatcherReceiver mHomeKeyReceiver = null;
    public AppListHandler appListHandler;
    //String cacheImg = "";
    /**
     * 获取推荐的广告列表
     */
    HashMap<String, String> web = new HashMap<>();
    String ClickOnTheAD = null;
    //广告视频链接
    //String urls = null;
    String AD_Name = null;


    private Mac_Dialog mdialog;
    private ActivityMainBinding binding;
    private SelEffectBridge selEffectBridge;
    private TimeHandler timeHandler;
    private HomeAppDialog homeAppDialog;
    private HomeAppsDialog homeAppsDialog;
    private BottomAppDialog bottomAppDialog = null;
    //private boolean updateScanOK = false;
    //private boolean marqueeScanOK = false;
    private List<String> imgUrls;
    private String mSerialData;
    //    private String img;
    private Context mContext;
    private int nba = 0; //清零计数
    private String TheLastSourceType = null;
    //private String url;
    //private boolean isFive = false;
    //private Drawable lastApps = null;
    //private boolean isThelast = false;
    //private boolean isDefault = true;
    private MyReceiver myReceiver = null;
    private BootCompletedReceiver mBootCompletedReceiver = null;
    /**
     * apk的下载状态
     * 0：没有下载过 1：下载中  2：下载完成  3：下载失败
     */
    //private Map<String, Integer> apkType = new HashMap<>();
    /**
     * 获取相应APP的主页面的图片
     */
    private Map<String, String> MainImgList = new HashMap<>();
    //网络数据/cache
    private RecommendBean recommendBean;
    //private RecommendlogoBean recommendlogoBean;
    //private Recommend3Bean recommend3Bean;
    //private RecommendmarqueeBean recommendmarqueeBean;
    //private RecommendbgBean recommendbgBean;


    private boolean isCheckedVersion = false;
    private FrameLayout[] flItems;
    private ProgressBar[] pbItems;
    private TextView[] tvItems;
    private ImageView[] ImageViewList;

    private int CustomId = -1;    //客户号  (国内)  录入版
    private String DeviceModelNumber = "750";//TVBOX 天谱
    private String host = "http://www.gztpapp.cn:8976/";    //天谱  （节流）
    private String lunchname = "TP0BDK70Q";
    private String appName = "TP0BDK70Q";      //天谱 （国内万利达）
    //private String lastoneApp = null;
    //private String removeResult;
    private MySerialPort serialPortUtils = new MySerialPort(this);
    private MyService myService;
    private Handler SerialPortReceivehandler;
    //private byte[] SerialPortReceiveBuffer;
    //private byte[] BluetoothOpen = {0x02, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, 0x04, 0x00, 0x0E, 0x7E};//蓝牙  K70 //  serialPortUtils.sendBuffer(BluetoothOpen,SizeOf(BluetoothOpen));
    private byte[] BluetoothClose = {0x01, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, 0x04, 0x00, 0x0D, 0x7E};//蓝牙  K50
    private int bluetooth = 0;
    //private byte[] CopperShaftLineIn = {0x02, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, 0x00, 0x01, 0x0B, 0x7E};//同轴  K70
    private byte[] CopperShaftClose = {0x01, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, 0x00, 0x01, 0x0A, 0x7E};//同轴     K50
    private int coppershaft = 0;
    //private byte[] OpticalFiberLineIn = {0x02, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, 0x00, 0x02, 0x0C, 0x7E};//光纤   K70
    private byte[] OpticalFiberClose = {0x01, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, 0x00, 0x02, 0x0B, 0x7E};//光纤    K50
    //private int opticalfiber = 0;
    //private byte[] SimulationLineIn = {0x02, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, (byte) 0x80, 0x00, (byte) 0x8A, 0x7E};//模拟  K70
    private byte[] SimulationLineInClose = {0x01, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, (byte) 0x80, 0x00, (byte) 0x89, 0x7E};//模拟  K50
    //private int simulation = 0;
    //private byte[] UsbOpen = {0x02, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, 0x08, 0x00, 0x12, 0x7E};//USB  K70
    private byte[] UsbClose = {0x01, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, 0x08, 0x00, 0x11, 0x7E};//USB  K50
    //private byte[] LastAppOpen = {0x02, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x0A, 0x7E};//最后使用的app  K70
    private byte[] LastChanelApp = {0x01, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x09, 0x7E};//最后使用的app  K50
    //private byte[] QueryState = {0x02, 0x01, 0x06, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x0A, 0x7E};//初始状态 K70 //
    private byte[] QueryStateK50 = {0x01, 0x01, 0x06, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x09, 0x7E};//初始状态  K50
    private long TimeTickCount = 0;
    private String InpputNumStr = "";
    private boolean isCharging = false;
    private boolean blutoothConnected = false;

    private View OldView = null;
    private NetUtils netUtils = null;

    public static void sendKeyEvent(final int KeyCode) {
        new Thread() {     //不可在主线程中调用
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(KeyCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContext = this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        //启动服务
        StartMyService();
        netUtils = new NetUtils(MainActivity.this, this);

        binding.fl5.setOnClickListener(this);
        //binding.fl5.setOnKeyListener(this);
        binding.fl6.setOnClickListener(this);
        //binding.fl6.setOnKeyListener(this);
        binding.fl3.setOnClickListener(this);
        //binding.fl3.setOnKeyListener(this);
        binding.fl4.setOnClickListener(this);
        // binding.fl4.setOnKeyListener(this);
        //binding.fl2.setOnKeyListener(this);
        binding.fl2.setOnClickListener(this);
        //binding.fl7.setOnKeyListener(this);
        binding.fl7.setOnClickListener(this);
        //binding.fl8.setOnKeyListener(this);
        binding.fl8.setOnClickListener(this);
        binding.fl1.setOnClickListener(this);
        //binding.fl1.setOnKeyListener(this);
        // binding.ad.setOnKeyListener(this);
        binding.ad.setOnClickListener(this);
        binding.fl4.setOnClickListener(this);
        //binding.fl4.setOnKeyListener(this);
        //binding.fl11.setOnKeyListener(this);
        binding.fl11.setOnClickListener(this);
        //binding.fl12.setOnKeyListener(this);
        binding.fl12.setOnClickListener(this);
        // binding.fl13.setOnKeyListener(this);
        binding.fl13.setOnClickListener(this);
        //binding.fl14.setOnKeyListener(this);
        binding.fl14.setOnClickListener(this);
        //binding.fl15.setOnKeyListener(this);
        binding.fl15.setOnClickListener(this);
        // binding.fl16.setOnKeyListener(this);
        //binding.fl16.setOnClickListener(this);
        binding.fl2.setOnClickListener(this);


        binding.fl0.setOnClickListener(this);
        binding.fl0.setOnKeyListener(this);


        binding.fl11.setOnTouchListener(this);
        binding.fl14.setOnTouchListener(this);
        binding.fl15.setOnTouchListener(this);
        //binding.fl16.setOnTouchListener(this);

        binding.fl0.setOnTouchListener(this);
        binding.fl1.setOnTouchListener(this);
        binding.fl2.setOnTouchListener(this);


        binding.fl3.setOnTouchListener(this);
        binding.fl4.setOnTouchListener(this);
        binding.fl6.setOnTouchListener(this);
        binding.fl7.setOnTouchListener(this);
        binding.fl8.setOnTouchListener(this);

        binding.ad.setOnTouchListener(this);

        binding.mainRl.getViewTreeObserver().addOnGlobalFocusChangeListener(this);

        flItems = new FrameLayout[]{binding.fl1, binding.fl2, binding.fl4};//需要下载k歌，腾讯视频，qq音乐
        pbItems = new ProgressBar[]{binding.progressBar1, binding.progressBar2, binding.progressBar9};
        tvItems = new TextView[]{binding.tvState1, binding.tvState2, binding.tvState9};
        //ImageViewList = new SimpleDraweeView[]{binding.bgIv1, binding.bgIv8};
        ImageViewList = new ImageView[]{binding.bgIv1, binding.bgIv2};

        appListHandler = new AppListHandler(MainActivity.this, HOME_TYPE);
        appListHandler.setOnScanListener(this);
        appListHandler.setAddRemoeveListener(this);
        appListHandler.setOnBottomListener(this);
        //appListHandler.scanRecent();
        appListHandler.scan();

        //时间更新
        timeHandler = new TimeHandler(this);
        timeHandler.setOnTimeDateListener(this);

        //String mac = netUtils.getMAC().toUpperCase();
        //binding.mac.setText(String.format("MAC: %s", mac));

        binding.scrollTv.setText("欢迎来到天谱！Welcome to Tianpu!欢迎来到天谱！Welcome to Tianpu!欢迎来到天谱！Welcome to Tianpu!");
        binding.scrollTv.setSelected(true);


        binding.ivFill.setVisibility(View.GONE);
        //initCache();

        binding.fl2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                lunchHomeAppDialog(v.getTag(), R.id.fl2);
                return true;
            }
        });


        //注册广播接收器
        myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.zhuchao.android.tianpu.services");
        registerReceiver(myReceiver, filter);

        mBootCompletedReceiver = new BootCompletedReceiver();
        filter = new IntentFilter();
        filter.addAction("com.iflytek.xiri.init.start");
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        registerReceiver(mBootCompletedReceiver, filter);


        requestPermition();

        selEffectBridge = (SelEffectBridge) binding.mainUpView.getEffectBridge();
        binding.mainRl.getViewTreeObserver().addOnGlobalFocusChangeListener(this);
        setupItemBottomTag();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "launcher is onStart");
        timeHandler.regTimeReceiver();
        appListHandler.regAppReceiver();
        //appListHandler.scanHome();//显示最近使用历史，扫描添加本地应用
        //appListHandler.scanRecent();

        if (bottomAppDialog != null) {
            appListHandler.scanBottom();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "launcher is onStop");
        timeHandler.unRegTimeReceiver();
        appListHandler.unRegAppReceiver();
        binding.adBg.stopAutoPlay();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //gotTheLastOne();
        pauseSystemMusic();
        binding.adBg.startAutoPlay();
        registerHomeKeyReceiver(this);
        //mBatteryHandler.post(mBatteryRunnable);
        View rootview = MainActivity.this.getWindow().getDecorView();
        View v = rootview.findFocus();

        if (OldView != null) {
            OldView.requestFocus();
        } else {
            binding.fl0.setFocusable(true);
            binding.fl0.requestFocus();
        }
        new Thread() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            View v = rootview.findFocus();
                            if (v != null) {
                                ViewGroup root = (ViewGroup) rootview;
                                Rect rect = new Rect();
                                root.offsetDescendantRectToMyCoords(v, rect);
                                if (rect.left > 0 && rect.right > 0) {
                                    setFocuseEffect(v);
                                    break;
                                }

                            }
                        }
                    }
                });

            }
        }.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        //appListHandler.scanRecent();
        appListHandler.scan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        timeHandler.release();
        appListHandler.release();

        timeHandler.setOnTimeDateListener(null);
        appListHandler.setAddRemoeveListener(null);
        appListHandler.setOnScanListener(null);
        timeHandler = null;
        netUtils.Free(null);
        unregisterHomeKeyReceiver(this);
        unregisterReceiver(myReceiver);
    }

    @Override
    public void onClick(View v) {
        //v.requestFocus();
        handleViewOnClic(v, -1, true);
        return;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.requestFocus();
        if (event.getAction() == ACTION_UP)
            handleViewOnClic(v, -1, true);

        return true;//super.onTouchEvent(event);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        //Log.e("key","onKey>>>>keyCode="+keyCode+"    KeyEvent="+event);
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MENU:
                    //Toast.makeText(mContext, "menu1", Toast.LENGTH_SHORT).show();
                    handleViewOnClic(v, keyCode, false);
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    //todo 底部弹窗
                    //handleViewKeyDown(v);
                    break;
                case KeyEvent.KEYCODE_HOME:
                case KeyEvent.KEYCODE_BACK:
                    binding.ivFill.setVisibility(View.GONE);

                    break;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Log.e("key","onKeyDown>>>>>event="+event);
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            binding.ivFill.setVisibility(View.GONE);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            binding.ivFill.setVisibility(View.GONE);
            inputNumber("BACK");
            return true;
        } else {
            switch (keyCode) {
                case KeyEvent.KEYCODE_0:// 0
                    inputNumber("0");
                    break;
                case KeyEvent.KEYCODE_1:// 1
                    inputNumber("1");
                    break;
                case KeyEvent.KEYCODE_2:// 2
                    inputNumber("2");
                    break;
                case KeyEvent.KEYCODE_3:// 3
                    inputNumber("3");
                    break;
                case KeyEvent.KEYCODE_4:// 4
                    inputNumber("4");
                    break;
                case KeyEvent.KEYCODE_5:// 5
                    inputNumber("5");
                    break;
                case KeyEvent.KEYCODE_6:// 6
                    inputNumber("6");
                    break;
                case KeyEvent.KEYCODE_7:// 7
                    inputNumber("7");
                    break;
                case KeyEvent.KEYCODE_8:// 8
                    inputNumber("8");
                    break;
                case KeyEvent.KEYCODE_9:// 9
                    inputNumber("9");
                    break;
                case KeyEvent.KEYCODE_F1: //F1
                    inputNumber("F1");
                    break;
                case KeyEvent.KEYCODE_F2:    //F2
                    inputNumber("F2");
                    break;
                case KeyEvent.KEYCODE_F3:     //F3
                    inputNumber("F3");
                    break;
                case KeyEvent.KEYCODE_MENU:
                case KeyEvent.KEYCODE_F11:    //天普遥控器的设置键
                    openSettings();
                    break;
                case KeyEvent.KEYCODE_G:      //天普遥控器的USB键
                    launchApp("com.android.music");
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    break;
                case KeyEvent.KEYCODE_ENTER:
                    View rootview = this.getWindow().getDecorView();
                    int focusId = rootview.findFocus().getId();
                    Log.i(TAG, "id = 0x" + Integer.toHexString(focusId));
                    break;
            }
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onTimeDate(String time, String date) {
        //Log.e(TAG, "onTimeDate " + time + " " + date);
        String times = time.split("#")[0];
        String week = time.split("#")[1];
        if (!TextUtils.isEmpty(date)) {
            binding.dateTv.setText(date);
        }
        if (!TextUtils.isEmpty(time)) {
            binding.timeTv.setText(times);
            binding.weekTv.setText(week);
        }
    }

    @Override
    public void onResponse(SparseArray<App> apps) {
        if (homeAppsDialog != null) {
            homeAppsDialog.loadAppData(apps);
        }
    }

    /**
     * 添加或者删除桌面图标
     *
     * @param id
     * @param app
     */
    @Override
    public void addRemove(int id, App app) {
        switch (id) {
            case R.id.fl2: {
                GlideMgr.loadNormalDrawableImg(MainActivity.this,
                        app.getIcon(), binding.ivAdd1);
                binding.tvAdd1.setText(app.getName());
                binding.fl2.setTag(app.getPackageName());
                return;
            }
        }
    }

    /**
     * 暂停系统播放器
     */
    private void pauseSystemMusic() {
        Intent freshIntent = new Intent();
        freshIntent.setAction("com.android.music.musicservicecommand.pause");
        freshIntent.putExtra("command", "pause");
        sendBroadcast(freshIntent);
    }

    public void showHomeAppsDialog(int rId) {
        homeAppsDialog = HomeAppsDialog.showHomeAppDialog(MainActivity.this, rId);
    }
    public void requestPermition() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int hasReadPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

            List<String> permissions = new ArrayList<String>();
            if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                //preferencesUtility.setString("storage", "true");
            }

            if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);

            } else {
                //preferencesUtility.setString("storage", "true");
            }

            if (!permissions.isEmpty()) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE},
                        0);
            }
        }

        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 10);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (!Settings.canDrawOverlays(this)) {
                    // SYSTEM_ALERT_WINDOW permission not granted...
                    //Toast.makeText(MainActivity.this,"not granted",Toast.LENGTH_SHORT);
                }
            }
        }
    }

    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        if (newFocus == null)
            return;

        int focusVId = newFocus.getId();

        if (focusVId == R.id.fl7) {
            binding.mac.setVisibility(View.VISIBLE);
        } else {
            //binding.mac.setVisibility(View.INVISIBLE);
        }

        OldView = newFocus;
        switch (focusVId) {
            case R.id.ad:
                selEffectBridge.setUpRectResource(R.drawable.home_sel_btn0);
                selEffectBridge.setVisibleWidget(false);
                binding.mainUpView.setFocusView(newFocus, oldFocus, 1.0f);
                newFocus.bringToFront();
                break;
            case R.id.fl4:
            case R.id.fl3:
            case R.id.fl6:
            case R.id.fl7:
            case R.id.fl8:
                selEffectBridge.setUpRectResource(R.drawable.home_sel_btn0);
                selEffectBridge.setVisibleWidget(false);
                binding.mainUpView.setFocusView(newFocus, oldFocus, 1.1f);
                newFocus.bringToFront();
                break;
            case R.id.fl5: //中央
                selEffectBridge.setUpRectResource(R.drawable.but);
                selEffectBridge.setVisibleWidget(false);
                binding.mainUpView.setFocusView(newFocus, oldFocus, 1.0f);
                newFocus.bringToFront();
                break;

            case R.id.fl2://最左边
            case R.id.fl0:
            case R.id.fl1:
                selEffectBridge.setUpRectResource(R.drawable.bgmbgm);
                selEffectBridge.setVisibleWidget(false);
                binding.mainUpView.setFocusView(newFocus, oldFocus, 1.1f);
                newFocus.bringToFront();
                break;

            case R.id.fl11: //中央下面的第一按钮
                selEffectBridge.setUpRectResource(R.drawable.left);
                selEffectBridge.setVisibleWidget(false);
                binding.mainUpView.setFocusView(newFocus, oldFocus, 1.0f);
                newFocus.bringToFront();
                break;
            case R.id.fl14:
            case R.id.fl15:
                selEffectBridge.setUpRectResource(R.drawable.home_sel_btn1);
                selEffectBridge.setVisibleWidget(false);
                binding.mainUpView.setFocusView(newFocus, oldFocus, 1.0f);
                newFocus.bringToFront();
                break;
            default:
                break;// throw new IllegalStateException("Unexpected value: " + focusVId);
        }

    }

    public void setFocuseEffect(View v) {
        onGlobalFocusChanged(null, v);
    }

    public void myServiceSendBytes(byte[] bytes) {
        if (myService != null)
            myService.sendCommand(bytes);
    }

    public void StartMyService() {
        Intent intent = new Intent(this, MyService.class);
        //启动servicce服务
        startService(intent);
        bindService(new Intent(this, MyService.class), this, BIND_AUTO_CREATE);


        Intent iii;
        iii = new Intent(MainActivity.this, iflytekService.class);
        Log.d(TAG, "start iflytekService");
        startService(iii);
    }

    private void switchToOtherChanel(String ChanelName) {
        binding.bgIv5.setVisibility(View.VISIBLE);
        //binding.bgIv5.setImageResource(R.drawable.bb2);
        Log.i(TAG, "切换通道：" + ChanelName);
        myServiceSendBytes(LastChanelApp);
    }


    @SuppressLint("LongLogTag")
    private void registerHomeKeyReceiver(Context context) {
        Log.i(TAG, "registerHomeKeyReceiver");
        mHomeKeyReceiver = new HomeWatcherReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        filter.addAction(HomeWatcherReceiver.ACTION_BATTERY_CHARGE);
        filter.addAction(HomeWatcherReceiver.ACTION_BATTERY_INFO);
        filter.addAction("BLUTOOLTH_STATUS");
        filter.addAction("COMMAND_DATA");
        context.registerReceiver(mHomeKeyReceiver, filter);
    }

    @SuppressLint("LongLogTag")
    private void unregisterHomeKeyReceiver(Context context) {
        Log.i(TAG, "unregisterHomeKeyReceiver");
        if (null != mHomeKeyReceiver) {
            context.unregisterReceiver(mHomeKeyReceiver);
        }
    }

    /**
     * 设置item底部标签
     */
    private void setupItemBottomTag() {
        for (int i = 0; i < pbItems.length; i++) {
            //todo 全部隐藏
            pbItems[i].setVisibility(View.GONE);
            tvItems[i].setVisibility(View.GONE);
        }
    }
    /**
     * 处理点击事件、菜单键
     *
     * @param v
     * @param keyCode
     * @param isClick true:点击 false:菜单
     */
    private void handleViewOnClic(View v, int keyCode, boolean isClick) {
        int id = v.getId();
        switch (id) {
            /**下面内容**/
            case R.id.fl11:
                //蓝牙
                binding.bgIv111.setImageResource(R.drawable.xsb);
                binding.bgIv113.setImageResource(R.drawable.xac);
                //binding.bgIv116.setImageResource(R.drawable.nnn);
                binding.bgIv112.setImageResource(R.drawable.xab);
                binding.bgIv114.setImageResource(R.drawable.xad);
                binding.bgIv115.setImageResource(R.drawable.xae);
                binding.bgIv5.setImageResource(R.drawable.ly);
                binding.bluetooth.setVisibility(View.VISIBLE);
                binding.bluetooth.setImageResource(R.drawable.bluetoothno);
                bluetooth++;
                TheLastSourceType = "蓝牙";
                binding.bgIcon.setVisibility(View.GONE);
                //myServiceSendBytes(BluetoothOpen);
                myServiceSendBytes(BluetoothClose);
                binding.fl11.requestFocus();
                break;
            case R.id.fl12:
                //同轴
                binding.bgIv112.setImageResource(R.drawable.xsa);
                binding.bgIv113.setImageResource(R.drawable.xac);
                //binding.bgIv116.setImageResource(R.drawable.nnn);
                binding.bgIv111.setImageResource(R.drawable.xaa);
                binding.bgIv114.setImageResource(R.drawable.xad);
                binding.bgIv115.setImageResource(R.drawable.xae);
                binding.bgIv5.setImageResource(R.drawable.tz);
                binding.bluetooth.setVisibility(View.GONE);
                TheLastSourceType = "同轴";
                binding.bgIcon.setVisibility(View.GONE);
                coppershaft++;
                //myServiceSendBytes(CopperShaftLineIn);
                myServiceSendBytes(CopperShaftClose);
                break;
            case R.id.fl13:
                //光纤
                binding.bgIv113.setImageResource(R.drawable.xsd);
                //binding.bgIv116.setImageResource(R.drawable.nnn);
                binding.bgIv111.setImageResource(R.drawable.xaa);
                binding.bgIv112.setImageResource(R.drawable.xab);
                binding.bgIv114.setImageResource(R.drawable.xad);
                binding.bgIv115.setImageResource(R.drawable.xae);
                binding.bgIv5.setImageResource(R.drawable.opt);
                binding.bluetooth.setVisibility(View.GONE);
                TheLastSourceType = "光纤";
                binding.bgIcon.setVisibility(View.GONE);
                //myServiceSendBytes(OpticalFiberLineIn);
                myServiceSendBytes(OpticalFiberClose);
                break;
            case R.id.fl14:
                //模拟
                binding.bgIv114.setImageResource(R.drawable.xsc);
                //binding.bgIv116.setImageResource(R.drawable.nnn);
                binding.bgIv111.setImageResource(R.drawable.xaa);
                binding.bgIv112.setImageResource(R.drawable.xab);
                binding.bgIv113.setImageResource(R.drawable.xac);
                binding.bgIv115.setImageResource(R.drawable.xae);
                binding.bgIv5.setImageResource(R.drawable.mn);
                binding.bluetooth.setVisibility(View.GONE);
                TheLastSourceType = "模拟";
                binding.bgIcon.setVisibility(View.GONE);
                //myServiceSendBytes(SimulationLineIn);
                myServiceSendBytes(SimulationLineInClose);
                binding.fl14.requestFocus();
                break;
            case R.id.fl15:
                //系统播放器
                binding.bgIv115.setImageResource(R.drawable.xse);
                //binding.bgIv116.setImageResource(R.drawable.nnn);
                binding.bgIv111.setImageResource(R.drawable.xaa);
                binding.bgIv112.setImageResource(R.drawable.xab);
                binding.bgIv113.setImageResource(R.drawable.xac);
                binding.bgIv114.setImageResource(R.drawable.xad);
                binding.bgIv5.setImageResource(R.drawable.usbortf);
                binding.bluetooth.setVisibility(View.GONE);
                TheLastSourceType = "player";
                binding.bgIcon.setVisibility(View.GONE);
                myServiceSendBytes(UsbClose);
                launchApp("com.android.music");
                binding.fl15.requestFocus();
                break;
/*            case R.id.fl16:
                //最后一个使用的app
                //binding.bgIv116.setImageResource(R.drawable.blue6);
                binding.bgIv111.setImageResource(R.drawable.xaa);
                binding.bgIv112.setImageResource(R.drawable.xab);
                binding.bgIv113.setImageResource(R.drawable.xac);
                binding.bgIv114.setImageResource(R.drawable.xad);
                binding.bgIv115.setImageResource(R.drawable.xae);
                binding.bluetooth.setVisibility(View.GONE);
                isThelast = true;
                TheLastSourceType = "last";
                switchToOtherChanel(v.getClass().getName());
                break;*/
            case R.id.fl4:
                //QQ音乐
                switchToOtherChanel("QQ音乐");
                launchApp(PackageName.qqMusic);
                binding.fl4.requestFocus();
                break;
            case R.id.fl0:
                //全民k歌
                switchToOtherChanel("全民k歌");
                launchApp(PackageName.qmSing);
                binding.fl0.requestFocus();
                break;
            case R.id.fl6:
                //文件管理器
                switchToOtherChanel("文件管理器");
                launchApp("com.softwinner.TvdFileManager");
                binding.fl6.requestFocus();
                break;
            case R.id.fl3:
                //我的应用
                switchToOtherChanel(v.getClass().getName());
                AppsActivity.lunchAppsActivity(this, MY_APP_TYPE);
                binding.fl3.requestFocus();
                break;
            case R.id.fl5:
                if (null != TheLastSourceType && !"".equals(TheLastSourceType)) {
                    if (TheLastSourceType.equals("蓝牙")) {
                        binding.ivFill.setVisibility(View.VISIBLE);
                        binding.ivFill.setImageResource(R.drawable.bly);
                        binding.bluetooth.setVisibility(View.VISIBLE);
                        binding.bluetooth.setImageResource(R.drawable.bluetoothno);
                        binding.bgIcon.setVisibility(View.GONE);

                        bluetooth++;
                    } else if (TheLastSourceType.equals("同轴")) {
                        binding.ivFill.setVisibility(View.VISIBLE);
                        binding.ivFill.setImageResource(R.drawable.btz);
                        binding.bluetooth.setVisibility(View.GONE);
                        binding.bgIcon.setVisibility(View.GONE);
                        onClick(binding.fl12);
                        coppershaft++;
                    } else if (TheLastSourceType.equals("光纤")) {
                        binding.ivFill.setVisibility(View.VISIBLE);
                        binding.ivFill.setImageResource(R.drawable.bopt);
                        binding.bluetooth.setVisibility(View.GONE);
                        //onClick(binding.fl13);
                        binding.bgIcon.setVisibility(View.GONE);
                    } else if (TheLastSourceType.equals("模拟")) {
                        binding.ivFill.setVisibility(View.VISIBLE);
                        binding.ivFill.setImageResource(R.drawable.bmn);
                        binding.bluetooth.setVisibility(View.GONE);
                        binding.bgIcon.setVisibility(View.GONE);
                        //onClick(binding.fl14);
                    } else if (TheLastSourceType.equals("player")) {
                        binding.ivFill.setVisibility(View.VISIBLE);
                        binding.ivFill.setImageResource(R.drawable.busbortf);
                        binding.bluetooth.setVisibility(View.GONE);
                        //launchApp("com.softwinner.TvdFileManager");
                        //launchApp("com.android.music");
                        binding.bgIcon.setVisibility(View.GONE);
                        binding.bluetooth.setVisibility(View.GONE);
                    } else if (TheLastSourceType.equals("last")) {
                        binding.bluetooth.setVisibility(View.GONE);
                    }
                }
                break;
            case R.id.fl7:
                //系统设置
                switchToOtherChanel("系统设置");
                openSettings();
                binding.fl7.requestFocus();
                break;
            case R.id.fl8:
                //hdp 频道
                launchApp(PackageName.hdp);
                binding.fl8.requestFocus();
                break;
            case R.id.fl1:
                //腾讯视频
                switchToOtherChanel("腾讯视频");
                launchApp(PackageName.qqTv);
                binding.fl1.requestFocus();
                break;
            case R.id.ad:
                if (web.size() > 0) {
                    switchToOtherChanel(v.getClass().getName());
                    WebRedirection();
                } else if (web.size() == 0) {
                    Toast.makeText(mContext, R.string.no_browsers, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.fl2:
                Object obj = v.getTag();
                if (obj == null || !isClick) {
                    switchToOtherChanel(v.getClass().getName());
                    lunchHomeAppDialog(obj, id);
                } else if (obj != null && isClick) {
                    launchApp(obj.toString());
                }
                break;
        }

        getSharedPreferences("TheLastSourceType", MODE_PRIVATE).edit().putString("TheLastSourceType", TheLastSourceType).commit();
    }

    Runnable HandleSerialPortrunnable = new Runnable() {
        @Override
        public void run() {
            if (mSerialData.equals("0201050000020000010B7E") || mSerialData.equals("0101050000020000010A7E")) {
                pauseSystemMusic();
                //同轴
                TheLastSourceType = "同轴";

                binding.fl12.requestFocus();

                binding.bgIv111.setImageResource(R.drawable.xaa);
                binding.bgIv113.setImageResource(R.drawable.xac);
                //binding.bgIv116.setImageResource(R.drawable.nnn);
                binding.bgIv112.setImageResource(R.drawable.xsa);
                binding.bgIv114.setImageResource(R.drawable.xad);
                binding.bgIv115.setImageResource(R.drawable.xae);
                binding.bgIcon.setVisibility(View.GONE);
                binding.bgIv5.setImageResource(R.drawable.tz);
                //} else
                binding.ivFill.setImageResource(R.drawable.btz);

            } else if (mSerialData.equals("0201050000020004000E7E") || mSerialData.equals("0101050000020004000D7E")) {
                //蓝牙
                pauseSystemMusic();
                TheLastSourceType = "蓝牙";

                binding.fl11.requestFocus();
                binding.bgIv111.setImageResource(R.drawable.xsb);
                binding.bgIv113.setImageResource(R.drawable.xac);
                //binding.bgIv116.setImageResource(R.drawable.nnn);
                binding.bgIv112.setImageResource(R.drawable.xab);
                binding.bgIv114.setImageResource(R.drawable.xad);
                binding.bgIv115.setImageResource(R.drawable.xae);
                binding.bgIv5.setImageResource(R.drawable.ly);
                binding.bgIcon.setVisibility(View.GONE);
                binding.ivFill.setImageResource(R.drawable.bly);
            } else if (mSerialData.equals("0201050000020080008A7E") || mSerialData.equals("010105000002008000897E")) {
                pauseSystemMusic();
                //模拟
                TheLastSourceType = "模拟";

                binding.bgIv111.setImageResource(R.drawable.xaa);
                binding.bgIv113.setImageResource(R.drawable.xac);
                //binding.bgIv116.setImageResource(R.drawable.nnn);
                binding.bgIv112.setImageResource(R.drawable.xab);
                binding.bgIv114.setImageResource(R.drawable.xsc);
                binding.bgIv115.setImageResource(R.drawable.xae);
                binding.bgIcon.setVisibility(View.GONE);

                binding.bgIv5.setImageResource(R.drawable.mn);
                binding.fl14.requestFocus();
                binding.ivFill.setImageResource(R.drawable.bmn);
                binding.bluetooth.setVisibility(View.INVISIBLE);
            } else if (mSerialData.equals("0201050000020000020C7E") || mSerialData.equals("0101050000020000020B7E")) {
                pauseSystemMusic();
                //光纤
                TheLastSourceType = "光纤";

                binding.bgIv111.setImageResource(R.drawable.xaa);
                binding.bgIv113.setImageResource(R.drawable.xsd);
                //binding.bgIv116.setImageResource(R.drawable.nnn);
                binding.bgIv112.setImageResource(R.drawable.xab);
                binding.bgIv114.setImageResource(R.drawable.xad);
                binding.bgIv115.setImageResource(R.drawable.xae);
                binding.bgIcon.setVisibility(View.GONE);

                binding.bgIv5.setImageResource(R.drawable.opt);
                binding.fl13.requestFocus();
                //}else
                binding.ivFill.setImageResource(R.drawable.bopt);
                binding.bluetooth.setVisibility(View.INVISIBLE);

            } else if (mSerialData.equals("0201050000020000000A7E") || mSerialData.equals("010105000002000000097E")) {
                pauseSystemMusic();
                //最后的app  I2S 通道
                TheLastSourceType = "last";
                //binding.fl16.requestFocus();
                binding.bgIv111.setImageResource(R.drawable.xaa);
                binding.bgIv113.setImageResource(R.drawable.xac);
                //binding.bgIv116.setImageResource(R.drawable.blue6);
                binding.bgIv112.setImageResource(R.drawable.xab);
                binding.bgIv114.setImageResource(R.drawable.xad);
                binding.bgIv115.setImageResource(R.drawable.xae);
                //binding.fl15.requestFocus();
                binding.bluetooth.setVisibility(View.INVISIBLE);

            } else if (mSerialData.equals("020105000002000800127E") || mSerialData.equals("010105000002000800117E")) {
                pauseSystemMusic();
                //usb/TF
                TheLastSourceType = "player";
                binding.bgIv111.setImageResource(R.drawable.xaa);
                binding.bgIv113.setImageResource(R.drawable.xac);
                //binding.bgIv116.setImageResource(R.drawable.nnn);
                binding.bgIv112.setImageResource(R.drawable.xab);
                binding.bgIv114.setImageResource(R.drawable.xad);
                binding.bgIv115.setImageResource(R.drawable.xse);
                binding.fl15.requestFocus();
                binding.bluetooth.setVisibility(View.INVISIBLE);
                launchApp("com.android.music");

            } else if (mSerialData.equals("")) {
                //mic A开
                binding.micA.setImageResource(R.drawable.ano);
            } else if (mSerialData.equals("")) {
                //mic A关
                binding.micA.setVisibility(View.GONE);
            } else if (mSerialData.equals("")) {
                //mic B开
                binding.micB.setImageResource(R.drawable.bno);
            } else if (mSerialData.equals("")) {
                //mic B关
                binding.micB.setVisibility(View.GONE);
            }

            if (mSerialData.equals("0201050000020004000E7E") || mSerialData.equals("0101050000020004000D7E")) {
                if (blutoothConnected) {
                    binding.bluetooth.setVisibility(View.VISIBLE);
                    binding.bluetooth.setImageResource(R.drawable.bluetoothhave);
                }
            }

            getSharedPreferences("TheLastSourceType", MODE_PRIVATE).edit().putString("TheLastSourceType", TheLastSourceType).commit();
        }
    };

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MyService.Binder binder = (MyService.Binder) service;
        myService = binder.getService();

        if (SerialPortReceivehandler == null) {
            SerialPortReceivehandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                }
            };
        }

        myService.setActionCallback(new MyService.Callback() {
            @Override
            public void onDataChange(String data) {
                mSerialData = data;
                Log.i("Callback.onDataChange", "data=" + data);
                if (null != SerialPortReceivehandler) {
                    SerialPortReceivehandler.post(HandleSerialPortrunnable);
                } else {
                    Log.e("tag", "SerialPortReceivehandler=null");
                }
            }
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.e("tag", "后台服务已断开！");
    }

    private void openSettings() {
        Intent in = new Intent();
        in.setClassName("com.android.settings", "com.android.settings.Settings");
        startActivity(in);
    }

    /**
     * 点击广告图片，跳转到相对应的网页
     */
    private void WebRedirection() {
        String url = null;
        url = web.get(ClickOnTheAD);
        //Log.e("tag", "url=" + url);
        caculateUserclickAd(AD_Name);
        if (!"".equals(url) && null != url) {
            Intent intent = new Intent();
            intent.setData(Uri.parse(url));
            intent.setAction(Intent.ACTION_VIEW);
            this.startActivity(intent);
        } else {
            Toast.makeText(mContext, R.string.no_browsers, Toast.LENGTH_SHORT).show();
        }
    }

    public void launchApp(String packageName) {
        if (PackageName.qqMusic.equals(packageName)) {
            handleClick(packageName, PreInstallApkPath.qqMusic);
        } else if (PackageName.qqTv.equals(packageName)) {
            handleClick(packageName, PreInstallApkPath.qqTv);
        } else if (PackageName.hdp.equals(packageName)) {
            handleClick(packageName, PreInstallApkPath.hdp);
        }
        appListHandler.launchApp(packageName);
    }

    private void handleClick(String packageName, String filePath) {
        if (!AppsManager.isInstallApp(mContext, packageName)) {
            if (AppsManager.isSystemAppInstallOk(mContext, filePath)) {
                Toast.makeText(mContext, "正在安装应用", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 处理item点击：打开/下载app
     *
     * @param index
     */
    private void handleClick(final int index, int keyCode) {
        if (recommendBean != null && recommendBean.getData().size() > index) {
            final RecommendBean.DataBean dataBean = recommendBean.getData().get(index);
            if (keyCode == KeyEvent.KEYCODE_MENU) {
                //中间弹窗
                Toast.makeText(mContext, "menu", Toast.LENGTH_SHORT).show();
            } else {
                if (AppsManager.isInstallApp(mContext, dataBean.getSyy_app_packageName())) {
                    launchApp(dataBean.getSyy_app_packageName());
                    CaculateclickApp(dataBean.getSyy_app_id());
                } else {
                    if (pbItems[index].getProgress() != 100) {
                        if (dataBean.getStatus() != null && ((double) dataBean.getStatus()) == 0) {
                            //app禁用
                            if (TextUtils.isEmpty(dataBean.getSyy_app_introduce())) {
                                Toast.makeText(mContext, R.string.maintain, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, dataBean.getSyy_app_introduce(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            final String filePath = AppsManager.getAppDir() + dataBean.getSyy_app_download().substring(dataBean.getSyy_app_download().lastIndexOf("/") + 1);

                            flItems[index].setEnabled(false);
                            tvItems[index].setText(R.string.download);
                            tvItems[index].setVisibility(View.VISIBLE);
                            pbItems[index].setVisibility(View.VISIBLE);
                            ProgressManager.getInstance().addResponseListener(dataBean.getSyy_app_download(), new ProgressListener() {
                                @Override
                                public void onProgress(ProgressInfo progressInfo) {
                                    pbItems[index].setProgress(progressInfo.getPercent());
                                    if (progressInfo.isFinish()) {
                                        AppsManager.install(mContext, filePath);
                                    }
                                }

                                @Override
                                public void onError(long id, Exception e) {

                                }
                            });
                            downloadApk(dataBean, index);
                        }
                    } else {
                        //已经下载过，启动安装
                        final String filePath = AppsManager.getAppDir() + dataBean.getSyy_app_download().substring(dataBean.getSyy_app_download().lastIndexOf("/") + 1);
                        AppsManager.install(mContext, filePath);
                    }
                }
            }

        }
    }

    public void lunchHomeAppDialog(Object obj, int id) {
        homeAppDialog = HomeAppDialog.showHomeAppDialog(this,
                obj != null ? obj.toString() : null, id);
    }

    @Override
    public void updateBottom(int vId, App app) {
        if (bottomAppDialog != null) {
            bottomAppDialog.updateBottom(vId, app);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "onLowMemory");
    }

    public void scanBottom() {
        appListHandler.scanBottom();
    }

    private void inputNumber(String i) {
        long inputTime = System.currentTimeMillis();
        if (inputTime - TimeTickCount < 1000) {
            //1s内输入有效
            if (i.equals("BACK")) {
                nba++;
            }
            InpputNumStr += i;
        } else {
            //如果输入时间超过1s,num统计的值重置为输入值
            InpputNumStr = i;
            nba = 0;
        }
        TimeTickCount = inputTime;

        switch (nba) {
            case 8:
                TimeTickCount = 0;
                clearData();
                nba = 0;
                break;
        }

        switch (InpputNumStr) {
            case StartDragonTest:
                //重置输入
                InpputNumStr = "";
                TimeTickCount = 0;
//                Toast.makeText(this, "启动测试:", Toast.LENGTH_SHORT).show();
                if (AppsManager.isInstallApp(mContext, "com.wxs.scanner")) {
//                    startActivity(new Intent().setClassName("com.kong.apptesttools", "com.kong.apptesttools.MainActivity"));
                    startActivity(new Intent().setClassName("com.wxs.scanner", "com.wxs.scanner.activity.workstation.CheckActivity"));
                } else {
//                    Toast.makeText(mContext, "未安装测试App", Toast.LENGTH_SHORT).show();
                    Toast.makeText(mContext, R.string.no_install_app, Toast.LENGTH_SHORT).show();
                }
                break;
            case StartDragonAging:
                //重置输入
                InpputNumStr = "";
                TimeTickCount = 0;
//                Toast.makeText(this, "启动老化测试:", Toast.LENGTH_SHORT).show();
                if (AppsManager.isInstallApp(mContext, "com.softwinner.agingdragonbox")) {
                    AppsManager.startAgingApk(mContext);
                } else {
//                    Toast.makeText(mContext, "未安装老化App", Toast.LENGTH_SHORT).show();
                    Toast.makeText(mContext, R.string.no_install_old_app, Toast.LENGTH_SHORT).show();
                }
                break;
            case versionInfo:
                InpputNumStr = "";
                TimeTickCount = 0;
                String deviceName;
                if ("702".equals(DeviceModelNumber)) {
                    deviceName = "柴喜";
                } else if ("701".equals(DeviceModelNumber)) {
//                    deviceName = "拓普赛特";
                    deviceName = "Mở rộng khu vực";
                } else if ("704".equals(DeviceModelNumber)) {
                    deviceName = "老凤祥";
                } else if ("696".equals(DeviceModelNumber)) {
                    deviceName = "精合智";
                } else {
                    deviceName = "其它";
                }
                new AlertDialog.Builder(mContext)
                        .setTitle("Phiên bản thông tin")
                        .setMessage(appName + "-" + BuildConfig.VERSION_NAME +
                                "\nPhạm vi dịch vụ：" + (host.startsWith("http://192.168.") ? "Mạng nội bộ" : "Mạng bên ngoài") +
                                "\nThương hiệu：" + deviceName)
                        .show();
                break;
            case "8888":
                InpputNumStr = "";
                TimeTickCount = 0;
                View view1 = LayoutInflater.from(mContext).inflate(R.layout.test_cache, null);
                //((TextView) view1.findViewById(R.id.tv_content)).setText("" + removeResult);
                new AlertDialog.Builder(mContext)
                        .setTitle("remove result")
                        .setView(view1)
                        .show();

                break;
        }
    }

    @Override
    public void wallperUpdate() {
        Log.d(TAG, "wallperUpdate");
        List<String> tmpImgUrls = null;
        if (tmpImgUrls == null || tmpImgUrls.size() == 0) {
            return;
        }
        binding.adBg.stopAutoPlay();
        this.imgUrls = tmpImgUrls;
        binding.adBg.setData(R.layout.ad_item, imgUrls, null);
        binding.adBg.setPageTransformer(Transformer.Cube);
        binding.adBg.setmAdapter(new XBanner.XBannerAdapter() {
            @Override
            public void loadBanner(XBanner banner, Object model, View view, int position) {
                String url = imgUrls.get(position);
                if (!TextUtils.isEmpty(url)) {
                    ((SimpleDraweeView) view).setImageURI(Uri.parse(imgUrls.get(position)));
                }
            }
        });
        binding.adBg.startAutoPlay();
    }

    public void synUpdateUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.netIv.setVisibility(View.VISIBLE);

                if (netUtils.isNetCanConnect())
                    binding.netIv.setImageResource(R.drawable.net);
                else
                    binding.netIv.setImageResource(R.drawable.netno);
            }
        });

    }

    private void showMACDialog(String type) {
        if (mdialog == null || mdialog.isShowing() != true) {
            mdialog = new Mac_Dialog(this);
            mdialog.setVolumeAdjustListener(MainActivity.this);
            mdialog.setCancelable(false);
            mdialog.show();
        }
        mdialog.adjustVolume(true, type);
    }

    @Override
    public void onNetStateChanged(boolean b, int i, String s, String s1, String s2, String s3, String s4) {
        Log.d(TAG, "onNetStateChanged>>>>>>>>>> " + b + " " + i + " " + s + " " + s1 + " " + s2 + " " + s3 + " " + s4);
        if (b) {//网络有效
            if (netUtils.isNetCanConnect()) {//网络可以建立连接
                caculateOnlineTime();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            checkDeviceIsAvailable(netUtils.getDeviceID().toUpperCase());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        }
        synUpdateUI();
    }

    @Override
    public void onWifiLevelChanged(int i) {
        Log.d(TAG, "onWifiLevelChanged>>>>>>>>>>" + i);
        if (netUtils.getNetType() != 1) return;

        switch (i) {
            case 0:
                binding.netIv.setImageResource(R.drawable.wifi0);
                break;
            case 1:
                binding.netIv.setImageResource(R.drawable.wifi1);
                break;
            case 2:
                binding.netIv.setImageResource(R.drawable.wifi2);
                break;
            case 3:
                binding.netIv.setImageResource(R.drawable.wifi3);
                break;
            default: // 2 -> ETH
                binding.netIv.setImageResource(R.drawable.wifi_out_of_range);
                break;
        }

    }

    // DMN DeviceModelNumber, // DID :Device ID, // CID: Custorm id, // ip: net ip , // RID: ip region,
    private String getMyUrl(String api, String DMN, String DID, int CID, String IP, String RID, String LuncherName) {
        String url = "";
        if (CustomId != -1) {
            url = host + api +
                    "cy_brand_id=" + DeviceModelNumber +
                    "&mac=" + DID +
                    "&netCardMac=" + DID +
                    "&CustomId=" + CID +
                    "&codeIp=" + IP +
                    "&region=" + RID +
                    "&CustomId=" + CustomId +
                    "&lunchname=" + lunchname;
        } else {
            url = host + api +
                    "cy_brand_id=" + DeviceModelNumber +
                    "&mac=" + DID +
                    "&netCardMac=" + DID +
                    "&codeIp=" + IP +
                    "&region=" + RID +
                    "&lunchname=" + lunchname;
        }
        return url;
    }

    private void checkDeviceIsAvailable(String mac) {
        String url = "";
        if(TextUtils.isEmpty(mac)) return;
        if (CustomId != -1) {
            url = host + "jhzBox/box/loadBox.do?cy_brand_id=" + DeviceModelNumber + "&mac=" + mac +
                    "&netCardMac=" + netUtils.getDeviceID() +
                    "&CustomId=" + CustomId +
                    "&codeIp=" + netUtils.getIP0() +
                    "&region=" + netUtils.getChineseRegion(netUtils.getLocation());
        } else {
            url = host + "jhzBox/box/loadBox.do?cy_brand_id=" + DeviceModelNumber + "&mac=" + mac +
                    "&netCardMac=" + netUtils.getDeviceID() +
                    "&codeIp=" + netUtils.getIP0() +
                    "&region=" + netUtils.getChineseRegion(netUtils.getLocation());
        }

        OkHttpUtils.request(url, new NormalRequestCallBack() {
            @Override
            public void onRequestComplete(String s, int i) {
                if (i >= 0 && !TextUtils.isEmpty(s)) {
                    final CheckMacBean checkMacBean = new Gson().fromJson(s, CheckMacBean.class);
                    if (checkMacBean.getStatus() == 0) {
                        asynUpdateUI(); //设备已经授权
                    } else  //设备不可用
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showMACDialog(netUtils.getDeviceID().toUpperCase());
                            }
                        });
                    }
                    if (!isCheckedVersion) {
                        isCheckedVersion = true;
                        checkSoftwareVersion();
                    }
                }
            }

        });
    }

    private synchronized void asynUpdateUI() {
        new Thread() {
            @Override
            public void run() {
                try {
                    getRecommendApp();
                    getRecommendAd();
                    getRecommendLogo();
                    getRecommendMarquee();
                    getRecommendBgImg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    /**
     * 获取卸载的app列表
     */
    private void getRemoveApp() {
        String url = host + "jhzBox/box/unload.do?cy_brand_id=" + DeviceModelNumber;
        OkHttpUtils.request(url, new NormalRequestCallBack() {
            @Override
            public void onRequestComplete(String s, int i) {
                RemoveAppBean removeAppBean = new Gson().fromJson(s, RemoveAppBean.class);
                if (removeAppBean.getStatus() == 0) {
                    //卸载app
                    List<String> data = removeAppBean.getData();
                    for (String pck : data) {
                        if (AppsManager.isInstallApp(mContext, pck)) {
                            AppsManager.uninstallApk(mContext, pck);
                        }
                    }
                }
            }
        });
    }

    /**
     * 获取推荐的app列表
     */
    private void getRecommendApp() {
        //String url= "";
        String url = getMyUrl("jhzBox/box/loadPushApp.do?pitClass=01&", DeviceModelNumber, netUtils.getDeviceID().toUpperCase(), CustomId, netUtils.getIP0(), netUtils.getChineseRegion(netUtils.getLocation()), lunchname);
        OkHttpUtils.request(url, new NormalRequestCallBack() {
            @Override
            public void onRequestComplete(String s, int i) {
                final RecommendBean rBean = new Gson().fromJson(s, RecommendBean.class);
                if (rBean.getStatus() == 0) {
                    //加载成功
                    final List<RecommendBean.DataBean> data = rBean.getData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //保存缓存
                            SPreference.saveSharedPreferences(MainActivity.this, "my_setting", "recommend_cache", s);
                            recommendBean = rBean;
                            //加载坑位的图片
                            if (data != null && data.size() >= ImageViewList.length) {
                                for (int i = 0; i < ImageViewList.length; i++) {
                                    if (!TextUtils.isEmpty(data.get(i).getSyy_app_img())) {
                                        Glide.with(mContext).load(data.get(i).getSyy_app_img()).into(ImageViewList[i]);
                                    }
                                }
                            }
                            //加载指定app主页图片
                            if (data != null && data.size() != 0) {
                                for (int i = 0; i < data.size(); i++) {
                                    String pkn = data.get(i).getSyy_app_packageName();
                                    String img = data.get(i).getSyy_appstatus_img();
                                    if (!"".equals(img) && null != img) {
                                        MainImgList.put(pkn, img);
                                    }
                                }
                            }
                            //是否显示未下载标签
                            setupItemBottomTag();
                        }

                    });
                }
            }
        });
    }

    /**
     * 获取推荐的跑马灯
     */
    private void getRecommendMarquee() {
        String url = getMyUrl("jhzBox/box/loadMarquee.do?", DeviceModelNumber, netUtils.getDeviceID().toUpperCase(), CustomId, netUtils.getIP0(), netUtils.getChineseRegion(netUtils.getLocation()), lunchname);
        OkHttpUtils.request(url, new NormalRequestCallBack() {

            @Override
            public void onRequestComplete(String s, int i) {
                final RecommendmarqueeBean rBean = new Gson().fromJson(s, RecommendmarqueeBean.class);
                if (rBean.getStatus() == 0) {
                    //加载成功
                    final RecommendmarqueeBean.DataBean data = rBean.getData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SPreference.saveSharedPreferences(MainActivity.this, "my_setting", "recommend_marquee_cache", s);
                            //显示跑马灯
                            if (data != null) {
                                binding.scrollTv.setText(data.getMarquee());
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 获取推荐的背景图
     */
    private void getRecommendBgImg() {
        String url = getMyUrl("jhzBox/box/backgroundImg.do?", DeviceModelNumber, netUtils.getDeviceID().toUpperCase(), CustomId, netUtils.getIP0(), netUtils.getChineseRegion(netUtils.getLocation()), lunchname);
        OkHttpUtils.request(url, new NormalRequestCallBack() {
            @Override
            public void onRequestComplete(String s, int i) {
                if (i < 0) return;
                final RecommendbgBean rBean = new Gson().fromJson(s, RecommendbgBean.class);
                if (rBean.getStatus() == 0) {
                    SPreference.saveSharedPreferences(MainActivity.this, "my_setting", "recommend_bg_cache", s);
                    if (rBean.getStatus() == 0) {
                        //加载成功
                        final List<RecommendbgBean.DatabgBean> data = rBean.getData();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //显示背景图
                                if (data != null && data.size() != 0) {
                                    for (int i = 0; i < data.size(); i++) {
                                        if (!TextUtils.isEmpty(data.get(i).getBackgroundImgAddress())) {
                                            Glide.with(mContext).load(data.get(i).getBackgroundImgAddress()).into(binding.ivBg);
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * 获取推荐的logo列表
     */
    private void getRecommendLogo() {
        String url = getMyUrl("jhzBox/box/loadLogo.do?", DeviceModelNumber, netUtils.getDeviceID().toUpperCase(), CustomId, netUtils.getIP0(), netUtils.getChineseRegion(netUtils.getLocation()), lunchname);
        OkHttpUtils.request(url, new NormalRequestCallBack() {
            @Override
            public void onRequestComplete(String s, int i) {
                final RecommendmarqueeBean rBean = new Gson().fromJson(s, RecommendmarqueeBean.class);
                if (rBean.getStatus() == 0) {
                    SPreference.saveSharedPreferences(MainActivity.this, "my_setting", "recommend_logo_cache", s);
                    //加载成功
                    final RecommendlogoBean logoBean = new Gson().fromJson(s, RecommendlogoBean.class);
                    if (logoBean.getStatus() == 0) {
                        //加载成功
                        final RecommendlogoBean.DataBean data = logoBean.getData();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 显示品牌logo
                                if (data != null) {
                                    Glide.with(mContext).load(data.getSyy_special_fileOne()).into(binding.logoIv);
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void getRecommendAd() {
        String url = getMyUrl("jhzBox/box/loadAdv.do?", DeviceModelNumber, netUtils.getDeviceID().toUpperCase(), CustomId, netUtils.getIP0(), netUtils.getChineseRegion(netUtils.getLocation()), lunchname);
        OkHttpUtils.request(url, new NormalRequestCallBack() {
            @Override
            public void onRequestComplete(String s, int i) {
                if (i < 0) return;
                final Recommend3Bean rBean = new Gson().fromJson(s, Recommend3Bean.class);
                SPreference.saveSharedPreferences(MainActivity.this, "my_setting", "recommend_ad_cache", s);
                if (rBean.getStatus() == 0) {
                    //加载成功
                    final List<Recommend3Bean.DataBean> data = rBean.getData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //显示广告
                            String urls = null;
                            if (data != null && data.size() > 0) {
                                for (int i = 0; i < data.size(); i++) {
                                    //广告链接
                                    web.put(data.get(i).getCy_advertisement_imgAddress(), data.get(i).getAdvLink());
                                    //视频广告链接
                                    if (data.get(i).getCy_advertisement_videoAddress() != null) {
                                        urls = (String) data.get(i).getCy_advertisement_videoAddress();
                                    }
                                }
                                if (urls == null) {
                                    binding.adBg.stopAutoPlay();
                                    binding.adBg.setData(R.layout.ad_item, data, null);
                                    binding.adBg.setmAdapter(new XBanner.XBannerAdapter() {
                                        @Override
                                        public void loadBanner(XBanner banner, Object model, View view, int position) {
                                            String url = data.get(position).getCy_advertisement_imgAddress();
                                            //获取被点击广告页的网址链接
                                            //String ad_id = null;
                                            if (position == 0) {
                                                ClickOnTheAD = data.get(data.size() - 1).getCy_advertisement_imgAddress();
                                                AD_Name = data.get(data.size() - 1).getCy_advertisement_id();
                                            } else {
                                                ClickOnTheAD = data.get(position - 1).getCy_advertisement_imgAddress();
                                                AD_Name = data.get(position - 1).getCy_advertisement_id();
                                            }
                                            if (!TextUtils.isEmpty(url)) {
                                                Glide.with(mContext).load(url).into((SimpleDraweeView) view);
                                            }
                                        }
                                    });
                                    binding.adBg.startAutoPlay();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 统计用户在线时长
     */
    private void caculateOnlineTime() {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String url = getMyUrl("jhzBox/box/onlineTime.do?&", DeviceModelNumber, netUtils.getDeviceID().toUpperCase(), CustomId, netUtils.getIP0(), netUtils.getChineseRegion(netUtils.getLocation()), lunchname);
                        OkHttpUtils.request(url, new NormalRequestCallBack() {
                            @Override
                            public void onRequestComplete(String s, int i) {

                            }
                        });
                        sleep(1000 * 60 * 5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * 清理数据
     */
    private void clearData() {
        String url = getMyUrl("jhzBox/box/removeIp.do?", DeviceModelNumber, netUtils.getDeviceID().toUpperCase(), CustomId, netUtils.getIP0(), netUtils.getChineseRegion(netUtils.getLocation()), lunchname);
        OkHttpUtils.request(url, new NormalRequestCallBack() {
            @Override
            public void onRequestComplete(String s, int i) {

            }
        });
    }

    /**
     * 统计用户点击app次数
     */
    private void CaculateclickApp(String app) {
        String url = getMyUrl("jhzBox/box/appLike.do?", DeviceModelNumber, netUtils.getDeviceID().toUpperCase(), CustomId, netUtils.getIP0(), netUtils.getChineseRegion(netUtils.getLocation()), lunchname);
        OkHttpUtils.request(url, new NormalRequestCallBack() {
            @Override
            public void onRequestComplete(String s, int i) {

            }
        });

    }

    /**
     * 统计用户点击广告的次数
     */
    private void caculateUserclickAd(String ad_) {
        String url = getMyUrl("jhzBox/box/advLike.do?cy_advertisement_id=" + ad_ + "&", DeviceModelNumber, netUtils.getDeviceID().toUpperCase(), CustomId, netUtils.getIP0(), netUtils.getChineseRegion(netUtils.getLocation()), lunchname);
        OkHttpUtils.request(url, new NormalRequestCallBack() {
            @Override
            public void onRequestComplete(String s, int i) {

            }
        });
    }

    /**
     * 检查更新版本
     */
    private void checkSoftwareVersion() {
        String url = getMyUrl("jhzBox/box/appOnlineVersion.do?versionNum=" + BuildConfig.VERSION_NAME + "&cy_versions_name=" + appName, DeviceModelNumber, netUtils.getDeviceID().toUpperCase(), CustomId, netUtils.getIP0(), netUtils.getChineseRegion(netUtils.getLocation()), lunchname);
        OkHttpUtils.request(url, new NormalRequestCallBack() {
            @Override
            public void onRequestComplete(String s, int i) {
                if (i < 0) return;
                final RecommendversionBean versionBean = new Gson().fromJson(s, RecommendversionBean.class);
                if (versionBean.getStatus() == 0) {
                    final RecommendversionBean.DataBean data = versionBean.getData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(mContext)
                                    .setTitle(R.string.version_updating)
                                    .setMessage(data.getCy_versions_info())
                                    .setNegativeButton(R.string.cancles, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Toast.makeText(mContext, R.string.background_download, Toast.LENGTH_SHORT).show();
                                            downloadApk(data.getCy_versions_path());
                                        }
                                    }).show();
                        }
                    });
                }
            }
        });
    }

    private void getRecommendVideo() {
        String url = getMyUrl("xpBox/box/loadBoot.do?", DeviceModelNumber, netUtils.getDeviceID().toUpperCase(), CustomId, netUtils.getIP0(), netUtils.getChineseRegion(netUtils.getLocation()), lunchname);
        OkHttpUtils.request(url, new NormalRequestCallBack() {
            @Override
            public void onRequestComplete(String s, int i) {

            }
        });
    }

    /**
     * 带进度下载apk
     *
     * @param dataBean
     * @param index
     */
    private void downloadApk(final RecommendBean.DataBean dataBean, final int index) {
        String url = dataBean.getSyy_app_download();//
        String toFilePath = AppsManager.getAppDir() + dataBean.getSyy_app_download().substring(dataBean.getSyy_app_download().lastIndexOf("/") + 1);
        OkHttpUtils.Download(url, toFilePath, dataBean.getSyy_app_packageName(), new NormalRequestCallBack() {
            @Override
            public void onRequestComplete(String s, int i) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (i >= 0) {
                            flItems[index].setEnabled(true);
                            pbItems[index].setVisibility(View.GONE);
                            tvItems[index].setVisibility(View.GONE);
                        } else {
                            flItems[index].setEnabled(true);
                            tvItems[index].setText(R.string.download_failed);
                        }
                    }
                });
            }
        });
    }

    /**
     * 普通下载apk安装
     *
     * @param url
     */
    private void downloadApk(final String url) {
        String toFilePath = AppsManager.getAppDir() + url.substring(url.lastIndexOf("/") + 1);
        OkHttpUtils.Download(url, toFilePath, this.getLocalClassName(), new NormalRequestCallBack() {
            @Override
            public void onRequestComplete(String s, int i) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (i >= 0) {
                            AppsManager.install(mContext, toFilePath);
                        } else {
                            Toast.makeText(mContext, R.string.download_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }


    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String pull_from = "52129";
            Bundle bundle = intent.getExtras();

            if (bundle == null) return;

            String _action = bundle.getString("_Action");


            if (_action == null) return;

            if ((_action.contains("首页")) || (_action.contains("桌面")) || (_action.contains("主页"))) {

                binding.ivFill.setVisibility(View.GONE);
                View rootview = MainActivity.this.getWindow().getDecorView();
            } else if (_action.contains("蓝牙")) {
                pauseSystemMusic();
                onClick(binding.fl11);
                //launchApp("com.h3launcher");
            } else if ((_action.contains("同轴")) || (_action.contains("同舟"))) {
                pauseSystemMusic();
                onClick(binding.fl12);
                //launchApp("com.h3launcher");
            } else if (_action.contains("光纤")) {
                pauseSystemMusic();
                onClick(binding.fl13);
                //launchApp("com.h3launcher");
            } else if ((_action.contains("输入")) || (_action.contains("Line in")) || (_action.contains("模拟"))) {
                pauseSystemMusic();
                onClick(binding.fl14);
                //Intent i = new Intent();
                //launchApp("com.h3launcher");
            } else if (_action.contains("USB") || _action.contains("U盘") || _action.contains("TF卡") || _action.contains("优盘") || _action.contains("卡")) {
                onClick(binding.fl15);
                Intent freshIntent = new Intent();
                freshIntent.setAction("com.android.music.musicservicecommand");
                freshIntent.putExtra("command", "play");
                sendBroadcast(freshIntent);
                //launchApp("com.android.music");
                //PackageManager packageManager = getPackageManager();
                //Intent openQQintent = new Intent();
                //openQQintent = packageManager.getLaunchIntentForPackage("com.android.music");
            } else if (_action.contains("文件")) {
                pauseSystemMusic();
                onClick(binding.fl6);
                //launchApp("com.softwinner.TvdFileManager");
            } else if (_action.contains("设置") || _action.contains("网络")) {
                onClick(binding.fl7);
                //openSettings();
            } else if (_action.contains("频道")) {
                binding.ivFill.setVisibility(View.GONE);
                onClick(binding.fl8);
            } else if ((_action.contains("全民K歌")) || (_action.contains("我要唱歌")) || (_action.contains("我想唱歌")) || (_action.contains("K歌")) || (_action.contains("KTV"))) {
                handleViewOnClic(binding.fl0, -1, true);
            } else if ((_action.contains("腾讯视频")) || (_action.contains("云视听"))) {
                handleViewOnClic(binding.fl1, -1, true);
            } else if (_action.contains("应用") || _action.contains("程序")) {
                AppsActivity.lunchAppsActivity(MainActivity.this, MY_APP_TYPE);
            } else if (_action.contains("最近")) {
                AppsActivity.lunchAppsActivity(MainActivity.this, RECENT_TYPE);
            } else if ((_action != null) && (_action.equals("music") || _action.equals("ktv"))) {
            }
        }
    }

    class HomeWatcherReceiver extends BroadcastReceiver {

        private static final String LOG_TAG = "HomeReceiver";
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
        private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";
        private static final String ACTION_BATTERY_CHARGE = "BATTERY_CHARGE";
        private static final String ACTION_BATTERY_INFO = "BATTERY_INFO";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //Log.i(LOG_TAG, "onReceive: action: " + action);
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                // android.intent.action.CLOSE_SYSTEM_DIALOGS
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);

                Log.i(LOG_TAG, "reason: " + reason);

                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                    // 短按Home键
                    Log.i(LOG_TAG, "homekey");
                    binding.ivFill.setVisibility(View.GONE);
                } else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                    // 长按Home键 或者 activity切换键
                    Log.i(LOG_TAG, "long press home key or activity switch");
                    //binding.ivFill.setVisibility(View.GONE);
                } else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
                    // 锁屏
                    //binding.ivFill.setVisibility(View.GONE);
                    Log.i(LOG_TAG, "lock");
                } else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
                    // samsung 长按Home键
                    binding.ivFill.setVisibility(View.GONE);
                    Log.i(LOG_TAG, "assist");
                }
            } else if (action.equals(ACTION_BATTERY_CHARGE)) {
                isCharging = intent.getBooleanExtra("isCharge", false);
                if (isCharging) {
                    binding.ivBattery.setImageResource(R.drawable.charge);
                    //binding.bgIv5.setVisibility(View.INVISIBLE);
                } else {
                    binding.bgIv5.setVisibility(View.VISIBLE);
                }
            } else if (action.equals(ACTION_BATTERY_INFO) && !isCharging) {
                int value = intent.getIntExtra("value", -1);
                binding.ivBattery.setVisibility(View.VISIBLE);
                binding.bgIv5.setVisibility(View.VISIBLE);

                if ((value < 5)) {
                    binding.bgIv5.setVisibility(View.VISIBLE);
                    binding.bgIv5.setImageResource(R.drawable.battery_warnning);
                }
                if (value < 10) {
                    binding.ivBattery.setImageResource(R.drawable.lowbattery);

                } else if (value <= 20)
                    binding.ivBattery.setImageResource(R.drawable.cell1);
                else if (value <= 60)
                    binding.ivBattery.setImageResource(R.drawable.cell2);
                else if (value <= 80)
                    binding.ivBattery.setImageResource(R.drawable.cell3);
                else if (value > 80)
                    binding.ivBattery.setImageResource(R.drawable.cell4);


            } else if (action.equals("BLUTOOLTH_STATUS")) {
                blutoothConnected = intent.getBooleanExtra("BLUTOOLTH_STATUS", false);
                if (blutoothConnected) {
                    binding.bluetooth.setVisibility(View.VISIBLE);
                    binding.bluetooth.setImageResource(R.drawable.bluetoothhave);
                } else {
                    binding.bluetooth.setVisibility(View.VISIBLE);
                    binding.bluetooth.setImageResource(R.drawable.bluetoothno);
                }
            } else if (action.equals("COMMAND_DATA")) {
                openSettings();
            }
        }
    }

    public class BootCompletedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.getAction().equals("com.iflytek.xiri.init.start")) {
                    Intent iii;
                    iii = new Intent(MainActivity.this, iflytekService.class);
                    Log.d(TAG, "com.iflytek.xiri.init.start");
                    startService(iii);
                }
                if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                    Intent iii;
                    iii = new Intent(MainActivity.this, iflytekService.class);
                    Log.d(TAG, "android.intent.action.BOOT_COMPLETED");
                    startService(iii);
                }
            }
        }
    }


}

