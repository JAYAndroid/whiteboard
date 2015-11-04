package com.yems.painter.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.yems.painter.Painter;
import com.yems.painter.R;
import com.yems.painter.common.Commons;
import com.yems.painter.control.ClientControl;
import com.yems.painter.listener.ConnectStateListener;

public class SplashMain extends Activity implements ConnectStateListener
{
	private EditText ip;
	private EditText port;
	private SharedPreferences sharedPreferences;
	private final static int CHANNEL_CONNECTED = 1;
	private final static int CHANNEL_DISCONNECTED = 2;

	private ProgressDialog dialog;

	private Handler mHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			dialog.dismiss();

			switch (msg.what)
			{
			case CHANNEL_CONNECTED:
				Toast.makeText(SplashMain.this, "连接成功", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(SplashMain.this, Painter.class);
				startActivity(intent);
				break;

			case CHANNEL_DISCONNECTED:
				Toast.makeText(SplashMain.this, "请检查服务端是否已经开启！", Toast.LENGTH_SHORT).show();
				break;

			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_main);

		setView();
		setValue();
	}

	private void setValue()
	{
		dialog = new ProgressDialog(this);
		sharedPreferences = getSharedPreferences("account", Activity.MODE_PRIVATE);
		ip.setText(sharedPreferences.getString("ip", ""));
		port.setText(sharedPreferences.getString("port", ""));
	}

	private void setView()
	{
		ip = (EditText) findViewById(R.id.et_ip);
		port = (EditText) findViewById(R.id.et_port);
	}

	/**
	 * @param view
	 * @description: 开始登录服务器
	 * @date: 2015-11-4 上午11:59:08
	 * @author: yems
	 */
	public void login(View view)
	{
		if (checkIpAndPort())
		{
			dialog.setMessage("正在连接服务端，请稍候！");
			dialog.show();
			ClientControl client = ClientControl.getInstance();
			client.setConnectListener(this);
			client.connect();
		}

	}

	private boolean checkIpAndPort()
	{
		Editor edit = sharedPreferences.edit();
		if (TextUtils.isEmpty(ip.getText().toString().trim()))
		{
			Toast.makeText(this, "IP地址不能为空", Toast.LENGTH_SHORT).show();
			return false;
		} else
		{
			edit.putString("ip", ip.getText().toString().trim());
			Commons.SERVER_IP_ADDRESS = ip.getText().toString().trim();
		}

		if (!TextUtils.isEmpty(port.getText().toString().trim()))
		{
			Commons.SERVER_PORT = Integer.valueOf(port.getText().toString().trim());
			edit.putString("port", port.getText().toString().trim());
		}

		edit.commit();
		return true;
	}

	@Override
	public void channelConnected()
	{
		mHandler.sendEmptyMessage(CHANNEL_CONNECTED);
	}

	@Override
	public void channelClosed()
	{
		mHandler.sendEmptyMessage(CHANNEL_DISCONNECTED);
	}

}
