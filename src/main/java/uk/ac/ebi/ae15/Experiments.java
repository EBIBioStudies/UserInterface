package uk.ac.ebi.ae15;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.commons.logging.Log;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.FactoryConfigurationError;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

class ExperimentListEntry {

    int id;
    String accession;
    boolean isPublic;

    public ExperimentListEntry( int _id, String _accession, boolean _public )
    {
        id = _id;
        accession = _accession;
        isPublic = _public;
    }

}


class GetExperimentXmlThread extends Thread implements Runnable {

    private final static Log log = org.apache.commons.logging.LogFactory.getLog( GetExperimentXmlThread.class );

    private boolean isNew = true;       // is thread new to the business?
    private boolean isReady = false;    // is thread ready to execute a statement?
    private boolean isDone = false;     // is thread done with a statement result?
    private boolean isRetired = false;  // is thread to be retired?
    private boolean isFaulty = false;   // has thready thrown any exception in the past - we abort
    DataSource dataSource;
    int experimentId;
    String result;

    private static String getExperimentXmlSql = "select XmlElement( \"experiment\"" +
        " , XmlAttributes( i.identifier as \"accnum\", e.id as \"id\", nvt_name.value as \"name\", nvt_releasedate.value as \"releasedate\", nvt_miamegold.value as \"miamegold\" )" +
        " , ( select XmlElement( \"users\", XmlAgg( XmlElement( \"user\", XmlAttributes( v.user_id as \"id\" ) ) ) ) from tt_extendable ext left outer join pl_visibility v on v.label_id = ext.label_id where ext.id = e.id )" +
        " , ( select XmlElement( \"secondaryaccessions\", XmlAgg( XmlElement(\"secondaryaccession\", sa.value ) ) ) from tt_namevaluetype sa where sa.t_extendable_id = e.id and sa.name = 'SecondaryAccession' )" +
        " , ( select XmlElement(\"sampleattributes\", XmlAgg( XmlElement( \"sampleattribute\", XmlAttributes( i4samattr.category, i4samattr.value )))) from ( select  /*+ LEADING(b) INDEX(o) INDEX(c) INDEX(b)*/ distinct b.experiments_id as id, o.category, o.value from tt_ontologyentry o, tt_characteris_t_biomateri c, tt_biomaterials_experiments b where b.biomaterials_id = c.t_biomaterial_id and c.characteristics_id = o.id ) i4samattr where i4samattr.id = e.id group by i4samattr.id )" +
        " , ( select XmlElement( \"factorvalues\", XmlAgg( XmlElement( \"factorvalue\", XmlAttributes( i4efvs.factorname, i4efvs.fv_oe, i4efvs.fv_measurement )))) from (select /*+ leading(d) index(d) index(doe) index(tl) index(f) index(fi) index(fv) index(voe) index(m) */ distinct d.t_experiment_id as id, fi.name as factorName, voe.value as FV_OE, m.value as FV_MEASUREMENT from tt_experimentdesign d, tt_ontologyentry doe, tt_types_t_experimentdesign tl, tt_experimentalfactor f, tt_identifiable fi, tt_factorvalue fv, tt_ontologyentry voe, tt_measurement m where doe.id = tl.types_id and tl.t_experimentdesign_id = d.id and f.t_experimentdesign_id (+) = d.id and fv.experimentalfactor_id (+) = f.id and voe.id (+) = fv.value_id and fi.id (+) = f.id and m.id (+) = fv.measurement_id) i4efvs where i4efvs.id = e.id group by i4efvs.id )                , ( select XmlElement( \"miamescores\", XmlAttributes( nvt_miame.value as \"miamescore\" ), XmlAgg( XmlElement( \"miamescore\", XmlAttributes( nvt_miamescores.name as \"name\", nvt_miamescores.value as \"value\" ) ) ) ) from tt_namevaluetype nvt_miamescores, tt_namevaluetype nvt_miame, tt_identifiable i4miame where nvt_miame.id=nvt_miamescores.t_namevaluetype_id and nvt_miame.t_extendable_id=i4miame.id and i4miame.identifier=i.identifier and nvt_miame.name='AEMIAMESCORE' group by nvt_miame.t_extendable_id, nvt_miame.value, i4miame.identifier )" +
        " , ( select XmlElement( \"miamescores\", XmlAttributes( nvt_miame.value as \"miamescore\" ), XmlAgg( XmlElement( \"miamescore\", XmlAttributes( nvt_miamescores.name as \"name\", nvt_miamescores.value as \"value\" ) ) ) ) from tt_namevaluetype nvt_miamescores, tt_namevaluetype nvt_miame, tt_identifiable i4miame where nvt_miame.id=nvt_miamescores.t_namevaluetype_id and nvt_miame.t_extendable_id=i4miame.id and i4miame.identifier=i.identifier and nvt_miame.name='AEMIAMESCORE' group by nvt_miame.t_extendable_id, nvt_miame.value, i4miame.identifier)" +
        " , ( select /*+ index(pba) */ XmlElement( \"arraydesigns\", XmlAgg( XmlElement( \"arraydesign\", XmlAttributes( a.arraydesign_id as \"id\", i4array.identifier as \"identifier\", nvt_array.value as \"name\" , count(a.arraydesign_id) as \"count\" ) ) ) ) from tt_bioassays_t_experiment ea inner join tt_physicalbioassay pba on pba.id = ea.bioassays_id inner join tt_bioassaycreation h on h.id = pba.bioassaycreation_id inner join tt_array a on a.id = h.array_id inner join tt_identifiable i4array on i4array.id = a.arraydesign_id inner join tt_namevaluetype nvt_array on nvt_array.t_extendable_id = a.arraydesign_id and nvt_array.name = 'AEArrayDisplayName' where ea.t_experiment_id = e.id group by a.arraydesign_id, i4array.identifier, nvt_array.value )" +
        " , ( select /*+ leading(i7) index(bad)*/ XmlElement( \"bioassaydatagroups\", XmlAgg( XmlElement( \"bioassaydatagroup\", XmlAttributes( i8.identifier as \"name\", badg.id as \"id\", count(badg.id) as \"num_bad_cubes\", ( select substr( i10.identifier, 3, 4 ) as \"arraydesign\" from tt_arraydesign_bioassaydat abad, tt_identifiable i10 where abad.bioassaydatagroups_id=badg.id and i10.id=abad.arraydesigns_id and rownum = 1 ) as \"arraydesign\", ( select d.dataformat from tt_bioassays_t_bioassayd b, tt_bioassaydata c, tt_biodatacube d, tt_bioassaydat_bioassaydat badbad where b.t_bioassaydimension_id = c.bioassaydimension_id and c.biodatavalues_id = d.id and badbad.bioassaydatas_id = c.id and badbad.bioassaydatagroups_id = badg.id and rownum = 1) as \"dataformat\", ( select count(bbb.bioassays_id) from tt_bioassays_bioassaydat bbb where bbb.bioassaydatagroups_id = badg.id ) as \"bioassay_count\", ( select count(badg.id) from tt_derivedbioassaydata dbad, tt_bioassaydat_bioassaydat bb where bb.bioassaydatagroups_id = badg.id and dbad.id = bb.bioassaydatas_id and rownum = 1) as \"is_derived\" ), ))) from  tt_bioassaydatagroup badg, tt_bioassaydat_bioassaydat bb, tt_bioassaydata bad, tt_identifiable i8 where badg.experiment_id = e.id and bb.bioassaydatagroups_id = badg.id and bad.id = bb.bioassaydatas_id and i8.id = bad.designelementdimension_id group by i8.identifier, badg.id )" +
        " , ( select XmlElement( \"bibliography\", XmlAttributes( trim(db.ACCESSION) as \"accession\", trim(b.publication) AS \"publication\", trim(b.authors) AS \"authors\", trim(b.title) AS \"title\", trim(b.year) AS \"year\", trim(b.volume) AS \"volume\", trim(b.issue) AS \"issue\", trim(b.pages) AS \"pages\", trim(b.uri) AS \"uri\" ) ) FROM tt_bibliographicreference b, tt_description dd, tt_accessions_t_bibliogra ab, tt_databaseentry db WHERE b.t_description_id=dd.id AND dd.t_describable_id=e.id AND ab.T_BIBLIOGRAPHICREFERENCE_ID(+)=b.id AND db.id (+)= ab.ACCESSIONS_ID and rownum=1)" +
        " , ( select distinct XmlElement ( \"providers\", XmlAgg ( XmlElement ( \"provider\", XmlAttributes ( pp.firstname || ' ' || pp.lastname AS \"contact\", c.email AS \"email\", value AS \"role\" ) ) ) ) FROM tt_identifiable ii, tt_ontologyentry o, tt_providers_t_experiment p, tt_roles_t_contact r, tt_person pp, tt_contact c WHERE c.id = r.t_contact_id AND ii.id = r.T_CONTACT_ID AND r.ROLES_ID = o.ID AND pp.id = ii.id AND ii.id = p.PROVIDERS_ID AND p.T_EXPERIMENT_ID = e.id )" +
        " , ( select /*+ index(ed) */ distinct XmlElement ( \"experimentdesigns\", XmlAgg( XmlElement ( \"experimentdesign\", XmlAttributes ( translate(replace(oe.value,'_design',''),'_',' ') as \"type\" ) ) ) ) FROM tt_experimentdesign ed, tt_types_t_experimentdesign tte, tt_ontologyentry oe WHERE ed.t_experiment_id = e.id AND tte.t_experimentdesign_id = ed.id AND oe.id = tte.types_id AND oe.CATEGORY = 'ExperimentDesignType' )" +
        " , XmlAgg( XmlElement( \"description\", XmlAttributes( d.id AS \"id\" ), d.text ) ) " +
        " ).getClobVal() as xml" +
        " from tt_experiment e" +
        "  left outer join tt_description d on d.t_describable_id = e.id" +
        "  left outer join tt_identifiable i on i.id = e.id" +
        "  left outer join tt_namevaluetype nvt_releasedate on ( nvt_releasedate.t_extendable_id = e.id and nvt_releasedate.name = 'ArrayExpressLoadDate' )" +
        "  left outer join tt_namevaluetype nvt_name on ( nvt_name.t_extendable_id = e.id and nvt_name.name = 'AEExperimentDisplayName' )" +
        "  left outer join tt_namevaluetype nvt_miamegold on ( nvt_miamegold.t_extendable_id=e.id and nvt_miamegold.name='AEMIAMEGOLD' )" +
        " where" +
        "  e.id = ?" +
        " group by" +
        "  e.id" +
        "  , i.identifier" +
        "  , nvt_name.value" +
        "  , nvt_releasedate.value" +
        "  , nvt_miamegold.value";

    public GetExperimentXmlThread( DataSource ds, int index )
    {
        super("GetExperimentXmlThread[" + Integer.toString(index) + "]");
        dataSource = ds;
    }

    public static String ClobToString(Clob cl) throws IOException, SQLException
    {
        if (cl == null)
            return null;

        StringBuffer strOut = new StringBuffer();
        String aux;

	    // We access to stream, as this way we don't have to use the CLOB.length() which is slower...
    	BufferedReader br = new BufferedReader(cl.getCharacterStream());

        while ((aux=br.readLine())!=null)
            strOut.append(aux);

        return strOut.toString();
    }

    public void run()
    {
        // initializing the thread's internals
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(getExperimentXmlSql);

            // loop the thread till we want it retired
            while (!isRetired) {

                // wait till id is set
                while (!isReady && !isRetired) {
                    Thread.sleep(250);
                }

                if (!isRetired) {
                    try {
                        setReady(false);
                        stmt.setInt( 1, experimentId );
                        ResultSet rs = stmt.executeQuery();

                        if ( rs.next() ) {
                            Clob xmlClob = rs.getClob(1);
                            result = ClobToString(xmlClob);
                        }
                        rs.close();
                    } catch ( SQLException x) {
                        log.error( "GetExperimentXmlThread.run SQLException: " + x.getMessage() );
                        result = null;
                    }
                    setDone(true);
                }
            }
        }
        catch(Exception x) {
            log.error( "GetExperimentXmlThread.run Exception: " + x.getMessage() );
        }

        if ( null != conn ) {
            try {
                conn.close();
            } catch (SQLException x) {
                log.error( "GetExperimentXmlThread.run (conn.close) SQLException: " + x.getMessage() );
            }
        }
    }

    public void setId( int id )
    {
//      log.info("setId: " + Integer.toString(id));
        experimentId = id;
        isNew = false;
        setDone(false);
        setReady(true);
    }

    public void retire()
    {
        isRetired = true;
    }

    public boolean isNew()
    {
        return isNew;
    }

    public boolean isDone()
    {
        return isDone;
    }

    public String getXml()
    {
//      log.info("getResult: " + result.substring(0, 50) + "...");
        return result;
    }

    private synchronized void setReady( boolean ready )
    {
        isReady = ready;
    }

    private synchronized void setDone( boolean done )
    {
        isDone = done;
    }
}

class experimentsre {
    private final static Log log = org.apache.commons.logging.LogFactory.getLog( experimentsre.class );

    private static String getExperimentsSql = "select distinct e.id, i.identifier as accession, case when v.user_id = 1 then 1 else 0 end as \"public\"" +
            " from tt_experiment e left outer join tt_identifiable i on i.id = e.id" +
            "  left outer join tt_extendable ext on ext.id = e.id" +
            "  left outer join pl_visibility v on v.label_id = ext.label_id" +
            " order by" +
            "  i.identifier asc";

    private static int numOfParallelConnections = 50;
    private static int initialXmlStringBufferSize = 10485760;  // 10 Mb

    private DocumentBuilder xmlDocumentBuilder = null;

    public experimentsre() {
        try {
            xmlDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        } catch (ParserConfigurationException x) {
            log.error( "experiments.ctor exception - " + x.getMessage() );
        }
    }

    private DataSource getJdbcDataSource( String dataSourceName )
    {
        DataSource dataSource = null;

        try {
            Context initContext = new InitialContext();
            Context envContext = (Context)initContext.lookup( "java:/comp/env" );

            dataSource = (DataSource)envContext.lookup( "jdbc/" + dataSourceName.toUpperCase() );
        } catch ( NamingException x ) {
            log.error( "getJdbcDataSource NamingException: " + x.getMessage() );
        }

        return dataSource;
    }

    private Document XmlStringToDomDocument( String xmlString ) {

        Document doc = null;

        try {
            InputSource inStream = new InputSource();
            inStream.setCharacterStream( new StringReader(xmlString) );
            doc = xmlDocumentBuilder.parse(inStream);
        } catch ( Exception x ) {
            log.error( "StringToXmlElement Exception: " + x.getMessage() );
        }

        return doc;
    }

/*
    private static String EscapeEntitiesInXmlString( String str )
    {
        StringBuffer buf = new StringBuffer(str.length() * 2);
        for ( int i = 0; i < str.length(); ++i ) {
            char ch = str.charAt(i);
            if ( (((int)ch) >= 0x10 && ((int)ch <= 0x1f)) || ((int)ch) == 0x7f  ) {
                // control -> skip this character
//          } else if ( ((int)ch) > 0x7F ) {
//              buf.append("&#" + Integer.toString((int)ch) + ";");
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }
*/

    public Document loadExperimentsFromDataSource( String dataSourceName, boolean onlyPublic )
    {
        log.info("ArrayExpress Repository XML reload started.");

        StringBuffer xmlBuf = new StringBuffer(initialXmlStringBufferSize);
        xmlBuf.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><experiments>");

        DataSource ds = getJdbcDataSource( dataSourceName );
        if ( null != ds ) {

            try {
                // get a list of experiments populated into expList
                List<ExperimentListEntry> expList = new ArrayList<ExperimentListEntry>();

                Connection conn = ds.getConnection();

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(getExperimentsSql);

                while ( rs.next() ) {
                    ExperimentListEntry entry = new ExperimentListEntry(
                            rs.getInt("id")
                            , rs.getString("accession")
                            , rs.getBoolean("public")
                    );
                    expList.add(entry);
                }

                rs.close();
                conn.close();

                //
                int poolSize = Math.min( expList.size(), numOfParallelConnections );

                // so we create a pool of threads :)
                List<GetExperimentXmlThread> threadPool = new ArrayList<GetExperimentXmlThread>();
                for ( int i = 0; i < poolSize; i++ ) {
                    GetExperimentXmlThread th = new GetExperimentXmlThread( ds, i );
                    th.start();
                    threadPool.add(th);
                }

                int expListIndex = 0;
                int threadPoolIndex = 0;
                int expCount = 0;


                while ( expListIndex < expList.size() ) {
                    if ( onlyPublic && !expList.get(expListIndex).isPublic ) {
                        // skipping this private experiment
                        expListIndex++;
                    } else {
                        GetExperimentXmlThread th = threadPool.get(threadPoolIndex);
                        if (th.isNew()) { // this thread is new - load it with work, fast!
                            th.setId(expList.get(expListIndex).id);
                            expListIndex++;
                        } else if (th.isDone()) { // this thread has done its job, relaod it with more work :)
                            xmlBuf.append(th.getXml());
                            expCount++; // increment number of resulting experiments
                            th.setId(expList.get(expListIndex).id);
                            expListIndex++;
                        }
                        threadPoolIndex++;
                        if (threadPoolIndex == threadPool.size())
                            threadPoolIndex = 0;

                        Thread.yield();
                    }
                }

                threadPoolIndex = 0;
                while ( threadPool.size() > 0 ) {
                    GetExperimentXmlThread th = threadPool.get(threadPoolIndex);
                    if (th.isDone()) {
                        xmlBuf.append(th.getXml());
                        expCount++; // increment number of resulting experiments
                        threadPool.remove(threadPoolIndex);
                        th.retire();
                    }
                    threadPoolIndex++;
                    if (threadPoolIndex >= threadPool.size())
                        threadPoolIndex = 0;

                    Thread.yield();
                }
                log.info("ArrayExpress Repository XML reload completed (" + expCount + "/" + expList.size() + " experiments loaded).");

            } catch ( SQLException x ) {
                log.error( "loadExperimentsFromDataSource SQLException: " + x.getMessage() );
            }

        } else {
            // well, something went terribly wrong, and we're not doing shit here

        }

        xmlBuf.append("</experiments>");

//      String xml = EscapeEntitiesInXmlString(xmlBuf.toString());
        String xml = xmlBuf.toString().replaceAll("[^\\p{Print}]"," ");    // replace is a nasty hack to get rid of non-printable characters altogether

//      try {
//          FileWriter fw = new FileWriter("/tmp/_xml");
//          fw.write(xml);
//          fw.flush();
//          fw.close();
//      } catch (IOException x) {
//          x.printStackTrace();
//      }

        return XmlStringToDomDocument(xml);
    }

 /*
    public void dumpExperimentsXmlToDisk( Object xml, String location, String ds )
    {
        try {
            // add db info to the resulting xml
            Document _xml = (Document)xml;
            Element exps = _xml.getDocumentElement();
            exps.setAttribute("ds", ds);

            OutputFormat format = new OutputFormat();
            format.setLineSeparator(LineSeparator.Web);
            format.setIndenting(true);
            format.setLineWidth(0);
            format.setPreserveSpace(true);
            FileWriter fw = new FileWriter(location);
            XMLSerializer serializer = new XMLSerializer(fw, format);
            serializer.asDOMSerializer();
            serializer.serialize(_xml);
            fw.flush();
            fw.close();
        } catch (IOException x) {
            log.error( "dumpExperimentsXmlToDisk IOException: " + x.getMessage() );
        }
    }
*/
}

public class Experiments {

    // logging macinery
    private static final Log log = org.apache.commons.logging.LogFactory.getLog(Experiments.class);

    public Experiments()
    {
        experimentsXmlCacheLocation = getCachedXmlLocation();
    }

    public Document getExperiments()
    {
        if ( null == experimentsDoc ) {
            if ( !loadExperimentsFromCache() ) {
                log.warn("Experiments XML Cache was NOT loaded, fresh start up possible");
                experimentsDoc = createExperimentsDocument();
            }

        }
        return experimentsDoc;
    }

    //  returns the location of xml cache ({java.io.tmpdir}/{ae.experiments.cache.location})
    private URL getCachedXmlLocation()
    {
        URL url = null;

        String tmpDir = System.getProperty("java.io.tmpdir");

        if ( null != tmpDir ) {
            String relCacheLocation = Application.Preferences().getProperty("ae.experiments.cache.location");
            if ( null != relCacheLocation ) {
                try {
                    url = new File( tmpDir + "/" + relCacheLocation ).toURL();
                } catch ( MalformedURLException e ) {
                    log.error( "Caught an exception while attempting to form a URL for XML cache. tmpDir: " + tmpDir + ", relCacheLocation: " + relCacheLocation + ", message: " + e.getMessage() );
                }
            }
        }

        if ( null == url ) {
            log.error("Experiments XML cache location was not determined, expect problems down the road");
        }

        return url;
    }

    private boolean loadExperimentsFromCache()
    {
        boolean isLoaded = false;

        if ( null != experimentsXmlCacheLocation ) {
            Document doc = null;
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                //Using factory get an instance of document builder
                DocumentBuilder db = dbf.newDocumentBuilder();

                //parse using builder to get DOM representation of the XML file
                doc = db.parse(experimentsXmlCacheLocation.toURI().toString());

                if ( null != doc ) {
                    experimentsDoc = doc;
                    isLoaded = true;
                }
            } catch ( Throwable e ) {
                log.debug( "Caught an exception:", e );
            }
        }

        return isLoaded;
    }


    private Document createExperimentsDocument()
    {
        Document doc = null;

            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                //Using factory get an instance of document builder
                DocumentBuilder db = dbf.newDocumentBuilder();

                doc = db.newDocument();

                Element expElement = doc.createElement("experiments");
                expElement.setAttribute( "total", "0" );

                doc.appendChild(expElement);

            } catch ( Throwable e ) {
                log.debug( "Caught an exception:", e );
            }

        if ( null == doc ) {
            log.error("Experiments Document WAS NOT created, expect problems down the road");
        }
        
        return doc;
    }
    
    // url that represents the location of xml file (for persistence)
    private URL experimentsXmlCacheLocation = null;

    //
    private Document experimentsDoc = null;
}
