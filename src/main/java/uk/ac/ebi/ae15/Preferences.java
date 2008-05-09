package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Properties;
import java.io.InputStream;

// class Preferences
//
// reads all configuration properties from /WEB-INF/classes/arrayexpress.properties

public class Preferences extends Properties {

    // logging macinery
    private static final Log log = org.apache.commons.logging.LogFactory.getLog(Preferences.class);

    private static final String preferencesFile = "arrayexpress.properties";

    public Preferences() {
    }

    public void load() {
        log.debug("Preferences.load() called");

        URL location = getResourceLocation(preferencesFile);
        if ( null != location ) {
            try {
                InputStream is = location.openStream();
                super.load(is);
            } catch ( Throwable e ) {
                log.debug( "Caught an exception:", e );
            }
        } else {
            log.error("Unable to locate ArrayExpress preferences file (/WEB-INF/classes/arrayexpress.properties)");
        }
    }

    // stolen from apache log4j

    private static URL getResourceLocation( String resource ) {

        URL url = null;

        try {
            ClassLoader classLoader = getThreadClassLoader();
  	        if(classLoader != null) {
                log.debug( "Trying to find " + resource + " using context classloader " + classLoader );
                url = classLoader.getResource(resource);
            }
        } catch ( Throwable e ) {
            log.debug( "Caught an exception:", e );
        }

        if ( null == url ) {
            log.debug( "Trying to find " + resource + " using ClassLoader.getSystemResource()" );
            url = ClassLoader.getSystemResource(resource);
        }

        if ( null == url ) {
            log.debug( "Still could not find " + resource + ", expect problems down the road" );
        }
        
        return url;
    }

    private static ClassLoader getThreadClassLoader() throws IllegalAccessException, InvocationTargetException {

        Method method;

        try {
            method = Thread.class.getMethod( "getContextClassLoader" );
        } catch ( NoSuchMethodException e ) {
            log.debug( "Caught an exception:", e );
            return null;
        }

        return (ClassLoader)method.invoke( Thread.currentThread() );
    }
}
