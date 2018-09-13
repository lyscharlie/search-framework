package com.lyscharlie.core.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 搜索索引读操作
 * 
 * @author LiYishi
 */
public class SearchIndexReader {

	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 搜索
	 * 
	 * @param indexSearcher
	 * @param query 查询条件
	 * @param filter 筛选器（可以为空）
	 * @param sort 排序器（可以为空）
	 * @param start 开始条数，从1开始
	 * @param end 结束条数
	 * @return
	 */
	public SearchResult searchIndex(IndexSearcher indexSearcher, Query query, Filter filter, Sort sort, int start, int end) {

		SearchResult result = new SearchResult();

		if (null == indexSearcher || null == query) {
			return result;
		}

		TopDocs td = null;

		try {
			if (null != sort) {
				td = indexSearcher.search(query, filter, end, sort);
			} else {
				td = indexSearcher.search(query, filter, end, new Sort());
			}
		} catch (IOException e) {
			logger.error("搜索数据失败", e);
			return result;
		}

		if (null == td || td.totalHits < 1 || td.totalHits < start) {
			return result;
		}

		List<Document> docList = new ArrayList<Document>();
		ScoreDoc[] sds = td.scoreDocs;
		for (int i = start - 1; i < sds.length; i++) {
			try {
				Document doc = indexSearcher.doc(sds[i].doc);
				docList.add(doc);
			} catch (IOException e) {
				logger.error("读取索引数据失败", e);
			}
		}
		result.setDocList(docList);
		result.setTotal(td.totalHits);
		return result;

	}

}
