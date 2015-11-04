package com.yems.painter.activity;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.yems.painter.R;
import com.yems.painter.common.Commons;
import com.yems.painter.control.ClientControl;
import com.yems.painter.control.ColorPickerControl;
import com.yems.painter.control.PainterCanvasControl;
import com.yems.painter.entity.BrushPreset;
import com.yems.painter.entity.PainterSettings;
import com.yems.painter.handler.PainterHandler;
import com.yems.painter.task.SaveTask;
import com.yems.painter.task.SetWallpaperTask;
import com.yems.painter.utils.Utils;

/**
 * 画板基类
 * 
 * @description:
 * @date: 2015-3-11 下午4:48:08
 * @author: yems
 */
public class BasePainter extends Activity implements OnClickListener
{
	/** 白板的画布 */
	protected PainterCanvasControl mCanvas;
	/** 自定义参数的布局 */
	protected RelativeLayout mSettingsLayout;
	/** 画笔属性设置栏 */
	protected LinearLayout mPropertiesBar;
	/** 代表画板客户端（相对C/S模式的服务端而言） */
	protected ClientControl client;
	/** 画板业务处理类 */
	protected PainterHandler mPainterHandler;
	/** 两次按下返回键的间隔时间 */
	protected long exitTime = 0;
	/** 画笔尺寸大小 */
	protected SeekBar mBrushSize;
	/** 保存画笔 */
	protected PainterSettings mSettings;
	/** 音量快捷键（可以改变画笔颜色、或者改变画笔尺寸） */
	protected int mVolumeButtonsShortcuts;
	/** 拾色器按钮 */
	protected ImageButton changeBrushColor;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// 进入画板界面时,屏幕保持常亮,当锁屏时仍显示该界面
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.setting_main, menu);
		return true;
	}

	@Override
	public void openOptionsMenu()
	{
		super.openOptionsMenu();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		if (itemId == R.id.menu_brush && !mCanvas.isSetup())
		{
			// 进入画笔样式设置界面
			enterBrushSetup();
		} else if (itemId == R.id.menu_save)
		{
			// 保存当前画板上的图形数据
			savePicture(Commons.ACTION_SAVE_AND_RETURN);
		} else if (itemId == R.id.menu_clear)
		{
			if (mCanvas.isChanged())
			{
				// 显示保存图形提示框
				showDialog(R.id.dialog_clear);
			} else
			{
				// 清除所有图形数据
				clear();
			}
		} else if (itemId == R.id.menu_share)
		{
			// 进入分享界面
			share();
		} else if (itemId == R.id.menu_open)
		{
			// 打开图形文件
			open();
		} else if (itemId == R.id.menu_undo)
		{
			// 执行撤销操作
			mCanvas.undo();
		} else if (itemId == R.id.menu_preferences)
		{
			// 进入参数设置界面
			mPainterHandler.showPreferences();
		} else if (itemId == R.id.menu_set_wallpaper)
		{
			// 设置壁纸
			new SetWallpaperTask(this, mCanvas, mSettings).execute();
		}
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		if (id == R.id.dialog_clear)
		{
			// 创建清除画板图形数据的对话框
			return mPainterHandler.createDialogClear(this);
		} else if (id == R.id.dialog_exit)
		{
			// 创建退出画板对话框
			return createDialogExit();
		} else if (id == R.id.dialog_share)
		{
			// 创建分享图形数据对话框
			return mPainterHandler.createDialogShare(this, mSettings);
		} else if (id == R.id.dialog_open)
		{
			// 创建打开图形数据对话框
			return createDialogOpen();
		}
		return super.onCreateDialog(id);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
		case KeyEvent.KEYCODE_BACK:
			if (mCanvas.isSetup())
			{
				// 按下返回键，如果当前处于画笔设置界面，则退出到画板主界面
				exitBrushSetup();
				return true;
			} else if (mCanvas.isChanged() || (!Commons.mIsNewFile && !new File(mSettings.getLastPicture()).exists()))
			{

				mSettings.setPreset(mCanvas.getCurrentPreset());
				Utils.getInstance().saveSettings(this, mSettings);

				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

				int beforeExit = Integer.parseInt(preferences.getString(getString(R.string.preferences_before_exit), String.valueOf(Commons.BEFORE_EXIT_SUBMIT)));

				if (mCanvas.isChanged() && beforeExit == Commons.BEFORE_EXIT_SUBMIT)
				{
					// 退出前，显示保存图形对话框
					showDialog(R.id.dialog_exit);
				} else if (beforeExit == Commons.BEFORE_EXIT_SAVED)
				{
					// 不显示提示框，直接退出
					savePicture(Commons.ACTION_SAVE_AND_EXIT);
				} else
				{
					return super.onKeyDown(keyCode, event);
				}
				return true;
			}

			// 按下两次返回键退出应用
			if (event.getAction() == KeyEvent.ACTION_DOWN)
			{
				if ((System.currentTimeMillis() - exitTime) > 2000)
				{
					Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
					exitTime = System.currentTimeMillis();
				} else
				{
					finishPainter();
				}
				return true;
			}
			break;

		// 按下音量增快捷键
		case KeyEvent.KEYCODE_VOLUME_UP:
			switch (mVolumeButtonsShortcuts)
			{
			// 快捷键功能为增加画笔尺寸
			case Commons.SHORTCUTS_VOLUME_BRUSH_SIZE:
				mCanvas.setPresetSize(mCanvas.getCurrentPreset().currentSize + 1);
				if (mCanvas.isSetup())
				{
					updateControls();
				}
				break;

			// 快捷键功能为执行撤销操作
			case Commons.SHORTCUTS_VOLUME_UNDO_REDO:
				if (!mCanvas.isSetup())
				{
					mCanvas.undo();
				}
				break;
			}
			return true;

			// 按下音量减快捷键
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			switch (mVolumeButtonsShortcuts)
			{
			// 快捷键功能为减小画笔尺寸
			case Commons.SHORTCUTS_VOLUME_BRUSH_SIZE:
				mCanvas.setPresetSize(mCanvas.getCurrentPreset().currentSize - 1);
				if (mCanvas.isSetup())
				{
					updateControls();
				}
				break;
			// 快捷键功能为执行撤销操作
			case Commons.SHORTCUTS_VOLUME_UNDO_REDO:
				if (!mCanvas.isSetup())
				{
					mCanvas.undo();
				}
				break;
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 
	 * @description: 进入画笔设置界面
	 * @date: 2015-3-16 下午12:05:07
	 * @author： yems
	 */
	protected void enterBrushSetup()
	{
		mSettingsLayout.setVisibility(View.VISIBLE);

		new Handler().postDelayed(new Runnable()
		{
			public void run()
			{
				mCanvas.setVisibility(View.INVISIBLE);
				setPanelVerticalSlide(mPropertiesBar, Commons.SLIDE_FROM, Commons.SLIDE_TO, Commons.SLIDE_DURATION, true);
				mCanvas.setup(true);
			}
		}, 10);
	}

	/**
	 * 
	 * @description: 清除画板里的所有图形
	 * @date: 2015-3-16 下午12:59:56
	 * @author： yems
	 */
	public void clear()
	{
		mCanvas.getThread().clearBitmap();
		mCanvas.changed(false);
		clearSettings();
		Commons.mIsNewFile = true;
		updateControls();

		client.clearAllShapes();
	}

	/**
	 * 
	 * @description: 分享的界面
	 * @date: 2015-3-16 下午1:08:39
	 * @author： yems
	 */
	protected void share()
	{
		if (!Utils.getInstance().isStorageAvailable(this))
		{
			return;
		}
		// 如果是新创建的图形，则先保存再分享
		if (mCanvas.isChanged() || Commons.mIsNewFile)
		{
			if (Commons.mIsNewFile)
			{
				savePicture(Commons.ACTION_SAVE_AND_SHARE);
			} else
			{
				showDialog(R.id.dialog_share);
			}
		} else
		{
			mPainterHandler.startShareActivity(mSettings.getLastPicture());
		}
	}

	/**
	 * 
	 * @description: 选旋转屏幕
	 * @date: 2015-3-16 下午1:12:24
	 * @author： yems
	 */
	protected void rotate()
	{
		mSettings.setForceOpenFile(true);

		if (!Commons.mIsNewFile || mCanvas.isChanged())
		{
			savePicture(Commons.ACTION_SAVE_AND_ROTATE);
		} else
		{
			Utils.getInstance().rotateScreen(this, mSettings);
		}
	}

	/**
	 * 
	 * @description: 加载设置的历史参数（画笔的颜色，大小）
	 * @date: 2015-3-16 下午1:12:42
	 * @author： yems
	 */
	protected void loadSettings()
	{
		mSettings = new PainterSettings();
		SharedPreferences settings = getSharedPreferences(Commons.SETTINGS_STORAGE, Context.MODE_PRIVATE);

		int orientation = settings.getInt(getString(R.string.settings_orientation), ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mSettings.setOrientation(orientation);

		if (getRequestedOrientation() != mSettings.getOrientation())
		{
			setRequestedOrientation(mSettings.getOrientation());
		}

		mSettings.setLastPicture(settings.getString(getString(R.string.settings_last_picture), null));

		int type = settings.getInt(getString(R.string.settings_brush_type), Commons.PEN);

		// 自定义画笔样式（改变画笔的颜色和尺寸大小）
		if (type == Commons.CUSTOM)
		{
			float size = settings.getFloat(getString(R.string.settings_brush_size), Commons.DEFAULT_BRUSH_SIZE);
			int color = settings.getInt(getString(R.string.settings_brush_color), Color.BLACK);

			mSettings.setPreset(new BrushPreset(size, color));
			mSettings.getPreset().setType(type);
			mCanvas.setSerializablePaintStyle(color, size);

		} else
		{
			mSettings.setPreset(new BrushPreset(settings.getInt(getString(R.string.settings_brush_color), Color.BLACK)));

			mCanvas.setSerializablePaintStyle(mSettings.getPreset().currentColor, mSettings.getPreset().currentSize);
		}
		mCanvas.setPreset(mSettings.getPreset());
		mSettings.setForceOpenFile(settings.getBoolean(getString(R.string.settings_force_open_file), false));
	}

	/**
	 * 
	 * @description: 退出画笔设置界面
	 * @date: 2015-3-16 上午11:37:25
	 * @author： yems
	 */
	protected void exitBrushSetup()
	{
		mSettingsLayout.setBackgroundColor(Color.WHITE);

		if (Commons.mIsHardwareAccelerated)
		{
			setPanelVerticalSlide(mPropertiesBar, 0.0f, 1.0f, Commons.SLIDE_DURATION, true);
			mCanvas.setup(false);
		} else
		{
			Handler handler = new Handler();
			handler.postDelayed(new Runnable()
			{
				public void run()
				{
					mCanvas.setVisibility(View.INVISIBLE);
					setPanelVerticalSlide(mPropertiesBar, 0.0f, 1.0f, Commons.SLIDE_DURATION, true);
					mCanvas.setup(false);
				}
			}, 10);
		}
	}

	/**
	 * @return
	 * @description: 创建退出画板的对话框
	 * @date: 2015-3-16 上午11:33:12
	 * @author： yems
	 */
	protected Dialog createDialogExit()
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setMessage(R.string.exit_app_prompt);
		alert.setCancelable(false);
		alert.setTitle(R.string.exit_app_prompt_title);

		alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
		{

			public void onClick(DialogInterface dialog, int which)
			{
				savePicture(Commons.ACTION_SAVE_AND_EXIT);
			}
		});
		alert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
		{

			public void onClick(DialogInterface dialog, int which)
			{
				finishPainter();
			}
		});
		return alert.create();
	}

	/**
	 * @return
	 * @description: 创建打开图形文件对话框
	 * @date: 2015-3-16 上午11:35:59
	 * @author： yems
	 */
	protected Dialog createDialogOpen()
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setMessage(R.string.open_prompt);
		alert.setCancelable(false);
		alert.setTitle(R.string.open_prompt_title);

		alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
		{

			public void onClick(DialogInterface dialog, int which)
			{
				savePicture(Commons.ACTION_SAVE_AND_OPEN);
			}
		});
		alert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
		{

			public void onClick(DialogInterface dialog, int which)
			{
				mPainterHandler.startOpenActivity();
			}
		});

		return alert.create();
	}

	/**
	 * @param action
	 * @description: 保存当前画板上的图形数据
	 * @date: 2015-3-16 上午11:37:41
	 * @author： yems
	 */
	protected void savePicture(int action)
	{
		if (!Utils.getInstance().isStorageAvailable(this))
		{
			return;
		}
		final int taskAction = action;

		new SaveTask(this, mCanvas, mSettings)
		{
			protected void onPostExecute(String pictureName)
			{
				Commons.mIsNewFile = false;

				if (taskAction == Commons.ACTION_SAVE_AND_SHARE)
				{
					mPainterHandler.startShareActivity(pictureName);
				}

				if (taskAction == Commons.ACTION_SAVE_AND_OPEN)
				{
					mPainterHandler.startOpenActivity();
				}

				super.onPostExecute(pictureName);

				if (taskAction == Commons.ACTION_SAVE_AND_EXIT)
				{
					finishPainter();
				}
				if (taskAction == Commons.ACTION_SAVE_AND_ROTATE)
				{
					Utils.getInstance().rotateScreen(BasePainter.this, mSettings);
				}
			}
		}.execute();
	}

	/**
	 * 
	 * @description:更新画笔尺寸控件的当前状态值
	 * @date: 2015-3-16 下午1:40:57
	 * @author： yems
	 */
	protected void updateControls()
	{
		mBrushSize.setProgress((int) mCanvas.getCurrentPreset().currentSize);
	}

	/**
	 * 设置面板垂直滑动(退出设置参数的界面时，如果设备支持硬件加速，则调用此方法)
	 * 
	 * @param layout
	 * @param from
	 * @param to
	 * @param duration
	 */
	protected void setPanelVerticalSlide(LinearLayout layout, float from, float to, int duration)
	{
		setPanelVerticalSlide(layout, from, to, duration, false);
	}

	/**
	 * @param layout
	 *            要播放动画的布局
	 * @param from
	 *            动画起始点
	 * @param to
	 *            动画结束点
	 * @param duration
	 *            动画时长
	 * @param last
	 * @description: 给指定布局添加垂直滑入动画
	 * @date: 2015-3-16 下午12:55:19
	 * @author： yems
	 */
	private void setPanelVerticalSlide(LinearLayout layout, float from, float to, int duration, boolean last)
	{
		Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, from, Animation.RELATIVE_TO_SELF, to);

		animation.setDuration(duration);
		animation.setFillAfter(true);
		animation.setInterpolator(this, android.R.anim.decelerate_interpolator);

		final float listenerFrom = Math.abs(from);
		final float listenerTo = Math.abs(to);
		final boolean listenerLast = last;
		final View listenerLayout = layout;

		if (listenerFrom > listenerTo)
		{
			listenerLayout.setVisibility(View.VISIBLE);
		}

		animation.setAnimationListener(new Animation.AnimationListener()
		{

			public void onAnimationStart(Animation animation)
			{
			}

			public void onAnimationRepeat(Animation animation)
			{
			}

			public void onAnimationEnd(Animation animation)
			{
				if (listenerFrom < listenerTo)
				{
					listenerLayout.setVisibility(View.INVISIBLE);
					if (listenerLast)
					{
						mCanvas.setVisibility(View.VISIBLE);
						Handler handler = new Handler();
						handler.postDelayed(new Runnable()
						{
							public void run()
							{
								mSettingsLayout.setVisibility(View.GONE);
							}
						}, 10);
					}
				} else
				{
					if (listenerLast)
					{
						mCanvas.setVisibility(View.VISIBLE);
						Handler handler = new Handler();
						handler.postDelayed(new Runnable()
						{
							public void run()
							{
								mSettingsLayout.setBackgroundColor(Color.TRANSPARENT);
							}
						}, 10);
					}
				}
			}
		});
		layout.setAnimation(animation);
	}

	/**
	 * 
	 * @description: 打开已经保存在SD上的文件
	 * @date: 2015-3-16 下午1:39:57
	 * @author： yems
	 */
	private void open()
	{
		if (!Utils.getInstance().isStorageAvailable(this))
		{
			return;
		}
		mSettings.setForceOpenFile(true);
		if (mCanvas.isChanged())
		{
			showDialog(R.id.dialog_open);
		} else
		{
			mPainterHandler.startOpenActivity();
		}
	}

	/**
	 * 
	 * @description: 清除设置参数
	 * @date: 2015-3-16 下午1:40:14
	 * @author： yems
	 */
	private void clearSettings()
	{
		mSettings.setLastPicture(null);
		deleteFile(Commons.SETTINGS_STORAGE);
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();

		// 点击了拾色器
		if (id == R.id.bt_change_brush_color)
		{
			new ColorPickerControl(this, new ColorPickerControl.OnColorChangedListener()
			{
				public void colorChanged(int color)
				{
					mCanvas.setPresetColor(color);
				}
			}, mCanvas.getCurrentPreset().currentColor).show();
			return;
		}
		updateControls();
	}

	/**
	 * 
	 * @description: 退出当前画板应用
	 * @date: 2015-3-16 上午11:40:47
	 * @author： yems
	 */
	private void finishPainter()
	{
		finish();
		// 0 表示正常终止运行当前应用的JVM，确保网络连接线程关闭
		// System.exit(0);
	}
}
