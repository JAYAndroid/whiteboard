package com.yems.painter;

import android.app.Activity;
import android.app.Application;

import com.yems.painter.handler.CrashHandler;

/**
 * @description: 程序入口初始化
 * @date: 2015-3-12 下午3:23:22
 * @author: yems
 */
public class PainterApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		initCrashSetting();
	}

	/**
	 * 
	 * @description: 初始化异常日志记录
	 * @date: 2015-3-12 下午4:00:59
	 * @author： yems
	 */
	private void initCrashSetting() {
		CrashHandler.getInstance().init(getApplicationContext());
	}
}
