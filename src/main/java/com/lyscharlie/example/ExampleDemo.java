package com.lyscharlie.example;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.lyscharlie.core.common.SearchIndexClient;
import com.lyscharlie.core.common.SearchIndexReader;
import com.lyscharlie.core.common.SearchIndexWriter;
import com.lyscharlie.core.common.SearchResult;

public class ExampleDemo {

	public static void main(String[] args) {
		SearchIndexClient client = new SearchIndexClient("test", "d:/indexes/my_test");
		client.setAnalyzer(new IKAnalyzer());
		client.setMatchVersion(Version.LUCENE_4_9);
		client.setSearchIndexWriter(new SearchIndexWriter());
		client.setSearchIndexReader(new SearchIndexReader());
		client.initIndex();

		List<Document> list = new ArrayList<Document>();
		for (int i = 1; i <= 100; i++) {
			Document doc = new Document();
			doc.add(new StringField("name", "test_" + i, Store.YES));
			doc.add(new IntField("number", i, Store.YES));

			list.add(doc);
		}

		client.writeIndexRealtime(list, true, "name");

		System.out.println(client.getTotalNumber());
		NumericRangeQuery<Integer> query = NumericRangeQuery.newIntRange("number", 3, 27, true, false);
		System.out.println(query.toString());

		SearchResult result = client.searchIndex(query, null, null, 1, 15);

		System.out.println(result.getTotal());
		System.out.println(result.getSearchCount());
	}

}
