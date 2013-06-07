package uk.ac.ebi.arrayexpress.utils;

import java.io.File;  
import javax.xml.transform.Transformer;  
import javax.xml.transform.TransformerFactory;  
import javax.xml.transform.stream.StreamResult;  
import javax.xml.transform.stream.StreamSource;  
  
public class Main {  
  
    /** 
     * Simple transformation method. 
     * @param sourcePath - Absolute path to source xml file. 
     * @param xsltPath - Absolute path to xslt file. 
     * @param resultDir - Directory where you want to put resulting files. 
     */  
    public static void simpleTransform(String sourcePath, String xsltPath,  
                                       String resultDir) {  
        TransformerFactory tFactory = TransformerFactory.newInstance();  
        try {  
            Transformer transformer =  
                tFactory.newTransformer(new StreamSource(new File(xsltPath)));  
  
            transformer.transform(new StreamSource(new File(sourcePath)),  
                                  new StreamResult(new File(resultDir)));  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
  
    public static void main(String[] args) {  
        //Set saxon as transformer.  
        System.setProperty("javax.xml.transform.TransformerFactory",  
                           "net.sf.saxon.TransformerFactoryImpl");  
  
//        simpleTransform("/Users/rpslpereira/Desktop/encoding.xml",  
//                        "/Users/rpslpereira/Desktop/encoding.xslt", "/Users/rpslpereira/Desktop/result.xml");  
        simpleTransform("/Users/rpslpereira/Desktop/encoding3.xml",  
                "/Users/rpslpereira/EclipseWorkspaces/BioSamples_TRUNK/bs-interface/webapp/src/main/webapp/WEB-INF/server-assets/stylesheets/biosamplesgroup3-html.xsl", "/Users/rpslpereira/Desktop/result.xml");  
        
  
    }  
}  