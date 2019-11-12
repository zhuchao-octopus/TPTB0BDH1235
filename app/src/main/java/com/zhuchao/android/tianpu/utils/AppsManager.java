package com.zhuchao.android.tianpu.utils;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.mypm.IPackageDeleteObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.RemoteException;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuchao.android.tianpu.activities.MainActivity;
import com.zhuchao.android.tianpu.BuildConfig;
import com.zhuchao.android.tianpu.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;

/**
 * Created by ZTZ on 2018/3/20.
 */

public class AppsManager {

    /* 卸载apk */
    public static void uninstallApk(Context context, String packageName) {
        Uri uri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        context.startActivity(intent);
    }
    /**
     * 静默安装
     * @return
     */
    public static boolean slientunInstall(String pck) {
        boolean result = false;
        Process process = null;
        OutputStream out = null;
        if (pck!=null) {
            try {
                process = Runtime.getRuntime().exec("su");
                out = process.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(out);
//                dataOutputStream.writeBytes("chmod 777 " + file.getPath()
//                        + "\n"); // 获取文件所有权限
                dataOutputStream
                        .writeBytes("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm uninstall "
                                + pck); // 进行静默安装命令
                // 提交命令
                dataOutputStream.flush();
                // 关闭流操作
                dataOutputStream.close();
                out.close();
                int value = process.waitFor();

                // 代表成功
                if (value == 0) {
                    Log.e("hao", "安装成功！");
                    result = true;
                } else if (value == 1) { // 失败
                    Log.e("hao", "安装失败！");
                    result = false;
                } else { // 未知情况
                    Log.e("hao", "未知情况！");
                    result = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return result;
    }
    /**
     * 静默安装
     * @return
     */
    public boolean slientInstall(File file) {
        boolean result = false;
        Process process = null;
        OutputStream out = null;
        System.out.println(file.getPath());
        if (file.exists()) {
            System.out.println(file.getPath() + "==");
            try {
                process = Runtime.getRuntime().exec("su");
                out = process.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(out);
                dataOutputStream.writeBytes("chmod 777 " + file.getPath()
                        + "\n"); // 获取文件所有权限
                dataOutputStream
                        .writeBytes("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -r "
                                + file.getPath()); // 进行静默安装命令
                // 提交命令
                dataOutputStream.flush();
                // 关闭流操作
                dataOutputStream.close();
                out.close();
                int value = process.waitFor();

                // 代表成功
                if (value == 0) {
                    Log.e("hao", "安装成功！");
                    result = true;
                } else if (value == 1) { // 失败
                    Log.e("hao", "安装失败！");
                    result = false;
                } else { // 未知情况
                    Log.e("hao", "未知情况！");
                    result = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            if (!result) {
//                Log.e("hao", "root权限获取失败，将进行普通安装");
//                Intent intent = new Intent();
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.setAction(android.content.Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.fromFile(file),
//                        "application/vnd.android.package-archive");
//                startActivity(intent);
//                result = true;
//            }
        }

        return result;
    }
    public static void myUninstall(Context context, String packagename) {
        PackageManager pm = context.getPackageManager();
//        Class[] types = new Class[]{Uri.class, IPackageInstallObserver.class, int.class, String.class};
        Class[] uninstalltypes = new Class[]{String.class, IPackageDeleteObserver.class, int.class};

        try {
            Method uninstallmethod = pm.getClass().getMethod("deletePackage", uninstalltypes);
            uninstallmethod.invoke(pm, new Object[]{packagename,new PackageDeleteObserver() , 0});

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


    }

    public interface OnPackagedObserver {

        public void packageInstalled(String packageName, int returnCode);

        public void packageDeleted(String packageName, int returnCode);
    }

    private static OnPackagedObserver onInstalledPackaged;

    static class PackageDeleteObserver extends IPackageDeleteObserver.Stub {

        public void packageDeleted(String packageName, int returnCode) throws RemoteException {
            if (onInstalledPackaged != null) {
                onInstalledPackaged.packageDeleted(packageName, returnCode);
            }
        }
    }



    public static void uninstallApp(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            Method[] methods = pm != null ? pm.getClass().getDeclaredMethods() : null;
            Method mDel = null;
            if (methods != null && methods.length > 0) {
                for (Method method : methods) {
                    if (method.getName().toString().equals("deletePackage")) {
                        mDel = method;
                        break;
                    }
                }
            }
            if (mDel != null) {
                mDel.setAccessible(true);
                mDel.invoke(pm, packageName, null, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //静默卸载
    private void uninstallSlient(Context context, String PACKAGE_NAME) {
        String cmd = "pm uninstall -k " + PACKAGE_NAME.replace(" ","");
        Process process = null;
        DataOutputStream os = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        try {
            //卸载也需要root权限
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.write(cmd.getBytes());
            os.writeBytes("\n");
            os.writeBytes("exit\n");
            os.flush();
            //执行命令
            process.waitFor();
            //获取返回结果
            successMsg = new StringBuilder();
            errorMsg = new StringBuilder();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //显示结果
        View view1 = LayoutInflater.from(context).inflate(R.layout.test_cache, null);
        ((TextView) view1.findViewById(R.id.tv_content)).setText("成功消息：" + successMsg.toString() + "\n" + "错误消息: " + errorMsg.toString());
        new AlertDialog.Builder(context)
                .setTitle("remove result")
                .setView(view1)
                .show();
    }

    /**
     * 卸载
     *
     * @param context
     * @param packageName
     */
    public static String unInstall(final Context context, String packageName) {
        boolean installApp = isInstallApp(context, packageName);
        if (installApp) {
            Log.e("appmanager", "unInstall: " + installApp);
            try {
                Runtime.getRuntime().exec("pm uninstall " + packageName);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            final String result = execCommand("pm", "uninstall", packageName);
//            return result;
//            ((MainActivity)context).runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(context, ""+res, Toast.LENGTH_LONG).show();
//                }
//            });
        }
        return "";
    }

    /**
     * APK静默安装
     *
     * @param packageName 需要卸载应用的包名
     * @return true 静默卸载成功 false 静默卸载失败
     */
    public static boolean uninstall(final Context context, String packageName) {
        String[] args = {"pm", "uninstall", packageName};
        final String result = apkProcess(args);
//        Log.e(TAG, "uninstall log:"+result);
        ((MainActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View view1 = LayoutInflater.from(context).inflate(R.layout.test_cache, null);
                ((TextView) view1.findViewById(R.id.tv_content)).setText("" + result);
                new AlertDialog.Builder(context)
                        .setTitle("remove result")
                        .setView(view1)
                        .show();
            }
        });
        if (result != null
                && (result.endsWith("Success")
                || result.endsWith("Success\n"))) {
            return true;
        }
        return false;
    }

    /**
     * 应用安装、卸载处理
     *
     * @param args 安装、卸载参数
     * @return Apk安装、卸载结果
     */
    public static String apkProcess(String[] args) {
        String result = null;
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }

    /**
     * 安装
     */
    public static void install(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", new File(filePath));
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

//        Uri uri = Uri.parse("file://" + filePath);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * install slient
     *
     * @param filePath
     * @return 0 means normal, 1 means file not exist, 2 means other exception error
     */
    public static int installSilent(String filePath) {
        File file = new File(filePath);
        if (filePath == null || filePath.length() == 0 || file == null || file.length() <= 0 || !file.exists() || !file.isFile()) {
            return 1;
        }

        String[] args = {"pm", "install", "-r", filePath};
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        int result;
        try {
            process = processBuilder.start();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }

        // TODO should add memory is not enough here
        if (successMsg.toString().contains("Success") || successMsg.toString().contains("success")) {
            result = 0;
        } else {
            result = 2;
        }
        Log.d("test-test", "successMsg:" + successMsg + ", ErrorMsg:" + errorMsg);
        return result;
    }

    /**
     * 本机是否有安装该应用
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isInstallApp(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfos) {
            if (packageInfo.packageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSystemAppInstallOk(Context context, String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                install(context, filePath);
                return true;
            } else {
                Toast.makeText(context, "应用文件不存在", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "安装异常：\n" + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
        return false;
    }

    /**
     * 第二种方法
     * 直接跳转到当贝
     * @param context
     * @param packageName
     */
//    public static void goToMarket(Context context, String packageName) {
//        try {
//            Uri uri = Uri.parse("market://details?id=" + packageName);
//            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
//            goToMarket.setClassName("com.dangbeimarket", "com.tencent.pangu.link.LinkProxyActivity");
//            context.startActivity(goToMarket);
//        } catch (ActivityNotFoundException e) {
//            e.printStackTrace();
//            Toast.makeText(context, "您的手机没有安装当贝市场", Toast.LENGTH_SHORT).show();
//        }
//    }
    public static void goToMarket(Context context, String packageName) {
        try {
            Uri uri = Uri.parse("market://details?id=" + packageName);
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context, "您的手机没有安装应用市场", Toast.LENGTH_SHORT).show();
        }
    }

    public static void goToDangbei(Context context) {
        Intent i = new Intent();
        ComponentName cn = new ComponentName("com.dangbeimarket", "com.dangbeimarket.AndroidSearch");
        i.setComponent(cn);
        i.setAction("android.intent.action.MAIN");
        context.startActivity(i);
    }
    public static String execCommand(String... command) {
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        String result = "";
        try {
            process = new ProcessBuilder().command(command).start();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            result = new String(baos.toByteArray());
            if (inIs != null)
                inIs.close();
            if (errIs != null)
                errIs.close();
            process.destroy();
        } catch (IOException e) {

            result = e.getMessage();
        }
        Log.e("appmanager", "execCommand: " + result);
        return result;
    }

    /**
     * 获取app的下载目录
     *
     * @return
     */
    public static String getAppDir() {
        String path = Environment.getExternalStorageDirectory() + "/my-app/";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    /**
     * 启动老化app
     *
     * @param context
     */
    public static void startAgingApk(Context context) {
        try {
            File path = new File(Environment.getExternalStorageDirectory(), "/DragonBox/");
            if (!path.exists()) {
                path.mkdirs();
            }
            File file = new File(path, "custom_aging_cases.xml");
            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write("<TestCase save_report=\"1\">\n" +
                    "\t<CaseComprehensive>\n" +
                    "\t\t<CaseMemory>\n" +
                    "\t\t\t<Passable minCap=\"\"></Passable>\n" +
                    "\t\t</CaseMemory>\n" +
                    "\t\t<CaseVideo></CaseVideo>\n" +
                    "\t\t<CaseThreeDimensional></CaseThreeDimensional>\n" +
                    "\t</CaseComprehensive>\n" +
                    "</TestCase>\n");
            bw.close();

            Intent intent = new Intent();
            intent.setClassName("com.softwinner.agingdragonbox", "com.softwinner.agingdragonbox.Main");
            intent.putExtra("configPath", file.getAbsolutePath());

            context.startActivity(intent);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }
}
