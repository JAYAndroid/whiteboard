package com.yems.painter.common;

import org.jboss.netty.channel.Channel;

import com.yems.painter.control.PainterCanvasControl;
import com.yems.painter.serializable.SerializablePath;

/**
 * @description: 公共常量类
 * @date: 2015-3-13 下午2:34:00
 * @author: yems
 */
public class Commons
{
	/** 服务器IP地址 */
	public static String SERVER_IP_ADDRESS = "";
	/** 端口号 */
	public static int SERVER_PORT = 8080;
	/** 线程池大小，默认为10个 */
	public static final int THREAD_COUNT = 10;
	/** 默认画笔大小 */
	public static final float DEFAULT_BRUSH_SIZE = 2;
	/** 当前设备的屏幕密度值 */
	public static float CURRENT_DENSITY;
	/** 当前设备的屏幕宽度 */
	public static float CURRENT_SCREEN_WIDTH;
	/** 当前设备的屏幕高度 */
	public static float CURRENT_SCREEN_HEIGHT;

	/** 画笔类型:铅笔 */
	public static final int PENCIL = 1;
	/** 画笔类型:毛笔 */
	public static final int BRUSH = 2;
	/** 画笔类型:麦克笔 */
	public static final int MARKER = 3;
	/** 画笔类型:钢笔 */
	public static final int PEN = 4;
	/** 画笔类型:自定义画笔 */
	public static final int CUSTOM = 5;

	/** 默认的公共画笔大小 */
	public static final float COMMON_SIZE = 2;
	/** 毛笔的默认大小 */
	public static final float BRUSH_SIZE = 15;
	/** 麦克笔的默认大小 */
	public static final float MARKER_SIZE = 20;
	/** 铅笔的默认模糊半径 */
	public static final int PENCIL_BLUR_RADIUS = 10;
	/** 毛笔的默认模糊半径 */
	public static final int BRUSH_BLUR_RADIUS = 18;

	/** 当前设备画笔颜色 */
	public static int currentColor;
	/** 当前画笔大小 */
	public static float currentSize;
	/** 代表当前客户端和服务端连接通道 */
	public static Channel currentChannel = null;
	/** 截图名称前缀 */
	public static final String PICTURE_PREFIX = "picture_";
	/** 截图名称后缀 */
	public static final String PICTURE_EXT = ".png";
	/** 存储设置的参数 */
	public static final String SETTINGS_STORAGE = "settings";
	/** 标示是否为新创建的图形 */
	public static boolean mIsNewFile = true;
	/** MIME类型表示图片 */
	public static final String PICTURE_MIME = "image/png";
	/** 打开文件的请求标识 */
	public static final int REQUEST_OPEN = 1;
	/** 标识设备是否支持硬件加速 ，true 支持； false 不支持 */
	public static boolean mIsHardwareAccelerated = false;
	/** 只复制从其他应用的目录打开的图片 */
	public static final int BACKUP_OPENED_ONLY_FROM_OTHER = 10;
	/** 总是（不管该图片来自本应用还是其他应用）把打开的图片复制到当前应用指定的目录下 */
	public static final int BACKUP_OPENED_ALWAYS = 20;
	/** 直接打开图片，从不复制该图片 */
	public static final int BACKUP_OPENED_NEVER = 100;
	/** 退出应用前，保存图片 */
	public static final int BEFORE_EXIT_SUBMIT = 10;
	/** 图片已经保存过，直接退出应用 */
	public static final int BEFORE_EXIT_SAVED = 20;
	/** 退出应用前，不做任何保存动作 */
	public static final int BEFORE_EXIT_NO_ACTION = 100;
	/** 音量键充当调整画笔尺寸的快捷键 */
	public static final int SHORTCUTS_VOLUME_BRUSH_SIZE = 10;
	/** 音量键充当调撤销操作的快捷键 */
	public static final int SHORTCUTS_VOLUME_UNDO_REDO = 20;
	/** 保存图片后退成应用 */
	public static final int ACTION_SAVE_AND_EXIT = 1;
	/** 保存图片后返回画板界面 */
	public static final int ACTION_SAVE_AND_RETURN = 2;
	/** 保存图片后打开分享图片的界面 */
	public static final int ACTION_SAVE_AND_SHARE = 3;
	/** 旋转屏幕前，先保存图片 */
	public static final int ACTION_SAVE_AND_ROTATE = 4;
	/** 保存图片后，打开该图片 */
	public static final int ACTION_SAVE_AND_OPEN = 5;
	/** 当前设备的唯一标示符 */
	public static String myUUID = "";
	/** 画笔的状态标识 */
	public static final int SLEEP = 0;
	/** 画笔处于准备状态 */
	public static final int READY = 1;
	/** 画笔处于设置参数状态 */
	public static final int SETUP = 2;
	/** 标志启动APP时，是否打开最近的图像文件，true 是，false 否 */
	public static boolean mOpenLastFile = true;
	/** 当前屏幕方向 */
	public static int requestedOrientation;
	/** 远程客户端（非当前客户端）发送来的历史图像数据 */
	public static SerializablePath lastRemotePath;
	/** 本地（当前）客户端的历史图像数据 */
	public static SerializablePath lastLocalPath;
	/** 标识是否先保存远程客户端的历史图形数据 */
	public static boolean pickRemotePathFirst;
	/** 界面垂直滑入动画的起始点 */
	public final static float SLIDE_FROM = 1.0f;
	/** 界面垂直滑入动画的结束点 */
	public final static float SLIDE_TO = 0.0f;
	/** 界面滑入动画的持续时长 */
	public final static int SLIDE_DURATION = 300;
}
