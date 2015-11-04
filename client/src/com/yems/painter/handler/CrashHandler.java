package com.yems.painter.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

/**
 * @description: UncaughtException处理类,当程序发生Uncaught异常的时候,由该类来接管程序,并记录发送错误报告.
 * @date: 2015-3-12 下午3:19:43
 * @author: yems
 */
public class CrashHandler implements UncaughtExceptionHandler {

	public static final String TAG = "CrashHandler";

	// 系统默认的UncaughtException处理类
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	// CrashHandler实例
	private static CrashHandler INSTANCE = new CrashHandler();
	// 程序的Context对象
	private Context mContext;
	// 用来存储设备信息和异常信息
	private Map<String, String> infos = new HashMap<String, String>();
	// 用于格式化日期,作为日志文件名的一部分
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// 保存异常信息的所在路径
	private String CRASH_COLLECT_PATH = Environment
			.getExternalStorageDirectory().getPath() + "/Painter/crash/";

	/**
	 * 保证只有一个CrashHandler实例
	 */
	private CrashHandler() {

	}

	/**
	 * @description: 静态内部类，用于实例化CrashHandler
	 * @date: 2015-3-12 下午3:26:16
	 * @author: yems
	 */
	private static class Builder {
		private static CrashHandler instance = new CrashHandler();
	}

	/**
	 * @return
	 * @description: 获取CrashHandler实例 ,单例模式
	 * @date: 2015-3-12 下午3:25:09
	 * @author： yems
	 */
	public static CrashHandler getInstance() {
		return Builder.instance;
	}

	/**
	 * @param context
	 * @description: 初始化
	 * @date: 2015-3-12 下午3:26:56
	 * @author： yems
	 */
	public void init(Context context) {
		mContext = context;
		// 获取系统默认的UncaughtException处理器
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		// 设置该CrashHandler为程序的默认处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 当UncaughtException发生时会转入该函数来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			// 如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				Log.e(TAG, "error : ", e);
			}
			// 退出程序
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1); // 1(或者非0，表示非正常退出程序)
		}
	}

	/**
	 * @param ex
	 * @return 如果处理了该异常信息;否则返回false
	 * @description: 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成
	 * @date: 2015-3-12 下午3:39:55
	 * @author： yems
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		// 收集设备参数信息
		collectDeviceInfo(mContext);
		// 保存日志文件
		saveCrashInfo2File(ex);
		return true;
	}

	/**
	 * @param ctx
	 * @description: 收集设备参数信息
	 * @date: 2015-3-12 下午3:45:48
	 * @author： yems
	 */
	public void collectDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null"
						: pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {
			Log.e(TAG, "收集包信息是发生异常", e);
		}
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
				Log.d(TAG, field.getName() + " : " + field.get(null));
			} catch (Exception e) {
				Log.e(TAG, "收集异常信息时，发生异常", e);
			}
		}
	}

	/**
	 * @param ex
	 * @return
	 * @description: 保存错误信息到文件中
	 * @date: 2015-3-12 下午3:46:53
	 * @author： yems
	 */
	private String saveCrashInfo2File(Throwable ex) {
		// long timestamp = System.currentTimeMillis();
		String time = formatter.format(new Date());

		StringBuffer sb = new StringBuffer();
		sb.append("\n");
		sb.append("\n");
		sb.append("\n");
		sb.append("time=" + time + "\n");

		for (Map.Entry<String, String> entry : infos.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n");
		}

		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		sb.append(result);
		try {
			String fileName = "Painter_Crash.log";
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				File dir = new File(CRASH_COLLECT_PATH);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(CRASH_COLLECT_PATH
						+ fileName, true);
				fos.write(sb.toString().getBytes());
				fos.close();
			}
			return fileName;
		} catch (Exception e) {
			Log.e(TAG, "写文件是发生异常", e);
		}
		return null;
	}
}
