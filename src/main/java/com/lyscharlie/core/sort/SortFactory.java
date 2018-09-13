package com.lyscharlie.core.sort;

import org.apache.lucene.search.Sort;

/**
 * 排序规则抽象工厂类
 * 
 * @author LiYishi
 * @param <T>
 */
public abstract class SortFactory<T> {

	/**
	 * 生成排序规则
	 * 
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public abstract Sort createSort(T query) throws Exception;

}
