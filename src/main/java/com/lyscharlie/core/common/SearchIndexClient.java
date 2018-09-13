package com.lyscharlie.core.common;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 索引实例
 * 
 * @author LiYishi
 */
public class SearchIndexClient extends SearchIndex {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private String name;// 客户端名称
	private String pathname;// 索引文件所在目录

	public SearchIndexClient(String name, String pathname) {
		this.name = name;
		this.pathname = pathname;
	}

	/**
	 * 搜索初始化
	 * 
	 * @return
	 */
	public synchronized boolean initIndex() {
		return openSearch();
	}

	/**
	 * 开启搜索
	 * 
	 * @return
	 */
	public synchronized boolean openSearch() {
		try {
			if (closed) {
				return false;
			}

			openDirectory(new File(pathname));
			openWriter();
			openReader();
		} catch (Exception e) {
			logger.error("初始化索引文件失败[" + pathname + "]", e);
			return false;
		}
		closed = false;
		return true;
	}

	/**
	 * 关闭搜索
	 * 
	 * @return
	 */
	public synchronized boolean closeSearch() {
		if (closed) {
			return true;
		}

		while (locked || searchNum.get() > 0) {
			try {
				Thread.sleep(10L);
			} catch (InterruptedException e) {
				logger.error("[" + name + "]关闭搜索失败，不能开起等待", e);
				return false;
			}
		}

		try {
			closeReader();
			closeWriter();
			closeDirectory();
		} catch (Exception e) {
			logger.error("[" + name + "]关闭搜索失败，不能开起等待", e);
			return false;
		}

		return true;
	}

	/**
	 * 写索引前加锁
	 * 
	 * @return
	 */
	private synchronized boolean lock() {
		if (!closed && !locked) {
			locked = true;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 写索引后解锁
	 * 
	 * @return
	 */
	private synchronized boolean unlock() {
		if (locked) {
			locked = false;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 索引读写是否已关闭
	 * 
	 * @return
	 */
	public boolean isClosed() {
		return closed;
	}

	/**
	 * 搜索
	 * 
	 * @param query
	 *            查询条件
	 * @param filter
	 *            筛选器（可以为空）
	 * @param sort
	 *            排序器（可以为空）
	 * @param start
	 *            开始条数，从1开始
	 * @param end
	 *            结束条数
	 * @return
	 */
	public SearchResult searchIndex(Query query, Filter filter, Sort sort, int start, int end) {
		SearchResult result = new SearchResult();

		if (closed) {
			return result;
		}

		searchNum.incrementAndGet();

		try {
			// 搜索
			result = this.searchIndexReader.searchIndex(indexSearcher, query, filter, sort, start, end);

			if (logger.isDebugEnabled()) {
				logger.debug("query expression is {} , total {} result", query.toString(), result.getTotal());
			}
		} catch (Exception e) {
			logger.error("[" + name + "]搜索异常", e);
		}

		searchNum.decrementAndGet();

		return result;

	}

	/**
	 * 写索引内容，不支持实时加载
	 * 
	 * @param docList
	 * @param updateIndex
	 *            是否覆盖更新
	 * @param updateByFieldName
	 *            覆盖更新依据的字段名
	 * @return 写入成功条数
	 */
	public synchronized int writeIndex(List<Document> docList, boolean updateIndex, String updateByFieldName) {
		int num = 0;

		if (closed) {
			return num;
		}

		while (true) {
			if (locked) {
				try {
					wait(10L);
					notify();
				} catch (InterruptedException e) {
					logger.error("[" + name + "]写索引异常", e);
					return num;
				}
			} else {
				if (!lock()) {
					try {
						// Thread.sleep(10L);
						wait(10L);
						notify();
					} catch (InterruptedException e) {
						logger.error("[" + name + "]写索引异常", e);
						return num;
					}
				} else {
					break;
				}
			}
		}

		// 写入索引
		if (updateIndex && StringUtils.isNotBlank(updateByFieldName)) {
			num = this.searchIndexWriter.updateIndex(writer, docList, updateByFieldName);
		} else {
			num = this.searchIndexWriter.addIndex(writer, docList);
		}

		unlock();

		return num;
	}

	/**
	 * 实时写索引
	 * 
	 * @param docList
	 * @param updateIndex
	 *            是否覆盖更新
	 * @param updateByFieldName
	 *            覆盖更新依据的字段名
	 * @return 写入成功条数
	 */
	public synchronized int writeIndexRealtime(List<Document> docList, boolean updateIndex, String updateByFieldName) {
		int num = writeIndex(docList, updateIndex, updateByFieldName);

		if (num < 1) {
			return num;
		}

		try {
			reopenReader();
		} catch (Exception e) {
			logger.error("[" + name + "]重新读取索引异常", e);
		}

		return num;
	}

	/**
	 * 删除索引数据
	 * 
	 * @param query
	 * @return
	 * @throws IOException
	 */
	public synchronized boolean removeIndexes(Query query) throws IOException {
		if (closed) {
			return false;
		}

		while (true) {
			if (locked) {
				try {
					Thread.sleep(10L);
				} catch (InterruptedException e) {
					logger.error("[" + name + "]写索引异常", e);
					return false;
				}
			} else {
				if (!lock()) {
					try {
						Thread.sleep(10L);
					} catch (InterruptedException e) {
						logger.error("[" + name + "]写索引异常", e);
						return false;
					}
				} else {
					break;
				}
			}
		}

		boolean success = this.searchIndexWriter.deleteIndexes(writer, query);

		try {
			reopenReader();
		} catch (Exception e) {
			logger.error("[" + name + "]重新读取索引异常", e);
		}

		unlock();
		return success;

	}

	/**
	 * 清除所有索引
	 * 
	 * @return
	 * @throws IOException
	 */
	public synchronized boolean cleanIndex() throws IOException {
		if (closed) {
			return false;
		}

		while (true) {
			if (locked || searchNum.get() > 0) {
				try {
					wait(10L);
					notify();
				} catch (InterruptedException e) {
					logger.error("[" + name + "]写索引异常", e);
					return false;
				}
			} else {
				if (!lock()) {
					try {
						wait(10L);
						notify();
					} catch (InterruptedException e) {
						logger.error("[" + name + "]写索引异常", e);
						return false;
					}
				} else {
					break;
				}
			}
		}

		boolean success = this.searchIndexWriter.deleteAllIndexes(writer);

		try {
			reopenReader();
		} catch (Exception e) {
			logger.error("[" + name + "]重新读取索引异常", e);
		}

		unlock();

		return success;
	}

	/**
	 * 判断索引是否为空
	 * 
	 * @return
	 */
	public boolean isIndexEmpty() {
		return reader.numDocs() < 1;
	}

	/**
	 * 显示有多少条数据
	 * 
	 * @return
	 */
	public int getTotalNumber() {
		return reader.numDocs();
	}

}
