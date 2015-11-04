package com.yems.painter.entity;


import android.content.pm.ActivityInfo;

/**
 * @description: 画板默认设置
 * @date: 2015-3-16 上午11:18:37
 * @author: yems
 */
public class PainterSettings {
	/** 保存画笔预设参数*/
	private BrushPreset mPreset = null;
	/** 画板上的最近一次图形数据*/
	private String mLastPicture = null;
	/** 标志是否强制打开图形文件,true 是（只打开，不保存）,false 否（打开的同时保存图片）*/
	private boolean mForceOpenFile = false;
	/** 当前设备的屏幕方向*/
	private int mOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	
	public BrushPreset getPreset() {
		return mPreset;
	}
	public void setPreset(BrushPreset preset) {
		mPreset = preset;
	}
	public String getLastPicture() {
		return mLastPicture;
	}
	public void setLastPicture(String lastPicture) {
		mLastPicture = lastPicture;
	}
	public boolean isForceOpenFile() {
		return mForceOpenFile;
	}
	public void setForceOpenFile(boolean forceOpenFile) {
		mForceOpenFile = forceOpenFile;
	}
	public int getOrientation() {
		return mOrientation;
	}
	public void setOrientation(int orientation) {
		mOrientation = orientation;
	}
}