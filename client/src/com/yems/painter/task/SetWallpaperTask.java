package com.yems.painter.task;

import java.io.IOException;

import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.widget.Toast;

import com.yems.painter.R;
import com.yems.painter.common.Commons;
import com.yems.painter.control.PainterCanvasControl;
import com.yems.painter.entity.PainterSettings;

/**
 * @description: 异步处理设置壁纸
 * @date: 2015-3-16 下午11:32:58
 * @author: yems
 */
public class SetWallpaperTask extends AsyncTask<Void, Void, Boolean> {
	private Context mContext;
	/** 画布对象*/
	private PainterCanvasControl mCanvas;
	/** 保存画板设置参数的对象*/
	private PainterSettings mSettings;
	/** 壁纸设置进度对话框*/
	private ProgressDialog mDialog;

	public SetWallpaperTask(Context context, PainterCanvasControl canvas,
			PainterSettings settings) {
		this.mContext = context;
		this.mCanvas = canvas;
		this.mSettings = settings;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mDialog = ProgressDialog.show(mContext,
				mContext.getString(R.string.wallpaper_title),
				mContext.getString(R.string.aply_wallpaper), true);
	}

	@Override
	protected Boolean doInBackground(Void... none) {
		WallpaperManager wallpaperManager = WallpaperManager
				.getInstance(mContext);
		
		int wallpaperWidth = (int) (Commons.CURRENT_SCREEN_WIDTH * 2);
		int wallpaperHeight = (int) (Commons.CURRENT_SCREEN_HEIGHT * 2);

		Bitmap currentBitmap = mCanvas.getThread().getBitmap();
		Bitmap wallpaperBitmap = Bitmap.createBitmap(wallpaperWidth,
				wallpaperHeight, Bitmap.Config.ARGB_4444);

		while (wallpaperBitmap == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				return false;
			}
		}

		Canvas wallpaperCanvas = new Canvas(wallpaperBitmap);
		wallpaperCanvas.drawColor(mCanvas.getThread().getBackgroundColor());
		wallpaperCanvas.drawBitmap(currentBitmap,
				(wallpaperWidth - currentBitmap.getWidth()) / 2,
				(wallpaperHeight - currentBitmap.getHeight()) / 2, null);
		try {
			wallpaperManager.setBitmap(wallpaperBitmap);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	protected void onPostExecute(Boolean success) {
		mDialog.hide();

		if (success) {
			Toast.makeText(mContext, R.string.wallpaper_setted,
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(mContext, R.string.wallpaper_error,
					Toast.LENGTH_SHORT).show();
		}
	}
}
