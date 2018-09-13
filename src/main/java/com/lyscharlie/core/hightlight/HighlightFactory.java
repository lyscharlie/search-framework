package com.lyscharlie.core.hightlight;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 高亮处理
 * 
 * @author LiYishi
 */
public abstract class HighlightFactory {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 
	 * @param doc
	 * @param fieldName
	 * @param keyword
	 * @return
	 * @throws Exception
	 */
	public abstract String highlight(Document doc, String fieldName, String keyword) throws Exception;

}
