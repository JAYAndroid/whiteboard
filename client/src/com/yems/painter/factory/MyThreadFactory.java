package com.yems.painter.factory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.yems.painter.common.Commons;

/**
 * @description: 自定义线程池管理工厂类(单利模式)
 * @date: 2015-3-16 下午10:33:26
 * @author: yems
 */
public class MyThreadFactory {

	/** 线程池管理实例*/
	private ExecutorService executorService = Executors
			.newFixedThreadPool(Commons.THREAD_COUNT);

	public ExecutorService getExecutorService() {
		return executorService;
	}

	private MyThreadFactory() {

	}

	private static class Builder {
		private static MyThreadFactory instance = new MyThreadFactory();
	}

	public static MyThreadFactory getInstance() {
		return Builder.instance;
	}
}
