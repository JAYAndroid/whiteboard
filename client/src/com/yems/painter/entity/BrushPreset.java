package com.yems.painter.entity;

import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Color;

import com.yems.painter.common.Commons;

/**
 * @description: 画笔参数预设值管理类
 * @date: 2015-3-13 下午5:01:37
 * @author: yems
 */
public class BrushPreset {
	/** 当前画笔样式*/
	public float currentSize = 2; 
	/** 当前画笔颜色*/
	public int currentColor = Color.BLACK; 
	/** 当前画笔模糊类型*/
	public Blur currentBlurType = null;  
	/** 当前画笔模糊半径*/
	public int currentBlurRadius = 0;  
	/** 当前画笔类型*/
	public int currentBrushType = Commons.CUSTOM; 

	/**
	 * @param type 画笔类型（铅笔、自定义画笔）
	 * @param color 画笔颜色
	 */
	public BrushPreset(int type, int color) {
		switch (type) {
		case Commons.PEN:
			set(Commons.COMMON_SIZE, color);
			break;
		case Commons.CUSTOM:
			setColor(color);
			break;
		}
		setType(type);
	}

	public BrushPreset(float size) {
		set(size);
	}

	public BrushPreset(float size, int color) {
		set(size, color);
	}

	public void set(float size) {
		setSize(size);
	}

	public void set(float size, int color) {
		setSize(size);
		setColor(color);
	}

	public void setColor(int color) {
		this.currentColor = color;
		Commons.currentColor = color;
	}

	public void setSize(float size) {
		currentSize = (size > 0) ? size : 1;
		Commons.currentSize = currentSize;
	}

	public void setType(int type) {
		this.currentBrushType = type;
	}
}