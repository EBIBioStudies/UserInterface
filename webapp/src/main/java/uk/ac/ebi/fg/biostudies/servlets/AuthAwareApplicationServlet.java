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

package uk.ac.ebi.fg.biostudies.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fg.biostudies.app.ApplicationServlet;
import uk.ac.ebi.fg.biostudies.components.Users;
import uk.ac.ebi.fg.biostudies.utils.CookieMap;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AuthAwareApplicationServlet extends ApplicationServlet
{
    private static final long serialVersionUID = -82727624065665432L;

    private transient final Logger logger = LoggerFactory.getLogger(getClass());

    private final static String AE_LOGIN_USER_COOKIE = "AeLoggedUser";
    private final static String AE_LOGIN_TOKEN_COOKIE = "AeLoginToken";

    private final static List<String> AE_PUBLIC_ACCESS = Arrays.asList("1");
    private final static List<String> AE_UNRESTRICTED_ACCESS = new ArrayList<String>();

    private static class AuthApplicationServletException extends ServletException
    {
        private static final long serialVersionUID = 1030249369830812548L;

        public AuthApplicationServletException( Throwable x )
        {
            super(x);
        }
    }

    protected abstract void doAuthenticatedRequest(
            HttpServletRequest request
            , HttpServletResponse response
            , RequestType requestType
            , String authUserName
            ) throws ServletException, IOException;

    protected void doRequest( HttpServletRequest request, HttpServletResponse response, RequestType requestType )
            throws ServletException, IOException
    {
        if (!checkAuthCookies(request)) {
            invalidateAuthCookies(response);
        }
        String authUserName = getAuthUserName(request);

        /* TODO: relocate id somewhere
        if (logger.isDebugEnabled()) {
            Enumeration headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
               String headerName = (String)headerNames.nextElement();
               logger.debug("Header [{}], value [{}]", headerName, request.getHeader(headerName));
            }
        }
        */

        // TODO: this is so hacky, I cannot believe I'm commiting this
        if (null != authUserName) {
            String host = request.getHeader("host");

            if (null != host && host.matches("www(dev)?[.]ebi[.]ac[.]uk")) {

                if (!"1".equals(request.getHeader("x-https"))) {

                    String requestUri =  request.getParameter("original-request-uri");

                    if (null != requestUri) {
                        String redirectUrl = "https://" + host + requestUri;
                        logger.info("Redirecting authenticated request to [{}]", redirectUrl);
                        response.sendRedirect(redirectUrl);
                        return;
                    }
                }
            }
        }
        doAuthenticatedRequest(request, response, requestType, authUserName);
    }

    private boolean checkAuthCookies( HttpServletRequest request ) throws ServletException
    {
        try {
            CookieMap cookies = new CookieMap(request.getCookies());
            String userName = cookies.getCookieValue(AE_LOGIN_USER_COOKIE);
            if (null != userName) {
                userName = URLDecoder.decode(userName, "UTF-8");
            }
            String token = cookies.getCookieValue(AE_LOGIN_TOKEN_COOKIE);
            String userAgent = request.getHeader("User-Agent");
            Users users = (Users) getComponent("Users");
            return users.verifyLogin(
                    userName
                    , token
                    , request.getRemoteAddr().concat(
                        userAgent != null ? userAgent : "unknown"
                    )
            );
        } catch (Exception x) {
            throw new AuthApplicationServletException(x);
        }
    }

    private void invalidateAuthCookies( HttpServletResponse response )
    {
        // deleting user cookie
        Cookie userCookie = new Cookie(AE_LOGIN_USER_COOKIE, "");
        userCookie.setPath("/");
        userCookie.setMaxAge(0);

        response.addCookie(userCookie);
    }

    protected String getAuthUserName( HttpServletRequest request ) throws ServletException
    {
        if (checkAuthCookies(request)) {
            try {
                String userName = new CookieMap(request.getCookies()).getCookieValue(AE_LOGIN_USER_COOKIE);
                if (null != userName) {
                    return URLDecoder.decode(userName, "UTF-8");
                }
            } catch (Exception x) {
                throw new AuthApplicationServletException(x);
            }
        }
        return null;
    }

    protected List<String> getUserIds( String userName ) throws ServletException
    {
        if (null == userName) {
            return AE_PUBLIC_ACCESS;
        }
        try {
            userName = URLDecoder.decode(userName, "UTF-8");
            Users users = (Users) getComponent("Users");

            if (users.isPrivilegedByName(userName)) {
                return AE_UNRESTRICTED_ACCESS;
            } else {
                List<String> userIds = users.getUserIDs(userName);
                // so we allow public access as well
                userIds.addAll(AE_PUBLIC_ACCESS);
                return userIds;
            }
        } catch (Exception x) {
           throw new AuthApplicationServletException(x);
        }
    }
}
