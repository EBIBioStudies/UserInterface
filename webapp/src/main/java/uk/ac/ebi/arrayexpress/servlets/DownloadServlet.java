package uk.ac.ebi.arrayexpress.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationServlet;
import uk.ac.ebi.arrayexpress.components.DownloadableFilesRegistry;
import uk.ac.ebi.arrayexpress.components.Experiments;
import uk.ac.ebi.arrayexpress.components.Users;
import uk.ac.ebi.arrayexpress.utils.CookieMap;
import uk.ac.ebi.arrayexpress.utils.RegExpHelper;
import uk.ac.ebi.arrayexpress.utils.files.FtpFileEntry;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DownloadServlet extends ApplicationServlet
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // buffer size
    private final int TRANSFER_BUFFER_SIZE = 8 * 1024 * 1024;

    // Respond to HTTP GET requests from browsers.
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        logRequest(logger, request);

        String accession = null;
        String name = null;
        String[] requestArgs = new RegExpHelper("servlets/download/([^/]+)/?([^/]*)", "i")
                .match(request.getRequestURL().toString());
        if (null != requestArgs) {
            if (requestArgs[1].equals("")) {
                name = requestArgs[0]; // old-style
            } else {
                accession = requestArgs[0];
                name = requestArgs[1];
            }
        }
        try {
            if (null != name) {
                sendFile(accession, name, request, response);
            } else {
                logger.error("Unable to get a filename from [{}]", request.getRequestURL());
                throw (new Exception());
            }
        } catch ( Throwable x ) {
            if (x.getClass().getName().equals("org.apache.catalina.connector.ClientAbortException")) {
                // generate log entry for client abortion
                logger.warn("Download aborted");
            } else {
                logger.debug("Caught an exception:", x);
                if (!response.isCommitted())
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    private void sendFile( String accession, String name, HttpServletRequest request, HttpServletResponse response ) throws IOException
    {
        logger.info("Requested download of [{}], accession[{}]", name, accession);
        DownloadableFilesRegistry filesRegistry = (DownloadableFilesRegistry) getComponent("DownloadableFilesRegistry");
        Experiments experiments = (Experiments) getComponent("Experiments");

        String userId = "1";
        CookieMap cookies = new CookieMap(request.getCookies());
        if (cookies.containsKey("AeLoggedUser") && cookies.containsKey("AeLoginToken")) {
            Users users = (Users) getComponent("Users");
            String user = cookies.get("AeLoggedUser").getValue();
            String passwordHash = cookies.get("AeLoginToken").getValue();
            if (users.verifyLogin(user, passwordHash, request.getRemoteAddr().concat(request.getHeader("User-Agent")))) {
                userId = String.valueOf(users.getUserRecord(user).getId());
            } else {
                logger.warn("Removing invalid session cookie for user [{}]", user);
                // resetting cookies
                Cookie userCookie = new Cookie("AeLoggedUser", "");
                userCookie.setPath("/");
                userCookie.setMaxAge(0);

                response.addCookie(userCookie);
            }

        }


        if (!filesRegistry.doesExist(accession, name)) {
            logger.error("File [{}], accession [{}] is not in files registry", name, accession);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            String fileLocation = filesRegistry.getLocation(accession, name);
            String contentType = getServletContext().getMimeType(fileLocation);
            if (null != contentType) {
                logger.debug("Setting content type to [{}]", contentType);
                response.setContentType(contentType);
            } else {
                logger.warn("Download servlet was unable to determine content type for [{}]", fileLocation);
            }

            logger.debug("Checking file [{}]", fileLocation);
            File file = new File(fileLocation);
            if (!file.exists()) {
                logger.error("File [{}] does not exist", fileLocation);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else if (!experiments.isAccessible(FtpFileEntry.getAccession(new FtpFileEntry(fileLocation, null, null)), userId)) {
                logger.error("Attempting to download file for the experiment that is not present in the index");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } else {
                FileInputStream fileInputStream = null;
                ServletOutputStream servletOutStream = null;
                try {
                    fileInputStream = new FileInputStream(file);
                    long size = file.length();
                    response.addHeader("Content-Length", Long.toString(size));

                    servletOutStream = response.getOutputStream();

                    int bytesRead;
                    byte[] buffer = new byte[TRANSFER_BUFFER_SIZE];

                    while ( true ) {
                        bytesRead = fileInputStream.read(buffer, 0, TRANSFER_BUFFER_SIZE);
                        if (bytesRead == -1) break;
                        servletOutStream.write(buffer, 0, bytesRead);
                        servletOutStream.flush();
                    }
                    logger.info("Download of [{}] completed, sent [{}] bytes", name, size);
                } finally {
                    if (null != fileInputStream)
                        fileInputStream.close();
                    if (null != servletOutStream)
                        servletOutStream.close();
                }
            }
        }
    }
}