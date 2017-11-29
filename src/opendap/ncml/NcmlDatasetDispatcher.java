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
package opendap.ncml;

import opendap.bes.BesDapDispatcher;
import opendap.bes.PathInfo;
import opendap.bes.dap4Responders.Dap4Responder;
import opendap.coreServlet.HttpResponder;
import opendap.coreServlet.ReqInfo;
import opendap.coreServlet.RequestCache;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ncml Dataset handler responses for Hyrax. Massages NcML content retrieved from the BES
 * path names are consistent with the paths from the users perspective.
 *
 */
public class NcmlDatasetDispatcher extends BesDapDispatcher {



    private Logger log;
    private boolean initialized;

    private Element _config;


    public NcmlDatasetDispatcher(){
        //super();
        log = LoggerFactory.getLogger(getClass());
        initialized = false;
    }


    @Override
    public void init(HttpServlet servlet,Element config) throws Exception {

        if(initialized)
            return;

        NcmlDatasetBesApi besApi = new NcmlDatasetBesApi();


        ingestConfig(config);

        init(servlet, config ,besApi);

        initialized = true;

    }


    private void ingestConfig(Element config){

        _config = config;
        Element PreloadNcmlIntoBes = _config.getChild("PreloadNcmlIntoBes");
        if(PreloadNcmlIntoBes!=null){
            String value = PreloadNcmlIntoBes.getTextNormalize();
            if(value.equalsIgnoreCase("true"))
                NcmlManager.setPreloadBes(true);

            log.debug("Pre-Load Catalog NcML into BES: ",NcmlManager.preloadBes());

        }
    }


    @Override
    public boolean requestCanBeHandled(HttpServletRequest request, PathInfo pi)
            throws Exception {
        return requestDispatch(request,pi,null, false);
    }


    @Override
    public boolean requestDispatch(HttpServletRequest request,
                                   PathInfo pi,
                                   HttpServletResponse response,
                                   boolean sendResponse)
            throws Exception {

        String relativeUrl = ReqInfo.getLocalUrl(request);
        log.debug("The client requested this resource: {}",relativeUrl);
        for (HttpResponder r : getResponders()) {
            log.debug("Checking responder: "+ r.getClass().getSimpleName()+ " (pathPrefix: "+r.getPathPrefix()+")");
            String candidateDataSourceId = getBesApi().getBesDataSourceID(relativeUrl,r.getRequestSuffixMatchPattern());
            if(candidateDataSourceId!=null) {
                String remainder = relativeUrl.substring(candidateDataSourceId.length());
                if (NcmlManager.isNcmlDataset(candidateDataSourceId)) {
                    log.info("The candidateDataSourceId: \"{}\" is an NcmlDataset.", candidateDataSourceId);
                    Pattern p = r.getRequestSuffixMatchPattern();
                    Matcher m = p.matcher(remainder);
                    if (m.matches()) {
                        if (sendResponse) {
                            if (!response.containsHeader("Last-Modified")) {
                                log.debug("requestDispatch() - Last-Modified header has not been set. Setting...");
                                long lmt = NcmlManager.getLastModified(candidateDataSourceId);
                                SimpleDateFormat httpDateFormat = new SimpleDateFormat(Dap4Responder.HttpDatFormatString);
                                response.setHeader("Last-Modified", httpDateFormat.format(lmt));
                                log.debug("requestDispatch() - Last-Modified: {}", httpDateFormat.format(lmt));
                            }
                            r.respondToHttpGetRequest(request, response);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }







}
