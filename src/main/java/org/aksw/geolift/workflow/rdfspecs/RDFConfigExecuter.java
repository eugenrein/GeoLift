/**
 * 
 */
package org.aksw.geolift.workflow.rdfspecs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.aksw.geolift.helper.vacabularies.SPECS;
import org.aksw.geolift.io.Reader;
import org.aksw.geolift.io.Writer;
import org.aksw.geolift.modules.Dereferencing.DereferencingModule;
import org.aksw.geolift.modules.conformation.ConformationModule;
import org.aksw.geolift.modules.filter.FilterModule;
import org.aksw.geolift.modules.linking.LinkingModule;
import org.aksw.geolift.modules.nlp.NLPModule;
import org.aksw.geolift.operators.MergeOperator;
import org.aksw.geolift.operators.SplitOperator;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.pfunction.library.container;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author sherif
 *
 */
public class RDFConfigExecuter {
	private static final Logger logger = Logger.getLogger(RDFConfigExecuter.class.getName());
	public static 	Model configModel;

	public static void main(String args[]) throws IOException{
		RDFConfigExecuter RDFConfigHandler = new RDFConfigExecuter();
		RDFConfigHandler.run(args[0]);
	}

	/**
	 * @param inputFile
	 * @author sherif
	 * @throws IOException 
	 */
	private void run(String inputFile) throws IOException {
		configModel =  Reader.readModel(inputFile);
		//		configModel.write(System.out,"TTL");
		List<Resource> finalDatasets = getFinalDatasets();
		logger.info("Found " + finalDatasets.size() + " output Datasets: " + finalDatasets);
		for(Resource finalDataset : finalDatasets){
			readDataset(finalDataset);
		}
	}


	/**
	 * @param module
	 * @param inputDatasets
	 * @return model resulted after executing the input module
	 * @author sherif
	 */
	private Model executeModule(Resource module, List<Model> inputDatasets) {
		Model enrichedModel = ModelFactory.createDefaultModel();
		Map<String, String> moduleParameters = getParameters(module);
		NodeIterator typeItr = configModel.listObjectsOfProperty(module, RDF.type);
		while(typeItr.hasNext()){
			RDFNode type = typeItr.next();
			if(type.equals(SPECS.Module)){
				continue;
			}
			if(type.equals(SPECS.NLPModule)){
				NLPModule geoEnricher = new NLPModule();
				enrichedModel = geoEnricher.process(inputDatasets.get(0), moduleParameters);
				return enrichedModel;
			}
			if(type.equals(SPECS.LinkingModule)){
				LinkingModule geoEnricher = new LinkingModule();
				enrichedModel = geoEnricher.process(inputDatasets.get(0), moduleParameters);
				return enrichedModel;
			}
			if(type.equals(SPECS.DereferencingModule)){
				DereferencingModule geoEnricher = new DereferencingModule();
				enrichedModel = geoEnricher.process(inputDatasets.get(0), moduleParameters);
				return enrichedModel;
			}
			if(type.equals(SPECS.ConformationModule)){
				ConformationModule geoEnricher = new ConformationModule();
				enrichedModel = geoEnricher.process(inputDatasets.get(0), moduleParameters);
				return enrichedModel;
			}
			if(type.equals(SPECS.FilterModule)){
				FilterModule geoEnricher = new FilterModule();
				enrichedModel = geoEnricher.process(inputDatasets.get(0), moduleParameters);
				return enrichedModel;
			}
		}
		logger.error(module + " module is not yet implemented,\n" +
				"Currently,the nlp, linking, dereferencing, filter and conformation modules are only implemented\n" +
				"Exit with error ...");
		System.exit(1);
		return null;
	}
	

	/**
	 * @param moduleOrOperator
	 * @param inputDatasetsModels
	 * @return model resulted after executing the input operator
	 * @author sherif
	 */
	private Model executeOperator(Resource operator, List<Model> inputDatasets) {
		Map<String, String> moduleParameters = getParameters(operator);
		NodeIterator typeItr = configModel.listObjectsOfProperty(operator, RDF.type);
		while(typeItr.hasNext()){
			RDFNode type = typeItr.next();
			if(type.equals(SPECS.Operator)){
				continue;
			}
			if(type.equals(SPECS.MergeOperator)){
				MergeOperator mergeOperator = new MergeOperator();
				List<Model> resultModels = mergeOperator.process(inputDatasets, moduleParameters);
				return resultModels.get(0);
			}
			if(type.equals(SPECS.SplitOperator)){
				SplitOperator splitOperator = new SplitOperator();
				List<Model> resultModels = splitOperator.process(inputDatasets, moduleParameters);
				return resultModels.get(0);
			}
		}
		logger.error(operator + " operator is not yet implemented,\n" +
				"Currently,the split and merge operators are only implemented\n" +
				"Exit with error ...");
		System.exit(1);
		return null;
	}

	/**
	 * @param moduleOrOperator
	 * @return map of mudule parameters
	 * @author sherif
	 */
	private static Map<String, String> getParameters(RDFNode moduleOrOperator) {
		String key = null;
		String value = null;
		Map<String, String> moduleParameters = new HashMap<String, String>();
		StmtIterator stItr = configModel.listStatements((Resource) moduleOrOperator, SPECS.hasParameter, (RDFNode) null);
		while(stItr.hasNext()){
			RDFNode parameter =  stItr.next().getObject(); 
			StmtIterator keyItr = configModel.listStatements((Resource) parameter, SPECS.hasKey, (RDFNode) null);
			if(keyItr.hasNext()){
				key =  keyItr.next().getObject().toString(); 
			}
			StmtIterator valueItr = configModel.listStatements((Resource) parameter, SPECS.hasValue, (RDFNode) null);
			if(valueItr.hasNext()){
				value =  valueItr.next().getObject().toString(); 
			}
			moduleParameters.put(key, value);
		}
		return moduleParameters;
	}

	/**
	 * @param dataset
	 * @return dataset model from file/uri/endpoint
	 * @author sherif
	 * @throws IOException 
	 */
	private Model readDataset(Resource dataset) throws IOException{
		// trivial case: read dataset from file/uri/endpoint
		NodeIterator uriItr = configModel.listObjectsOfProperty(dataset, SPECS.FromEndPoint);
		if(uriItr.hasNext()){
			Model cbd = readCBD(dataset, uriItr.next().toString());
			writeDataset(dataset,cbd);
			return cbd;
		}
		uriItr = configModel.listObjectsOfProperty(dataset, SPECS.hasUri);
		if(uriItr.hasNext()){
			Model cbd = Reader.readModel(uriItr.next().toString());
			writeDataset(dataset,cbd);
			return cbd;
		}
		uriItr = configModel.listObjectsOfProperty(dataset, SPECS.inputFile);
		if(uriItr.hasNext()){
			Model cbd = Reader.readModel(uriItr.next().toString());
			writeDataset(dataset,cbd);
			return cbd;
		}
		
		// recursive case: read dataset from previous module/operator output
		Resource moduleOrOperator = getModuleOrOperator(null, dataset);
		Model outputModel = executeModuleOrOperator(moduleOrOperator);
		writeDataset(dataset,outputModel);
		return outputModel;
	}
	
	/**
	 * @param datasetUri
	 * @param dataSetModel
	 * @throws IOException
	 * @author sherif
	 */
	private void writeDataset(Resource datasetUri, Model dataSetModel) throws IOException{
		NodeIterator uriItr = configModel.listObjectsOfProperty(datasetUri, SPECS.outputFile);
		if(uriItr.hasNext()){
			String outputFile = uriItr.next().toString();
			String outputFormat = "TTL"; // turtle is default format
			uriItr = configModel.listObjectsOfProperty(datasetUri, SPECS.outputFormat);
			if(uriItr.hasNext()){
				outputFormat = uriItr.next().toString();
			}
			Writer.writeModel(dataSetModel, outputFormat, outputFile);
		}else{ 
			return;
		}
	}

	/**
	 * @param string
	 * @return
	 * @author sherif
	 */
	private Model readCBD(Resource dataset, String endpointUri) {
		long startTime = System.currentTimeMillis();
		Model result = ModelFactory.createDefaultModel();
		NodeIterator uriItr = configModel.listObjectsOfProperty(dataset, SPECS.hasUri);
		if(uriItr.hasNext()){
			String uri = uriItr.next().toString();
			logger.info("Generating CBD for " + uri + " from " + endpointUri+ "...");
			String sparqlQueryString = "CONSTRUCT {<" + uri + "> ?p ?o} WHERE { <" + uri + "> ?p ?o.}";
			QueryFactory.create(sparqlQueryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointUri, sparqlQueryString);
			result = qexec.execConstruct();
			qexec.close() ;
			logger.info("Generating CBD is done in " + (System.currentTimeMillis() - startTime) + "ms, " + 
					result.size() + " triples found.");
		}else{
			logger.error("No " + SPECS.hasUri + " defined to generate CBD from " + endpointUri 
					+ ", exit with error." );
			System.exit(1);
		}
		return result;
	}

	/**
	 * @param moduleOrOperator
	 * @author sherif
	 * @throws IOException 
	 */
	private Model executeModuleOrOperator(Resource moduleOrOperator) throws IOException {
		List<Resource> inputDatasetsUris = getInputDatasetsUris(moduleOrOperator);
		List<Model> inputDatasetsModels = new ArrayList<Model>();
		for(Resource inputdatasetUri : inputDatasetsUris){
			inputDatasetsModels.add(readDataset(inputdatasetUri));
		}
		NodeIterator typeItr = configModel.listObjectsOfProperty(moduleOrOperator, RDF.type);
		while(typeItr.hasNext()){
			RDFNode type = typeItr.next();
			if(type.equals(SPECS.Module)){
				return executeModule(moduleOrOperator, inputDatasetsModels);
			}
			else if(type.equals(SPECS.Operator)){
				return executeOperator(moduleOrOperator, inputDatasetsModels);
			}
		}
		return null;
	}
	
	/**
	 * @return 	a list of all final output datasets, which are included as output 
	 * 			of some operators/models and not as input to any operator/model
	 * @author sherif
	 */
	private List<Resource> getFinalDatasets(){
		List<Resource> result = new ArrayList<Resource>();
		String sparqlQueryString = 
				"SELECT DISTINCT ?d {?s1 <" + SPECS.hasOutput + "> ?d. " +
						"FILTER (NOT EXISTS {?s2 <" + SPECS.hasInput + "> ?d . } )}";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
		ResultSet queryResults = qexec.execSelect();
		while(queryResults.hasNext()){
			QuerySolution qs = queryResults.nextSolution();
			Resource dataset = qs.getResource("?d");
			result.add(dataset);
		}
		qexec.close() ;
		return result;
	}

	/**
	 * @return 	a list of all input datasets to a certain module/operator
	 * @author sherif
	 */
	private List<Resource> getInputDatasetsUris(Resource moduleOrOperator){
		List<Resource> result = new ArrayList<Resource>();
		String s = "<" + moduleOrOperator + ">";
		String sparqlQueryString = 
				"SELECT DISTINCT ?d {"+ s + " <" + SPECS.hasInput + "> ?d. }";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
		ResultSet queryResults = qexec.execSelect();
		while(queryResults.hasNext()){
			QuerySolution qs = queryResults.nextSolution();
			Resource dataset = qs.getResource("?d");
			result.add(dataset);
		}
		qexec.close() ;
		return result;
	}

	/**
	 * @return 	a list of all output datasets to a certain module/operator
	 * @author sherif
	 */
	private List<Resource> getOutputDatasets(Resource moduleOrOperator){
		List<Resource> result = new ArrayList<Resource>();
		String s = "<" + moduleOrOperator + ">";
		String sparqlQueryString = 
				"SELECT DISTINCT ?d {" + s + " <" + SPECS.hasOutput + "> ?d. }";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
		ResultSet queryResults = qexec.execSelect();
		while(queryResults.hasNext()){
			QuerySolution qs = queryResults.nextSolution();
			Resource dataset = qs.getResource("?d");
			result.add(dataset);
		}
		qexec.close() ;
		return result;
	}

	/**
	 * @return 	module/operator for a given in/output dataset. 
	 * @author sherif
	 */
	private Resource getModuleOrOperator(Resource inputDataset, Resource outputDataset){
		Resource result = ResourceFactory.createResource();
		if(inputDataset == null){
			String q = "<" + outputDataset + ">";
			String sparqlQueryString = 
					"SELECT DISTINCT ?s { ?s <" + SPECS.hasOutput + "> " + q + ". }";
			QueryFactory.create(sparqlQueryString);
			QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
			ResultSet queryResults = qexec.execSelect();
			while(queryResults.hasNext()){
				QuerySolution qs = queryResults.nextSolution();
				result = qs.getResource("?s");
			}
			qexec.close() ;
		}
		else if(outputDataset == null){
			String q = "<" + inputDataset + ">";
			String sparqlQueryString = 
					"SELECT DISTINCT ?s { ?s <" + SPECS.hasInput + "> " + q + ". }";
			QueryFactory.create(sparqlQueryString);
			QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
			ResultSet queryResults = qexec.execSelect();
			while(queryResults.hasNext()){
				QuerySolution qs = queryResults.nextSolution();
				result = qs.getResource("?s");
			}
			qexec.close() ;
		}else{
			String in = "<" + inputDataset + ">";
			String out = "<" + outputDataset + ">";
			String sparqlQueryString = 
					"SELECT DISTINCT ?s { ?s <" + SPECS.hasInput + "> " + in + ". " +
							"?s <" + SPECS.hasOutput + "> " + out + ".}";
			QueryFactory.create(sparqlQueryString);
			QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
			ResultSet queryResults = qexec.execSelect();
			while(queryResults.hasNext()){
				QuerySolution qs = queryResults.nextSolution();
				result = qs.getResource("?s");
			}
			qexec.close() ;
		}
		return result;
	}

}
