package com.yems.painter.core;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.yems.painter.constant.Commons;
import com.yems.painter.factory.MyChannelPipelineFactory;

/**
 * Netty服务端(单利模式)
 * 
 * @description:
 * @date: 2015-3-12 上午09:40:37
 * @author: yems
 */
public class Server {

	/**
	 * 线程池管理
	 */
	private ExecutorService mCachedThreadPool;

	private Server() {
		mCachedThreadPool = Executors.newCachedThreadPool();

		// Server服务启动器
		ServerBootstrap bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(mCachedThreadPool,
						mCachedThreadPool));

		// 设置一个处理客户端消息和各种消息事件的类(Handler)
		bootstrap.setPipelineFactory(new MyChannelPipelineFactory());
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		// 服务端绑定到指定端口
		bootstrap.bind(new InetSocketAddress(Commons.PORT));
		System.out.println("Netty--服务器正常启动！");
	}

	private static class Builder {
		private static Server instance = new Server();
	}

	public static Server getInstance() {
		return Builder.instance;
	}

	public static void main(String args[]) {
		Server.getInstance();
	}
}
