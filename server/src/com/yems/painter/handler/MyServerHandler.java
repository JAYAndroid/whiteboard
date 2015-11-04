package com.yems.painter.handler;

import java.net.InetSocketAddress;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.yems.painter.constant.Commons;
import com.yems.painter.core.MetadataRepositories;
import com.yems.painter.serializable.SerializablePath;

/**
 * @ClassName: MyHandler
 * @Description: 自定义通道处理器，用于处理通道间消息的收发等
 * @author lwtx-yems
 * @date Mar 6, 2015 10:40:51 AM
 * 
 */
public class MyServerHandler extends SimpleChannelHandler {

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		Channel channel = e.getChannel();
		Commons.currentClientIP = ((InetSocketAddress) e.getChannel()
				.getRemoteAddress()).getAddress().getHostAddress();
		// MetadataRepositories.getInstance().getChannels().put(
		// Commons.currentClientIP, channel);
		System.out.println("客户: " + Commons.currentClientIP + " 进入！");
		sendAllShapesToClient(channel);
	}

	/**
	 * 当有新客户端进入时，向该客户端发送所有图形对象
	 * 
	 * @param channel
	 */
	private void sendAllShapesToClient(Channel channel) {
		if (MetadataRepositories.getInstance().getBufferShapes().size() != 0) {
			System.out.println("发送所有图形数据到客户端");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			channel.write(MetadataRepositories.getInstance().getBufferShapes());
		}
	}

	/**
	 * 发送新图形数据到非当前客户端
	 * 
	 * @param newPath
	 */
	private void sendNewShapeToClient(SerializablePath newPath) {
		System.out.println("发送最新图形数据到客户端");
		Map<String, Channel> channels = MetadataRepositories.getInstance()
				.getChannels();

		for (String myUUID : channels.keySet()) {
			// 开始向非本客户端写入数据
			if (!Commons.myUUID.equals(myUUID)) {
				System.out.println("服务器：向客户 " + myUUID + "写入数据-----");
				Channel channel = channels.get(myUUID);

				System.out.println("connected--" + channel.isConnected()
						+ ",writable-" + channel.isWritable() + ",open--"
						+ channel.isOpen() + ",readable--"
						+ channel.isReadable());
				channel.write(newPath);
			}
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Channel channel = e.getChannel();
		SerializablePath newPath = (SerializablePath) e.getMessage();
		Commons.myUUID = newPath.getMyUUID();

		recordMyUUID(Commons.myUUID, channel);
		System.out.println("服务端收到消息,设备标示符--" + Commons.myUUID);
		if ("send_uuid".equals(newPath.getOPType()))
			return;

		if ("clear".equals(newPath.getOPType())) {
			if (MetadataRepositories.getInstance().getBufferShapes().size() != 0) {
				System.out.println("清除服务端所有图形数据");
				MetadataRepositories.getInstance().getBufferShapes().clear();
			}
			return;
		}

		MetadataRepositories.getInstance().getBufferShapes().add(newPath);
		// // 向非本客戶端写入新图形数据
		sendNewShapeToClient(newPath);
	}

	/**
	 * 记录当前设备唯一标示符和Channel相互对应
	 */
	private void recordMyUUID(String myUUID, Channel channel) {

		if (MetadataRepositories.getInstance().getChannels()
				.containsKey(myUUID)
				&& MetadataRepositories.getInstance().getChannels().get(myUUID)
						.isOpen())
			return;

		MetadataRepositories.getInstance().getChannels().put(myUUID, channel);
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		// String hostAddress = ((InetSocketAddress) e.getChannel()
		// .getRemoteAddress()).getAddress().getHostAddress();
		// MetadataRepositories.getInstance().getChannels().remove(hostAddress);

		// System.out.println("客户: " + hostAddress + " 退出！");
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		System.out.println("通道关闭");
		// e.getChannel().disconnect();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		System.out.println("发生异常--" + e.getCause().toString());
		e.getChannel().close();
	}
}
