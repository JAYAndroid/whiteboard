package com.yems.painter.task;

import java.io.File;
import java.io.FileNotFoundException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.yems.painter.R;
import com.yems.painter.control.PainterCanvasControl;
import com.yems.painter.entity.PainterSettings;
import com.yems.painter.utils.Utils;

/**
 * @description: 异步处理图片的保存
 * @date: 2015-3-16 下午11:30:46
 * @author: yems
 */
public class SaveTask extends AsyncTask<Void, Void, String> {
	private Context mContext;
	/** 画布对象*/
	private PainterCanvasControl mCanvas;
	/** 保存画板设置参数的对象*/
	private PainterSettings mSettings;
	/** 保存图片进度对话框*/
	private ProgressDialog dialog;

	public SaveTask(Context context, PainterCanvasControl canvas,
			PainterSettings settings) {
		this.mContext = context;
		this.mCanvas = canvas;
		this.mSettings = settings;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		dialog = ProgressDialog.show(mContext,
				mContext.getString(R.string.saving_title),
				mContext.getString(R.string.saving_to_sd), true);
	}

	@Override
	protected String doInBackground(Void... none) {
		mCanvas.getThread().freeze();
		String pictureName = Utils.getInstance().getUniquePictureName(
				Utils.getInstance().getSaveDir(mContext), mSettings);
		try {
			mCanvas.saveBitmap(pictureName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		mSettings.setPreset(mCanvas.getCurrentPreset());
		Utils.getInstance().saveSettings(mContext, mSettings);
		return pictureName;
	}

	@Override
	protected void onPostExecute(String pictureName) {
		Uri uri = Uri.fromFile(new File(pictureName));
		mContext.sendBroadcast(new Intent(
				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
		dialog.hide();
		mCanvas.getThread().activate();
	}
}
