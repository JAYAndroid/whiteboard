package com.yems.painter.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jboss.netty.channel.Channel;

import com.yems.painter.serializable.SerializablePath;

/**
 * @ClassName: MetadataRepositories
 * @Description: 存放客户端的连接数等数据（单例模式）
 * @author lwtx-yems
 * @date Mar 6, 2015 10:58:39 AM
 * 
 */
public class MetadataRepositories implements Serializable {
	
	private Map<String, Channel> channels;// 保存客户端的连接 key--IP地址；value--客户端和服务端建立的通道
	
	private  List<SerializablePath> bufferShapes;// 用于服务器端保存图形列表

	private MetadataRepositories() {
		channels = new HashMap<String, Channel>();
		bufferShapes = new ArrayList<SerializablePath>();
	}

	private static class Builder {
		private static MetadataRepositories instance = new MetadataRepositories();
	}
	
	public static MetadataRepositories getInstance(){
		return Builder.instance;
	}

	public Map<String, Channel> getChannels() {
		return channels;
	}

	public void setChannels(Map<String, Channel> channels) {
		this.channels = channels;
	}

	public  List<SerializablePath> getBufferShapes() {
		return bufferShapes;
	}

	public  void setBufferShapes(Vector<SerializablePath> bufferShapes) {
		this.bufferShapes = bufferShapes;
	}

}
