/**
 * 
 */
package org.aksw.geolift.modules.nlp;



import java.util.Collections;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.FileManager;


/**
 * @author sherif
 * This class ranks the  lateral properties of a model according to the average 
 * size of each lateral property by the number of instances of such property
 */
public class LiteralPropertyRanker {
	private static final Logger logger = Logger.getLogger(LiteralPropertyRanker.class.getName());

	Model model;
	TreeMap<Float, Property> rank2LiteralProperties;
	boolean isRanked;



	public LiteralPropertyRanker(Model model) {
		super();
		this.model = model;
		this.rank2LiteralProperties = this.getRank2LiteralProperties();
		this.isRanked = true;
	}

	/**
	 * 
	 *@author sherif
	 */
	public LiteralPropertyRanker() {
		super();
		this.model = null;
		this.rank2LiteralProperties = null;
		this.isRanked = false;
	}



	public Model loadModel(String fileNameOrUri){
		model=ModelFactory.createDefaultModel();
		java.io.InputStream in = FileManager.get().open( fileNameOrUri );
		if (in == null) {
			throw new IllegalArgumentException(
					"File/URI: " + fileNameOrUri + " not found");
		}
		if(fileNameOrUri.endsWith(".ttl")){
			logger.info("Opening Turtle file");
			model.read(in, null, "TTL");
		}else if(fileNameOrUri.endsWith(".rdf")){
			logger.info("Opening RDFXML file");
			model.read(in, null);
		}else if(fileNameOrUri.endsWith(".nt")){
			logger.info("Opening N-Triples file");
			model.read(in, null, "N-TRIPLE");
		}else{
			logger.info("Content negotiation to get RDFXML from " + fileNameOrUri);
			model.read(fileNameOrUri);
		}

		logger.info("loading "+ fileNameOrUri + " is done!!");
		return model;
	}


	/**
	 * @return the model
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * @return the propertyRank
	 */
	public TreeMap<Float, Property> getRank2LiteralProperties() {
		if(model.isEmpty() || model==null)
			return null;
		if(!isRanked){
			rankLitralProperties();			
		}
		return rank2LiteralProperties;
	}

	public  TreeMap<Float, Property> getTopNRankedLiteralProperty(int n) {
		if(model.isEmpty() || model==null)
			return null;
		if(!isRanked){
			rankLitralProperties();			
		}

		int count = 0;
		TreeMap<Float, Property> result = new TreeMap<Float, Property>(Collections.reverseOrder());
		for (Entry<Float, Property> entry : rank2LiteralProperties.entrySet()) {
			if (count >= n) break;
			result.put(entry.getKey(), entry.getValue());
			count++;
		}
		return result;
	}

	private void rankLitralProperties(){
		if(isRanked)
			return;

		rank2LiteralProperties = new TreeMap<Float, Property>(Collections.reverseOrder());
		String sparqlQueryString= "SELECT DISTINCT ?p {?s ?p ?o}";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, model);
		ResultSet queryResults = qexec.execSelect();

		while(queryResults.hasNext()){
			QuerySolution qs=queryResults.nextSolution();
			Property property = ResourceFactory.createProperty(qs.getResource("?p").toString());
			float litPropRank = rankProperty(property);
			rank2LiteralProperties.put(litPropRank, property );
		}
		qexec.close() ;
		isRanked=true;
	}


	/**
	 * @param literalProperty
	 * @return
	 * @author sherif
	 * Ranks each literal property as the total size for all its instances 
	 * divided by the total number of its instances
	 */
	private float rankProperty(Property literalProperty) {
		float literalObjectsCount = 0, totalLiteralObjectsSize=1, literalPropertyRank = 0;

		NodeIterator objectsItr = model.listObjectsOfProperty(literalProperty);
		while (objectsItr.hasNext()) {
			RDFNode litarlObject = objectsItr.nextNode();			
			if(litarlObject.isLiteral()){  // Rank Literal property as total size by count
				totalLiteralObjectsSize += litarlObject.toString().length();
				++literalObjectsCount;
			} else{
				return  0;					// Rank non-literal Property by Zero
			}
		}
		literalPropertyRank = totalLiteralObjectsSize/literalObjectsCount;
		return literalPropertyRank;
	}




	/**
	 * @param model the model to set
	 */
	public void setModel(Model model) {
		this.model = model;
		isRanked=false;
	}

	/**
	 * @param propertyRank the propertyRank to set
	 */

	public Property getTopRankedLiteralProperty(){
		return getTopNRankedLiteralProperty(1).firstEntry().getValue();
	}





	public static void main(String args[]){
		LiteralPropertyRanker l = new LiteralPropertyRanker();
		l.loadModel(args[0]);
		TreeMap<Float, Property> 	map = l.getRank2LiteralProperties();
		int index=1;
		for (Entry<Float, Property> entry : map.entrySet()) {
			logger.info(index++ + ". Rank = " + entry.getKey() + ", Property: " + entry.getValue() );
		}
		logger.info("TOP PROPERTY: "+l.getTopRankedLiteralProperty());

	}
}
