package uk.ac.ebi.arrayexpress.lucene;

import java.io.IOException;
import java.util.Stack;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.AttributeSource;

public class SynonymFilter extends TokenFilter {
	public static final String TOKEN_TYPE_SYNONYM = "SYNONYM";
	private Stack synonymStack;
	private SynonymEngine engine;
	private TermAttribute termAttr;
	private AttributeSource save;

	public SynonymFilter(TokenStream in, SynonymEngine engine) {
		super(in);
		synonymStack = new Stack(); // #1
		termAttr = (TermAttribute) addAttribute(TermAttribute.class);
		save = in.cloneAttributes();
		this.engine = engine;
	}

	public boolean incrementToken() throws IOException {
		if (synonymStack.size() > 0) { // #2
			State syn = (State) synonymStack.pop(); // #2
			restoreState(syn); // #2
			return true;
		}
		if (!input.incrementToken()) // #3
			return false;
		addAliasesToStack(); // #4
		return true; // #5
	}

	private void addAliasesToStack() throws IOException {
		String[] synonyms = engine.getSynonyms(termAttr.term()); // #6
		if (synonyms == null)
			return;
		State current = captureState();
		for (int i = 0; i < synonyms.length; i++) { // #7
			save.restoreState(current);
			AnalyzerUtils.setTerm(save, synonyms[i]); // #7

			AnalyzerUtils.setType(save, TOKEN_TYPE_SYNONYM); // #7
			AnalyzerUtils.setPositionIncrement(save, 0); // #8
			synonymStack.push(save.captureState()); // #7
		}
	}
}