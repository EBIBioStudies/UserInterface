package uk.ac.ebi.arrayexpress.utils.saxon.search;

import java.io.IOException;

import org.apache.lucene.search.TopDocs;

import uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap;

public interface IIndexEnvironment {

	public abstract String queryPaged(Integer queryId, QueryInfo info,
			HttpServletRequestParameterMap map) throws IOException;

	public abstract String queryPartialDocs(TopDocs hits, int initialExp,
			int finalExp, HttpServletRequestParameterMap map)
			throws IOException;

	public abstract TopDocs queryAllDocs(Integer queryId, QueryInfo info,
			HttpServletRequestParameterMap map) throws IOException;

}