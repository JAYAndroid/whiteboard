package com.yems.painter.handler;

import java.net.InetSocketAddress;
import java.util.List;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import android.util.Log;

import com.yems.painter.common.Commons;
import com.yems.painter.control.PainterCanvasControl;
import com.yems.painter.entity.MetadataRepositories;
import com.yems.painter.listener.ConnectStateListener;
import com.yems.painter.serializable.SerializablePath;
import com.yems.painter.serializable.ShapeRepositories;
import com.yems.painter.utils.Utils;

/**
 * @ClassName: ClientHandler
 * @Description: 自定义客户端处理器（用于处理通道中消息的收发等）
 * @author yems
 * @date 2015-3-6 下午01:37:47
 * 
 */
public class MyClientHandler extends SimpleChannelHandler
{
	private String TAG = "ClientHandler";

	/** 自定义的画布对象 */
	private PainterCanvasControl mPainterCanvas;
	/** 远程客户端（非当前客户端）发送来的当前图像数据 */
	private SerializablePath currentRemotePath;
	private ConnectStateListener mConnectStateListener;

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
	{
		Log.i(TAG, "通道连接成功");
		Commons.currentChannel = e.getChannel();
		if (mConnectStateListener != null)
		{
			mConnectStateListener.channelConnected();
		}
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
	{
		
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
	{
		try
		{
			if (mPainterCanvas != null)
			{
				Object message = e.getMessage();
				String hostAddress = ((InetSocketAddress) e.getRemoteAddress()).getAddress().getHostAddress();
				System.out.println("客户端端收到" + hostAddress + "发来的消息");

				// 接收服务端发送过来的所有图形数据列表
				if (message instanceof List)
				{
					List<SerializablePath> paths = (List<SerializablePath>) message;
					MetadataRepositories.getInstance().getBufferShapes().addAll(paths);
					mPainterCanvas.repaint(true);
					ShapeRepositories.getInstance().getUndoCaches().addAll(paths);

				} else if (message instanceof SerializablePath)
				{
					Commons.pickRemotePathFirst = false;
					// 接收服务端发送过来的一条最近更新的图形数据
					currentRemotePath = (SerializablePath) message;
					MetadataRepositories.getInstance().getBufferShapes().add(currentRemotePath);

					// 接收远程客户端发来的历史图形数据，需先保存本地客户端的历史图形数据
					Utils.pickUndoCaches();
					Commons.lastRemotePath = currentRemotePath;
					mPainterCanvas.repaint(false);
				}
			}

		} catch (Exception e2)
		{
			Log.i(TAG, e2.toString());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
	{
		e.getChannel().close();
		Log.i(TAG, "发生异常--" + e.getCause().toString());
	}

	/**
	 * @param painterCanvas
	 * @description: 设置画布对象
	 * @date: 2015-3-16 下午2:03:47
	 * @author: yems
	 */
	public void setPainterCanvas(PainterCanvasControl painterCanvas)
	{
		this.mPainterCanvas = painterCanvas;
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
	{
		Log.i(TAG, "服务端断开连接");
		if (mConnectStateListener != null)
		{
			mConnectStateListener.channelClosed();
		}
	}

	public void setConnectListener(ConnectStateListener connectStateListener)
	{
		mConnectStateListener = connectStateListener;
	}
}
