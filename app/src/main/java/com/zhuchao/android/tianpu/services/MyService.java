package com.zhuchao.android.tianpu.services;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;

import com.zhuchao.android.tianpu.R;
import com.zhuchao.android.tianpu.utils.ForegroundAppUtil;
import com.zhuchao.android.tianpu.views.dialogs.Mac_Dialog;
import com.zhuchao.android.tianpu.views.dialogs.MusicDialog;
import com.zhuchao.android.tianpu.views.dialogs.Sound_Effect_Dialog;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import utils.ChangeTool;

import utils.MySerialPort;

public class MyService extends Service {
    private final String TAG = "MyService";
    private MySerialPort MyPortDevice = new MySerialPort(this);
    private byte[] SerialPortReceiveBuffer;
    private String lo;
    private Handler SerialPortReceivehandler;
    private MusicDialog dialog;
    private Sound_Effect_Dialog sdialog;
    private Mac_Dialog mdialog;

    private Callback callback;
    private byte[] bytes;
    private int h = 0;

    private static int MicVolume = 0;
    private static int MusicVolume = 0;
    private MyReceiver receiver = null;
    //private String mType = "";
    private byte tbb[] = {0, 0, 0, 0};
    private byte[] SetMusicVolume = {0x02, 0x01, 0x02, 0x00, 0x00, 0x02, 0x00, 0x04, 0x00, 0x0b, 0x7E};//设置音乐音量  K70//
    private byte[] SetMusicVolumeK50 = {0x01, 0x01, 0x02, 0x00, 0x00, 0x02, 0x00, 0x04, 0x00, 0x0a, 0x7E};//设置音乐音量  K50
    private boolean isCharging = false;
    private MediaPlayer mMediaPlayer = null;
    private static String mTopPackageName;

    @Override
    public void onCreate() {
        super.onCreate();

        SerialPortReceivehandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        boolean bRet = MyPortDevice.openPort("/dev/ttyS1", 9600, true);

        if (bRet == false) {
            Log.e("Service", "onCreate：串口打开失败！！！！！");
        } else {
            Log.e("Service", "onCreate：串口打开成功！！！！！");
            CheckSerialPortEvent();
        }

    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

    }

    public void sendCommand(byte[] bytes) {
        try {
            MyPortDevice.sendBuffer(bytes);
            Log.i("MyService.发送数据", utils.ChangeTool.ByteArrToHexStr(bytes, 0, bytes.length));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    public class Binder extends android.os.Binder{  //
        public MyService getService() {

            receiver = new MyReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.iflytek.xiri2.hal.volume");
            filter.addAction("com.iflytek.xiri2.hal.iflytekService");
            registerReceiver(receiver, filter);

            return MyService.this;
        }

    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public static interface Callback {
        void onDataChange(String data);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //handler.postDelayed(r, 1000);
        return START_STICKY;
    }


    private Handler handler = new Handler();
    private Runnable r = new Runnable() {
        @Override
        public void run() {
            mTopPackageName = ForegroundAppUtil.getForegroundActivityName(getApplicationContext());
            //Toast.makeText(getApplicationContext(), foregroundActivityName, Toast.LENGTH_SHORT).show();

            handler.postDelayed(r, 1000);
        }
    };

    public static String GetTopPackageName() {
        return mTopPackageName;
    }


    public static int getMicVolume() {
        return MicVolume;
    }

    public static void setMicVolume(int micVolume) {
        MicVolume = micVolume;
    }

    public static int getMusicVolume() {
        return MusicVolume;
    }

    public static void setMusicVolume(int musicVolume) {
        MusicVolume = musicVolume;
    }


    private void playMusic(final Context c, final int resID) {

        new Thread() {
            @Override
            public void run() {
                if (mMediaPlayer == null) {
                    mMediaPlayer = MediaPlayer.create(c, resID);
                    mMediaPlayer.start();
                } else {
                    //mMediaPlayer.stop();
                    mMediaPlayer.reset();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                    mMediaPlayer = MediaPlayer.create(c, resID);
                    mMediaPlayer.start();
                }
            }
        }.start();

        Log.i(TAG, "playMusic" + resID);
    }

    //private void playMyMusic(String filePath)
    //{
    // Video mvideo = new Video(filePath);
    //}

    private void CheckSerialPortEvent() {
        //串口数据监听事件
        MyPortDevice.setOnDataReceiveListener(new MySerialPort.OnDataReceiveListener() {
            @Override
            public void onDataReceive(Context context, byte[] buffer, int size) {
                SerialPortReceiveBuffer = buffer;
                lo = utils.ChangeTool.ByteArrToHexStr(SerialPortReceiveBuffer, 0, size);

                if (buffer[2] == 0x06) {
                    MusicVolume = buffer[7];
                    MicVolume = buffer[8];
                    return;
                }
                //播放特效声
                if (buffer[2] == 0x21) {
                    byte result = buffer[5];
                    switch (result) {
                        case 0:
                            //playMusic(context, R.raw.tp00);
                            break;
                        case 1:
                            playMusic(context, R.raw.tp001);
                            break;
                        case 2:
                            playMusic(context, R.raw.tp002);
                            break;
                        case 3:
                            playMusic(context, R.raw.tp003);
                            break;
                        case 4:
                            playMusic(context, R.raw.tp004);
                            break;
                        case 5:
                            playMusic(context, R.raw.tp005);
                            break;
                        case 6:
                            playMusic(context, R.raw.tp006);
                            break;
                        case 7:
                            playMusic(context, R.raw.tp007);
                            break;
                        case 8:
                            playMusic(context, R.raw.tp008);
                            break;
                        case 9:
                            playMusic(context, R.raw.tp009);
                            break;
                        case 10:
                            playMusic(context, R.raw.tp010);
                            break;
                        case 11:
                            playMusic(context, R.raw.tp011);
                            break;
                        case 12:
                            playMusic(context, R.raw.tp012);
                            break;
                    }
                    return;
                }
                //Setting
                if (buffer[2] == 0x01) {
                    byte result = buffer[7];
                    //Intent intent = new Intent("COMMAND_DATA");
                    //intent.putExtra("COMMAND_DATA", 0x20);
                    //sendBroadcast(intent);
                    if (result == 0x20) {
                        Intent in = new Intent();
                        in.setClassName("com.android.settings", "com.android.settings.Settings");
                        startActivity(in);
                    }
                    return;
                }
                //是蓝牙连接
                if (buffer[2] == 0x24) {
                    byte result = buffer[7];
                    Intent intent = new Intent("BLUTOOLTH_STATUS");
                    intent.putExtra("BLUTOOLTH_STATUS", result == 0x01);
                    sendBroadcast(intent);
                    return;
                }
                //是否充电
                if (buffer[2] == 0x22) {
                    byte result = buffer[7];
                    //String s = ChangeTool.Byte2Hex(result);
                    //Log.e("battery", "onDataReceive: " + s);
                    Intent intent = new Intent("BATTERY_CHARGE");
                    if (result == 0x01)
                        isCharging = true;
                    else
                        isCharging = false;

                    intent.putExtra("isCharge", isCharging);
                    sendBroadcast(intent);
                    return;
                }
                //电量值
                if (buffer[2] == 0x23) {
                    Intent intent = new Intent("BATTERY_INFO");
                    int v = ChangeTool.HexToInt(ChangeTool.Byte2Hex(buffer[7]));
                    intent.putExtra("value", v);
                    sendBroadcast(intent);
                    Log.i("onDataReceive", lo);
                    if ((v <= 10) && (!isCharging))
                        playMusic(context, R.raw.charge);
                    return;
                }

                if (buffer[2] == 0x25) {
                    int v = ChangeTool.HexToInt(ChangeTool.Byte2Hex(buffer[7]));
                    SystemProperties.set("ro.dsp.version", String.valueOf(v));
                    //System.setProperty("ro.dsp.version", String.valueOf(v));
                    Log.i(TAG, "SystemProperties.set(\"ro.dsp.version\", String.valueOf(v));v=" + v);
                    return;
                }
                if (null != SerialPortReceivehandler) {
                    SerialPortReceivehandler.post(runnable);
                } else {
                    Log.i("onDataReceive", "SerialPortReceivehandler=null");
                }
            }


            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    if (lo.length() >= 16) {
                        VolumeChange(lo);
                    }
                }
            };

        });
    }

    private void VolumeChange(String volume) {

        String type = volume.substring(4, 8);
        String value = volume.substring(12, 16);
        int v = Integer.valueOf(value, 16);
        Log.i("VolumeChange", "type=" + type + "   lo=" + lo + "    h=" + h++);
        //mType = type;
        if (type.equals("0200")) {
            //音乐音量
            if ((v >= 0) && (v <= 60)) {
                showVolumeDialog(v, type);
                MusicVolume = v;
            }
        } else if (type.equals("0300")) {
            //mic音量
            if ((v >= 0) && (v <= 60)) {
                showVolumeDialog(v, type);
                MicVolume = v;
            }
        } else if (type.equals("0400")) {
            //音效
            //showEffectDialog(v, type);
            switch (v) {
                case 1:
                    showEffectDialog3(R.drawable.u20, v, "1 主持", type);
                    break;
                case 2:
                    showEffectDialog3(R.drawable.u20, v, "2 唱将", type);
                    break;
                case 3:
                    showEffectDialog3(R.drawable.u20, v, "3 喊麦", type);
                    break;
                case 4:
                    showEffectDialog3(R.drawable.u20, v, "4 专业", type);
                    break;
                case 5:
                    showEffectDialog3(R.drawable.u20, v, "5 演唱", type);
                    break;
            }

        } else if (type.equals("0500")) {
            if (callback != null) {
                //if(IsTopActivtyFromLolipopOnwards("com.zhuchao.android.tianpu")==false)
                {
                    Intent i = new Intent();
                    i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    ComponentName cn = new ComponentName("com.zhuchao.android.tianpu", "com.zhuchao.android.tianpu.activities.MainActivity");
                    i.setComponent(cn);

                    startActivity(i);
                }
                callback.onDataChange(lo);
            }
        } else if (type.equals("0700")) {
            //吉他
            showVolumeDialog2(R.drawable.jt, v, type);
        } else if (type.equals("0800")) {
            //监听
            showVolumeDialog2(R.drawable.ej, v, type);
        } else if (type.equals("0900")) {
            //直播
            showVolumeDialog2(R.drawable.zb, v, type);
        }

    }

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

    private void showVolumeDialog(int direction, String type) {
        if (dialog == null || dialog.isShowing() != true) {
            dialog = new MusicDialog(this);
            //dialog.setVolumeAdjustListener();
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            //dialog.show();
        }
        dialog.adjustVolume(direction, true, type);
    }

    private void showVolumeDialog2(int resid, int v, String type) {
        if (dialog == null || dialog.isShowing() != true) {
            dialog = new MusicDialog(this);
            //dialog.setVolumeAdjustListener();
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            //dialog.show();
        }
        dialog.setImageView(resid);
        dialog.adjustVolume(v, true, type);
    }

    private void showEffectDialog(int direction, String type) {
        if (sdialog == null || sdialog.isShowing() != true) {
            sdialog = new Sound_Effect_Dialog(this);
            //sdialog.setVolumeAdjustListener();
            sdialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            sdialog.show();
        }
        sdialog.adjustVolume(direction, true, type);
    }

    private void showEffectDialog3(int resID, int v, String Str, String type) {
        if (sdialog == null || sdialog.isShowing() != true) {
            sdialog = new Sound_Effect_Dialog(this);
            sdialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            sdialog.show();
        }
        sdialog.adjustVolume(v, true, type);
        sdialog.setContentValue(resID, Str);

    }

    public class MyReceiver extends BroadcastReceiver {
        //adb shell am broadcast -a com.iflytek.xiri2.hal.volume
        //adb shell am broadcast -a com.iflytek.xiri2.hal.volume --es volume 30
        //adb shell am broadcast -a com.zhuchao.android.tianpu.services
        @Override
        public void onReceive(Context context, Intent intent) {
            int data = -1;
            String mType = "0200";
            Bundle bundle = intent.getExtras();

            if (bundle != null) {
                data = bundle.getInt("volume", -1);
                mType = bundle.getString("type", "0200");
            }


            if (intent.getAction().equals("com.iflytek.xiri2.hal.volume")) {
                if (data > 60) data = 60;

                Log.d("MyReceiver--->", intent.getAction() + ":volume=" + data);

                if (intent.getAction() == "com.iflytek.xiri2.hal.volume") {
                    if ((data >= 0) && (data <= 60)) {
                        if (mType.equals("0200")) {
                            //音乐音量
                            showVolumeDialog(data, mType);
                            MusicVolume = data;
                            setVolume(MusicVolume);
                        } else if (mType.equals("0300")) {
                            //mic音量
                            showVolumeDialog(data, mType);
                            MicVolume = data;
                        } else {
                            showVolumeDialog(data, mType);
                            MusicVolume = data;
                        }
                    } else {
                        showVolumeDialog(MusicVolume, "0200");
                    }
                }
            }
            if (intent.getAction().equals("com.iflytek.xiri2.hal.iflytekService"))
            {
                byte[] bytes = bundle.getByteArray("SerialData");
                sendCommand(bytes);
            }
        }
    }


    private void setVolume(int i) {
        tbb = utils.ChangeTool.intToBytes(i);

        SetMusicVolume[7] = tbb[3];
        SetMusicVolume[8] = tbb[2];
        SetMusicVolume[9] = utils.ChangeTool.BytesAdd(SetMusicVolume, 9);

        SetMusicVolumeK50[7] = tbb[3];
        SetMusicVolumeK50[8] = tbb[2];
        SetMusicVolumeK50[9] = utils.ChangeTool.BytesAdd(SetMusicVolumeK50, 9);


        MyPortDevice.sendBuffer(SetMusicVolume);
        MyPortDevice.sendBuffer(SetMusicVolumeK50);
        //Log.e("MyPortDevice", utils.ChangeTool.ByteArrToHexStr(bytes, 0, bytes.length));
    }


    public Boolean IsTopActivtyFromLolipopOnwards(String PackageName) {
        String topPackageName;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);
            if (stats != null) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : stats) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    topPackageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                    Log.e("TopPackage Name", topPackageName);
                    if (topPackageName.equals(PackageName)) return true;
                }
            }
        }
        return false;
    }

}
