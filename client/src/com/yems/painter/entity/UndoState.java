package com.yems.painter.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 图形撤销（恢复）状态记录管理类（单例模式）
 * @date: 2015-3-12 上午10:18:06
 * @author: yems
 */
public class UndoState {
	/** 缓存历史图形，用于撤销操作*/
	private List<byte[]> undoCaches;

	private UndoState() {
		undoCaches = new ArrayList<byte[]>();
	}

	/**
	 * @return
	 * @description: 获取历史图形数据集合
	 * @date: 2015-3-16 下午2:08:23
	 * @author: yems
	 */
	public List<byte[]> getUndoCaches() {
		return undoCaches;
	}

	private static class Builder {
		private static UndoState instance = new UndoState();
	}

	public static UndoState getInstance() {
		return Builder.instance;
	}
}
