package com.yems.painter.control;

import java.io.FileNotFoundException;
import java.util.List;

import org.apache.commons.lang.SerializationUtils;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.yems.painter.common.Commons;
import com.yems.painter.entity.BrushPreset;
import com.yems.painter.entity.MetadataRepositories;
import com.yems.painter.entity.UndoState;
import com.yems.painter.handler.PainterHandler;
import com.yems.painter.serializable.SerializablePaint;
import com.yems.painter.serializable.SerializablePath;
import com.yems.painter.serializable.SerializablePoint;
import com.yems.painter.serializable.ShapeRepositories;
import com.yems.painter.utils.Utils;

/**
 * @description: 实现绘图功能的自定义画布类
 * @date: 2015-3-16 下午2:01:12
 * @author: yems
 */
public class PainterCanvasControl extends SurfaceView implements Callback {
	private String TAG = "PainterCanvas";

	/** 画板绘制线程*/
	private PainterThreadControl mThread;
	/** 缓存画板上的图形数据*/
	private Bitmap mBitmap;
	/** 画笔预设值管理对象*/
	private BrushPreset mPreset;
	/** 画笔业务处理类*/
	private PainterHandler mPainterHandler;
	/** 标志是否处于画笔参数设置界面,true是:false否*/
	private boolean mIsSetup;
	/** 标志画板图像数据是否处于变化状态，true是：false否*/
	private boolean mIsChanged;
	/** 管理网络图像传输的客户端对象*/
	private ClientControl mClient;
	/** 画板上的当前刚绘制完成的路径 */
	public SerializablePath mCurrentPath;
	/** 用于网络传输的画笔系列化对象*/
	private SerializablePaint mPaint;

	/** 
	 * @return SerializablePaint
	 * @description:  获取画笔对象 
	 * @date 2015-3-16 下午10:10:31    
	 * @author: yems 
	 */
	public SerializablePaint getPaint() {
		return mPaint;
	}

	public PainterCanvasControl(Context context, AttributeSet attrs) {
		super(context, attrs);
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		mPreset = new BrushPreset(Commons.PEN, Color.BLACK);
		mPaint = new SerializablePaint();
		setFocusable(true);
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if (!hasWindowFocus) {
			getThread().freeze();
		} else {
			if (!isSetup()) {
				getThread().activate();
			} else {
				getThread().setup();
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (mBitmap == null) {
			mBitmap = Bitmap.createBitmap(width, height,
					Bitmap.Config.ARGB_4444);
			getThread().setBitmap(mBitmap, true);

			Bitmap bitmap = mPainterHandler.getLastPicture();

			if (bitmap != null) {
				float bitmapWidth = bitmap.getWidth();
				float bitmapHeight = bitmap.getHeight();
				float scale = 1.0f;

				Matrix matrix = new Matrix();
				if (width != bitmapWidth || height != bitmapHeight) {
					if (width == bitmapHeight || height == bitmapWidth) {
						if (width > height) {
							matrix.postRotate(-90, width / 2, height / 2);
						} else if (bitmapWidth != bitmapHeight) {
							matrix.postRotate(90, width / 2, height / 2);
						} else {
							if (Commons.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
								matrix.postRotate(-90, width / 2, height / 2);
							}
						}
					} else {
						if (Commons.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
							if (bitmapWidth > bitmapHeight
									&& bitmapWidth > width) {
								scale = (float) width / bitmapWidth;
							} else if (bitmapHeight > bitmapWidth
									&& bitmapHeight > height) {
								scale = (float) height / bitmapHeight;
							}
						} else {
							if (bitmapHeight > bitmapWidth
									&& bitmapHeight > height) {
								scale = (float) height / bitmapHeight;
							} else if (bitmapWidth > bitmapHeight
									&& bitmapWidth > width) {
								scale = (float) width / bitmapWidth;
							}
						}
					}

					if (scale == 1.0f) {
						matrix.preTranslate((width - bitmapWidth) / 2,
								(height - bitmapHeight) / 2);
					} else {
						matrix.postScale(scale, scale, bitmapWidth / 2,
								bitmapHeight / 2);
						matrix.postTranslate((width - bitmapWidth) / 2,
								(height - bitmapHeight) / 2);
					}
				}
				getThread().restoreBitmap(bitmap, matrix);
			}
		} else {
			getThread().setBitmap(mBitmap, false);
		}
		getThread().setPreset(mPreset);
		if (!isSetup()) {
			getThread().activate();
		} else {
			getThread().setup();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		getThread().on();
		getThread().start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		getThread().off();
		while (retry) {
			try {
				getThread().join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
		mThread = null;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!getThread().isReady()) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mIsChanged = true;
			Commons.pickRemotePathFirst = true;
			getThread().clearBrushPoint();
			setPathValue();

			// 当前实现只有画曲线“curve”,后续可扩展其他图形
			mCurrentPath.points.add(new SerializablePoint(event.getX(), event
					.getY()));
			break;

		case MotionEvent.ACTION_MOVE:
			getThread().draw((int) event.getX(), (int) event.getY());
			mCurrentPath.points.add(new SerializablePoint(event.getX(), event
					.getY()));
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			getThread().clearBrushPoint();
			applyShape();

			if (mClient != null) {
				mClient.commitShapToServer();
			}
			break;
		}
		return true;
	}

	/** 
	 * @return void
	 * @description:  给每一条路径设置初始值
	 * @date 2015-3-16 下午10:11:57    
	 * @author: yems 
	 */
	private void setPathValue() {
		mCurrentPath = new SerializablePath();
		mCurrentPath.setOPType("send");
		mCurrentPath.setScreenHeight(Commons.CURRENT_SCREEN_HEIGHT);
		mCurrentPath.setScreenWidth(Commons.CURRENT_SCREEN_WIDTH);
		mCurrentPath.setMyUUID(Commons.myUUID);
		mCurrentPath
				.setSerializablePaint((SerializablePaint) SerializationUtils
						.clone(mPaint));

		Utils.pickUndoCaches();
	}

	public PainterThreadControl getThread() {
		if (mThread == null) {
			mThread = new PainterThreadControl(getHolder(), this);
			mThread.setState(UndoState.getInstance());
		}
		return mThread;
	}

	private void applyShape() {
		try {
			MetadataRepositories.getInstance().addShape(
					(SerializablePath) SerializationUtils.clone(mCurrentPath));
			Commons.lastLocalPath = mCurrentPath;
			mCurrentPath = null;
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	/** 
	 * @return void
	 * @description: 保存当前画板上的图形
	 * @date 2015-3-16 下午10:12:28    
	 * @author: yems 
	 */
	public void saveBitmap(String pictureName) throws FileNotFoundException {
		synchronized (getHolder()) {
			Utils.getInstance()
					.saveBitmap(pictureName, getThread().getBitmap());
		}
		changed(false);
	}

	/** 
	 * @return BrushPreset
	 * @description: 获取当前画笔 
	 * @date 2015-3-16 下午10:13:48    
	 * @author: yems 
	 */
	public BrushPreset getCurrentPreset() {
		return mPreset;
	}

	/** 
	 * @return void
	 * @description: 设置画笔颜色 
	 * @date 2015-3-16 下午10:13:58    
	 * @author: yems 
	 */
	public void setPresetColor(int color) {
		mPaint.setColor(color);
		mPreset.setColor(color);
		Log.i(TAG, "设置了画笔颜色值--" + color);
		getThread().setPreset(mPreset);
	}

	/** 
	 * @return void
	 * @description: 设置画笔尺寸 
	 * @date 2015-3-16 下午10:14:08    
	 * @author: yems 
	 */
	public void setPresetSize(float size) {
		mPreset.setSize(size);
		mPaint.setSize(size);
		Log.i(TAG, "设置了画笔大小--" + size);
		getThread().setPreset(mPreset);
	}

	/** 
	 * @return void
	 * @description: 设置画笔 
	 * @date 2015-3-16 下午10:14:17    
	 * @author: yems 
	 */
	public void setPreset(BrushPreset preset) {
		mPreset = preset;
		getThread().setPreset(mPreset);
	}

	/** 
	 * @return boolean
	 * @description: 是否处于设置画笔参数界面 
	 * @date 2015-3-16 下午10:14:36    
	 * @author: yems 
	 */
	public boolean isSetup() {
		return mIsSetup;
	}

	/** 
	 * @return void
	 * @description: 切换画板界面和画笔参数设置界面 
	 * @date 2015-3-16 下午10:15:40    
	 * @author: yems 
	 */
	public void setup(boolean setup) {
		mIsSetup = setup;

		if (mIsSetup) {
			getThread().setup();
		} else {
			getThread().activate();
		}
	}

	/** 
	 * @return boolean
	 * @description: 是否画板上图形数据有变化 
	 * @date 2015-3-16 下午10:16:16    
	 * @author: yems 
	 */
	public boolean isChanged() {
		return mIsChanged;
	}

	/**
	 * @param changed
	 *            true表示当前画布图形更新了
	 * @description: 设置当前画布上的图形是否有更新
	 * @date: 2015-3-16 下午2:31:28
	 * @author: yems
	 */
	public void changed(boolean changed) {
		mIsChanged = changed;
	}

	/** 
	 * @return void
	 * @description: 执行撤销图形操作 
	 * @date 2015-3-16 下午10:17:08    
	 * @author: yems 
	 */
	public void undo() {
		getThread().undo();
	}

	/** 
	 * @return void
	 * @description: 重新绘制图形 
	 * @date 2015-3-16 下午10:17:25    
	 * @author: yems 
	 */
	public void repaint(boolean isRepaintAll) {
		List<SerializablePath> bufferShapes = MetadataRepositories
				.getInstance().getBufferShapes();
		if (isRepaintAll) {
			getThread().repaintAllShapes(bufferShapes);
		} else {
			getThread().repaintNewShape(bufferShapes);
		}
	}

	/** 
	 * @return void
	 * @description: 设置序列化画笔的参数 
	 * @date 2015-3-16 下午10:18:38    
	 * @author: yems 
	 */
	public void setSerializablePaintStyle(int color, float size) {
		mPaint.setSize(size);
		mPaint.setColor(color);
	}

	/** 
	 * @return void
	 * @description: 初始化客户端对象和 画板业务处理对象 
	 * @date 2015-3-16 下午10:45:06    
	 * @author: yems 
	 */
	public void init(ClientControl client, PainterHandler painterHandler) {
		mPainterHandler = painterHandler;
		mClient = client;
	}
}
