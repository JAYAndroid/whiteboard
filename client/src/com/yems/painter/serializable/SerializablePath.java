package com.yems.painter.serializable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @description: 用于网络中传输的序列化路径对象
 * @date: 2015-3-16 下午11:08:23
 * @author: yems
 */
public class SerializablePath implements Serializable {
	/** 组成该路径的点*/
	public ArrayList<SerializablePoint> points = new ArrayList();
	/** 本地客户端操作类型（send-发送图形数据、clear-清空图形数据、send_uuid-发送设备唯一标示符）*/
	private String OPType = "send"; 
	/** 当前设备的屏幕宽度*/
	private float screenWidth; 
	/** 当前设备的屏幕高度*/
	private float screenHeight; 
	/** 当前设备的唯一标示符*/
	private String myUUID; 
	/** 当前图形所用的画笔样式*/
	private SerializablePaint serializablePaint; 
	/** */
	public String shapeType = "CURVE";
	
	public String getMyUUID() {
		return myUUID;
	}

	public void setMyUUID(String myUUID) {
		this.myUUID = myUUID;
	}

	public float getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(float screenWidth) {
		this.screenWidth = screenWidth;
	}

	public float getScreenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(float screenHeight) {
		this.screenHeight = screenHeight;
	}

	public SerializablePaint getSerializablePaint() {
		return serializablePaint;
	}

	public void setSerializablePaint(SerializablePaint serializablePaint) {
		this.serializablePaint = serializablePaint;
	}

	public String getOPType() {
		return OPType;
	}

	public void setOPType(String oPType) {
		OPType = oPType;
	}
}