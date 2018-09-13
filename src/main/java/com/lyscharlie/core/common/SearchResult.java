package com.lyscharlie.core.common;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.lucene.document.Document;

/**
 * 搜索结果集封装类
 * 
 * @author LiYishi
 */
public class SearchResult {

	/**
	 * 查询结果总数
	 */
	private int total = 0;

	/**
	 * 当前获取数据
	 */
	private List<Document> docList;

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public List<Document> getDocList() {
		return docList;
	}

	public void setDocList(List<Document> docList) {
		this.docList = docList;
	}

	/**
	 * 本次查询获得数量
	 * 
	 * @return
	 */
	public int getSearchCount() {
		return CollectionUtils.isNotEmpty(docList) ? docList.size() : 0;
	}

}
