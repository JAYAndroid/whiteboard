package com.yems.painter.control;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.util.Log;
import android.view.SurfaceHolder;

import com.yems.painter.common.Commons;
import com.yems.painter.entity.BrushPreset;
import com.yems.painter.entity.UndoState;
import com.yems.painter.serializable.SerializablePaint;
import com.yems.painter.serializable.SerializablePath;
import com.yems.painter.serializable.ShapeRepositories;

/**
 * @description: 负责画板绘制的线程
 * @date: 2015-3-11 下午4:47:19
 * @author: yems
 */
public class PainterThreadControl extends Thread {
	private String TAG = "PainterThread";

	/** 控制surfaceView界面的显示 */
	private SurfaceHolder mHolder;
	/** 自定义画布实例 */
	PainterCanvasControl mPainterCanvas;
	/** 画笔实例对象 */
	public Paint mBrush;
	/** 画笔尺寸 */
	private float mBrushSize;
	/** 记录最后画笔X轴位置点，用来消除锯齿 */
	private int mLastBrushPointX;
	/** 记录最后画笔Y轴位置点，用来消除锯齿 */
	private int mLastBrushPointY;
	/** 画布的背景色 */
	private int mCanvasBgColor;
	/** 绘制图形的画布 */
	private Canvas mCanvas;
	/** 用于缓存画布上绘制的图形 */
	private Bitmap mBitmap;
	/** 标识当前绘制线程是否处于激活状态，ture是；false否 */
	private boolean mIsActive;
	/** 画笔的运行状态（休眠、准备、设置） */
	private int mStatus;
	/** 撤销图形操作的对象 */
	private UndoState mState;

	/**
	 * @param surfaceHolder
	 * @param painterCanvas
	 */
	public PainterThreadControl(SurfaceHolder surfaceHolder,
			PainterCanvasControl painterCanvas) {
		// base data
		mHolder = surfaceHolder;
		this.mPainterCanvas = painterCanvas;

		// defaults brush settings
		mBrushSize = 2;
		mBrush = new Paint();
		mBrush.setAntiAlias(true);
		mBrush.setColor(Color.rgb(0, 0, 0));
		mBrush.setStrokeWidth(mBrushSize);
		mBrush.setStrokeCap(Cap.ROUND);

		Commons.currentColor = Color.rgb(0, 0, 0);
		Commons.currentSize = 2;

		// default canvas settings
		mCanvasBgColor = Color.WHITE;

		// set negative coordinates for reset last point
		mLastBrushPointX = -1;
		mLastBrushPointY = -1;
	}

	@Override
	public void run() {
		waitForBitmap();

		while (isRun()) {
			Canvas canvas = null;
			try {
				canvas = mHolder.lockCanvas();
				synchronized (mHolder) {
					switch (mStatus) {
					case Commons.READY: {
						if (canvas != null) {
							canvas.drawBitmap(mBitmap, 0, 0, null);
						}
						break;
					}
					case Commons.SETUP: {
						if (canvas != null) {
							canvas.drawColor(mCanvasBgColor);
							canvas.drawLine(50,
									(mBitmap.getHeight() / 100) * 35,
									mBitmap.getWidth() - 50,
									(mBitmap.getHeight() / 100) * 35, mBrush);
						}
						break;
					}
					}
				}
			} finally {
				if (canvas != null) {
					mHolder.unlockCanvasAndPost(canvas);
				}
				if (isFreeze()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * @return
	 * @description: 获取当前画笔
	 * @date: 2015-3-16 下午2:11:47
	 * @author: yems
	 */
	public Paint getmBrush() {
		return mBrush;
	}

	/**
	 * @param bitmap
	 *            要还原到画板上的图片
	 * @param matrix
	 *            携带着bitmap旋转的角度值
	 * @description: 还原指定的bitmap图片到画板上
	 * @date: 2015-3-16 下午2:12:02
	 * @author: yems
	 */
	public void restoreBitmap(Bitmap bitmap, Matrix matrix) {
		mCanvas.drawBitmap(bitmap, matrix, new Paint(Paint.FILTER_BITMAP_FLAG));
	}

	/**
	 * @param preset
	 * @description: 当画笔的颜色、尺寸改变时调用该函数
	 * @date: 2015-3-16 下午2:15:14
	 * @author: yems
	 */
	public void setPreset(BrushPreset preset) {
		mBrush.setColor(preset.currentColor);
		mBrushSize = preset.currentSize;
		mBrush.setStrokeWidth(mBrushSize);
	}

	/**
	 * 
	 * @description: 开始绘制前，清除位置点的标记值
	 * @date: 2015-3-16 下午2:15:58
	 * @author: yems
	 */
	public void clearBrushPoint() {
		mLastBrushPointX = -1;
		mLastBrushPointY = -1;
	}

	/**
	 * @param x
	 *            x轴坐标值
	 * @param y
	 *            y轴坐标值
	 * @description: 绘制图形
	 * @date: 2015-3-12 上午11:17:26
	 * @author： yems
	 */
	public void draw(int x, int y) {
		Log.i("paths", "mCanvas---" + mCanvas + ",mBrush--" + mBrush);
		if (mLastBrushPointX > 0) {
			if (mLastBrushPointX - x == 0 && mLastBrushPointY - y == 0) {
				return;
			}
			if (mCanvas == null) {
				// 第一次进入APP，由于系统Surface创建的延时性，此时mCanvas还未实例化完成，服务端发送过来的数据不能马上绘制
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mCanvas.drawLine(x, y, mLastBrushPointX, mLastBrushPointY,
						mBrush);
			} else {
				mCanvas.drawLine(x, y, mLastBrushPointX, mLastBrushPointY,
						mBrush);
			}

		} else {
			if (mCanvas == null) {
				// 第一次进入APP，由于系统Surface创建的延时性，此时mCanvas还未实例化完成，服务端发送过来的数据不能马上绘制
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mCanvas.drawCircle(x, y, mBrush.getStrokeWidth() * .5f, mBrush);
			} else {
				mCanvas.drawCircle(x, y, mBrush.getStrokeWidth() * .5f, mBrush);
			}
		}
		mLastBrushPointX = x;
		mLastBrushPointY = y;
	}

	/**
	 * @param bitmap
	 *            要加载的背景图
	 * @param clear
	 *            true 表示先清除画板在加载背景图，false表示直接加载背景图
	 * @description: 设置当前画板的背景
	 * @date: 2015-3-16 下午2:19:11
	 * @author: yems
	 */
	public void setBitmap(Bitmap bitmap, boolean clear) {
		mBitmap = bitmap;
		if (clear) {
			mBitmap.eraseColor(mCanvasBgColor);
		}
		mCanvas = new Canvas(mBitmap);
	}

	/**
	 * 
	 * @description: 清除画板所有图形数据
	 * @date: 2015-3-16 下午2:19:25
	 * @author: yems
	 */
	public void clearBitmap() {
		mBitmap.eraseColor(mCanvasBgColor);
		if (ShapeRepositories.getInstance().getUndoCaches().size() != 0) {
			ShapeRepositories.getInstance().getUndoCaches().clear();
		}
		Commons.lastLocalPath = null;
		Commons.lastRemotePath = null;
	}

	/**
	 * @return
	 * @description: 获取当前绘制的图形
	 * @date: 2015-3-16 下午2:19:48
	 * @author: yems
	 */
	public Bitmap getBitmap() {
		return mBitmap;
	}

	/**
	 * 
	 * @description: 表示开启绘制线程
	 * @date: 2015-3-16 下午2:23:24
	 * @author: yems
	 */
	public void on() {
		mIsActive = true;
	}

	/**
	 * 
	 * @description: 表示关闭绘制线程
	 * @date: 2015-3-16 下午2:23:42
	 * @author: yems
	 */
	public void off() {
		mIsActive = false;
	}

	/**
	 * 
	 * @description: 表示休眠（暂停）绘制线程
	 * @date: 2015-3-16 下午2:24:12
	 * @author: yems
	 */
	public void freeze() {
		mStatus = Commons.SLEEP;
	}

	/**
	 * 
	 * @description: 表示绘制线程已经准备好，可以绘制了
	 * @date: 2015-3-16 下午2:24:29
	 * @author: yems
	 */
	public void activate() {
		mStatus = Commons.READY;
	}

	/**
	 * 
	 * @description: 画板处于设置画笔参数的状态
	 * @date: 2015-3-16 下午2:25:26
	 * @author: yems
	 */
	public void setup() {
		mStatus = Commons.SETUP;
	}

	/**
	 * @return
	 * @description: 判断画板是否处于冻结（绘制线程休眠）的状态
	 * @date: 2015-3-16 下午2:26:08
	 * @author: yems
	 */
	public boolean isFreeze() {
		return (mStatus == Commons.SLEEP);
	}

	/**
	 * @return
	 * @description: 判断画板是否处于设置画笔参数的状态
	 * @date: 2015-3-16 下午2:27:31
	 * @author: yems
	 */
	public boolean isSetup() {
		return (mStatus == Commons.SETUP);
	}

	/**
	 * @return
	 * @description: 判断画板是否处于准备好绘制的状态
	 * @date: 2015-3-16 下午2:27:34
	 * @author: yems
	 */
	public boolean isReady() {
		return (mStatus == Commons.READY);
	}

	/**
	 * @return
	 * @description: 判断绘制线程是否处于启动的状态
	 * @date: 2015-3-16 下午2:27:36
	 * @author: yems
	 */
	public boolean isRun() {
		return mIsActive;
	}

	/**
	 * 
	 * @description: 撤销操作（恢复上一次的图形数据）
	 * @date: 2015-3-12 下午12:53:39
	 * @author： yems
	 */
	public void undo() {
		int undoSize = ShapeRepositories.getInstance().getUndoCaches().size();
		if (undoSize != 0) {
			undoRepaint();
			// 恢复一次历史数据，对应集合里保存的数据就要移除掉，避免下次执行撤销操作时，又出现该图形
			ShapeRepositories.getInstance().getUndoCaches()
					.remove(undoSize - 1);

		} else {
			mBitmap.eraseColor(mCanvasBgColor);
			mPainterCanvas.changed(false);
			Commons.lastLocalPath = null;
			Commons.lastRemotePath = null;
		}
	}

	/**
	 * 
	 * @description: 撤销操作时，重绘制图形
	 * @date: 2015-3-12 下午4:44:39
	 * @author： yems
	 */
	private void undoRepaint() {
		mBitmap.eraseColor(mCanvasBgColor);
		repaintAllShapes(ShapeRepositories.getInstance().getUndoCaches());
	}

	/**
	 * @return
	 * @description: 获取画布的当前背景色
	 * @date: 2015-3-16 下午2:32:51
	 * @author: yems
	 */
	public int getBackgroundColor() {
		return mCanvasBgColor;
	}

	/**
	 * @param state
	 * @description: 设置撤销图形操作的对象
	 * @date: 2015-3-16 下午2:33:20
	 * @author: yems
	 */
	public void setState(UndoState state) {
		this.mState = state;
	}

	/**
	 * 
	 * @description: 等待画布背景图实例的创建
	 * @date: 2015-3-16 下午2:35:01
	 * @author: yems
	 */
	private void waitForBitmap() {
		while (mBitmap == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @description: 如果客户端是第一次连接到服务端，则绘制全部图形；否则只绘制最后一个
	 * @date: 2015-3-12 下午4:10:24
	 * @author： yems
	 */
	public void repaintNewShape(List<SerializablePath> paths) {
		int size = paths.size();
		clearBrushPoint();

		// 获取集合中最后一个图形数据（服务端传过来的最新图形数据）
		SerializablePath newSerializablePath = paths.get(size - 1);
		float srcScreenWidth = newSerializablePath.getScreenWidth();
		float srcScreenHeight = newSerializablePath.getScreenHeight();
		SerializablePaint serializablePaint = newSerializablePath
				.getSerializablePaint();

		updatePaintParams(serializablePaint);

		// 在屏幕像素相同的设备上绘图，不用转换坐标值
		if (checkScreenDisplay(srcScreenWidth, srcScreenHeight)) {
			int pointCount = newSerializablePath.points.size(); // 每一条路径的组成的点数
			for (int j = 0; j < pointCount; j++) {
				int x = (int) newSerializablePath.points.get(j).getX();
				int y = (int) newSerializablePath.points.get(j).getY();
				draw(x, y);
			}
		} else {
			// 不同像素的设备上绘图，需要转换坐标值
			int pointCount = newSerializablePath.points.size(); // 每一条路径的组成的点数
			for (int j = 0; j < pointCount; j++) {
				int x = (int) newSerializablePath.points.get(j).getX();
				int y = (int) newSerializablePath.points.get(j).getY();
				draw((int) (x * (Commons.CURRENT_SCREEN_WIDTH / srcScreenWidth)),
						(int) (y * (Commons.CURRENT_SCREEN_HEIGHT / srcScreenHeight)));
			}
		}
		clearBrushPoint();
		resetColorAndSize();
	}

	/**
	 * 重绘制所有的图形数据（当有一个新的客户端后续接入通道时，服务端原有的图形数据需要一次性重绘制到该新客户端）
	 */
	public void repaintAllShapes(List<SerializablePath> paths) {
		// List<SerializablePath> paths = MetadataRepositories.getInstance()
		// .getBufferShapes();
		int size = paths.size();
		Log.i("paths", "重绘制所有的图形数据，图形数目为---" + size);

		for (int i = 0; i < paths.size(); i++) {
			clearBrushPoint();
			// 遍历获取集合中每一个图形数据（服务端传过来的所有图形数据）
			SerializablePath serializablePath = paths.get(i);
			float srcScreenWidth = serializablePath.getScreenWidth();
			float srcScreenHeight = serializablePath.getScreenHeight();
			SerializablePaint serializablePaint = serializablePath
					.getSerializablePaint();
			updatePaintParams(serializablePaint);

			// 在屏幕分辨率相同的设备上绘图，不用转换坐标值
			if (checkScreenDisplay(srcScreenWidth, srcScreenHeight)) {
				int pointCount = serializablePath.points.size(); // 每一条路径的组成的点数
				for (int j = 0; j < pointCount; j++) {
					int x = (int) serializablePath.points.get(j).getX();
					int y = (int) serializablePath.points.get(j).getY();
					draw(x, y);
				}
			} else {
				// 不同像素的设备上绘图，需要转换坐标值
				int pointCount = serializablePath.points.size(); // 每一条路径的组成的点数
				for (int j = 0; j < pointCount; j++) {
					int x = (int) serializablePath.points.get(j).getX();
					int y = (int) serializablePath.points.get(j).getY();
					draw((int) (x * (Commons.CURRENT_SCREEN_WIDTH / srcScreenWidth)),
							(int) (y * (Commons.CURRENT_SCREEN_HEIGHT / srcScreenHeight)));
				}
			}
			clearBrushPoint();
			resetColorAndSize();
		}
	}

	/**
	 * 检测设备间的屏幕分辨率是否相同
	 * 
	 * @param srcScreenWidth
	 * @param srcScreenHeight
	 * @return
	 */
	private boolean checkScreenDisplay(float srcScreenWidth,
			float srcScreenHeight) {
		return srcScreenWidth == Commons.CURRENT_SCREEN_WIDTH
				&& srcScreenHeight == Commons.CURRENT_SCREEN_HEIGHT;
	}

	private void resetColorAndSize() {
		mBrush.setStrokeWidth(Commons.currentSize);
		mBrush.setColor(Commons.currentColor);
	}

	/**
	 * @param serializablePaint
	 * @description:  更新画笔的参数（颜色、尺寸）
	 * @date: 2015-3-16 下午2:38:05
	 * @author: yems
	 */
	private void updatePaintParams(SerializablePaint serializablePaint) {
		// 记录当前设备画笔的参数值，在多台设备间
		Commons.currentColor = mBrush.getColor();
		Commons.currentSize = mBrush.getStrokeWidth();

		mBrush.setStrokeWidth(serializablePaint.getSize());
		mBrush.setColor(serializablePaint.getColor());
	}
}
