package com.simon.imagewaveloading;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * java端异常捕获工具类 (注意使用时需要添加 读写sd权限)
 * 权限为：
 *   <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
 * 当接收java端未捕获的异常时，会将写到sd中，以便调试使用
 * 保存的路径为： Environment.getExternalStorageDirectory().getPath()+File.separator+CRASH_FILE_PATH+ File.separator+ "crash"
 */
public class CrashHandleUtil implements UncaughtExceptionHandler {
    private static final String TAG = "Crash";
    private Context mContext;
    private UncaughtExceptionHandler exceptionHandler;
    private Map<String, String> infos = new HashMap<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private String CRASH_FILE_PATH="";

    public static CrashHandleUtil mInstance;

    public static CrashHandleUtil getmInstance() {
        if(mInstance==null){
            synchronized (CrashHandleUtil.class){
                if(mInstance==null){
                    return new CrashHandleUtil();
                }
            }
        }
        return mInstance;
    }

    /**
     * 当异常时，会转到这个方法来处理
     *
     * @param thread 线程
     * @param ex 可抛出的异常
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && exceptionHandler != null) {
            exceptionHandler.uncaughtException(thread, ex);
        }
    }

    /**
     * @param context 设置这个异常处理器为系统默认的系统处理器
     * @category author zz4760762
     */
    public void init(Context context,String crashPath) {
        mContext = context;
        this.CRASH_FILE_PATH=crashPath;
        exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        // 收集设备参数
        collectionDeviceInfo(mContext);
//		 保存日志文件
        saveCrashInfo2File(ex);
        return true;
    }

    private void saveCrashInfo2File(Throwable ex) {
        ex.printStackTrace();
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        ex.printStackTrace(pw);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(pw);
            cause = cause.getCause();
        }
        pw.close();

        String result = writer.toString();
        sb.append(result);
        String time = dateFormat.format(new Date());
        String fileName =time+"-"+"crash" + ".txt";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String path = Environment.getExternalStorageDirectory().getPath()+File.separator+CRASH_FILE_PATH+ File.separator+ "crash";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(path + fileName);
            try {
                if (!file.exists())
                    file.createNewFile();
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 收集设备参数信息
     *
     * @param c
     */
    private void collectionDeviceInfo(Context c) {
        try {
            PackageManager pm = c.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(c.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi == null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            } catch (IllegalAccessException e) {
                Log.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }
}
