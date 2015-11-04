package com.yems.painter.serializable;

import java.io.Serializable;

/**
 * @description: 组成路径的坐标点
 * @date: 2015-3-16 下午11:18:09
 * @author: yems
 */
public class SerializablePoint implements Serializable {
	/** x轴坐标值*/
	private float x;
	/** y轴坐标值*/
	private float y;

	public SerializablePoint(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float getX() {
		return this.x;
	}

	public float getY() {
		return this.y;
	}

	public void setX(float paramFloat) {
		this.x = paramFloat;
	}

	public void setY(float paramFloat) {
		this.y = paramFloat;
	}

	public String toString() {
		return "SerializablePoint [x=" + this.x + ", y=" + this.y + "]";
	}
}