/**
 * 
 */
package uk.ac.ebi.arrayexpress.servlets;

import java.io.PrintWriter;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import net.sf.saxon.om.DocumentInfo;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import uk.ac.ebi.arrayexpress.components.SaxonEngine;
import uk.ac.ebi.arrayexpress.components.SearchEngine;
import uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap;
import uk.ac.ebi.xml.jaxb.Experiments;
import uk.ac.ebi.xml.jaxb.utils.ExperimentUtils;

/**
 * @author rpslpereira
 *
 */
public class QueryPageWriterFileServlet extends QueryPageWriterServlet {



	
	
	@Override
	protected void processPagingHeader(PrintWriter out, ScoreDoc[] hits,
			HttpServletRequestParameterMap params, int queryId,
			SearchEngine search, SaxonEngine saxonEngine, DocumentInfo source,
			String index, String stylesheet, String outputType, String sortBy,
			Boolean descending, String basePath) {


	}

	@Override
	protected void processPagingFooter(PrintWriter out, ScoreDoc[] hits,
			HttpServletRequestParameterMap params, int queryId,
			SearchEngine search, SaxonEngine saxonEngine, DocumentInfo source,
			String index, String stylesheet, String outputType, String sortBy,
			Boolean descending, String basePath) {


	}

	@Override
	protected void processPagingNumber(PrintWriter out, ScoreDoc[] hits,
			HttpServletRequestParameterMap params, int queryId,
			SearchEngine search, SaxonEngine saxonEngine, DocumentInfo source,
			String index, String stylesheet, String outputType, int pageInit,
			int pageEnd, String sortBy, Boolean descending, String basePath) {

		String xml;
		String tabChar="	";
		try {
			xml = search.getController().queryDB(queryId, hits, pageInit,
					pageEnd, params);

			Experiments exp;
			JAXBContext context = JAXBContext.newInstance(Experiments.class);

			Unmarshaller um = context.createUnmarshaller();
			exp = (Experiments) um.unmarshal(new StringReader(xml));

			for (int i = 0; i < exp.getAll().toArray().length; i++) {
				System.out.println("Experiment " + (i + 1) + ": " + exp.getAll().get(i).getExperiment().getAccession());
				out.print(exp.getAll().get(i).getExperiment().getAccession() +tabChar);
				out.print(exp.getAll().get(i).getExperiment().getName()+ tabChar);
				out.print(exp.getAll().get(i).getExperiment().getAssays()+ tabChar);
				out.print(exp.getAll().get(i).getExperiment().getSpecies()+ tabChar);
				out.print(exp.getAll().get(i).getExperiment().getReleasedate()+ tabChar);

			int count=ExperimentUtils.getNumberFiles(exp.getAll().get(i).getFolder(),"raw");
				if(count>1){
					out.print(basePath + "/files/" + exp.getAll().get(i).getExperiment().getAccession() +"?kind=raw" + tabChar);
				}
				else if(count==1){
 					out.print(basePath + "/files/" + exp.getAll().get(i).getExperiment().getAccession() + "/" + ExperimentUtils.getFile(exp.getAll().get(i).getFolder(),"raw").getName() + tabChar);
 			        				}
				else{
					out.print("Data is not available" + tabChar);
				}
		
				
				count=ExperimentUtils.getNumberFiles(exp.getAll().get(i).getFolder(),"fgem");
				if(count>1){
				out.print(basePath + "/files/" + exp.getAll().get(i).getExperiment().getAccession() +"?kind=fgem" + tabChar);
				}
				else if(count==1){
					out.print(basePath + "/files/" + exp.getAll().get(i).getExperiment().getAccession() + "/" + ExperimentUtils.getFile(exp.getAll().get(i).getFolder(),"fgem").getName() + tabChar);
				}
				else{
					out.print("Data is not available" + tabChar);
				}
		
				
			if(exp.getAll().get(i).getExperiment().isLoadedinatlas()){
				out.print("Yes" + "	");					
			}
			
			out.print(basePath + "/experiments/" + exp.getAll().get(i).getExperiment().getAccession() + tabChar);
			out.println();

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}	
	
	

}
