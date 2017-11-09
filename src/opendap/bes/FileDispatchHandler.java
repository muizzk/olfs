/*
 * /////////////////////////////////////////////////////////////////////////////
 * // This file is part of the "Hyrax Data Server" project.
 * //
 * //
 * // Copyright (c) 2013 OPeNDAP, Inc.
 * // Author: Nathan David Potter  <ndp@opendap.org>
 * //
 * // This library is free software; you can redistribute it and/or
 * // modify it under the terms of the GNU Lesser General Public
 * // License as published by the Free Software Foundation; either
 * // version 2.1 of the License, or (at your option) any later version.
 * //
 * // This library is distributed in the hope that it will be useful,
 * // but WITHOUT ANY WARRANTY; without even the implied warranty of
 * // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * // Lesser General Public License for more details.
 * //
 * // You should have received a copy of the GNU Lesser General Public
 * // License along with this library; if not, write to the Free Software
 * // Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 * //
 * // You can contact OPeNDAP, Inc. at PO Box 112, Saunderstown, RI. 02874-0112.
 * /////////////////////////////////////////////////////////////////////////////
 */

package opendap.bes;

import opendap.bes.dap2Responders.BesApi;
import opendap.bes.dap4Responders.MediaType;
import opendap.coreServlet.*;
import opendap.dap.Request;
import opendap.http.error.Forbidden;
import opendap.io.HyraxStringEncoding;
import org.jdom.Element;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Provides access to files held in the BES that the BES does not recognize as data.
 *
 */
public class FileDispatchHandler implements DispatchHandler {

    private org.slf4j.Logger log;
    //private static boolean allowDirectDataSourceAccess = false;
    private boolean initialized;

    private BesApi _besApi;

    public FileDispatchHandler() {
        log = org.slf4j.LoggerFactory.getLogger(getClass());
        initialized = false;

    }



    public void init(HttpServlet servlet,Element config) throws Exception {

        if(initialized) return;

        _besApi = new BesApi();


        initialized = true;

    }

    @Override
    public boolean requestCanBeHandled(HttpServletRequest request, PathInfo pi)
            throws Exception {

        return !pi.remainder().isEmpty() || !pi.isFile();
    }



    /*
    public void handleRequest(HttpServletRequest request,
                              HttpServletResponse response)
            throws Exception {
        String relativeUrl = ReqInfo.getLocalUrl(request);
        PathInfo pi = Squeak.besGetPathInfo(relativeUrl);
        handleRequest(request,pi,response);
    }
    */

    @Override
    public void handleRequest(HttpServletRequest request,
                              PathInfo pi,
                              HttpServletResponse response)
            throws Exception {

        if(pi.isAccessible()){
            if(!pi.isData()){
                sendFile(request, response);
            }else {
                throw new Forbidden("Datasets may not be accessed directly.");
            }
        }
        else {
            throw new Forbidden("You do not have permission to access the requested resource.");
        }

    }


    /*
    public long getLastModified(HttpServletRequest req) {

        String name = ReqInfo.getLocalUrl(req);

        log.debug("getLastModified(): Tomcat requesting getlastModified() for collection: " + name );


        try {
            ResourceInfo dsi = new BESResource(name,_besApi);
            log.debug("getLastModified(): Returning: " + new Date(dsi.lastModified()));

            return dsi.lastModified();
        }
        catch (Exception e) {
            log.debug("getLastModified(): Returning: -1");
            return -1;
        }


    }
   */


    @Override
    public long getLastModified(PathInfo pi) {

        return pi.lastModified().getTime();
    }



    @Override
    public void destroy() {
        log.info("Destroy complete.");

    }


    public void sendFile(HttpServletRequest req,
                         HttpServletResponse response)
            throws Exception {


        String name = ReqInfo.getLocalUrl(req);


        log.debug("sendFile(): Sending file \"" + name + "\"");

        String downloadFileName = Scrub.fileName(name.substring(name.lastIndexOf("/")+1));

        log.debug("sendFile() downloadFileName: " + downloadFileName );

        // I commented these two lines  out because it was incorrectly causing browsers to downloadJobOutput
        // (as opposed to display) EVERY file retrieved.
        //String contentDisposition = " attachment; filename=\"" +downloadFileName+"\"";
        //response.setHeader("Content-Disposition",contentDisposition);

        String suffix = ReqInfo.getRequestSuffix(req);

        if (suffix != null) {
            MediaType responseMediaType = MimeTypes.getMediaType(suffix);
            if (responseMediaType != null) {
                response.setContentType(responseMediaType.getMimeType());
                log.debug("sendFile() - MIME type: " + responseMediaType.getMimeType() + "  ");
            }
        }


        ServletOutputStream sos = response.getOutputStream();
        _besApi.writeFile(name, sos);

        sos.flush();
    }



}
