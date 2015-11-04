package com.yems.painter.entity;

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
public class MetadataRepositories {
	
	/** 用于服务器端保存图形列表*/
	private  List<SerializablePath> bufferShapes;

	private MetadataRepositories() {
		bufferShapes = new ArrayList<SerializablePath>();
	}

	private static class Builder {
		private static MetadataRepositories instance = new MetadataRepositories();
	}
	
	public static MetadataRepositories getInstance(){
		return Builder.instance;
	}

	public  List<SerializablePath> getBufferShapes() {
		return bufferShapes;
	}

	public  void addShape(SerializablePath newShape) {
		bufferShapes.add(newShape);
	}
}
