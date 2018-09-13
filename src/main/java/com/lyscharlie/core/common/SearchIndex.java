package com.lyscharlie.core.common;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * @author LiYishi
 */
public abstract class SearchIndex {

	protected Analyzer analyzer;// 默认分词器

	protected Version matchVersion;// Lucene版本

	protected IndexReader reader;// 全局读
	protected IndexWriter writer;// 全局写

	protected Directory directory;
	protected IndexWriterConfig indexWriterConfig;
	protected IndexSearcher indexSearcher;

	protected SearchIndexReader searchIndexReader;
	protected SearchIndexWriter searchIndexWriter;

	protected volatile boolean closed = false;// 是否关闭目录
	protected volatile boolean locked = false;// 写入操作中
	protected AtomicInteger searchNum = new AtomicInteger(0);// 当前查询数量原子计数器

	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}

	public void setMatchVersion(Version matchVersion) {
		this.matchVersion = matchVersion;
	}

	public void setSearchIndexReader(SearchIndexReader searchIndexReader) {
		this.searchIndexReader = searchIndexReader;
	}

	public void setSearchIndexWriter(SearchIndexWriter searchIndexWriter) {
		this.searchIndexWriter = searchIndexWriter;
	}

	protected void openDirectory(File file) throws IOException {
		directory = FSDirectory.open(file);
		closed = false;
	}

	protected void closeDirectory() throws Exception {
		if (null != directory) {
			directory.close();
			directory = null;
		}
		closed = true;
	}

	protected void openWriter() throws IOException {
		if (null != writer) {
			return;
		}

		IndexWriterConfig config = new IndexWriterConfig(matchVersion, analyzer);
		config.setMaxBufferedDocs(1000);

		indexWriterConfig = config;
		writer = new IndexWriter(directory, indexWriterConfig);
		writer.commit();
	}

	protected void closeWriter() throws IOException {
		if (null != writer) {
			writer.close();
			indexWriterConfig = null;
			writer = null;
		}
	}

	protected void openReader() throws IOException {
		if (null == reader) {
			reader = DirectoryReader.open(directory);
		}
		if (null == indexSearcher) {
			indexSearcher = new IndexSearcher(reader);
		}
	}

	protected void closeReader() throws Exception {
		while (searchNum.get() > 0) {
			wait(10L);
			notify();
		}

		if (null != indexSearcher) {
			indexSearcher = null;
		}

		if (null != reader) {
			reader.close();
			reader = null;
		}
	}

	public synchronized void reopenReader() throws Exception {
		IndexReader ir = DirectoryReader.openIfChanged((DirectoryReader) reader, writer, true);
		if (null != ir) {
			reader.close();
			reader = ir;
			IndexSearcher is = new IndexSearcher(reader);
			indexSearcher = is;
		}
	}

}
