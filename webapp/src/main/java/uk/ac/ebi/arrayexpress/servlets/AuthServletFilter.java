package uk.ac.ebi.arrayexpress.servlets;

import uk.ac.ebi.arrayexpress.utils.CookieMap;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;


public class AuthServletFilter implements Filter
{
    protected FilterConfig filterConfig;

    public void init( FilterConfig filterConfig ) throws ServletException
    {
        this.filterConfig = filterConfig;
    }

    public void destroy()
    {
        this.filterConfig = null;
    }

    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws java.io.IOException, ServletException
    {
        doAuthCheck(request);

        chain.doFilter(request, response);
    }

    private void doAuthCheck( ServletRequest request )
    {
        HttpServletRequest httpRequest = (HttpServletRequest)request;

        CookieMap cookies = new CookieMap(httpRequest.getCookies());
    }
}