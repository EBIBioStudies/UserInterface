package uk.ac.ebi.arrayexpress.utils.search;

import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.components.SaxonEngine;
import uk.ac.ebi.arrayexpress.utils.RegExpHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Search experiments based on full-text information stored in ExperimentText class
 */
public class ExperimentSearch
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // array of ExperimentText objects in the same order they exist in the document
    private List<ExperimentText> expText = new ArrayList<ExperimentText>();

    // mapping of experiment accession numbers to experiment text indices
    private Map<String, Integer> accessionIdx = new HashMap<String, Integer>();

    public boolean isEmpty()
    {
        return expText.isEmpty();
    }

    public void buildText( XdmNode experiments )
    {
        if (null != experiments && null != experiments.getUnderlyingNode()) {
            try {
                expText.clear();
                accessionIdx.clear();

                XdmValue exps = ((SaxonEngine)Application.getAppComponent("SaxonEngine")).evaluateXPath(experiments, "/experiments/experiment");
                if (null != exps) {
                    for (XdmItem expObj : exps) {
                        ExperimentText text = new ExperimentText().populateFromExperiment((XdmNode)expObj);
                        expText.add(text);
                        accessionIdx.put(text.accession, expText.size() - 1);
                    }
                }
            } catch ( Throwable x ) {
                logger.error("Caught an exception:", x);
            }
        }
    }

    public boolean matchText( String textIdx, String keywords, boolean wholeWords )
    {
        if (null == textIdx) {
            return false;
        }
        Integer idx = Integer.parseInt(textIdx);
        return (null != idx) && matchString(expText.get(idx).text, keywords, wholeWords);
    }

    public boolean matchAccession( String textIdx, String accession )
    {
        if (null == textIdx) {
            return false;
        }
        Integer idx = Integer.parseInt(textIdx);
        return (null != idx) && expText.get(idx).accessions.contains(" ".concat(accession).concat(" ").toLowerCase());
    }

    public boolean matchSpecies( String textIdx, String species )
    {
        if (null == textIdx) {
            return false;
        }
        Integer idx = Integer.parseInt(textIdx);
        return (null != idx) && expText.get(idx).species.contains(species.trim().toLowerCase());
    }

    public boolean matchArray( String textIdx, String array )
    {
        if (null == textIdx) {
            return false;
        }
        Integer idx = Integer.parseInt(textIdx);
        return (null != idx) && expText.get(idx).array.contains(array.trim().toLowerCase());
    }

    public boolean matchExperimentType( String textIdx, String experimentType )
    {
        if (null == textIdx) {
            return false;
        }
        Integer idx = Integer.parseInt(textIdx);
        return (null != idx) && expText.get(idx).experimentType.contains(experimentType.trim().toLowerCase());
    }

    public boolean matchUser( String textIdx, String user )
    {
        if (null == textIdx) {
            return false;
        }
        Integer idx = Integer.parseInt(textIdx);
        return (null != idx) && expText.get(idx).users.contains(" ".concat(user).concat(" "));
    }

    public boolean isAccessible( String accession, String user )
    {
        if (user.equals("0")) {
            return true;
        }

        Integer idx = accessionIdx.get(accession.toLowerCase());
        return (null != idx) && matchUser(String.valueOf(idx), user);
    }

    public boolean doesPresent( String accession )
    {
        return accessionIdx.containsKey(accession.toLowerCase());
    }

    private String keywordToPattern( String keyword, boolean wholeWord )
    {
        return (wholeWord ? "\\b\\Q" + keyword + "\\E\\b" : "\\Q" + keyword + "\\E");
    }

    private boolean matchString( String input, String keywords, boolean wholeWords )
    {
        // trim spaces on both sides
        keywords = keywords.trim();

        if (0 < keywords.length()) {
            // by default (i.e. no keywords) it'll always match

            if (2 < keywords.length() && '"' == keywords.charAt(0) && '"' == keywords.charAt(keywords.length() - 1)) {
                // if keywords are adorned with double-quotes we do phrase matching
                return new RegExpHelper(keywordToPattern(keywords.substring(1, keywords.length() - 1), true), "i").test(input);
            } else {
                // any keyword fails -> no match :)
                String[] kwdArray = keywords.split("\\s+");
                for ( String keyword : kwdArray ) {
                    if (!new RegExpHelper(keywordToPattern(keyword, wholeWords), "i").test(input))
                        return false;
                }
            }
        }
        return true;
    }
}
