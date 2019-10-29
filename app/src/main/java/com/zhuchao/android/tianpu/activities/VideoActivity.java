package com.zhuchao.android.tianpu.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.VideoView;

import com.zhuchao.android.tianpu.data.json.regoem.RecommendvideoBean;

import com.zhuchao.android.tianpu.utils.HttpUtils;
import com.zhuchao.android.tianpu.utils.NetTool;
import com.zhuchao.android.tianpu.utils.Utils;
import com.google.gson.Gson;
import com.zhuchao.android.tianpu.R;
//import com.zhuchao.android.netutil.CheckNet;
//import com.zhuchao.android.netutil.DownloadManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;



public class VideoActivity extends AppCompatActivity implements NetTool.OnNetListener{

    private String deviceId = "750";//西浦
    //自由版
//    private String lunchname = "XPSY01";
//    int cid = 19 ;      //客户号

    //录入版
    private String lunchname = "TP0WLD";
    int cid = -1;      //客户号

    private String netMac;   //设备的以太网mac地址
    //服务器地址
   private String host = "http://www.gztpapp.cn:8976/";
//    private String host = "http://www.e.istarbox.com:8976/";
    private String url;
    boolean g = true;
    private String str = "";
    private NetTool netTool;

    private static final int REQUEST_PERMISSION = 0;
    private VideoView vv;
    //private Video video;
    private String RemoteVideoUrl = "http://192.168.5.101/cc.mp4";
    private String urlPath = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_video);
        requestPermition();
        vv = findViewById(R.id.vv_video);

        String n = getEthernetMacAddress();
        String w = getWiFiMacAddress(VideoActivity.this);
        if (!"".equals(n) && null != n) {
            netMac = n;
        } else if (!"".equals(w) && null != w) {
            netMac = w;
        }
//        urlPath = getSharedPreferences("video_path",MODE_PRIVATE).getString("videoPath","");
        //监听网络状态
        netTool = new NetTool(this);
        netTool.setOnNetListener(this);
        //Log.e("VideoActivity","urlPath="+urlPath);
        //play();
        //video = new Video("http://www.e.istarbox.com:8976/syySpecial/1551320884931033.mp4", null, null);
        //video = new Video(DownloadManager.getInstance().GetLocalCacheFile(urlPath), null, null);
        //video.with(VideoActivity.this).playInto(vv);
        //advertisementVideo(urlPath);//("http://192.168.5.101/cc.mp4");
//        if (CheckNet.isInternetOk())
//            advertisementVideo(urlPath);//("http://192.168.5.101/cc.mp4");
//        else {
//            advertisementVideo(DownloadManager.getInstance().GetLocalCacheFile(urlPath));

//            getRecommendVideo();
//        }
    }
////    private void play(){
//        //本地下载有视频就播放本地视频 ，否则就去播放并下载网络视频
////        if (DownloadManager.getInstance().ExistsLocalCacheFile(urlPath)) {
////            advertisementVideo(DownloadManager.getInstance().GetLocalCacheFile(urlPath));
// /*           video = new Video(DownloadManager.getInstance().GetLocalCacheFile(urlPath), null, null);
//            video.with(this).playInto(vv);
//            video.callback(new PlayerCallBackInterface() {
//                @Override
//                public void OnEventCallBack(int i, long l, long l1, float v, int i1, int i2, int i3, float v1) {
//                    //播放完成后，返回主页
//                    if (i == 262){
//                        finish();
//                    }
//                }
//            });*/
//        } else if (CheckNet.isInternetOk()){
//            getRecommendVideo();
//        }else {
//            ;//this.finish();
//        }
//    }
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
                        REQUEST_PERMISSION);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        netTool.registerNetReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        netTool.unRegisterNetReceiver();
        String strProp = getProp("persists.sys.bootstartapp");
        if (strProp != null && !strProp.isEmpty() && strProp.equals("com.tencent.karaoketv"))
        {
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.tencent.karaoketv");
            if (intent != null) {
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "未安装全民K歌", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        netTool.release();
        netTool.setOnNetListener(null);
        netTool = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //监听到按返回键时，不处理（即为按返回键无效）
        if (keyCode == KeyEvent.KEYCODE_BACK){
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    public String getProp(String propName) {
        Class<?> classType = null;
        String buildVersion = null;
        try {
            classType = Class.forName("android.os.SystemProperties");
            Method getMethod = classType.getDeclaredMethod("get", new Class<?>[]{String.class});
            buildVersion = (String) getMethod.invoke(classType, new Object[]{propName});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buildVersion;
    }




    /**
     * 获取推荐的视频链接
     */
    private void getRecommendVideo() {
        if (cid == 19) {
            //自由版
            url = host + "jhzBox/box/loadBoot.do?cy_brand_id=" + deviceId + "&mac=" + Utils.getDevID().toUpperCase() + "&lunchname=" + lunchname + "&netCardMac=" + netMac + "&cid=" + cid;
        } else if (cid == -1) {
            //录入版
            url = host + "jhzBox/box/loadBoot.do?cy_brand_id=" + deviceId + "&mac=" + Utils.getDevID().toUpperCase() + "&lunchname=" + lunchname + "&netCardMac=" + netMac;
        }
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
//                        DownloadManager.getInstance().with(VideoActivity.this).download(urlPath);
                        final String uri = rBean.getCartoon();
//                        urlPath = uri;
                        //下载到本地,并播放视频
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                downloadVideo(uri);
                            }
                        });
                    } else {
                        finish();
                    }
                } else {
                    finish();
                }
            }
        });
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
     * 视频广告播放器
     */
    private void advertisementVideo(String u) {
//        Toast.makeText(getApplicationContext(),"uu="+u,Toast.LENGTH_LONG).show();
//        MediaController controller = new MediaController(this);//实例化控制器
        if ("".equals(u) || null == u) {
            finish();
        } else {

            vv.setVideoURI(Uri.parse(u));
            /**
             * 将控制器和播放器进行互相关联
             */
//            controller.setMediaPlayer(vv);
//            vv.setMediaController(controller);
            vv.start();
            Log.e("tag","开始播放");

//            vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    Log.e("tag","播放完毕");
//                    finish();
//                }
//            });
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    while (g) {
                        try {
                            sleep(1000);
                            if (!vv.isPlaying()) {
                                finish();
                                g = false;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }
    }
    private void downloadVideo(final String url) {
        // 创建文件夹，在存储卡下
        String dirName = Environment.getExternalStorageDirectory() + "/" + getApplicationContext().getPackageName();
        File file = new File(dirName);
        // 文件夹不存在时创建
        if (!file.exists()) {
            file.mkdir();
            Log.e("tag","file.mkdir");
        }
        Log.e("tag","url="+url);
        // 下载后的文件名
        int i = url.lastIndexOf("/"); // 取的最后一个斜杠后的字符串为名
        final String fileName = dirName + url.substring(i);
        File file1 = new File(fileName);
        Log.e("tag","file1="+file1+"     >"+file1.exists());

        if (file1.exists()) {
            // 如果已经存在, 就不下载了, 去播放
            Log.e("tag", "bofang");
           advertisementVideo(fileName);

        } else {
//            advertisementVideo(null);
            Log.e("tag", "文件不存在");
            new Thread(new Runnable() {
                @Override
                public void run() {
//                    String cache_video = FileUtils.getMusics("/com.h3launcher/");
//                    if (null != cache_video && !cache_video.equals(fileName)) {
//                        File file = new File(cache_video);
//                        if (file.exists() && file.isFile()) {
//                            file.delete();
//                            Log.e("tag", "删除旧视频");
//                        }
//                    }
                    DOWNLOAD(url, fileName);
                }
            }).start();

            finish();
        }

    }
    // 下载具体操作
    private void DOWNLOAD(String path, String fileName) {
        try {
//            str = "开始下载";
//            getSharedPreferences("my_setting", MODE_PRIVATE).edit().putString("str", str).commit();
            URL url = new URL(path);
            // 打开连接
            URLConnection conn = url.openConnection();
            // 打开输入流
            InputStream is = conn.getInputStream();
            // 创建字节流
            byte[] bs = new byte[1024 * 100];
            int len;
            OutputStream os = new FileOutputStream(fileName);
            // 写数据
            while ((len = is.read(bs)) != -1) {
                os.write(bs, 0, len);
                //Log.e("tag", "len="+len);
            }
            // 完成后关闭流
            Log.e("tag", "download-finish");
//            str = "下载完毕";
//            getSharedPreferences("my_setting", MODE_PRIVATE).edit().putString("str", str).commit();
            getSharedPreferences("video_path",MODE_PRIVATE).edit().putString("videoPath",path).commit();
            Log.e("Tag","path=="+path);
            os.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("tag", "e.getMessage() --- " + e.getMessage());
        }
    }

    @Override
    public void onNetState(boolean isConnected, int type) {
        boolean b = NetTool.isNetworkOK();
        Log.e("Tag","b="+b);
        if (b){
            getRecommendVideo();
        } else {
            final String url = getSharedPreferences("video_path",MODE_PRIVATE).getString("videoPath","");
            if (!"".equals(url) && null != url){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downloadVideo(url);
                    }
                });
            }else {
                finish();
            }
        }
    }

    @Override
    public void wifiLevel(int level) {

    }
}

//        if (DownloadManager.getInstance().ExistsLocalCacheFile(urlPath))
////            advertisementVideo(DownloadManager.getInstance().GetLocalCacheFile(urlPath));
////    }
////
//    private void advertisementVideo(String u) {
//        MediaController controller = new MediaController(this);//实例化控制器
//        if ("".equals(u) || null == u) {
//            finish();
//        } else {
//            vv.setVideoURI(Uri.parse(u));
//
//            /**
//             * 将控制器和播放器进行互相关联
//             */
//            controller.setMediaPlayer(vv);
//            vv.setMediaController(controller);
//            vv.start();
//            new Thread() {
//                @Override
//                public void run() {
//                    super.run();
//                    try {
//                        sleep(10000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                    while (g) {
//                        try {
//                            sleep(500);
//                            if (!vv.isPlaying()) {
//                               finish();
//                                g = false;
//                            }
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }.start();
//        }
//    }
//
//}
