/*
 * Copyright 2009-2015 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package uk.ac.ebi.fg.biostudies.components;

import net.sf.saxon.om.DocumentInfo;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.modules.XPathQueryService;
import uk.ac.ebi.fg.biostudies.app.Application;
import uk.ac.ebi.fg.biostudies.app.ApplicationComponent;
import uk.ac.ebi.fg.biostudies.utils.persistence.FilePersistence;
import uk.ac.ebi.fg.biostudies.utils.saxon.IDocumentSource;
import uk.ac.ebi.fg.biostudies.utils.saxon.PersistableDocumentContainer;
import uk.ac.ebi.microarray.arrayexpress.shared.auth.AuthenticationHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Users extends ApplicationComponent implements IDocumentSource
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static String MAP_USERS_FOR_ACCESSION = "users-for-accession";

    private AuthenticationHelper authHelper;
    private FilePersistence<PersistableDocumentContainer> document;

    private SaxonEngine saxon;
    private SearchEngine search;
    private XmlDbConnectionPool xmlDbConnectionPool ;
    
    public final String INDEX_ID = "users";

    public enum UserSource
    {
        AE1, AE2;

        public String getStylesheetName()
        {
            switch (this) {
                case AE1:   return "preprocess-users-ae1-xml.xsl";
                case AE2:   return "preprocess-users-ae2-xml.xsl";
            }
            return null;
        }
    }

    public Users()
    {
    }

    @Override
    public void initialize() throws Exception
    {
    	this.authHelper = new AuthenticationHelper();
    	this.xmlDbConnectionPool = (XmlDbConnectionPool) Application.getAppComponent("XmlDbConnectionPool");
    }

    @Override
    public void terminate() throws Exception
    {
    }

    // implementation of IDocumentSource.getDocumentURI()
    @Override
    public String getDocumentURI()
    {
        return "users.xml";
    }

    

    


//    public boolean isAccessible( String accession, List<String> userIds ) throws IOException
//    {
//        @SuppressWarnings("unchecked")
//        Set<String> ids = (Set<String>)this.userMap.getValue(accession);
//        for (String userId : userIds) {
//            if (isPrivilegedByID(userId) || (null != ids && ids.contains(userId)))
//                return true;
//        }
//        return false;
//    }
//
//   

//    public boolean isPrivilegedByName( String name ) throws IOException
//    {
//        name = StringEscapeUtils.escapeXml(name);
//        try {
//            return  (Boolean)saxon.evaluateXPathSingle(
//                    getDocument()
//                    , "(/users/user[name = '" + name + "']/is_privileged = true())"
//            );
//        } catch (XPathException x) {
//            throw new RuntimeException(x);
//        }
//    }
//
//    public boolean isPrivilegedByID( String id ) throws IOException
//    {
//        id = StringEscapeUtils.escapeXml(id);
//        try {
//            return (Boolean)saxon.evaluateXPathSingle(
//                getDocument()
//                , "(/users/user[id = '" + id + "']/is_privileged = true())"
//        );
//        } catch (XPathException x) {
//            throw new RuntimeException(x);
//        }
//    }

    public List<String> getUserIDs( String name ) throws Exception
    {
        name = StringEscapeUtils.escapeXml(name);
       
        List<String> retPass= new ArrayList<String>();
        Collection coll=xmlDbConnectionPool.getCollection();
		XPathQueryService service = (XPathQueryService) coll
				.getService("XPathQueryService", "1.0");
		
		ResourceSet set = null;

		set = service
				.query("/Users/User[Name='" +  name+  "']/@id/string()");
			ResourceIterator iter = set.getIterator();

		// Loop through all result items
		while (iter.hasMoreResources()) {

			retPass.add((String)iter.nextResource().getContent());
		}
        //coll.close();
        return retPass;
    }

    private List<String> getUserPasswords( String name ) throws Exception
    {
        name = StringEscapeUtils.escapeXml(name);       
        List<String> retPass= new ArrayList<String>();
        Collection coll=xmlDbConnectionPool.getCollection();
		XPathQueryService service = (XPathQueryService) coll
				.getService("XPathQueryService", "1.0");
		
		ResourceSet set = null;

		set = service
				.query("/Users/User[Name='" +  name+  "']/Password/text()");
			ResourceIterator iter = set.getIterator();

		// Loop through all result items
		while (iter.hasMoreResources()) {

			retPass.add((String)iter.nextResource().getContent());
		}
        //coll.close();
        return retPass;

    }

    public String hashLogin( String username, String password, String suffix ) throws Exception
    {
        if ( null != username && null != password && null != suffix ) {
            List<String> userPasswords = getUserPasswords(username);
            //logger.debug("userPasswords->" + userPasswords);
            for (String userPassword : userPasswords)
                if ( password.equals(userPassword) ) {
                    return this.authHelper.generateHash(username, password, suffix);
                }
        }
        return "";
    }

    public boolean verifyLogin( String username, String hash, String suffix ) throws Exception
    {
        if ( null != username && null != hash && null != suffix ) {
            List<String> userPasswords = getUserPasswords(username);
            for (String userPassword : userPasswords)
                if ( this.authHelper.verifyHash(hash, username, userPassword, suffix) ) {
                    return true;
                }
        }
        return false;
    }

    public boolean isPrivilegedByName( String name ) throws IOException
    {
    	return false;
    }


    public boolean isPrivilegedByID( String id ) throws IOException
    {
       return false;
    }   
    
    
	@Override
	public DocumentInfo getDocument() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDocument(DocumentInfo doc) throws Exception {
		// TODO Auto-generated method stub
		
	}

 
    
    @SuppressWarnings("unchecked")
    public String remindPassword( String nameOrEmail, String accession ) throws IOException
    {
    	return "";
//        nameOrEmail = StringEscapeUtils.escapeXml(nameOrEmail);
//        accession = null != accession ? accession.toUpperCase() : "";
//
//        try {
//            List users = null;
//
//            Object userIds = this.userMap.getValue(accession);
//            if (userIds instanceof Set) {
//                Set<String> uids = (Set<String>)(userIds);
//                String ids = StringTools.arrayToString(uids.toArray(new String[uids.size()]), ",");
//
//                users = this.saxon.evaluateXPath(
//                        getDocument()
//                        , "/users/user[(name|email = '" + nameOrEmail + "') and id = (" + ids + ")]"
//                );
//            }
//
//            String reportMessage;
//            String result = "Unable to find matching account information, please contact us for assistance.";
//            if (null != users && users.size() > 0) {
//                if (1 == users.size()) {
//                    String username = (String)this.saxon.evaluateXPathSingle((NodeInfo)users.get(0), "string(name)");
//                    String email = (String)this.saxon.evaluateXPathSingle((NodeInfo)users.get(0), "string(email)");
//                    String password = (String)this.saxon.evaluateXPathSingle((NodeInfo)users.get(0), "string(password)");
//
//                    getApplication().sendEmail(
//                            getPreferences().getString("ae.password-remind.originator")
//                            , new String[]{email}
//                            , getPreferences().getString("ae.password-remind.subject")
//                            , "Dear " + username + "," + StringTools.EOL
//                            + StringTools.EOL
//                            + "Your ArrayExpress account information is:" + StringTools.EOL
//                            + StringTools.EOL
//                            + "    User name: " + username + StringTools.EOL
//                            + "    Password: " + password + StringTools.EOL
//                            + StringTools.EOL
//                            + "Regards," + StringTools.EOL
//                            + "ArrayExpress." + StringTools.EOL
//                            + StringTools.EOL
//                    );
//
//
//                    reportMessage = "Sent account information to the user [" + username + "], email [" + email + "], accession [" + accession + "]";
//                    result = "Account information sent, please check your email";
//                } else {
//                    // multiple results, report this to administrators
//                    reportMessage = "Request failed: found multiple users for name/email [" + nameOrEmail + "] accessing [" + accession + "].";
//                }
//            } else {
//                // no results, report this to administrators
//                reportMessage = "Request failed: found no users for name/email [" + nameOrEmail + "] accessing [" + accession + "].";
//            }
//
//            getApplication().sendEmail(
//                    getPreferences().getString("ae.password-remind.originator")
//                    , getPreferences().getStringArray("ae.password-remind.recipients")
//                    , "ArrayExpress account information request"
//                    , reportMessage + StringTools.EOL
//                    + StringTools.EOL
//                    + "Sent by [${variable.appname}] running on [${variable.hostname}]" + StringTools.EOL
//                    + StringTools.EOL
//            );
//            return result;
//
//        } catch (XPathException x) {
//            throw new RuntimeException(x);
//        }

    }

}
