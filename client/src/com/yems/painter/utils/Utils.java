package com.yems.painter.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.yems.painter.R;
import com.yems.painter.common.Commons;
import com.yems.painter.entity.PainterSettings;
import com.yems.painter.serializable.ShapeRepositories;

/**
 * @ClassName: Utils
 * @Description: 工具类（值转换）
 * @author lwtx-yems
 * @date 2015-3-4 上午10:27:40
 * 
 */
public class Utils
{
	private Utils()
	{

	}

	private static class Builder
	{
		private static Utils instance = new Utils();
	}

	public static Utils getInstance()
	{
		return Builder.instance;
	}

	/**
	 * 获取图形文件存储的路径
	 * 
	 * @return
	 */
	public String getSaveDir(Context context)
	{
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + '/' + context.getString(R.string.app_name) + '/';

		File file = new File(path);
		if (!file.exists())
		{
			file.mkdirs();
		}
		return path;
	}

	/**
	 * 获取唯一的截图名称
	 * 
	 * @param path
	 *            截图保存路径
	 * @param settings
	 *            画笔设置参数实例对象
	 * @return
	 */
	public String getUniquePictureName(String path, PainterSettings settings)
	{
		if (settings.getLastPicture() != null)
		{
			return settings.getLastPicture();
		}

		String prefix = Commons.PICTURE_PREFIX;
		String ext = Commons.PICTURE_EXT;
		String pictureName = "";

		int suffix = 1;
		pictureName = path + prefix + suffix + ext;

		while (new File(pictureName).exists())
		{
			pictureName = path + prefix + suffix + ext;
			suffix++;
		}

		settings.setLastPicture(pictureName);
		return pictureName;
	}

	/**
	 * 保存图形图片为PNG格式
	 * 
	 * @param pictureName
	 *            图片名称
	 * @param bitmap
	 *            要保存的图片
	 * @throws FileNotFoundException
	 */
	public void saveBitmap(String pictureName, Bitmap bitmap) throws FileNotFoundException
	{
		FileOutputStream fos = new FileOutputStream(pictureName);
		bitmap.compress(CompressFormat.PNG, 100, fos);
	}

	/**
	 * 保存画笔设置参数
	 */
	/**
	 * @param context
	 * @param settings
	 *            画笔设置参数实例
	 */
	public void saveSettings(Context context, PainterSettings settings)
	{
		SharedPreferences sp = context.getSharedPreferences(Commons.SETTINGS_STORAGE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();

		try
		{
			PackageInfo pack = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			editor.putInt(context.getString(R.string.settings_version), pack.versionCode);
		} catch (NameNotFoundException e)
		{
		}

		editor.putInt(context.getString(R.string.settings_orientation), settings.getOrientation());
		editor.putString(context.getString(R.string.settings_last_picture), settings.getLastPicture());
		editor.putFloat(context.getString(R.string.settings_brush_size), settings.getPreset().currentSize);
		editor.putInt(context.getString(R.string.settings_brush_color), settings.getPreset().currentColor);
		editor.putInt(context.getString(R.string.settings_brush_blur_style), (settings.getPreset().currentBlurType != null) ? settings.getPreset().currentBlurType.ordinal() + 1 : 0);
		editor.putInt(context.getString(R.string.settings_brush_blur_radius), settings.getPreset().currentBlurRadius);
		editor.putBoolean(context.getString(R.string.settings_force_open_file), settings.isForceOpenFile());
		editor.commit();
	}

	/**
	 * 判断SD卡是否可用
	 * 
	 * @param context
	 * @return
	 */
	public boolean isStorageAvailable(Context context)
	{
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state))
		{
			return true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
		{
			Toast.makeText(context, R.string.sd_card_not_writeable, Toast.LENGTH_SHORT).show();
		} else
		{
			Toast.makeText(context, R.string.sd_card_not_available, Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	/**
	 * 检测设备是否支持硬件加速
	 */
	public void checkHardwareAccelerated(Activity activity)
	{
		try
		{
			ActivityInfo info = activity.getPackageManager().getActivityInfo(activity.getComponentName(), PackageManager.GET_META_DATA);
			Commons.mIsHardwareAccelerated = (info.flags & ActivityInfo.FLAG_HARDWARE_ACCELERATED) == ActivityInfo.FLAG_HARDWARE_ACCELERATED;
		} catch (Exception e)
		{
			Commons.mIsHardwareAccelerated = false;
		}
	}

	/**
	 * 获取当前设备屏幕宽、高
	 */
	public void getScreenSize(Activity activity)
	{
		DisplayMetrics metric = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
		Commons.CURRENT_SCREEN_WIDTH = metric.widthPixels;
		Commons.CURRENT_SCREEN_HEIGHT = metric.heightPixels;
	}

	/**
	 * 获取URI中包含的文件路径
	 * 
	 * @param contentUri
	 * @param activity
	 * @return
	 */
	public String getRealPathFromURI(Uri contentUri, Activity activity)
	{
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = activity.managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();

		return cursor.getString(column_index).replace(" ", "%20");
	}

	/**
	 * 旋转屏幕
	 */
	public void rotateScreen(Activity activity, PainterSettings settings)
	{
		if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
		{
			settings.setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			Utils.getInstance().saveSettings(activity, settings);

			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else
		{
			settings.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			Utils.getInstance().saveSettings(activity, settings);
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

	/**
	 * 获取设备的唯一标识（UUID）
	 * 
	 * @param context
	 * @return
	 */
	public String getMyUUID(Context context)
	{
		// TelephonyManager tm = (TelephonyManager)
		// context.getSystemService(Context.TELEPHONY_SERVICE);
		// String androidId =
		// android.provider.Settings.Secure.getString(context.getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
		// UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tm
		// .getDeviceId().hashCode() << 32) |
		// tm.getSimSerialNumber().hashCode());
		// return deviceUuid.toString();

		return getDeviceID(context);
	}

	/**
	 * @param context
	 * @return String
	 * @description: 获取设备唯一标识
	 * @date: 2015年3月11日 下午5:27:56
	 * @author： Li Yihua
	 */
	public String getDeviceID(Context context)
	{
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String deviceId = "";
		try
		{
			deviceId = tm.getDeviceId() != null ? tm.getDeviceId().toUpperCase() : getUniqueID(context);

		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return deviceId;
	}

	/**
	 * @param context
	 * @return String
	 * @description: 手机获取不到设备唯一标识的则根据相关策略生成手机唯一识别号
	 * @date: 2015年3月11日 下午5:28:24
	 */
	public String getUniqueID(Context context)
	{
		// 1 compute IMEI
		TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
		String m_szImei = TelephonyMgr.getDeviceId(); 
														
		// 2 compute DEVICE ID ,we make this look like a valid IMEI
		String m_szDevIDShort = "35" + Build.BOARD.length() % 10 + Build.BRAND.length() % 10 + Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 + Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 + Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 + Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 + Build.TAGS.length() % 10
				+ Build.TYPE.length() % 10 + Build.USER.length() % 10;

		// 3 android ID - unreliable
		String m_szAndroidID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

		// 4 wifi manager, read MAC address - requires
		WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
		// 5 Bluetooth MAC address android.permission.BLUETOOTH required
		// BluetoothAdapter m_BluetoothAdapter = null; // Local Bluetooth
		// adapter
		String m_szBTMAC = "";
		try
		{
			Object m_BluetoothAdapter = ReflectUtil.invoke(Class.forName("android.bluetooth.BluetoothAdapter"), null, "getDefaultAdapter", new Class[] {}, new Object[] {});
			if (m_BluetoothAdapter != null)
			{
				m_szBTMAC = (String) ReflectUtil.invoke(m_BluetoothAdapter, "getAddress", new Class[] {}, new Object[] {});
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		// 6 SUM THE IDs
		String m_szLongID = m_szImei + m_szDevIDShort + m_szAndroidID + m_szWLANMAC + m_szBTMAC;
		MessageDigest m = null;
		try
		{
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
		byte p_md5Data[] = m.digest();
		String m_szUniqueID = new String();
		for (int i = 0; i < p_md5Data.length; i++)
		{
			int b = (0xFF & p_md5Data[i]);
			// if it is a single digit, make sure it have 0 in front (proper padding)
			if (b <= 0xF)
				m_szUniqueID += "0";
			// add number to string
			m_szUniqueID += Integer.toHexString(b);
		}
		m_szUniqueID = m_szUniqueID.toUpperCase().length() >= 32 ? m_szUniqueID.toUpperCase().substring(16) : m_szUniqueID.toUpperCase();
		return m_szUniqueID;
	}

	/**
	 * @param meid
	 * @return
	 * @description: bit计算
	 * @date: 2015年3月11日 下午5:29:35
	 * @author： Li Yihua
	 */
	public String calculateParityBit(String meid)
	{

		if (!checkMeid(meid))
		{
			return "-1";
		}
		if (isHexMeid(meid))
		{
			return calculateParityBit(meid, 16);

		} else
		{
			return calculateParityBit(meid, 10);
		}

	}

	/**
	 * @param meid
	 * @return
	 * @description: hexmeid
	 * @date: 2015年3月11日 下午5:29:56
	 * @author： Li Yihua
	 */
	public boolean isHexMeid(String meid)
	{
		if (meid.charAt(0) < 0x41)
		{
			return false;
		}
		return true;
	}

	/**
	 * @param meid
	 * @return
	 * @description: check meid
	 * @date: 2015年3月11日 下午5:30:17
	 * @author： Li Yihua
	 */
	public boolean checkMeid(String meid)
	{
		if (null == meid || (14 != meid.length()))
		{
			return false;
		}
		for (int i = 0; i < meid.length(); i++)
		{
			char tmp = meid.charAt(i);
			if (!(0x30 <= tmp && tmp <= 0x39) && !(0x41 <= tmp && tmp <= 0x46) && !(0x61 <= tmp && tmp <= 0x66))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * @param meid
	 * @param radix
	 * @return String
	 * @description: bit计算
	 * @date: 2015年3月11日 下午5:30:35
	 * @author： Li Yihua
	 */
	public String calculateParityBit(String meid, int radix)
	{
		if (!checkMeid(meid))
		{
			return "0";
		}
		int[] odd_parity = get_odd_parity(meid, radix);
		int res = 0;
		int tmp = 0;
		for (int i = 0, count = 0; i < meid.length(); i = i + 2, count++)
		{
			try
			{
				tmp = Integer.valueOf(meid.substring(i, i + 1), radix).intValue();
			} catch (NumberFormatException e)
			{
				tmp = 0;
			}
			res = res + tmp + odd_parity[count] / radix + odd_parity[count] % radix;
		}
		if (0 == res % radix)
		{
			return "0";
		} else
		{
			return Integer.toHexString(radix - (res % radix));
		}
	}

	/**
	 * @param meid
	 * @param radix
	 * @return int[]
	 * @description: 奇数
	 * @date: 2015年3月11日 下午5:30:58
	 * @author： Li Yihua
	 */
	public int[] get_odd_parity(String meid, int radix)
	{
		int[] odd_parity_array = new int[meid.length() / 2];
		int count = 0;
		int tmp = 0;
		for (int i = 1; i < meid.length(); i = i + 2, count++)
		{
			try
			{
				tmp = Integer.valueOf(meid.substring(i, i + 1), radix).intValue();
			} catch (NumberFormatException e)
			{
				tmp = 0;
			}
			odd_parity_array[count] = tmp * 2;
		}
		return odd_parity_array;
	}

	/**
	 * 
	 * @param pickRemotePathFirst
	 * @description: 存储历史图像数据（包括来自当前客户端和远程客户端的图像数据 ）
	 * @date: 2015-3-16 上午12:04:39
	 * @author： yems
	 */
	public static void pickUndoCaches()
	{
		if (Commons.pickRemotePathFirst)
		{
			pickRemotePath();
		} else
		{
			pickLocalPath();
		}
	}

	/**
	 * 
	 * @description: 接收远程客户端发来的图形数据前，先保存本地客户端的图形数据（用于撤销操作）
	 * @date: 2015-3-16 上午10:42:34
	 * @author： yems
	 */
	private static void pickLocalPath()
	{
		// 本地客户端历史图像数据
		if (Commons.lastLocalPath != null)
		{
			ShapeRepositories.getInstance().getUndoCaches().add(Commons.lastLocalPath);
		}

		// 远程客户端历史图像数据
		if (Commons.lastRemotePath != null)
		{
			ShapeRepositories.getInstance().getUndoCaches().add(Commons.lastRemotePath);
		}
	}

	/**
	 * 
	 * @description: 绘制本地客户端的图形前，先保存远程客户端的图形数据（用于撤销操作）
	 * @date: 2015-3-16 上午10:45:19
	 * @author： yems
	 */
	private static void pickRemotePath()
	{
		// 远程客户端历史图像数据
		if (Commons.lastRemotePath != null)
		{
			ShapeRepositories.getInstance().getUndoCaches().add(Commons.lastRemotePath);
		}
		// 本地客户端历史图像数据
		if (Commons.lastLocalPath != null)
		{
			ShapeRepositories.getInstance().getUndoCaches().add(Commons.lastLocalPath);
		}
	}
}
