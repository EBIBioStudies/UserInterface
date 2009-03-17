package uk.ac.ebi.arrayexpress.utils;

import javax.servlet.http.Cookie;
import java.util.HashMap;

public class CookieMap extends HashMap<String, Cookie>
{
    public CookieMap( Cookie[] cookies )
    {
        if ( null != cookies ) {
            for ( Cookie c : cookies ) {
                this.put(c.getName(), c);
            }
        }
    }
}
