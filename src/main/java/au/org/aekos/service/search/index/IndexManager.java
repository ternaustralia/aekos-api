package au.org.aekos.service.search.index;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.springframework.stereotype.Component;

@Component
public class IndexManager {

	private String indexPath = "C:\\lucene\\indexes\\index1";
	
	private boolean createNew = true;

	public void createIndex() throws IOException{
		Directory dir = FSDirectory.open(Paths.get(indexPath));
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		if (createNew) {
			// Create a new index in the directory, removing any
			// previously indexed documents:
			iwc.setOpenMode(OpenMode.CREATE);
		} else {
			// Add new documents to an existing index:
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		}

		IndexWriter writer = new IndexWriter(dir, iwc);
		writer.addDocument(createTestDocument());
		writer.commit();
		writer.close();

	}
	
	String fieldName = "species";
	String traitName = "trait";
	
	public Document createTestDocument(){
		Document d1 = new Document();
		IndexableField field = new StringField(fieldName,"Species1",Store.YES);
		d1.add(field);
		IndexableField field2 = new StringField(traitName,"trait1",Store.YES);
		d1.add(field2);
		return d1;
	}
	
	public void searchForDocumentsTest() throws IOException{
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();
		QueryBuilder qb = new QueryBuilder(analyzer);
		//qb.cr
		
		
		
		
		
		
	}

}