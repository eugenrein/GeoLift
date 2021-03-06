/**
 * 
 */
package org.aksw.geolift.io;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author sherif
 *
 */
public class Writer {
	private static final Logger logger = Logger.getLogger(Writer.class.getName());
	
	public static void writeModel(Model model, String format, String outputFile) throws IOException
	{
		logger.info("Saving dataset to " + outputFile + "...");
		long starTime = System.currentTimeMillis();
		FileWriter fileWriter = new FileWriter(outputFile);
		model.write(fileWriter, format);
		logger.info("Saving file done in " + (System.currentTimeMillis() - starTime) +"ms.");
	}
}
