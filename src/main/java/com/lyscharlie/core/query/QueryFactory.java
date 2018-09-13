package com.lyscharlie.core.query;

import org.apache.lucene.search.Query;

/**
 * 查询条件抽象工厂类
 * 
 * @author LiYishi
 * @param <T>
 */
public abstract class QueryFactory<T> {

	/**
	 * 生成搜索条件
	 * 
	 * @param <T>
	 * 
	 * @param <T>
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public abstract Query createQuery(T query) throws Exception;

}
