package com.yems.painter.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.yems.painter.R;

/**
 * @description: 参数值设置列表界面
 * @date: 2015-3-16 下午10:21:05
 * @author: yems
 */
public class PainterPreferences extends PreferenceActivity implements
		OnPreferenceClickListener {

	private String mAboutPreferenceKey;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int canvasOrientation = getIntent().getIntExtra("orientation",
				getRequestedOrientation());

		if (getRequestedOrientation() != canvasOrientation) {
			setRequestedOrientation(canvasOrientation);
		}
		addPreferencesFromResource(R.xml.preferences);

		mAboutPreferenceKey = getString(R.string.preferences_about);
		getPreferenceScreen().findPreference(mAboutPreferenceKey)
				.setOnPreferenceClickListener(this);

		setTitle(getString(R.string.app_name) + " - "
				+ getString(R.string.menu_preferences));
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == R.id.dialog_about) {
			return createDialogAbout();
		}
		return super.onCreateDialog(id);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (mAboutPreferenceKey.equals(preference.getKey())) {
			showDialog(R.id.dialog_about);
			return true;
		}
		return false;
	}

	/** 
	 * @return Dialog
	 * @description: 创建“关于”对话框 
	 * @date 2015-3-16 下午10:26:30    
	 * @author: yems 
	 */
	private Dialog createDialogAbout() {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.dialog_about, null);
		dialogBuilder.setView(dialogView);

		try {
			((TextView) dialogView.findViewById(R.id.version))
					.setText(getString(
							R.string.app_version,
							getPackageManager().getPackageInfo(
									getPackageName(),
									PackageManager.GET_META_DATA).versionName));
		} catch (Exception e) {
		}

		dialogBuilder.setCancelable(true);
		dialogBuilder.setPositiveButton(android.R.string.ok, null);
		return dialogBuilder.create();
	}
}