/**
 * 
 */
package org.aksw.geolift.modules;

import java.util.List;
import java.util.Map;
import org.aksw.geolift.json.ParameterType;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author sherif
 *
 */
public interface GeoLiftModule {
	public Model process(Model model, Map<String, String> parameters);
	public List<String> getParameters();
	public List<String> getNecessaryParameters();
	public Map<String, String> selfConfig(Model source, Model target);
    public List<ParameterType> getParameterWithTypes();
}
