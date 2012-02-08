/**
 * 
 */
package uk.ac.ebi.arrayexpress.utils.saxon.search;

import java.io.IOException;

import org.apache.commons.configuration.HierarchicalConfiguration;

import uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap;

/**
 * @author rpslpereira
 *
 */
public class ExperimentsIndexEnvironment extends IndexEnvironment {

	/**
	 * @param indexConfig
	 */
	
	private int countAssaysFiltered;
	


	public int getCountAssaysFiltered() {
		return countAssaysFiltered;
	}

	public void setCountAssaysFiltered(int countAssaysFiltered) {
		this.countAssaysFiltered = countAssaysFiltered;
	}

	public ExperimentsIndexEnvironment(HierarchicalConfiguration indexConfig) {
		super(indexConfig);
		// TODO Auto-generated constructor stub
	}
	
	public String queryPaged( Integer queryId, QueryInfo info, HttpServletRequestParameterMap map) throws IOException
    {
		String res=super.queryPaged(queryId, info, map);
		System.out.println("ENTROU!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		return res;
    
    }

}
