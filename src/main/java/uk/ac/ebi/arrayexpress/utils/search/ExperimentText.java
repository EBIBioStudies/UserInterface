package uk.ac.ebi.arrayexpress.utils.search;

import net.sf.saxon.om.NodeInfo;

/**
 * A transitional class holding text that will be matched when searching for experiments.
 * To be replaced by Lucene engine in 1.2.
 */

public class ExperimentText
{
    // all text, concatenated
    public String text;

    public String accession;

    // all accessions, concatenated
    public String accessions;

    // all species, concatenated
    public String species;

    // all array info, concatenated
    public String array;

    // experiment type info, concatenated
    public String experimentType;

    // user ids, concatenated
    public String users;

    public ExperimentText populateFromExperiment( NodeInfo exp )
    {
// TODO - commented
//        SaxonEngine saxon = (SaxonEngine) Application.getAppComponent("SaxonEngine");
//        if (null != exp) {
//            text = saxon.concatAllText(exp);
//            accession = saxon.evaluateXPathSingle(exp, "accession").toLowerCase();
//            accessions = " ".concat(accession).concat(" ").concat(saxon.concatAllText(saxon.evaluateXPath(exp, "secondaryaccession"))).toLowerCase();
//            species = saxon.concatAllText(saxon.evaluateXPath(exp, "species")).toLowerCase();
//            array = saxon.concatAllText(saxon.evaluateXPath(exp, "arraydesign")).toLowerCase();
//            experimentType = saxon.concatAllText(saxon.evaluateXPath(exp, "experimenttype")).toLowerCase();
//            users = " ".concat(saxon.concatAllText(saxon.evaluateXPath(exp, "user")));
//        }
//
        return this;
    }
}
