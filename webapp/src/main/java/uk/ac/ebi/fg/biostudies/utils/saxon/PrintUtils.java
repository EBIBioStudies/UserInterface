/**
 * 
 */
package uk.ac.ebi.fg.biostudies.utils.saxon;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XPathException;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Properties;

/**
 * @author rpslpereira
 *
 */
public class PrintUtils {
	
	
	public static String printNodeInfo(NodeInfo node, Configuration config) {
	 	NodeInfo nodeInfo=(NodeInfo) node;
		Properties props = new Properties();
//		props.setProperty("method", "xml");
//		props.setProperty("indent", "yes");
		StringWriter resultStr = new StringWriter();
		Result strResult = new StreamResult(resultStr);

		
		Receiver serializer;
		try {
			serializer = config.getSerializerFactory().getReceiver(strResult,
			config.makePipelineConfiguration(), props);
			//nodeInfo.copy(serializer, NodeInfo.ALL_NAMESPACES, true, 0);
			nodeInfo.copy(serializer, NodeInfo.ALL_NAMESPACES, 0);
		} catch (XPathException e) {
			// TODO Auto-generated catch block
			// TODO Exception management
			e.printStackTrace();
		}
	

		String nodeinfoXmlText = resultStr.toString();
//		System.out.println("\nNODE->" + nodeinfoXmlText);
		return nodeinfoXmlText;
	
	}
	
}
