/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yems.painter.control;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.yems.painter.R;

/**
 * @description: 拾色器对话框
 * @date: 2015-3-13 下午5:23:19
 * @author: yems
 */
public class ColorPickerControl extends Dialog {

	/**
	 * @description: 颜色变化监听接口
	 * @date: 2015-3-16 下午6:02:51
	 * @author: yems
	 */
	public interface OnColorChangedListener {
		void colorChanged(int color);
	}

	/** 画笔颜色变化监听器*/
	private OnColorChangedListener mListener;
	/** 拾色器初始化颜色*/
	private int mInitialColor;

	/**
	 * @description: 拾色器视图类
	 * @date: 2015-3-16 下午5:54:32
	 * @author: yems
	 */
	private static class ColorPickerView extends View {
		/** */
		private Paint mPaint;
		/** 负责绘制中间选中后的颜色*/
		private Paint mCenterPaint;
		/** 负责绘制环形渐变颜色*/
		private Paint mRadialPaint;
		/** 存在当前选中颜色的环形渐变色数据*/
		private final int[] mRadialColors;
		/** 颜色变化监听器*/
		private OnColorChangedListener mListener;
		/** 负责绘制梯度渐变颜色*/
		private Paint mGradientPaint;
		/** 存储线性渐变颜色值*/
		private int[] mLinearColors;

		ColorPickerView(Context c, OnColorChangedListener listener, int color) {
			super(c);
			mListener = listener;
			mRadialColors = new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF,
					0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };
			// 实例化着色器对象
			Shader s = new SweepGradient(0, 0, mRadialColors, null);
			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			// 设置画笔的指定拾色器
			mPaint.setShader(s);
			// 设置画笔样式
			mPaint.setStyle(Paint.Style.STROKE);
			// 设置画笔尺寸
			mPaint.setStrokeWidth(32);
			
			mLinearColors = getColors(color);
			Shader shader = new LinearGradient(0, 0, Center_X * 2, 0,
					mLinearColors, null, Shader.TileMode.CLAMP);

			mGradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mGradientPaint.setStyle(Paint.Style.STROKE);
			mGradientPaint.setShader(shader);
			mGradientPaint.setStrokeWidth(32);

			mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mCenterPaint.setColor(color);
			mCenterPaint.setStrokeWidth(6);

			mRadialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mRadialPaint.setColor(color);
			mRadialPaint.setStrokeWidth(6);
		}

		/**
		 * 
		 * @description: 获取指定颜色值的线性渐变颜色值（从黑过渡到指定的颜色，再到白色：黑色--color--白色）
		 * @date:  2015-3-16 下午9:06:46
		 * @author： yems
		 */
		private int[] getColors(int color) {
			if (color == Color.BLACK || color == Color.WHITE) {
				return new int[] { Color.BLACK, Color.WHITE };
			}
			return new int[] { Color.BLACK, color, Color.WHITE };
		}

		/** 标识是否高亮中间颜色选中块*/
		private boolean mHighlightCenter;
		/** 标记当前手指是否点击拾色器的中间位置，true是:false否*/
		private boolean mTrackingCenter;
		/** 标记当前手指是否点击在环形拾色器外围，true是:false否*/
		private boolean mTrackingLinGradient;

		@Override
		protected void onDraw(Canvas canvas) {
			float r = COLOR_CIRCLE - mPaint.getStrokeWidth() * 0.5f;
			canvas.translate(Center_X, COLOR_CIRCLE);
			canvas.drawOval(new RectF(-r, -r, r, r), mPaint);
			canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint);

			if (mTrackingCenter) {
				int color = mCenterPaint.getColor();
				mCenterPaint.setStyle(Paint.Style.STROKE);

				if (mHighlightCenter) {
					mCenterPaint.setAlpha(0xFF);
				} else {
					mCenterPaint.setAlpha(0x80);
				}
				canvas.drawCircle(0, 0,
						CENTER_RADIUS + mCenterPaint.getStrokeWidth(),
						mCenterPaint);

				mCenterPaint.setStyle(Paint.Style.FILL);
				mCenterPaint.setColor(color);
			}

			int color = mRadialPaint.getColor();
			mLinearColors = getColors(color);
			Shader shader = new LinearGradient(0, 0, Center_X * 2, 0,
					mLinearColors, null, Shader.TileMode.CLAMP);
			mGradientPaint.setShader(shader);

			canvas.translate(-Center_X, 0);
			canvas.drawLine(0, r + 50, Center_X * 2, r + 50, mGradientPaint);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			setMeasuredDimension(Center_X * 2, Center_Y * 2 + 70);
		}

		/** 拾色器中心点x轴坐标值*/
		private static int Center_X = 110;
		/** 拾色器中心点y轴坐标值*/
		private static int Center_Y = 100;
		/** 拾色器中心圆形半径*/
		private static final int CENTER_RADIUS = 32;
		/** 拾色器环形半径*/
		private static final int COLOR_CIRCLE = 100;
		/** π值*/
		private static final float PI = 3.1415926f;

		private int ave(int s, int d, float p) {
			return s + Math.round(p * (d - s));
		}

		private int interpColor(int colors[], float unit) {
			if (unit <= 0) {
				return colors[0];
			}
			if (unit >= 1) {
				return colors[colors.length - 1];
			}

			float p = unit * (colors.length - 1);
			int i = (int) p;
			p -= i;

			// p的取值范围为小数部分[0,1),i是索引值
			int colorPrev = colors[i];
			int colorNext = colors[i + 1];
			int alpha = ave(Color.alpha(colorPrev), Color.alpha(colorNext), p);
			int red = ave(Color.red(colorPrev), Color.red(colorNext), p);
			int green = ave(Color.green(colorPrev), Color.green(colorNext), p);
			int blue = ave(Color.blue(colorPrev), Color.blue(colorNext), p);
			return Color.argb(alpha, red, green, blue);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX() - Center_X;
			float y = event.getY() - COLOR_CIRCLE;
			// 手指是否落在中心点
			boolean inCenter = Math.sqrt(x * x + y * y) <= CENTER_RADIUS;
			// 手指是否落在拾色器环形外围
			boolean outOfRadialGradient = y > COLOR_CIRCLE;

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mTrackingCenter = inCenter;
				mTrackingLinGradient = outOfRadialGradient;
				if (inCenter) {
					mHighlightCenter = true;
					invalidate();
					break;
				}
			case MotionEvent.ACTION_MOVE:
				if (mTrackingCenter) {
					if (mHighlightCenter != inCenter) {
						mHighlightCenter = inCenter;
						invalidate();
					}
				} else if (mTrackingLinGradient) {
					float unit = Math.max(0,
							Math.min(Center_X * 2, x + Center_X))
							/ (Center_X * 2);
					mCenterPaint.setColor(interpColor(mLinearColors, unit));
					invalidate();
				} else {
					float angle = (float) Math.atan2(y, x);
					float unit = angle / (2 * PI);
					if (unit < 0) {
						unit += 1;
					}
					int color = interpColor(mRadialColors, unit);
					mCenterPaint.setColor(color);
					mRadialPaint.setColor(color);
					invalidate();
				}
				break;
			case MotionEvent.ACTION_UP:
				if (mTrackingCenter) {
					if (inCenter) {
						mListener.colorChanged(mCenterPaint.getColor());
					}
					mTrackingCenter = false;
					invalidate();
				}
				break;
			}
			return true;
		}
	}

	public ColorPickerControl(Context context, OnColorChangedListener listener,
			int initialColor) {
		super(context);
		mListener = listener;
		mInitialColor = initialColor;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		OnColorChangedListener l = new OnColorChangedListener() {
			public void colorChanged(int color) {
				mListener.colorChanged(color);
				dismiss();
			}
		};
		setContentView(new ColorPickerView(getContext(), l, mInitialColor));
		setTitle(R.string.color_pick);
	}
}