package au.org.aekos.service.search.index;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang.SystemUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LuceneIndexBuilderServiceImpl implements LuceneIndexBuilderService {

	private static final Logger logger = LoggerFactory.getLogger(LuceneIndexBuilderServiceImpl.class);
	
	@Value("${lucene.index.path}")
	private String indexPath;
	
	@Value("${lucene.index.wpath}")
	private String windowsIndexPath;
	
	@Value("${lucene.index.createMode}")
	private boolean createMode;
	
	@Value("${species.csv.resourcePath}")
	private String speciesResourcePath;
	
	public String getSpeciesResourcePath() {
		if(speciesResourcePath == null){
			return "taxa_names.csv";
		}
		
		return speciesResourcePath;
	}

	public String getIndexPath(){
		if(SystemUtils.IS_OS_WINDOWS){
			return windowsIndexPath;
		}
		return indexPath;
	}
	
	//Ensure index path exists, if not attempt to create it. 
	public boolean ensureIndexPathExists(){
		if(Files.isDirectory(Paths.get(getIndexPath()))){
			return true;
		}
		//else try and create the path
		try {
			Files.createDirectories(Paths.get(getIndexPath()));
		} catch (IOException e) {
			logger.error("Can't create index path :" + getIndexPath(), e);
			return false;
		}
		return true;
	}

	
	private Path getSpeciesListPathFromResourceFile(){
		String speciesResourcePath = getSpeciesResourcePath();
		logger.info("Building species index from "+ speciesResourcePath);
		Path path = null;
		try {
			path =  Paths.get(ClassLoader.getSystemResource(speciesResourcePath).toURI());
			logger.info("Resolved the species list file to: " + path.toAbsolutePath());
		} catch (URISyntaxException e) {
			logger.error("Issue with speciesResourcePath " + speciesResourcePath, e);
			throw new RuntimeException("Issue with speciesResourcePath " + speciesResourcePath, e);
		}
		return path;
	}
	
	@Override
	public RAMDirectory buildSpeciesRAMDirectory() {
		RAMDirectory idx = new RAMDirectory();
		IndexWriterConfig conf = new IndexWriterConfig( new StandardAnalyzer());
		try(InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream(getSpeciesResourcePath());
		    InputStreamReader isr = new InputStreamReader(in);
		    BufferedReader reader = new BufferedReader(isr);){
			IndexWriter writer = new IndexWriter(idx, conf);
			int commitCounter = 0;
			int commitLimit = 1000;
			
			//Path filePath = getSpeciesListPathFromResourceFile();
			
			
			
			//try(BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)){
			String line = null; 
			while ((line = reader.readLine()) != null){
				Document doc = createIndexDocument(line);
				writer.addDocument(doc);
				if(++commitCounter == commitLimit ){
					writer.commit();
					commitCounter = 0;
				}
			}
				
				writer.commit();
		        writer.close();
		}catch(IOException e){
			logger.error("issue with RAM Directory index writing",e);
			throw new RuntimeException("Species Index could not be initialised");
		}
		return idx;			
	}
	
	private static Document createIndexDocument(String speciesName){
		speciesName = speciesName.trim();
		Document doc = new Document();
		doc.add(new StringField(IndexConstants.TRAIT_VALUE , speciesName, Field.Store.YES ));
		doc.add(new StringField(IndexConstants.DISPLAY_VALUE , speciesName, Field.Store.YES ));
		
		//Case insensitive tokenisation with boosting
		manualLowercaseTokeniseAndAddBoost(speciesName, doc);
		return doc;
	}
	
	static private void manualLowercaseTokeniseAndAddBoost(String traitDisplayValue, Document doc){
		String lowercase = traitDisplayValue.toLowerCase().replace(" ", "").replace(".", "");
		Field textField = new TextField(IndexConstants.SEARCH, lowercase, Field.Store.NO );
		
		textField.setBoost(10.0f);
		doc.add(textField);
		doc.add(new SortedDocValuesField(IndexConstants.SEARCH, new BytesRef(lowercase)));
        
		//Text field for levenstein distance search - guess what?? We don't use it !!
		String lev = lowercase.replaceAll(",", "").replaceAll("'", "").replaceAll("-","");
	    Field textFieldLev = new TextField(IndexConstants.SEARCH_LEV, lev, Field.Store.NO );
		doc.add(textFieldLev);
		if(traitDisplayValue.contains(" ")){
			String lc = traitDisplayValue.toLowerCase();
			String [] tokens = lc.split(" ");
			if(tokens.length > 0){
				for(int x = 1; x < tokens.length; x++ ){
					String token = tokens[x];
					if(StringUtils.hasLength(token) && ! token.contains(".") ){
					    Field field = new TextField(IndexConstants.SEARCH_SUB, token, Field.Store.NO);
					    doc.add(field);
					}
				}
			}
		}
	}
	
}
