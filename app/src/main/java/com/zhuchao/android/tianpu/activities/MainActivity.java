package com.zhuchao.android.tianpu.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
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

import android.databinding.DataBindingUtil;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
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

import com.bumptech.glide.Glide;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.stx.xhb.xbanner.XBanner;
import com.stx.xhb.xbanner.transformers.Transformer;
import com.zhuchao.android.tianpu.BuildConfig;
import com.zhuchao.android.tianpu.R;
import com.zhuchao.android.tianpu.bridge.SelEffectBridge;
import com.zhuchao.android.tianpu.data.App;
import com.zhuchao.android.tianpu.data.PackageName;
import com.zhuchao.android.tianpu.data.PreInstallApkPath;
import com.zhuchao.android.tianpu.data.json.regoem.CheckMacBean;
import com.zhuchao.android.tianpu.data.json.regoem.IpBean;
import com.zhuchao.android.tianpu.data.json.regoem.Recommend3Bean;
import com.zhuchao.android.tianpu.data.json.regoem.RecommendBean;
import com.zhuchao.android.tianpu.data.json.regoem.RecommendbgBean;
import com.zhuchao.android.tianpu.data.json.regoem.RecommendlogoBean;
import com.zhuchao.android.tianpu.data.json.regoem.RecommendmarqueeBean;
import com.zhuchao.android.tianpu.data.json.regoem.RecommendversionBean;
import com.zhuchao.android.tianpu.data.json.regoem.RecommendvideoBean;
import com.zhuchao.android.tianpu.data.json.regoem.RemoveAppBean;

import com.zhuchao.android.tianpu.databinding.ActivityMainBinding;
import com.zhuchao.android.tianpu.services.MyService;
import com.zhuchao.android.tianpu.services.iflytekService;
import com.zhuchao.android.tianpu.utils.AppHandler;
import com.zhuchao.android.tianpu.utils.AppManager;
import com.zhuchao.android.tianpu.utils.GlideMgr;
import com.zhuchao.android.tianpu.utils.HttpUtils;
import com.zhuchao.android.tianpu.utils.NetTool;
import com.zhuchao.android.tianpu.utils.ShareAdapter;
import com.zhuchao.android.tianpu.utils.TimeHandler;
import com.zhuchao.android.tianpu.utils.Utils;
import com.zhuchao.android.tianpu.utils.WallperHandler;
import com.zhuchao.android.tianpu.views.dialogs.BottomAppDialog;
import com.zhuchao.android.tianpu.views.dialogs.HomeAppDialog;
import com.zhuchao.android.tianpu.views.dialogs.HomeAppsDialog;
import com.zhuchao.android.tianpu.views.dialogs.Mac_Dialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.ProgressManager;
import me.jessyan.progressmanager.body.ProgressInfo;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import utils.MySerialPort;

import static android.view.MotionEvent.ACTION_UP;
import static com.zhuchao.android.tianpu.utils.PageType.HOME_TYPE;
import static com.zhuchao.android.tianpu.utils.PageType.MY_APP_TYPE;
import static com.zhuchao.android.tianpu.utils.PageType.RECENT_TYPE;

public class MainActivity extends Activity implements OnTouchListener, OnGlobalFocusChangeListener,
        View.OnClickListener, TimeHandler.OnTimeDateListener, NetTool.OnNetListener,
        AppHandler.OnScanListener, AppHandler.OnAddRemoeveListener,
        View.OnKeyListener, AppHandler.OnBottomListener, WallperHandler.OnWallperUpdateListener, ServiceConnection {

    private static final String TAG = "MainActivity";

    private boolean isDownloadingMarket = false;
    private Mac_Dialog mdialog;
    private ActivityMainBinding binding;
    private SelEffectBridge selEffectBridge;
    private TimeHandler timeHandler;
    private NetTool netTool;
    public AppHandler appHandler;

    private HomeAppDialog homeAppDialog;
    private HomeAppsDialog homeAppsDialog;
    private BottomAppDialog bottomAppDialog;

    private boolean updateScanOK = false;
    private boolean marqueeScanOK = false;
    private List<String> imgUrls;

    private String mSerialData;
    //    private String img;
    private Context mContext;
    private int nba = 0;
    private String saveType = null;
    private String url;
    private boolean isFive = false;
    private Drawable lastApps = null;
    private boolean isThelast = false;
    private boolean isDefault = true;
    private static HomeWatcherReceiver mHomeKeyReceiver = null;
    private MyReceiver receiver = null;
    private BootCompletedReceiver mBootCompletedReceiver = null;
    /**
     * apk的下载状态
     * 0：没有下载过 1：下载中  2：下载完成  3：下载失败
     */
    private Map<String, Integer> apkType = new HashMap<>();

    /**
     * 获取相应APP的主页面的图片
     */
    private Map<String, String> stImg = new HashMap<>();

    //网络数据/cache
    private RecommendBean recommendBean;
    private RecommendlogoBean recommendlogoBean;
    private Recommend3Bean recommend3Bean;
    private RecommendmarqueeBean recommendmarqueeBean;
    private RecommendbgBean recommendbgBean;

    //加载推荐app成功
    private boolean isLoadAppSucc = false;
    private boolean isCheckVersion = false;
    private FrameLayout[] flItems;
    private ProgressBar[] pbItems;
    private TextView[] tvItems;

    private ImageView[] ivs;

    String cacheImg = "";
    public static final String bootVideo = "/system/media/boot.mp4";
    public static final String newBootVideo = "/system/media/new_boot.zip";
    private String deviceId = "750";//TVBOX 天谱
    private String host = "http://www.gztpapp.cn:8976/";    //天谱  （节流）
    private int cid = -1;    //客户号  (国内)  录入版
    private String appName = "TP0BDK70Q";      //天谱 （国内万利达）
    private String lunchname = "TP0BDK70Q";


    private String netMac;
    private String cidIP;   //设备的IP
    private String region; //设备所在地区

    private String lastoneApp = null;

    private String removeResult;


    private MySerialPort serialPortUtils = new MySerialPort(this);
    private MyService myService;
    private Handler SerialPortReceivehandler;
    private byte[] SerialPortReceiveBuffer;
    //private byte[] BluetoothOpen = {0x02, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, 0x04, 0x00, 0x0E, 0x7E};//蓝牙  K70 //  serialPortUtils.sendBuffer(BluetoothOpen,SizeOf(BluetoothOpen));
    private byte[] BluetoothClose = {0x01, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, 0x04, 0x00, 0x0D, 0x7E};//蓝牙  K50
    private int bluetooth = 0;
    //private byte[] CopperShaftLineIn = {0x02, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, 0x00, 0x01, 0x0B, 0x7E};//同轴  K70
    private byte[] CopperShaftClose = {0x01, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, 0x00, 0x01, 0x0A, 0x7E};//同轴     K50
    private int coppershaft = 0;
    //private byte[] OpticalFiberLineIn = {0x02, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, 0x00, 0x02, 0x0C, 0x7E};//光纤   K70
    private byte[] OpticalFiberClose = {0x01, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, 0x00, 0x02, 0x0B, 0x7E};//光纤    K50
    private int opticalfiber = 0;
    //private byte[] SimulationLineIn = {0x02, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, (byte) 0x80, 0x00, (byte) 0x8A, 0x7E};//模拟  K70
    private byte[] SimulationLineInClose = {0x01, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, (byte) 0x80, 0x00, (byte) 0x89, 0x7E};//模拟  K50
    private int simulation = 0;
    //private byte[] UsbOpen = {0x02, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, 0x08, 0x00, 0x12, 0x7E};//USB  K70
    private byte[] UsbClose = {0x01, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, 0x08, 0x00, 0x11, 0x7E};//USB  K50

    //private byte[] LastAppOpen = {0x02, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x0A, 0x7E};//最后使用的app  K70
    private byte[] LastChanelApp = {0x01, 0x01, 0x05, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x09, 0x7E};//最后使用的app  K50

    //private byte[] QueryState = {0x02, 0x01, 0x06, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x0A, 0x7E};//初始状态 K70 //
    private byte[] QueryStateK50 = {0x01, 0x01, 0x06, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x09, 0x7E};//初始状态  K50

    private static final String StartDragonTest = "1379";//测试
    private static final String StartDragonAging = "2379";//老化
    private static final String versionInfo = "3379";//版本信息

    private long oldTime = 0;
    private String num = "";
    private boolean isCharging = false;
    private boolean blutoothConnected = false;
    private View OldView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //启动服务
        StartService();

        //跳转到广告视频界面
        //startVedioPlayerActivity();
        //kaijiziqi();
        mContext = this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        flItems = new FrameLayout[]{binding.fl1, binding.fl2, binding.fl4};//需要下载k歌，腾讯视频，qq音乐
        pbItems = new ProgressBar[]{binding.progressBar1, binding.progressBar2, binding.progressBar9};
        tvItems = new TextView[]{binding.tvState1, binding.tvState2, binding.tvState9};
        //ivs = new SimpleDraweeView[]{binding.bgIv1, binding.bgIv8};
        ivs = new ImageView[]{binding.bgIv1, binding.bgIv2};

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

        //时间更新
        timeHandler = new TimeHandler(this);
        timeHandler.setOnTimeDateListener(this);
        //监听网络状态,用于显示标识
        netTool = new NetTool(this);
        netTool.setOnNetListener(this);

        appHandler = new AppHandler(MainActivity.this, HOME_TYPE);
        appHandler.setOnScanListener(this);
        appHandler.setAddRemoeveListener(this);
        appHandler.setOnBottomListener(this);

        appHandler.scanRecent();
        appHandler.scan();

        //binding.mac.setVisibility(View.GONE);
        String mac = Utils.getDevID().toUpperCase();
        binding.mac.setText(String.format("MAC: %s", mac));
        NetTool.setMac(mac);
        binding.scrollTv.setText("欢迎来到天谱！Welcome to Tianpu!欢迎来到天谱！Welcome to Tianpu!欢迎来到天谱！Welcome to Tianpu!");
        binding.scrollTv.setSelected(true);


        bindService(new Intent(this, MyService.class), this, BIND_AUTO_CREATE);

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
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.zhuchao.android.tianpu.services");
        registerReceiver(receiver, filter);

        mBootCompletedReceiver = new BootCompletedReceiver();
        filter = new IntentFilter();
        filter.addAction("com.iflytek.xiri.init.start");
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        registerReceiver(mBootCompletedReceiver, filter);

        Intent iii;
        iii = new Intent(MainActivity.this, iflytekService.class);
        Log.d(TAG, "start iflytekService");
        startService(iii);

        //requestPermition();

        new Thread() {
            @Override
            public void run() {
                super.run();
                while (true) {
                    try {
                        onlineTime();
                        sleep(1000 * 60 * 5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();


        selEffectBridge = (SelEffectBridge) binding.mainUpView.getEffectBridge();
        binding.mainRl.getViewTreeObserver().addOnGlobalFocusChangeListener(this);
        setupItemBottomTag();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Log.e(TAG, "onStart");
        timeHandler.regTimeReceiver();
        netTool.registerNetReceiver();
        appHandler.regAppReceiver();
        appHandler.scanHome();//显示最近使用历史，扫描添加本地应用
        appHandler.scanRecent();

        if (bottomAppDialog != null) {
            appHandler.scanBottom();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        Log.d(TAG, "launcher is stop");
        timeHandler.unRegTimeReceiver();
        netTool.unRegisterNetReceiver();
        appHandler.unRegAppReceiver();
        binding.adBg.stopAutoPlay();
    }

    /**
     * 暂停系统播放器
     */
    private void pauseMusic() {
        Intent freshIntent = new Intent();
        freshIntent.setAction("com.android.music.musicservicecommand.pause");
        freshIntent.putExtra("command", "pause");
        sendBroadcast(freshIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //gotTheLastOne();
        pauseMusic();
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
        appHandler.scanRecent();
        appHandler.scan();

        unregisterHomeKeyReceiver(this);
        //mBatteryHandler.removeCallbacksAndMessages(null);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        Log.d(TAG, "launcher was killed");
        timeHandler.release();
        netTool.release();
        appHandler.release();
        //wallperHandler.release();
        timeHandler.setOnTimeDateListener(null);
        netTool.setOnNetListener(null);
        appHandler.setAddRemoeveListener(null);
        appHandler.setOnScanListener(null);
        timeHandler = null;
        netTool = null;
    }


    @Override
    public void onClick(View v) {
        //v.requestFocus();
        handleViewKey(v, -1, true);
        return;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.requestFocus();
        if (event.getAction() == ACTION_UP)
            handleViewKey(v, -1, true);

        return true;//super.onTouchEvent(event);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        //Log.e("key","onKey>>>>keyCode="+keyCode+"    KeyEvent="+event);
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MENU:
                    //Toast.makeText(mContext, "menu1", Toast.LENGTH_SHORT).show();
                    handleViewKey(v, keyCode, false);
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
        Log.e(TAG, "onTimeDate " + time + " " + date);
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
    public void onNetState(boolean isConnected, int type) {
        boolean b = NetTool.isNetworkOK();
        if (b) {
            String n = getEthernetMacAddress();
            String w = getWiFiMacAddress(mContext);
            if (!"".equals(n) && null != n) {
                netMac = n;   //设备的以太网mac地址
            } else if (!"".equals(w) && null != w) {
                netMac = w;   //设备的WIFI的mac地址
            }

            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        GetNetIp();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            if (!binding.netIv.isShown()) {
                binding.netIv.setVisibility(View.VISIBLE);
            }
            switch (type) {
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
                    binding.netIv.setImageResource(R.drawable.net);
                    break;
            }
        } else {
            binding.netIv.setImageResource(R.drawable.netno);
            //binding.netIv.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void wifiLevel(int level) {
        switch (level) {
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
            default:
                binding.netIv.setImageResource(R.drawable.wifi_out_of_range);
                break;
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
     * 显示指定app的主页
     */
    private void ShowMasterPages() {
        int s = -1;
        if (null != stImg) {
            Iterator<String> it = stImg.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                if (null != lastoneApp) {
                    if (key.equals(lastoneApp)) {
                        Glide.with(mContext).load(stImg.get(key)).into(binding.bgIv5);
                        s = 1;
                    }
                } else {
                    binding.bgIv5.setImageResource(R.drawable.tp10);
                }
            }
            if (s == -1) {
                binding.bgIv5.setImageResource(R.drawable.tp10);
            }
        } else {
            binding.bgIv5.setImageResource(R.drawable.tp10);
        }
    }

    public void showHomeAppsDialog(int rId) {
        homeAppsDialog = HomeAppsDialog.showHomeAppDialog(MainActivity.this, rId);
    }

    public void scan() {
        appHandler.scan();
    }

    public void scanRecent() {
        appHandler.scanRecent();
    }

    public void launchApp(String packageName) {
        if (PackageName.qqMusic.equals(packageName)) {
            handleClick(packageName, PreInstallApkPath.qqMusic);
        } else if (PackageName.qqTv.equals(packageName)) {
            handleClick(packageName, PreInstallApkPath.qqTv);
        } else if (PackageName.hdp.equals(packageName)) {
            handleClick(packageName, PreInstallApkPath.hdp);
        }
        appHandler.launchApp(packageName);
    }

    private void handleClick(String packageName, String filePath) {
        if (!AppManager.isInstallApp(mContext, packageName)) {
            if (AppManager.isSystemAppInstallOk(mContext, filePath)) {
                Toast.makeText(mContext, "正在安装应用", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 处理点击事件、菜单键
     *
     * @param v
     * @param keyCode
     * @param isClick true:点击 false:菜单
     */
    private void handleViewKey(View v, int keyCode, boolean isClick) {
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
                isThelast = false;
                saveType = "蓝牙";
                binding.bgIcon.setVisibility(View.GONE);
                //StartServiceSendBytes(BluetoothOpen);
                StartServiceSendBytes(BluetoothClose);
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
                saveType = "同轴";
                binding.bgIcon.setVisibility(View.GONE);
                coppershaft++;
                isThelast = false;
                //StartServiceSendBytes(CopperShaftLineIn);
                StartServiceSendBytes(CopperShaftClose);
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
                saveType = "光纤";
                binding.bgIcon.setVisibility(View.GONE);
                opticalfiber++;
                isThelast = false;
                //StartServiceSendBytes(OpticalFiberLineIn);
                StartServiceSendBytes(OpticalFiberClose);
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
                saveType = "模拟";
                isThelast = false;
                binding.bgIcon.setVisibility(View.GONE);
                //StartServiceSendBytes(SimulationLineIn);
                StartServiceSendBytes(SimulationLineInClose);
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
                isThelast = false;
                saveType = "player";
                binding.bgIcon.setVisibility(View.GONE);
                StartServiceSendBytes(UsbClose);
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
                saveType = "last";
                if (null != lastoneApp) {
                    launchApp(lastoneApp);
                }
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
                if (null != saveType && !"".equals(saveType)) {
                    if (saveType.equals("蓝牙")) {
                        binding.ivFill.setVisibility(View.VISIBLE);
                        binding.ivFill.setImageResource(R.drawable.bly);
                        binding.bluetooth.setVisibility(View.VISIBLE);
                        binding.bluetooth.setImageResource(R.drawable.bluetoothno);
                        binding.bgIcon.setVisibility(View.GONE);

                        bluetooth++;
                    } else if (saveType.equals("同轴")) {
                        binding.ivFill.setVisibility(View.VISIBLE);
                        binding.ivFill.setImageResource(R.drawable.btz);
                        binding.bluetooth.setVisibility(View.GONE);
                        binding.bgIcon.setVisibility(View.GONE);
                        onClick(binding.fl12);
                        coppershaft++;
                    } else if (saveType.equals("光纤")) {
                        binding.ivFill.setVisibility(View.VISIBLE);
                        binding.ivFill.setImageResource(R.drawable.bopt);
                        binding.bluetooth.setVisibility(View.GONE);
                        //onClick(binding.fl13);
                        opticalfiber++;
                        binding.bgIcon.setVisibility(View.GONE);
                    } else if (saveType.equals("模拟")) {
                        binding.ivFill.setVisibility(View.VISIBLE);
                        binding.ivFill.setImageResource(R.drawable.bmn);
                        binding.bluetooth.setVisibility(View.GONE);
                        binding.bgIcon.setVisibility(View.GONE);
                        //onClick(binding.fl14);
                    } else if (saveType.equals("player")) {
                        binding.ivFill.setVisibility(View.VISIBLE);
                        binding.ivFill.setImageResource(R.drawable.busbortf);
                        binding.bluetooth.setVisibility(View.GONE);
                        //launchApp("com.softwinner.TvdFileManager");
                        //launchApp("com.android.music");
                        binding.bgIcon.setVisibility(View.GONE);
                        binding.bluetooth.setVisibility(View.GONE);
                    } else if (saveType.equals("last")) {
                        binding.bluetooth.setVisibility(View.GONE);
                        if (null != lastoneApp) {
                            //launchApp(lastoneApp);
                        }
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

        getSharedPreferences("saveType", MODE_PRIVATE).edit().putString("saveType", saveType).commit();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (SerialPortReceivehandler == null) {
            SerialPortReceivehandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                }
            };
        }
        MyService.Binder binder = (MyService.Binder) service;
        myService = binder.getService();


        myService.setActionCallback(new MyService.Callback() {
            @Override
            public void onDataChange(String data) {
                mSerialData = data;
                Log.i("Callback.onDataChange", "data=" + data);
                if (null != SerialPortReceivehandler) {
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                sleep(200);
                                SerialPortReceivehandler.post(runnable);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } else {
                    Log.e("tag", "SerialPortReceivehandler=null");
                }
            }
        });
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            isThelast = false;
            if (mSerialData.equals("0201050000020000010B7E") || mSerialData.equals("0101050000020000010A7E")) {
                pauseMusic();
                //同轴
                saveType = "同轴";

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
                pauseMusic();
                saveType = "蓝牙";

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
                if (blutoothConnected)
                    isFive = true;

            } else if (mSerialData.equals("0201050000020080008A7E") || mSerialData.equals("010105000002008000897E")) {
                pauseMusic();
                //模拟
                saveType = "模拟";

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
            } else if (mSerialData.equals("0201050000020000020C7E") || mSerialData.equals("0101050000020000020B7E")) {
                pauseMusic();
                //光纤
                saveType = "光纤";

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


            } else if (mSerialData.equals("0201050000020000000A7E") || mSerialData.equals("010105000002000000097E")) {
                pauseMusic();
                //最后的app
                saveType = "last";
                isThelast = true;
                //binding.fl16.requestFocus();
                binding.bgIv111.setImageResource(R.drawable.xaa);
                binding.bgIv113.setImageResource(R.drawable.xac);
                //binding.bgIv116.setImageResource(R.drawable.blue6);
                binding.bgIv112.setImageResource(R.drawable.xab);
                binding.bgIv114.setImageResource(R.drawable.xad);
                binding.bgIv115.setImageResource(R.drawable.xae);
                if (null != lastoneApp && !"".equals(lastoneApp)) {
                    launchApp(lastoneApp);
                }

            } else if (mSerialData.equals("020105000002000800127E") || mSerialData.equals("010105000002000800117E")) {
                pauseMusic();
                //usb/TF
                saveType = "player";
                binding.bgIv111.setImageResource(R.drawable.xaa);
                binding.bgIv113.setImageResource(R.drawable.xac);
                //binding.bgIv116.setImageResource(R.drawable.nnn);
                binding.bgIv112.setImageResource(R.drawable.xab);
                binding.bgIv114.setImageResource(R.drawable.xad);
                binding.bgIv115.setImageResource(R.drawable.xse);
                binding.fl15.requestFocus();
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

            getSharedPreferences("saveType", MODE_PRIVATE).edit().putString("saveType", saveType).commit();
        }
    };


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
        clickAd(ad_id);
        if (!"".equals(url) && null != url) {
            Intent intent = new Intent();
            intent.setData(Uri.parse(url));
            intent.setAction(Intent.ACTION_VIEW);
            this.startActivity(intent);
        } else {
            Toast.makeText(mContext, R.string.no_browsers, Toast.LENGTH_SHORT).show();
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
                if (AppManager.isInstallApp(mContext, dataBean.getSyy_app_packageName())) {
                    launchApp(dataBean.getSyy_app_packageName());
                    clickApp(dataBean.getSyy_app_id());
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
                            final String filePath = AppManager.getAppDir() + dataBean.getSyy_app_download().substring(dataBean.getSyy_app_download().lastIndexOf("/") + 1);

                            flItems[index].setEnabled(false);
                            tvItems[index].setText(R.string.download);
                            tvItems[index].setVisibility(View.VISIBLE);
                            pbItems[index].setVisibility(View.VISIBLE);
                            ProgressManager.getInstance().addResponseListener(dataBean.getSyy_app_download(), new ProgressListener() {
                                @Override
                                public void onProgress(ProgressInfo progressInfo) {
                                    pbItems[index].setProgress(progressInfo.getPercent());
                                    if (progressInfo.isFinish()) {
                                        AppManager.install(mContext, filePath);
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
                        final String filePath = AppManager.getAppDir() + dataBean.getSyy_app_download().substring(dataBean.getSyy_app_download().lastIndexOf("/") + 1);
                        AppManager.install(mContext, filePath);
                    }
                }
            }

        }
    }


    // 从系统文件中获取以太网MAC地址
    public static String getEthernetMacAddress() {
        try {
            return loadFileAsString("/sys/class/net/eth0/address").toUpperCase().substring(0, 17);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 读取系统文件
    private static String loadFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    // 从系统文件中获取WIFI MAC地址
    public static String getWiFiMacAddress(Context context) {
        WifiManager my_wifiManager = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE));
        WifiInfo wifiInfo = my_wifiManager.getConnectionInfo();
        return wifiInfo.getMacAddress();
    }


    /**
     * 获取外网IP
     *
     * @return
     */
    private void GetNetIp() {
        URL infoUrl = null;
        InputStream inStream = null;
        try {
            infoUrl = new URL("http://ip-api.com/json/");
            URLConnection connection = infoUrl.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));
                StringBuilder strber = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null)
                    strber.append(line + "\n");
                inStream.close();
                //从反馈的结果中提取出IP地址
                line = strber.toString();
                final IpBean rBean = new Gson().fromJson(line, IpBean.class);
                cidIP = rBean.getQuery();
                String province = rBean.getRegionName();
                ModifyTheLanguageOfTheRegion(province);
            }
            //获取到IP等信息后检查该设备是否可用
            checkMac(Utils.getDevID().toUpperCase());
            Log.e("Tag", "netMac=" + netMac + "     cidIP=" + cidIP + "    region=" + region + "     >" + Utils.getDevID().toUpperCase());

            //getRecommendVideo();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handleViewKeyDown(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fl6:
            case R.id.ad:
            case R.id.fl4:
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
        appHandler.scanBottom();
    }


    private void inputNumber(String i) {
        long inputTime = System.currentTimeMillis();
        if (inputTime - oldTime < 1000) {
            //1s内输入有效
            if (i.equals("BACK")) {
                nba++;
            }
            num += i;
        } else {
            //如果输入时间超过1s,num统计的值重置为输入值
            num = i;
            nba = 0;
        }
        oldTime = inputTime;
        switch (nba) {
            case 8:
                oldTime = 0;
                clearData();
                nba = 0;
                break;
        }
        switch (num) {
            case StartDragonTest:
                //重置输入
                num = "";
                oldTime = 0;
//                Toast.makeText(this, "启动测试:", Toast.LENGTH_SHORT).show();
                if (AppManager.isInstallApp(mContext, "com.wxs.scanner")) {
//                    startActivity(new Intent().setClassName("com.kong.apptesttools", "com.kong.apptesttools.MainActivity"));
                    startActivity(new Intent().setClassName("com.wxs.scanner", "com.wxs.scanner.activity.workstation.CheckActivity"));
                } else {
//                    Toast.makeText(mContext, "未安装测试App", Toast.LENGTH_SHORT).show();
                    Toast.makeText(mContext, R.string.no_install_app, Toast.LENGTH_SHORT).show();
                }
                break;
            case StartDragonAging:
                //重置输入
                num = "";
                oldTime = 0;
//                Toast.makeText(this, "启动老化测试:", Toast.LENGTH_SHORT).show();
                if (AppManager.isInstallApp(mContext, "com.softwinner.agingdragonbox")) {
                    AppManager.startAgingApk(mContext);
                } else {
//                    Toast.makeText(mContext, "未安装老化App", Toast.LENGTH_SHORT).show();
                    Toast.makeText(mContext, R.string.no_install_old_app, Toast.LENGTH_SHORT).show();
                }
                break;
            case versionInfo:
                num = "";
                oldTime = 0;
                String deviceName;
                if ("702".equals(deviceId)) {
                    deviceName = "柴喜";
                } else if ("701".equals(deviceId)) {
//                    deviceName = "拓普赛特";
                    deviceName = "Mở rộng khu vực";
                } else if ("704".equals(deviceId)) {
                    deviceName = "老凤祥";
                } else if ("696".equals(deviceId)) {
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
                num = "";
                oldTime = 0;
                View view1 = LayoutInflater.from(mContext).inflate(R.layout.test_cache, null);
                ((TextView) view1.findViewById(R.id.tv_content)).setText("" + removeResult);
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
//        List<String> tmpImgUrls = wallperHandler.getImgUrls();
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
//                Log.d(TAG, "loadBanner " + view);
                String url = imgUrls.get(position);
//                Log.d(TAG, "loadBanner " + url);
                if (!TextUtils.isEmpty(url)) {
                    ((SimpleDraweeView) view).setImageURI(Uri.parse(imgUrls.get(position)));
                }
            }
        });
        binding.adBg.startAutoPlay();
    }

    /**
     * 检查mac是否可用
     */
    private void checkMac(String mac) {

        // TODO: 2019/8/30 test 模拟有效的mac地址
        if (cid != -1) {
            url = host + "jhzBox/box/loadBox.do?cy_brand_id=" + deviceId + "&mac=" + mac + "&netCardMac=" + netMac + "&cid=" + cid + "&codeIp=" + cidIP + "&region=" + region;
        } else {
            url = host + "jhzBox/box/loadBox.do?cy_brand_id=" + deviceId + "&mac=" + mac + "&netCardMac=" + netMac + "&codeIp=" + cidIP + "&region=" + region;
        }

        HttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("tag", "访问失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    String res = response.body().string();
                    Log.e("Tag", "res=" + res);
                    final CheckMacBean checkMacBean = new Gson().fromJson(res, CheckMacBean.class);
                    if (checkMacBean.getStatus() != 0) {
                        //设备不可用
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showMACDialog(Utils.getDevID().toUpperCase());
                            }
                        });


                    } else {//设备可用时加载数据
//                        getRemoveApp();
                        if (!isLoadAppSucc) {
                            getRecommendApp();
                            getRecommendAd();
                            getRecommendLogo();
                            getRecommendMarquee();
                            getRecommendBgImg();
                        }
                    }
                    if (!isCheckVersion) {
                        checkVersion();
                    }
                }

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

    /**
     * 获取卸载的app列表
     */
    private void getRemoveApp() {
        String url = host + "jhzBox/box/unload.do?cy_brand_id=" + deviceId;
        HttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    String res = response.body().string();
//                    Log.e(TAG, "onResponse: " + res);
                    RemoveAppBean removeAppBean = new Gson().fromJson(res, RemoveAppBean.class);
                    if (removeAppBean.getStatus() == 0) {
                        //卸载app
                        List<String> data = removeAppBean.getData();
                        for (String pck : data) {
//                            removeResult = AppManager.unInstall(mContext, pck);
//                            AppManager.clientUninstall(pck);
//                            AppManager.uninstall(mContext,pck);
//                            AppManager.uninstallApp(mContext,pck);
                            if (AppManager.isInstallApp(mContext, pck)) {
//                                AppManager.myUninstall(mContext,pck);
//                                removeResult = AppManager.execCommand("rm", "-f", "/data/app/"+pck+"-1.apk");
//                                removeResult = AppManager.execCommand("pm", "uninstall",pck);
//                                removeResult = String.valueOf(AppManager.slientunInstall(pck));
                                AppManager.uninstallApk(mContext, pck);
                            }
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
        if (cid != -1) {
            url = host + "jhzBox/box/loadPushApp.do?pitClass=01&cy_brand_id=" + deviceId + "&mac=" + Utils.getDevID().toUpperCase() + "&lunchname=" + lunchname + "&netCardMac=" + netMac + "&cid=" + cid;
        } else if (cid == -1) {
            url = host + "jhzBox/box/loadPushApp.do?pitClass=01&cy_brand_id=" + deviceId + "&mac=" + Utils.getDevID().toUpperCase() + "&lunchname=" + lunchname + "&netCardMac=" + netMac;
        }
        HttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    final String res = response.body().string();
                    Log.e(TAG, "onResponse:app= " + res);
                    final RecommendBean rBean = new Gson().fromJson(res, RecommendBean.class);
                    if (rBean.getStatus() == 0) {
                        //加载成功
                        final List<RecommendBean.DataBean> data = rBean.getData();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //保存缓存
                                getSharedPreferences("my_setting", MODE_PRIVATE).edit().putString("recommend_cache", res).commit();
                                recommendBean = rBean;
                                isLoadAppSucc = true;
                                //加载坑位的图片
                                if (data != null && data.size() >= ivs.length) {
                                    for (int i = 0; i < ivs.length; i++) {
                                        if (!TextUtils.isEmpty(data.get(i).getSyy_app_img())) {
                                            Glide.with(mContext).load(data.get(i).getSyy_app_img()).into(ivs[i]);
                                        }
                                    }

//                                    if (data.get(2).getSyy_app_name() != null) {
//                                        binding.titleTv8.setVisibility(View.VISIBLE);
//                                        binding.titleTv8.setText(data.get(2).getSyy_app_name());
//                                    }
                                }
                                //加载指定app主页图片
                                if (data != null && data.size() != 0) {
                                    for (int i = 0; i < data.size(); i++) {
                                        String pkn = data.get(i).getSyy_app_packageName();
                                        String img = data.get(i).getSyy_appstatus_img();
                                        if (!"".equals(img) && null != img) {
                                            stImg.put(pkn, img);
                                        }
                                    }
                                }
                                //是否显示未下载标签
                                setupItemBottomTag();
                            }

                        });
                    }
                }
            }
        });
    }

    /**
     * 获取推荐的跑马灯
     */
    private void getRecommendMarquee() {
        if (cid != -1) {
            url = host + "jhzBox/box/loadMarquee.do?cy_brand_id=" + deviceId + "&mac=" + Utils.getDevID().toUpperCase() + "&lunchname=" + lunchname + "&netCardMac=" + netMac + "&cid=" + cid;
        } else if (cid == -1) {
            url = host + "jhzBox/box/loadMarquee.do?cy_brand_id=" + deviceId + "&mac=" + Utils.getDevID().toUpperCase() + "&lunchname=" + lunchname + "&netCardMac=" + netMac;
        }
        HttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    final String res = response.body().string();
//                    Log.e(TAG, "onResponse:ma= " + res);
                    final RecommendmarqueeBean rBean = new Gson().fromJson(res, RecommendmarqueeBean.class);
                    if (rBean.getStatus() == 0) {
                        //加载成功
                        final RecommendmarqueeBean.DataBean data = rBean.getData();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getSharedPreferences("my_setting", MODE_PRIVATE).edit().putString("recommend_marquee_cache", res).commit();
                                //显示跑马灯
                                if (data != null) {
                                    binding.scrollTv.setText(data.getMarquee());
                                }
                            }
                        });
                    }
                }
            }
        });
    }


    /**
     * 获取推荐的背景图
     */
    private void getRecommendBgImg() {
        String url = host + "jhzBox/box/backgroundImg.do?cy_brand_id=" + deviceId + "&lunchname=" + lunchname;
        HttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    final String res = response.body().string();
//                    Log.e(TAG, "onResponse:bg= " + res);
                    final RecommendbgBean rBean = new Gson().fromJson(res, RecommendbgBean.class);
                    if (rBean.getStatus() == 0) {
                        //加载成功
                        final List<RecommendbgBean.DatabgBean> data = rBean.getData();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getSharedPreferences("my_setting", MODE_PRIVATE).edit().putString("recommend_bg_cache", res).commit();
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
        if (cid != -1) {
            url = host + "jhzBox/box/loadLogo.do?&cy_brand_id=" + deviceId + "&mac=" + Utils.getDevID().toUpperCase() + "&lunchname=" + lunchname + "&netCardMac=" + netMac + "&cid=" + cid;
        } else if (cid == -1) {
            url = host + "jhzBox/box/loadLogo.do?&cy_brand_id=" + deviceId + "&mac=" + Utils.getDevID().toUpperCase() + "&lunchname=" + lunchname + "&netCardMac=" + netMac;
        }
        HttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    final String res = response.body().string();
//                    Log.e(TAG, "onResponse:logo= " + res);
                    final RecommendlogoBean logoBean = new Gson().fromJson(res, RecommendlogoBean.class);
                    if (logoBean.getStatus() == 0) {
                        //加载成功
                        final RecommendlogoBean.DataBean data = logoBean.getData();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getSharedPreferences("my_setting", MODE_PRIVATE).edit().putString("recommend_logo_cache", res).commit();
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

    /**
     * 获取推荐的广告列表
     */
    HashMap<String, String> web = new HashMap<>();
    String ClickOnTheAD = null;
    String ad_id = null;

    //广告视频链接
    String urls = null;

    private void getRecommendAd() {
        if (cid != -1) {
            url = host + "jhzBox/box/loadAdv.do?cy_brand_id=" + deviceId + "&mac=" + Utils.getDevID().toUpperCase() + "&lunchname=" + lunchname + "&netCardMac=" + netMac + "&cid=" + cid;
        } else if (cid == -1) {
            url = host + "jhzBox/box/loadAdv.do?cy_brand_id=" + deviceId + "&mac=" + Utils.getDevID().toUpperCase() + "&lunchname=" + lunchname + "&netCardMac=" + netMac;
        }
        HttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    final String res = response.body().string();
//                    Log.e(TAG, "onResponse:ad= " + res);
                    final Recommend3Bean rBean = new Gson().fromJson(res, Recommend3Bean.class);
                    if (rBean.getStatus() == 0) {
                        //加载成功
                        final List<Recommend3Bean.DataBean> data = rBean.getData();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //显示广告
                                if (data != null && data.size() > 0) {
                                    getSharedPreferences("my_setting", MODE_PRIVATE).edit().putString("recommend_ad_cache", res).commit();

                                    for (int i = 0; i < data.size(); i++) {
                                        //广告链接
                                        web.put(data.get(i).getCy_advertisement_imgAddress(), data.get(i).getAdvLink());
                                        //视频广告链接
                                        if (data.get(i).getCy_advertisement_videoAddress() != null) {
                                            urls = (String) data.get(i).getCy_advertisement_videoAddress();
                                        }
                                    }
//                                    urls = "http://192.168.5.101/cc.mp4";
                                    if (urls != null) {
//                                        advertisementVideo(urls);
                                    } else {
                                        binding.adBg.stopAutoPlay();
                                        binding.adBg.setData(R.layout.ad_item, data, null);
                                        binding.adBg.setmAdapter(new XBanner.XBannerAdapter() {
                                            @Override
                                            public void loadBanner(XBanner banner, Object model, View view, int position) {
                                                String url = data.get(position).getCy_advertisement_imgAddress();
                                                //获取被点击广告页的网址链接
                                                if (position == 0) {
                                                    ClickOnTheAD = data.get(data.size() - 1).getCy_advertisement_imgAddress();
                                                    ad_id = data.get(data.size() - 1).getCy_advertisement_id();
                                                } else {
                                                    ClickOnTheAD = data.get(position - 1).getCy_advertisement_imgAddress();
                                                    ad_id = data.get(position - 1).getCy_advertisement_id();
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
            }
        });
    }


    /**
     * 统计用户在线时长
     */
    private void onlineTime() {
        if (cid != -1) {
            url = host + "jhzBox/box/onlineTime.do?&cy_brand_id=" + deviceId + "&mac=" + Utils.getDevID().toUpperCase() + "&netCardMac=" + netMac + "&cid=" + cid;
        } else if (cid == -1) {
            url = host + "jhzBox/box/onlineTime.do?&cy_brand_id=" + deviceId + "&mac=" + Utils.getDevID().toUpperCase() + "&netCardMac=" + netMac;
        }
        HttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Tag", "访问失败！");
            }

            @Override
            public void onResponse(Call call, Response response) {
                Log.e("Tag", "访问成功！");
            }
        });
    }

    /**
     * 清理数据
     */
    private void clearData() {
        if (cid != -1) {
            url = host + "jhzBox/box/removeIp.do?&cy_brand_id=" + deviceId + "&mac=" + Utils.getDevID().toUpperCase() + "&cid=" + cid;
        } else if (cid == -1) {
            url = host + "jhzBox/box/removeIp.do?&cy_brand_id=" + deviceId + "&mac=" + Utils.getDevID().toUpperCase();
        }
        HttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Tag", "访问失败！");
            }

            @Override
            public void onResponse(Call call, Response response) {
                Log.e("Tag", "访问成功！");
            }
        });
    }

    /**
     * 统计用户点击app次数
     */
    private void clickApp(String app) {
        if (cid != -1) {
            url = host + "jhzBox/box/appLike.do?&cy_brand_id=" + deviceId + "&mac=" + Utils.getDevID().toUpperCase() + "&netCardMac=" + netMac + "&cid=" + cid + "&lunchname=" + lunchname + "&syy_app_id=" + app;
        } else if (cid == -1) {
            url = host + "jhzBox/box/appLike.do?&cy_brand_id=" + deviceId + "&mac=" + Utils.getDevID().toUpperCase() + "&netCardMac=" + netMac + "&lunchname=" + lunchname + "&syy_app_id=" + app;
        }
        HttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Tag", "app访问失败！");
            }

            @Override
            public void onResponse(Call call, Response response) {
                Log.e("Tag", "app访问成功！");
            }
        });
    }


    /**
     * 统计用户点击广告的次数
     */
    private void clickAd(String ad) {
        if (cid != -1) {
            url = host + "jhzBox/box/advLike.do?&cy_brand_id=" + deviceId + "&mac=" + Utils.getDevID().toUpperCase() + "&netCardMac=" + netMac + "&cid=" + cid + "&lunchname=" + lunchname + "&cy_advertisement_id=" + ad;
        } else if (cid == -1) {
            url = host + "jhzBox/box/advLike.do?&cy_brand_id=" + deviceId + "&mac=" + Utils.getDevID().toUpperCase() + "&netCardMac=" + netMac + "&lunchname=" + lunchname + "&cy_advertisement_id=" + ad;
        }
        HttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Tag", "ad访问失败！");
            }

            @Override
            public void onResponse(Call call, Response response) {
                Log.e("Tag", "ad访问成功！");
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
        HttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder()
                .url(dataBean.getSyy_app_download()).tag(dataBean.getSyy_app_packageName()).build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                flItems[index].setEnabled(true);
//                                tvItems[index].setText("下载失败");
                                tvItems[index].setText(R.string.download_failed);
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
//                        Log.e(TAG, "响应:" + response);
                        if (response != null && response.isSuccessful()) {
                            InputStream inputStream = response.body().byteStream();
                            String filePath = AppManager.getAppDir() + dataBean.getSyy_app_download().substring(dataBean.getSyy_app_download().lastIndexOf("/") + 1);

//                            Log.e(TAG, "下载路径：" + filePath);
                            FileOutputStream fos = new FileOutputStream(filePath);
                            int len = 0;
                            byte[] buffer = new byte[1024 * 10];
                            while ((len = inputStream.read(buffer)) != -1) {
                                fos.write(buffer, 0, len);
                            }
                            fos.flush();
                            fos.close();
                            inputStream.close();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    flItems[index].setEnabled(true);
                                    pbItems[index].setVisibility(View.GONE);
                                    tvItems[index].setVisibility(View.GONE);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    flItems[index].setEnabled(true);
//                                    tvItems[index].setText("下载失败");
                                    tvItems[index].setText(R.string.download_failed);
                                }
                            });

                        }
                    }
                });
    }


    /**
     * 普通下载apk安装
     *
     * @param url
     */
    private void downloadApk(final String url) {
        HttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder()
                .url(url).build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        isDownloadingMarket = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Toast.makeText(mContext, "下载失败!", Toast.LENGTH_SHORT).show();
                                Toast.makeText(mContext, R.string.download_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response != null && response.isSuccessful()) {
                            InputStream inputStream = response.body().byteStream();

                            final String filePath = AppManager.getAppDir() + url.substring(url.lastIndexOf("/") + 1);
                            FileOutputStream fos = new FileOutputStream(filePath);
                            int len = 0;
                            byte[] buffer = new byte[1024 * 10];
                            while ((len = inputStream.read(buffer)) != -1) {
                                fos.write(buffer, 0, len);
                            }
                            fos.flush();
                            fos.close();
                            inputStream.close();

                            isDownloadingMarket = false;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //安装
                                    AppManager.install(mContext, filePath);
                                }
                            });
                        } else {
                            isDownloadingMarket = false;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //Toast.makeText(mContext, "下载失败!", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(mContext, R.string.download_failed, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
    }


    /**
     * 检查更新版本
     */
    private void checkVersion() {
        isCheckVersion = true;

        if (cid != -1) {
            url = host + "jhzBox/box/appOnlineVersion.do?versionNum=" + BuildConfig.VERSION_NAME + "&cy_brand_id=" + deviceId
                    + "&cy_versions_name=" + appName + "&lunchname=" + lunchname + "&mac=" + Utils.getDevID().toUpperCase() + "&cid=" + cid + "&netCardMac=" + netMac;
        } else if (cid == -1) {
            url = host + "jhzBox/box/appOnlineVersion.do?versionNum=" + BuildConfig.VERSION_NAME + "&cy_brand_id=" + deviceId
                    + "&cy_versions_name=" + appName + "&lunchname=" + lunchname + "&mac=" + Utils.getDevID().toUpperCase() + "&netCardMac=" + netMac;
        }

        HttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response != null && response.isSuccessful()) {
                            String res = response.body().string();
//                            Log.e(TAG, "onResponse:version= " + res);
                            final RecommendversionBean versionBean = new Gson().fromJson(res, RecommendversionBean.class);
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
                    }
                });
    }


    private void getRecommendVideo() {

        if (cid == 19) {
            //自由版
            url = host + "xpBox/box/loadBoot.do?cy_brand_id=" + deviceId + "&mac=" + Utils.getDevID().toUpperCase() + "&lunchname=" + lunchname + "&netCardMac=" + netMac + "&cid=" + cid;
        } else if (cid == -1) {
            //录入版
            url = host + "xpBox/box/loadBoot.do?cy_brand_id=" + deviceId + "&mac=" + Utils.getDevID().toUpperCase() + "&lunchname=" + lunchname + "&netCardMac=" + netMac;
        }

        Log.d("getRecommendVideo----->", url);

        HttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    final String res = response.body().string();
                    Log.e("tag", "onResponse:video= " + res);

                    final RecommendvideoBean rBean = new Gson().fromJson(res, RecommendvideoBean.class);

                    if (null != rBean.getCartoon() && !"".equals(rBean.getCartoon())) {
                        //加载成功
                        final String uri = rBean.getCartoon();
                        String uriOld = getSharedPreferences("video_path", MODE_PRIVATE).getString("videoPath", "");
                        if (uriOld.equals(uri)) return;

                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    sleep(1000 * 60);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                // DownloadManager.getInstance().removeFile((DownloadManager.getInstance().getDownloadCacheDir(MainActivity.this)));
                                // DownloadManager.getInstance().with(MainActivity.this).download(uri);
                                getSharedPreferences("video_path", MODE_PRIVATE).edit().putString("videoPath", uri).commit();
                            }
                        }.start();
                    }
                }
            }
        });
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
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                //startActivityForResult(intent, 10);
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
            ;//binding.mac.setVisibility(View.INVISIBLE);
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

    /**
     * 获取广播数据
     *
     * @author jiqinlin
     */
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
                pauseMusic();
                onClick(binding.fl11);
                //launchApp("com.h3launcher");
            } else if ((_action.contains("同轴")) || (_action.contains("同舟"))) {
                pauseMusic();
                onClick(binding.fl12);
                //launchApp("com.h3launcher");
            } else if (_action.contains("光纤")) {
                pauseMusic();
                onClick(binding.fl13);
                //launchApp("com.h3launcher");
            } else if ((_action.contains("输入")) || (_action.contains("Line in")) || (_action.contains("模拟"))) {
                pauseMusic();
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
                pauseMusic();
                onClick(binding.fl6);
                //launchApp("com.softwinner.TvdFileManager");
            } else if (_action.contains("设置") || _action.contains("网络")) {
                onClick(binding.fl7);
                //openSettings();
            } else if (_action.contains("频道")) {
                binding.ivFill.setVisibility(View.GONE);
                onClick(binding.fl8);
            } else if ((_action.contains("全民K歌")) || (_action.contains("我要唱歌")) || (_action.contains("我想唱歌")) || (_action.contains("K歌")) || (_action.contains("KTV"))) {
                handleViewKey(binding.fl0, -1, true);
            } else if ((_action.contains("腾讯视频")) || (_action.contains("云视听"))) {
                handleViewKey(binding.fl1, -1, true);
            } else if (_action.contains("应用") || _action.contains("程序")) {
                AppsActivity.lunchAppsActivity(MainActivity.this, MY_APP_TYPE);
            } else if (_action.contains("最近")) {
                AppsActivity.lunchAppsActivity(MainActivity.this, RECENT_TYPE);
            } else if ((_action != null) && (_action.equals("music") || _action.equals("ktv"))) {
            }
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            binding.bgIv5.setVisibility(View.VISIBLE);
            binding.bgIv5.setImageResource(R.drawable.battery_warnning);
            super.handleMessage(msg);
        }
    };
    private final Timer timer = new Timer();
    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };

    private void playMusic(final int resID) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    MediaPlayer mMediaPlayer;
                    mMediaPlayer = MediaPlayer.create(MainActivity.this, resID);
                    mMediaPlayer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

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
                }
                else
                {
                    binding.bgIv5.setVisibility(View.VISIBLE);
                }
            } else if (action.equals(ACTION_BATTERY_INFO) && !isCharging) {
                int value = intent.getIntExtra("value", -1);
                binding.ivBattery.setVisibility(View.VISIBLE);
                binding.bgIv5.setVisibility(View.VISIBLE);

                if ((value < 5))
                {
                    binding.bgIv5.setVisibility(View.VISIBLE);
                    binding.bgIv5.setImageResource(R.drawable.battery_warnning);
                }
                if (value < 10)
                {
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

    //循环获取电量
    /*Handler mBatteryHandler = new Handler();
    Runnable mBatteryRunnable = new Runnable() {
        @Override
        public void run() {
            //Log.e(TAG, "run: battery info");
            mBatteryHandler.postDelayed(mBatteryRunnable, 10000);//2s
            StartServiceSendBytes(Constants.BatteryCharge);
            StartServiceSendBytes(Constants.BatteryInfo);
        }
    };
    */

    private boolean isTopActivity(String packageName) {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(5);
        if (tasksInfo.size() > 0) {
            //应用程序位于堆栈的顶层
            String str = tasksInfo.get(0).topActivity.getPackageName();
            if (packageName.equals(str)) {
                return true;
            }
        }
        return false;
    }

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


    //把拼音的省份改成中文
    private void ModifyTheLanguageOfTheRegion(String province) {
        if (province.equals("Guangdong")) {
            region = "广东省";
        } else if (province.equals("Guangxi")) {
            region = "广西壮族自治区";
        } else if (province.equals("Hainan")) {
            region = "海南省";
        } else if (province.equals("Beijing")) {
            region = "北京市";
        } else if (province.equals("Tianjin")) {
            region = "天津市";
        } else if (province.equals("Shanghai")) {
            region = "上海市";
        } else if (province.equals("Chongqing")) {
            region = "重庆市";
        } else if (province.equals("Hebei")) {
            region = "河北省";
        } else if (province.equals("Henan")) {
            region = "河南省";
        } else if (province.equals("Yunan")) {
            region = "云南省";
        } else if (province.equals("Liaoning")) {
            region = "辽宁省";
        } else if (province.equals("Heilongjiang")) {
            region = "黑龙江省";
        } else if (province.equals("Hunan")) {
            region = "湖南省";
        } else if (province.equals("Anhui")) {
            region = "安徽省";
        } else if (province.equals("Shandong")) {
            region = "山东省";
        } else if (province.equals("Xinjiang")) {
            region = "新疆维吾尔族自治区";
        } else if (province.equals("Jiangsu")) {
            region = "江苏省";
        } else if (province.equals("Zhejiang")) {
            region = "浙江省";
        } else if (province.equals("Jiangxi")) {
            region = "江西省";
        } else if (province.equals("Hubei")) {
            region = "湖北省";
        } else if (province.equals("Gansu")) {
            region = "甘肃省";
        } else if (province.equals("Shanxi")) {
            region = "山西省";
        } else if (province.equals("Shanxi")) {
            region = "陕西省";
        } else if (province.equals("Neimenggu")) {
            region = "内蒙古蒙古族自治区";
        } else if (province.equals("Jilin")) {
            region = "吉林省";
        } else if (province.equals("Fujian")) {
            region = "福建省";
        } else if (province.equals("Guizhou")) {
            region = "贵州省";
        } else if (province.equals("Qinghai")) {
            region = "青海省";
        } else if (province.equals("Sichuan")) {
            region = "四川省";
        } else if (province.equals("Xizang")) {
            region = "西藏藏族自治区";
        } else if (province.equals("Ningxia")) {
            region = "宁夏回族自治区";
        } else if (province.equals("Taiwan")) {
            region = "台湾省";
        } else if (province.equals("Hong Kong")) {
            region = "香港特别行政区";
        } else if (province.equals("Macao")) {
            region = "澳门特别行政区";
        }
    }


    private void startVedioPlayerActivity() {
        Intent intent1 = new Intent();
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComponentName cn = new ComponentName("com.zhuchao.android.tianpu", "com.zhuchao.android.tianpu.activities.VideoActivity");
        intent1.setComponent(cn);
        startActivity(intent1);
    }

   /*
   private void kaijiziqi() {
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.tencent.karaoketv");
        if (intent != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "未安装全民K歌", Toast.LENGTH_LONG).show();
        }
    }
    */

    //传参数的服务
    public void StartServiceSendBytes(byte[] bytes) {
        //Intent intent = new Intent(this, MyService.class);
        //if (bytes != null) {
        //    intent.putExtra("serial", bytes);
        //}
        //启动servicce服务
        //startService(intent);
        if (myService != null)
            myService.sendCommand(bytes);
    }

    //不传参数的服务
    public void StartService() {
        Intent intent = new Intent(this, MyService.class);
        //启动servicce服务
        startService(intent);
    }

    private void gotTheLastOne() {
        String lastApp = ShareAdapter.getInstance().getStr("last_app");
        if (null != lastApp && !"".equals(lastApp)) {
            for (int i = 0; i < appHandler.saAPP.size(); i++) {
                if (lastApp.equals(appHandler.saAPP.get(i).getPackageName())) {
                    App app = appHandler.saAPP.get(i);
                    Log.e("Tag", "appp=" + app);
                    //binding.ivLastone.setBackground(app.getIcon());
                    lastoneApp = app.getPackageName();
                    lastApps = app.getIcon();
                }
            }
            if (isThelast) {
                binding.bgIcon.setVisibility(View.VISIBLE);
                binding.bgIv5.setImageResource(R.drawable.bb2);
                GlideMgr.loadNormalDrawableImg(mContext, lastApps, binding.bgIcon);
            }
        }
    }

    private void switchToOtherChanel(String ChanelName) {
        binding.bgIv5.setVisibility(View.VISIBLE);
        //binding.bgIv5.setImageResource(R.drawable.bb2);
        Log.i(TAG, "切换通道：" + ChanelName);
        StartServiceSendBytes(LastChanelApp);
    }

    /**
     * 初始化缓存
     */
    private void initCache() {
        String type = getSharedPreferences("saveType", MODE_PRIVATE).getString("saveType", null);
        Log.i("tag", "type=" + type);
        if (null != type && !"".equals(type)) {
            saveType = type;
            isThelast = false;
            if (saveType != null) {
                if (saveType.equals("同轴")) {
                    //StartServiceSendBytes(CopperShaftLineIn);
                    StartServiceSendBytes(CopperShaftClose);
                    binding.bgIv112.setImageResource(R.drawable.xsa);
                    binding.bgIv5.setImageResource(R.drawable.tz);
                    binding.bluetooth.setVisibility(View.GONE);
                    binding.bgIcon.setVisibility(View.GONE);
                } else if (saveType.equals("蓝牙")) {
                    //StartServiceSendBytes(BluetoothOpen);
                    StartServiceSendBytes(BluetoothClose);
                    binding.bgIv111.setImageResource(R.drawable.xsb);
                    binding.bgIv5.setImageResource(R.drawable.ly);
                    binding.bluetooth.setVisibility(View.VISIBLE);
                    binding.bluetooth.setImageResource(R.drawable.bluetoothno);
                    binding.bgIcon.setVisibility(View.GONE);
                } else if (saveType.equals("光纤")) {
                    //StartServiceSendBytes(OpticalFiberLineIn);
                    StartServiceSendBytes(OpticalFiberClose);
                    binding.bgIv113.setImageResource(R.drawable.xsd);
                    binding.bgIv5.setImageResource(R.drawable.opt);
                    binding.bluetooth.setVisibility(View.GONE);
                    binding.bgIcon.setVisibility(View.GONE);
                } else if (saveType.equals("模拟")) {
                    //StartServiceSendBytes(SimulationLineIn);
                    StartServiceSendBytes(SimulationLineInClose);
                    binding.bgIv114.setImageResource(R.drawable.xsc);
                    binding.bgIv5.setImageResource(R.drawable.mn);
                    binding.bluetooth.setVisibility(View.GONE);
                    binding.bgIcon.setVisibility(View.GONE);
                } else if (saveType.equals("player")) {
                    //StartServiceSendBytes(UsbOpen);
                    StartServiceSendBytes(UsbClose);
                    binding.bgIv115.setImageResource(R.drawable.xse);
                    binding.bgIv5.setImageResource(R.drawable.usbortf);
                    binding.bluetooth.setVisibility(View.GONE);
                    binding.bgIcon.setVisibility(View.GONE);
                } else if (saveType.equals("last")) {
                    //StartServiceSendBytes(LastAppOpen);
                    //switchToOtherChanel();
                    //binding.bgIv116.setImageResource(R.drawable.blue6);
                    binding.bluetooth.setVisibility(View.GONE);
                    isThelast = true;
                }
            } else {
                //binding.fl16.requestFocus();
            }
        }

        //app
        String cache_app = getSharedPreferences("my_setting", MODE_PRIVATE).getString("recommend_cache", null);
        if (cache_app != null) {
            recommendBean = new Gson().fromJson(cache_app, RecommendBean.class);
            final List<RecommendBean.DataBean> data = recommendBean.getData();
            if (data != null && data.size() >= ivs.length) {
                for (int i = 0; i < ivs.length; i++) {
                    if (!TextUtils.isEmpty(data.get(i).getSyy_app_img())) {
                        //glide
                        if (true) {
                            cacheImg += i + ": " + data.get(i).getSyy_app_img() + "\n";
                            Glide.with(mContext).load(data.get(i).getSyy_app_img()).into(ivs[i]);
                            continue;
                        }
                        //fresco
                        FileBinaryResource resource = (FileBinaryResource) Fresco.getImagePipelineFactory().getMainFileCache().getResource(new SimpleCacheKey(data.get(i).getSyy_app_img()));
                        if (resource != null) {
                            File file = resource.getFile();
                            cacheImg += i + ": " + file.getPath() + "\n";
                            ivs[i].setImageURI(Uri.fromFile(file));
//                            Glide.with(mContext).load(file).into(ivs[i]);
                        } else {
                            cacheImg += i + ": " + data.get(i).getSyy_app_img() + "\n";
                            ivs[i].setImageURI(Uri.parse(data.get(i).getSyy_app_img()));
                        }

                    }

                }
            }
            //加载指定app主页图片
            if (data != null && data.size() != 0) {
                for (int i = 0; i < data.size(); i++) {
                    String pkn = data.get(i).getSyy_app_packageName();
                    String img = data.get(i).getSyy_appstatus_img();
                    if (!"".equals(img) && null != img) {
                        stImg.put(pkn, img);
                    }
                }
                //显示清理缓存的文字
//                if (data.size() >= 3 && data.get(2).getSyy_app_name() != null) {
//                    binding.titleTv8.setVisibility(View.VISIBLE);
//                    binding.titleTv8.setText(data.get(2).getSyy_app_name());
//                }
            }
        }
        //跑马灯
        String cache_marquee = getSharedPreferences("my_setting", MODE_PRIVATE).getString("recommend_marquee_cache", null);
        if (cache_marquee != null) {
            recommendmarqueeBean = new Gson().fromJson(cache_marquee, RecommendmarqueeBean.class);
            final RecommendmarqueeBean.DataBean data = recommendmarqueeBean.getData();
            if (data != null) {
                if (!TextUtils.isEmpty(data.getMarquee()))
                    binding.scrollTv.setText(data.getMarquee());
            }
        }
        //广告
        String cache_ad = getSharedPreferences("my_setting", MODE_PRIVATE).getString("recommend_ad_cache", null);
        if (cache_ad != null) {
            recommend3Bean = new Gson().fromJson(cache_ad, Recommend3Bean.class);
            final List<Recommend3Bean.DataBean> data = recommend3Bean.getData();
            if (data != null && data.size() != 0) {
                binding.adBg.stopAutoPlay();
                binding.adBg.setData(R.layout.ad_item, data, null);
                binding.adBg.setmAdapter(new XBanner.XBannerAdapter() {
                    @Override
                    public void loadBanner(XBanner banner, Object model, View view, int position) {
                        String url = data.get(position).getCy_advertisement_imgAddress();
                        if (!TextUtils.isEmpty(url)) {
//                            ((SimpleDraweeView) view).setImageURI(Uri.parse(data3.get(position).getCy_advertisement_imgAddress()));
                            Glide.with(mContext).load(url).into((SimpleDraweeView) view);
                        }
                    }
                });
                binding.adBg.startAutoPlay();
            }
        }
        //logo
        String cache_logo = getSharedPreferences("my_setting", MODE_PRIVATE).getString("recommend_logo_cache", null);
        if (cache_logo != null) {
            recommendlogoBean = new Gson().fromJson(cache_logo, RecommendlogoBean.class);
            final RecommendlogoBean.DataBean data = recommendlogoBean.getData();
            //显示品牌logo
            if (data != null) {
                if (!TextUtils.isEmpty(data.getSyy_special_fileOne())) {
                    Glide.with(mContext).load(data.getSyy_special_fileOne()).into(binding.logoIv);
                }
            }

        }
        setupItemBottomTag();
        //背景图
        String cache_bg = getSharedPreferences("my_setting", MODE_PRIVATE).getString("recommend_bg_cache", null);
        if (cache_bg != null) {
            recommendbgBean = new Gson().fromJson(cache_bg, RecommendbgBean.class);
            final List<RecommendbgBean.DatabgBean> data = recommendbgBean.getData();
            if (data != null && data.size() != 0) {
                for (int i = 0; i < data.size(); i++) {
                    if (!TextUtils.isEmpty(data.get(i).getBackgroundImgAddress())) {
                        Glide.with(mContext).load(data.get(i).getBackgroundImgAddress()).into(binding.ivBg);
                    }
                }
            }
        }

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
//        List<RecommendBean.DataBean> data = recommendBean.getData();
        for (int i = 0; i < pbItems.length; i++) {
            //todo 全部隐藏
            pbItems[i].setVisibility(View.GONE);
            tvItems[i].setVisibility(View.GONE);
        }
    }

}

