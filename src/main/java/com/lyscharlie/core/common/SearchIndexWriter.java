package com.lyscharlie.core.common;

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 搜索索引写操作
 * 
 * @author LiYishi
 */
public class SearchIndexWriter {

	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 新增索引
	 * 
	 * @param writer
	 * @param docList
	 * @return
	 */
	public synchronized int addIndex(IndexWriter writer, List<Document> docList) {

		int num = 0;

		if (null == writer || CollectionUtils.isEmpty(docList)) {
			return num;
		}

		for (Document document : docList) {
			try {
				writer.addDocument(document);
				num++;
			} catch (IOException e) {
				logger.error("写入索引内容失败", e);
			}
		}

		try {
			writer.commit();
		} catch (IOException e) {
			logger.error("提交索引事务异常", e);
		}

		return num;
	}

	/**
	 * 更新索引
	 * 
	 * @param writer
	 * @param docList
	 * @param FieldName
	 * @return
	 */
	public synchronized int updateIndex(IndexWriter writer, List<Document> docList, String FieldName) {
		int num = 0;

		if (null == writer || CollectionUtils.isEmpty(docList) || StringUtils.isBlank(FieldName)) {
			return num;
		}

		for (Document document : docList) {
			try {
				writer.updateDocument(new Term(FieldName, document.get(FieldName)), document);
				num++;
			} catch (IOException e) {
				logger.error("写入索引内容失败", e);
			}
		}

		try {
			writer.commit();
		} catch (IOException e) {
			logger.error("提交索引事务异常", e);
		}

		return num;
	}

	/**
	 * 删除索引
	 * 
	 * @param writer
	 * @param query
	 * @return
	 */
	public synchronized boolean deleteIndexes(IndexWriter writer, Query query) {
		try {
			writer.deleteDocuments(query);
			writer.commit();
			return true;
		} catch (IOException e) {
			logger.error("删除索引失败", e);
			return false;
		}
	}

	/**
	 * 删除所有索引内容
	 * 
	 * @param writer
	 * @return
	 */
	public synchronized boolean deleteAllIndexes(IndexWriter writer) {
		if (null == writer) {
			return false;
		}

		try {
			writer.deleteAll();
			writer.commit();
		} catch (IOException e) {
			logger.error("清理所有索引内容异常", e);
			return false;
		}

		return true;
	}

}
