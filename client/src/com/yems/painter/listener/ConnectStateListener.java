package com.yems.painter.listener;

/**
 * @description: 连接状态监听器
 * @date: 2015-11-4 下午1:00:19
 * @author: yems
 */
public interface ConnectStateListener
{
	/**
	 * 
	 * @description:和服务端建立连接
	 * @date: 2015-11-4 下午1:00:56
	 * @author: yems
	 */
	public void channelConnected();

	/**
	 * 
	 * @description:和服务端断开连接
	 * @date: 2015-11-4 下午1:00:58
	 * @author: yems
	 */
	public void channelClosed();
}
