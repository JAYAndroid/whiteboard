package com.yems.painter;

import java.io.File;
import java.net.URI;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.yems.painter.activity.BasePainter;
import com.yems.painter.common.Commons;
import com.yems.painter.control.ClientControl;
import com.yems.painter.control.PainterCanvasControl;
import com.yems.painter.handler.PainterHandler;
import com.yems.painter.utils.FileUtil;
import com.yems.painter.utils.Utils;

/**
 * @description: 画板主界面
 * @date: 2015-3-11 下午4:47:57
 * @author: yems
 */
public class Painter extends BasePainter

{

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.painter_main);

		// 初始化布局视图
		setView();
		// 初始化监听器
		initListener();
		// 初始化参数
		setValue();
	}

	/**
	 * 
	 * @description: 初始化监听器
	 * @date: 2015-3-16 下午1:41:54
	 * @author： yems
	 */
	private void initListener()
	{
		changeBrushColor.setOnClickListener(this);

		// 监听画笔大小的变化
		mBrushSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{

			public void onStopTrackingTouch(SeekBar seekBar)
			{
				if (seekBar.getProgress() > 0)
				{
					mCanvas.setPresetSize(seekBar.getProgress());
				}
			}

			public void onStartTrackingTouch(SeekBar seekBar)
			{

			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				if (progress > 0)
				{
					if (fromUser)
					{
						mCanvas.setPresetSize(seekBar.getProgress());
					}
				} else
				{
					mBrushSize.setProgress(1);
				}
			}
		});
	}

	/**
	 * 
	 * @description: 初始化界面视图
	 * @date: 2015-3-16 下午1:42:09
	 * @author： yems
	 */
	private void setView()
	{
		mCanvas = (PainterCanvasControl) findViewById(R.id.canvas);
		mBrushSize = (SeekBar) findViewById(R.id.brush_size);
		mPropertiesBar = (LinearLayout) findViewById(R.id.properties_bar);
		mSettingsLayout = (RelativeLayout) findViewById(R.id.settings_layout);
		changeBrushColor = (ImageButton) findViewById(R.id.bt_change_brush_color);
		mPropertiesBar.setVisibility(View.INVISIBLE);
	}

	/**
	 * 
	 * @description: 初始化基础数据（屏幕大小、硬件加速、画笔预设参数值等）
	 * @date: 2015-3-16 下午1:42:31
	 * @author: yems
	 */
	private void setValue()
	{
		Utils.getInstance().getScreenSize(this);
		Utils.getInstance().checkHardwareAccelerated(this);
		loadSettings();
		updateControls();

		mPainterHandler = new PainterHandler(this, mCanvas, mSettings);
		Commons.myUUID = Utils.getInstance().getMyUUID(this);
		Commons.requestedOrientation = getRequestedOrientation();

		client = ClientControl.getInstance();
		client.setPainterCanvas(mCanvas);
		mCanvas.init(client, mPainterHandler);
		mPainterHandler.setPainter(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		// 读取打开最近一次的图形数据标识
		Commons.mOpenLastFile = preferences.getBoolean(getString(R.string.preferences_last_file), true);
		// 读取音量键的快捷功能标识
		mVolumeButtonsShortcuts = Integer.parseInt(preferences.getString(getString(R.string.preferences_volume_shortcuts), String.valueOf(Commons.SHORTCUTS_VOLUME_BRUSH_SIZE)));
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		client.releaseInstance();
		if (Commons.currentChannel != null)
		{
			// 关闭网络连接通道
			Commons.currentChannel.close();
		}
	}

	@Override
	protected void onStop()
	{
		mSettings.setPreset(mCanvas.getCurrentPreset());
		Utils.getInstance().saveSettings(this, mSettings);
		super.onStop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		switch (requestCode)
		{
		case Commons.REQUEST_OPEN:
			if (resultCode == Activity.RESULT_OK)
			{
				// 加载保存再SD上的图片
				Uri uri = intent.getData();
				String path = "";

				if (uri != null)
				{
					if (uri.toString().toLowerCase().startsWith("content://"))
					{
						path = "file://" + Utils.getInstance().getRealPathFromURI(uri, this);
					} else
					{
						path = uri.toString();
					}
					URI file_uri = URI.create(path);

					if (file_uri != null)
					{
						File picture = new File(file_uri);

						if (picture.exists())
						{
							Bitmap bitmap = null;
							try
							{
								bitmap = BitmapFactory.decodeFile(picture.getAbsolutePath());

								Config bitmapConfig = bitmap.getConfig();
								if (bitmapConfig != Config.ARGB_4444)
								{
									bitmap = null;
								}
							} catch (Exception e)
							{
								e.printStackTrace();
							}

							if (bitmap != null)
							{
								if (bitmap.getWidth() > bitmap.getHeight())
								{
									// 如果图片的宽度大于高度，则设置屏幕的方向为横向
									mSettings.setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
								} else if (bitmap.getWidth() != bitmap.getHeight())
								{
									// 否则设置屏幕的方向为竖向
									mSettings.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
								} else
								{
									mSettings.setOrientation(getRequestedOrientation());
								}

								SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

								// 对打开的图片是否复制到本应用指定的目录
								int backupOption = Integer.parseInt(preferences.getString(getString(R.string.preferences_backup_openeded_file), String.valueOf(Commons.BACKUP_OPENED_ONLY_FROM_OTHER)));
								String pictureName = null;

								switch (backupOption)
								{
								// 只复制从其他应用的目录打开的图片
								case Commons.BACKUP_OPENED_ONLY_FROM_OTHER:
									if (!picture.getParentFile().getName().equals(getString(R.string.app_name)))
									{
										pictureName = FileUtil.copyFile(picture.getAbsolutePath(), Utils.getInstance().getSaveDir(Painter.this) + picture.getName());
									} else
									{
										pictureName = picture.getAbsolutePath();
									}
									break;
								// 总是复制图片到当前应用指定的目录下（不管该图片是来自本应用还是非本应用）
								case Commons.BACKUP_OPENED_ALWAYS:
									pictureName = FileUtil.copyFile(picture.getAbsolutePath(), Utils.getInstance().getSaveDir(Painter.this) + picture.getName());
									break;
								// 只打开图片，从不复制
								case Commons.BACKUP_OPENED_NEVER:
									pictureName = picture.getAbsolutePath();
									break;
								}

								if (pictureName != null)
								{
									mSettings.setLastPicture(pictureName);
									Utils.getInstance().saveSettings(this, mSettings);
									mPainterHandler.restart();
								} else
								{
									Toast.makeText(this, R.string.file_not_found, Toast.LENGTH_SHORT).show();
								}
							} else
							{
								Toast.makeText(this, R.string.invalid_file, Toast.LENGTH_SHORT).show();
							}
						} else
						{
							Toast.makeText(this, R.string.file_not_found, Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
			break;
		}
	}
}