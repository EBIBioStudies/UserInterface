package uk.ac.ebi.fg.biostudies.utils.saxon.search;

import org.apache.lucene.search.TopDocs;
import uk.ac.ebi.fg.biostudies.utils.HttpServletRequestParameterMap;

import java.io.IOException;

public interface IIndexEnvironment {

	public abstract String queryPaged(Integer queryId, QueryInfo info,
			HttpServletRequestParameterMap map) throws IOException;

	public abstract String queryPartialDocs(TopDocs hits, int initialExp,
			int finalExp, HttpServletRequestParameterMap map)
			throws IOException;

	public abstract TopDocs queryAllDocs(Integer queryId, QueryInfo info,
			HttpServletRequestParameterMap map) throws IOException;

}