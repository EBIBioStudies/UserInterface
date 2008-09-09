package uk.ac.ebi.ae15.utils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class ParameterMap extends HashMap<String,String>
{
    public ParameterMap( HttpServletRequest request )
    {
        if (null != request) {
            Map params = request.getParameterMap();
            for ( Object param : params.entrySet() ) {
                Map.Entry p = (Map.Entry) param;
                this.put((String) p.getKey(), arrayToString((String[]) p.getValue()));
            }
        }
    }

    private static String arrayToString( String[] array )
    {
        StringBuilder sb = new StringBuilder();
        for ( String item : array ) {
            sb.append(item).append(' ');
        }
        return sb.toString().trim();
    }
}
