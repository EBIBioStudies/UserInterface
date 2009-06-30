package uk.ac.ebi.arrayexpress.utils.model.parser;

//import org.apache.commons.digester.Digester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.model.ExperimentBean;
import uk.ac.ebi.arrayexpress.utils.model.ExperimentList;

public class ExperimentParser
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

//    private Digester digester;

    private ExperimentParserMode parserMode;
    
    public ExperimentParser(ExperimentParserMode mode)
    {
        parserMode = mode;

//        digester = new Digester();
//        digester.setValidating(false);
//        digester.setXIncludeAware(false);
//
//        String experimentPath = "experiment";
//
//        if (ExperimentParserMode.MULTIPLE_EXPERIMENTS == parserMode) {
//            digester.addObjectCreate("experiments", ExperimentList.class);
//
//            experimentPath = "experiments/experiment";
//            digester.addObjectCreate(experimentPath, ExperimentBean.class);
//            digester.addSetNext(experimentPath, "add");
//        } else {
//            digester.addObjectCreate(experimentPath, ExperimentBean.class);
//        }
//
//        digester.addCallMethod(experimentPath, "setAttributes", 5);
//        digester.addCallParam(experimentPath, 0, "id");
//        digester.addCallParam(experimentPath, 1, "accession");
//        digester.addCallParam(experimentPath, 2, "name");
//        digester.addCallParam(experimentPath, 3, "releasedate");
//        digester.addCallParam(experimentPath, 4, "miamegold");
//
//        digester.addCallMethod(experimentPath + "/user", "addUser", 1);
//        digester.addCallParam(experimentPath + "/user", 0);
//
//        digester.addCallMethod(experimentPath + "/secondaryaccession", "addSecondaryAccession", 1);
//        digester.addCallParam(experimentPath + "/secondaryaccession", 0);
//
//        digester.addCallMethod(experimentPath + "/sampleattribute", "addSampleAttribute", 2);
//        digester.addCallParam(experimentPath + "/sampleattribute", 0, "category");
//        digester.addCallParam(experimentPath + "/sampleattribute", 1, "value");
//
//        digester.addCallMethod(experimentPath + "/experimentalfactor", "addExperimentalFactor", 2);
//        digester.addCallParam(experimentPath + "/experimentalfactor", 0, "name");
//        digester.addCallParam(experimentPath + "/experimentalfactor", 1, "value");
//
//        digester.addCallMethod(experimentPath + "/miamescore", "addMiameScore", 2);
//        digester.addCallParam(experimentPath + "/miamescore", 0, "name");
//        digester.addCallParam(experimentPath + "/miamescore", 1, "value");
//
//        digester.addCallMethod(experimentPath + "/arraydesign", "addArrayDesign", 4);
//        digester.addCallParam(experimentPath + "/arraydesign", 0, "id");
//        digester.addCallParam(experimentPath + "/arraydesign", 1, "accession");
//        digester.addCallParam(experimentPath + "/arraydesign", 2, "name");
//        digester.addCallParam(experimentPath + "/arraydesign", 3, "count");
//
//        digester.addCallMethod(experimentPath + "/bioassaydatagroup", "addBioAssayDataGroup", 7);
//        digester.addCallParam(experimentPath + "/bioassaydatagroup", 0, "id");
//        digester.addCallParam(experimentPath + "/bioassaydatagroup", 1, "name");
//        digester.addCallParam(experimentPath + "/bioassaydatagroup", 2, "bioassaydatacubes");
//        digester.addCallParam(experimentPath + "/bioassaydatagroup", 3, "arraydesignprovider");
//        digester.addCallParam(experimentPath + "/bioassaydatagroup", 4, "dataformat");
//        digester.addCallParam(experimentPath + "/bioassaydatagroup", 5, "bioassays");
//        digester.addCallParam(experimentPath + "/bioassaydatagroup", 6, "isderived");
//
//        digester.addCallMethod(experimentPath + "/bibliography", "addBibliography", 9);
//        digester.addCallParam(experimentPath + "/bibliography", 0, "accession");
//        digester.addCallParam(experimentPath + "/bibliography", 1, "publication");
//        digester.addCallParam(experimentPath + "/bibliography", 2, "authors");
//        digester.addCallParam(experimentPath + "/bibliography", 3, "title");
//        digester.addCallParam(experimentPath + "/bibliography", 4, "year");
//        digester.addCallParam(experimentPath + "/bibliography", 5, "volume");
//        digester.addCallParam(experimentPath + "/bibliography", 6, "issue");
//        digester.addCallParam(experimentPath + "/bibliography", 7, "pages");
//        digester.addCallParam(experimentPath + "/bibliography", 8, "uri");
//
//        digester.addCallMethod(experimentPath + "/provider", "addProvider", 3);
//        digester.addCallParam(experimentPath + "/provider", 0, "contact");
//        digester.addCallParam(experimentPath + "/provider", 1, "email");
//        digester.addCallParam(experimentPath + "/provider", 2, "role");
//
//        digester.addCallMethod(experimentPath + "/experimentdesign", "addExperimentDesign", 1);
//        digester.addCallParam(experimentPath + "/experimentdesign", 0);
//
//        digester.addCallMethod(experimentPath + "/experimenttype", "addExperimentType", 1);
//        digester.addCallParam(experimentPath + "/experimenttype", 0);
//
//        digester.addCallMethod(experimentPath + "/description", "addDescription", 2);
//        digester.addCallParam(experimentPath + "/description", 0, "id");
//        digester.addCallParam(experimentPath + "/description", 1);
    }

    public ExperimentBean parseSingle(String xml)
    {
        ExperimentBean exp = null;

//        if (ExperimentParserMode.MULTIPLE_EXPERIMENTS == parserMode) {
//            logger.error("Parser initialized for processing multiple experiments");
//        } else {
//            try {
//                exp = (ExperimentBean)digester.parse(new StringReader(xml));
//            } catch (Throwable x) {
//                logger.error("Caught an exception:", x);
//            }
//        }
        return exp;
    }

    public ExperimentList parseMultiple(String xml)
    {
        logger.debug("Started parsing");
        ExperimentList exps = null;
//        if (ExperimentParserMode.SINGLE_EXPERIMENT == parserMode) {
//            logger.error("Parser initialized for processing single experiment");
//        } else {
//            try {
//                exps = (ExperimentList)digester.parse(new StringReader(xml));
//                logger.debug("Parsing complete");
//            } catch (Throwable x) {
//                logger.error("Caught an exception:", x);
//            }
//        }
        return exps;
    }
}
