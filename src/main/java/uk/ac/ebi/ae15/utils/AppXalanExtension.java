package uk.ac.ebi.ae15.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xalan.extensions.ExpressionContext;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xpath.NodeSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uk.ac.ebi.ae15.app.Application;
import uk.ac.ebi.ae15.components.DownloadableFilesRegistry;
import uk.ac.ebi.ae15.components.Experiments;
import uk.ac.ebi.ae15.utils.files.FtpFileEntry;

import javax.xml.transform.TransformerException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppXalanExtension
{
    // logging machinery
    private static final Log log = LogFactory.getLog(AppXalanExtension.class);
    // Application reference
    private static Application application;

    public static String toUpperCase( String str )
    {
        return str.toUpperCase();
    }

    public static String toLowerCase( String str )
    {
        return str.toLowerCase();
    }

    public static String capitalize( String str )
    {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static String normalizeSpecies( String species )
    {
        // if more than one word: "First second", otherwise "First"
        String[] spArray = species.trim().split("\\s");
        if (0 == spArray.length) {
            return "";
        } else if (1 == spArray.length) {
            return capitalize(spArray[0]);
        } else {
            return capitalize(spArray[0] + ' ' + spArray[1]);
        }
    }

    public static String dateToRfc822()
    {
        return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(new Date());
    }

    public static String dateToRfc822( String dateString )
    {
        if (null != dateString && 0 < dateString.length()) {
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
                dateString = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(date);
            } catch (Throwable x) {
                log.debug("Caught an exception:", x);
            }
        } else {
            dateString = "";
        }

        return dateString;
    }

    public static boolean isFileAvailableForDownload( String accession, String name )
    {
        return ((DownloadableFilesRegistry) application.getComponent("DownloadableFilesRegistry"))
                .doesExist(accession, name);
    }

    public static boolean isExperimentInWarehouse( String accession )
    {
        return ((Experiments) application.getComponent("Experiments"))
                .isInWarehouse(accession);
    }

    public static NodeSet getFilesForExperiment( ExpressionContext context, String accession )
    {
        NodeSet n = new NodeSet();
        if (null != context && null != context.getContextNode()) {
            Document doc = context.getContextNode().getOwnerDocument();
            if (null != doc) {
                List<FtpFileEntry> files = ((DownloadableFilesRegistry) application.getComponent("DownloadableFilesRegistry"))
                        .getFilesMap()
                        .getEntriesByAccession(accession);
                for (FtpFileEntry file : files) {
                    Element fileElt = doc.createElement("file");
                    fileElt.setAttribute("name", FtpFileEntry.getName(file));
                    fileElt.setAttribute("size", String.valueOf(file.getSize()));
                    fileElt.setAttribute("lastmodified", new SimpleDateFormat("d MMMMM yyyy, HH:mm").format(new Date(file.getLastModified())));
                    n.addElement(fileElt);
                }
            }
        }
        return n;
    }
    
    public static boolean testRegexp( String input, String pattern, String flags )
    {
        boolean result = false;

        try {
            int patternFlags = (flags.indexOf("i") >= 0 ? Pattern.CASE_INSENSITIVE : 0);

            String inputStr = (input == null ? "" : input);
            String patternStr = (pattern == null ? "" : pattern);

            Pattern p = Pattern.compile(patternStr, patternFlags);
            Matcher matcher = p.matcher(inputStr);
            result = matcher.find();
        } catch ( Throwable t ) {
            log.debug("Caught an exception:", t);
        }

        return result;
    }

    public static String replaceRegexp( String input, String pattern, String flags, String replace )
    {
        int patternFlags = (flags.indexOf("i") >= 0 ? Pattern.CASE_INSENSITIVE : 0);

        String inputStr = (input == null ? "" : input);
        String patternStr = (pattern == null ? "" : pattern);
        String replaceStr = (replace == null ? "" : replace);

        Pattern p = Pattern.compile(patternStr, patternFlags);
        Matcher matcher = p.matcher(inputStr);
        return (flags.indexOf("g") >= 0 ? matcher.replaceAll(replaceStr) : matcher.replaceFirst(replaceStr));
    }

    private static String keywordToPattern( String keyword, boolean wholeWord )
    {
        return (wholeWord ? "\\b\\Q" + keyword + "\\E\\b" : "\\Q" + keyword + "\\E");
    }

    public static boolean testSpecies( NodeList nl, String species )
    {
        boolean result = false;

        try {
            if (0 < nl.getLength()) {

                String textIdx = ((Element) nl.item(0)).getAttribute("textIdx");
                return ((Experiments) application.getComponent("Experiments")).getSearch().matchSpecies(textIdx, species);
            }
        } catch (Throwable x) {
            log.error("Caught an exception:", x);
        }

        return result;
    }

    public static boolean testArray( NodeList nl, String array )
    {
        boolean result = false;

        try {
            if (0 < nl.getLength()) {

                String textIdx = ((Element) nl.item(0)).getAttribute("textIdx");
                return ((Experiments) application.getComponent("Experiments")).getSearch().matchArray(textIdx, array);
            }
        } catch (Throwable x) {
            log.error("Caught an exception:", x);
        }

        return result;
    }

    public static boolean testExperimentType( NodeList nl, String experimentType )
    {
        boolean result = false;

        try {
            if (0 < nl.getLength()) {

                String textIdx = ((Element) nl.item(0)).getAttribute("textIdx");
                return ((Experiments) application.getComponent("Experiments")).getSearch().matchExperimentType(textIdx, experimentType);
            }
        } catch (Throwable x) {
            log.error("Caught an exception:", x);
        }

        return result;
    }

    public static boolean testKeywords( NodeList nl, String keywords, boolean wholeWords )
    {
        boolean result = false;

        try {
            if (0 < nl.getLength()) {

                String textIdx = ((Element) nl.item(0)).getAttribute("textIdx");
                return ((Experiments) application.getComponent("Experiments")).getSearch().matchText(textIdx, keywords, wholeWords);
            }
        } catch (Throwable x) {
            log.error("Caught an exception:", x);
        }
        
        return result;
    }

    public static String markKeywords( String input, String keywords, boolean wholeWords )
    {
        String result = input;

        try {
            if (null != keywords && 0 < keywords.length()) {
                String[] kwdArray = keywords.split("\\s");
                for ( String keyword : kwdArray ) {
                    result = replaceRegexp(result, "(" + keywordToPattern(keyword, wholeWords) + ")", "ig", "\u00ab$1\u00bb");
                }
            }
            boolean shouldRemoveExtraMarkers = true;
            String newResult;

            while ( shouldRemoveExtraMarkers ) {
                newResult = replaceRegexp(result, "\u00ab([^\u00ab\u00bb]*)\u00ab", "ig", "\u00ab$1");
                shouldRemoveExtraMarkers = !newResult.equals(result);
                result = newResult;
            }
            shouldRemoveExtraMarkers = true;
            while ( shouldRemoveExtraMarkers ) {
                newResult = replaceRegexp(result, "\u00bb([^\u00ab\u00bb]*)\u00bb", "ig", "$1\u00bb");
                shouldRemoveExtraMarkers = !newResult.equals(result);
                result = newResult;
            }
        } catch ( Throwable x ) {
            log.error("Caught an exception:", x);
        }

        return result;
    }

    public static void logInfo( XSLProcessorContext c, ElemExtensionCall extElt )
    {
        try {
            log.info(extElt.getAttribute("select", c.getContextNode(), c.getTransformer()));
        } catch ( TransformerException e ) {
            log.debug("Caught an exception:", e);
        }
    }

    public static void logDebug( XSLProcessorContext c, ElemExtensionCall extElt )
    {
        try {
            log.debug(extElt.getAttribute("select", c.getContextNode(), c.getTransformer()));
        } catch ( TransformerException e ) {
            log.debug("Caught an exception:", e);
        }
    }

    public static void setApplication( Application app )
    {
        application = app;
    }
}
