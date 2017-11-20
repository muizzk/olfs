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

package opendap.gateway;

import opendap.bes.BesDapDispatcher;
import opendap.bes.PathInfo;
import opendap.coreServlet.ReqInfo;
import opendap.coreServlet.RequestCache;
import opendap.coreServlet.Util;
import org.jdom.Element;
import org.slf4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: ndp
 * Date: 2/15/13
 * Time: 10:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class DispatchHandler extends BesDapDispatcher {


    private Logger log;
    private boolean _initialized;
    private String _prefix = "gateway/";

    // private Element _config;

    private Vector<String> trustedHosts;


    private BesGatewayApi _besApi;


    private GatewayForm _gatewayForm;


    public DispatchHandler() {

        super();
        log = org.slf4j.LoggerFactory.getLogger(getClass());
        trustedHosts = new Vector<String>();
        _initialized = false;
        _besApi = null;


    }

    @Override
    public void init(HttpServlet servlet, Element config) throws Exception {

        ingestPrefix(config);

        _besApi = new BesGatewayApi(_prefix);
        init(servlet, config, _besApi);
        _gatewayForm  =  new GatewayForm(getSystemPath(), _prefix);
    }


    public BesGatewayApi getBesGatewayApi(){
        return _besApi;
    }


    @Override
    public boolean requestCanBeHandled(HttpServletRequest request, PathInfo pi){
        boolean isMyRequest = false;

        String relativeURL = pi.path();
        log.debug("relativeURL:    "+relativeURL);
        if (relativeURL != null) {
            while(relativeURL.startsWith("/") && relativeURL.length()>1)
                relativeURL = relativeURL.substring(1,relativeURL.length());

            boolean itsJustThePrefixWithoutTheSlash =
                    _prefix.substring(0,_prefix.lastIndexOf("/")).equals(relativeURL);
            
            if (relativeURL.startsWith(_prefix) || itsJustThePrefixWithoutTheSlash ) {
                isMyRequest = true;
            }
        }
        return isMyRequest;
    }

    @Override
    public void handleRequest(
            HttpServletRequest request,
            PathInfo pi,
            HttpServletResponse response)
        throws Exception {

        log.debug("handleRequest() - pi is an instance of {}",pi.getClass().getName());

        log.info("handleRequest() - Sending Gateway Response");
        String relativeURL = pi.path();

        log.debug("handleRequest() - relativeURL:    "+relativeURL);
        if (relativeURL != null) {
            while (relativeURL.startsWith("/") && relativeURL.length() > 1)
                relativeURL = relativeURL.substring(1, relativeURL.length());

            boolean itsJustThePrefixWithoutTheSlash =
                    _prefix.substring(0, _prefix.lastIndexOf("/")).equals(relativeURL);

            boolean itsJustThePrefix = _prefix.equals(relativeURL);

            if (itsJustThePrefixWithoutTheSlash) {
                response.sendRedirect(_prefix);
                log.debug("handleRequest() - Sent redirect to service prefix: " + _prefix);
            } else if (itsJustThePrefix) {

                _gatewayForm.respondToHttpGetRequest(request, response);
                log.info("handleRequest() - Sent Gateway Access Form");

            } else {
                GatewayPathInfo gpi = new GatewayPathInfo( pi.path(), _besApi);
                if (!super.requestDispatch(request, gpi, response, true) && !response.isCommitted()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unable to locate requested resource.");
                    log.info("handleRequest() - Sent 404 Response.");
                } else
                    log.info("handleRequest() - Sent DAP Gateway Response.");
            }
        }

    }


    public GatewayPathInfo getGateWayPathInfo(HttpServletRequest req){
        String relativeUrl = ReqInfo.getLocalUrl(req);
        return getGateWayPathInfo(relativeUrl);
    }

    public GatewayPathInfo getGateWayPathInfo(String resourcePath) {
        GatewayPathInfo gpi;
        String cacheKey = PathInfo.getCacheKey(resourcePath);
        gpi = (GatewayPathInfo) RequestCache.get(cacheKey);
        if(gpi!=null) {
            log.debug("getBesPathInfo() - RequestCache has PathInfo for key {}",cacheKey);
            return gpi;
        }
        gpi = new GatewayPathInfo(resourcePath,_besApi);
        RequestCache.put(cacheKey,gpi);
        log.debug("getBesPathInfo() - Adding PathInfo for key {} to RequestCache",cacheKey);

        return gpi;
    }

    @Override
    public long getLastModified(PathInfo pi){
        return pi.lastModified().getTime();
    }


    private void ingestTrustedHosts(Element config) throws URISyntaxException, MalformedURLException {

        if(config==null)
            return;

        String msg;
        Element hostElem;
        URL url;
        URI uri;
        List hosts = config.getChildren("trustedHost");

        if(hosts.isEmpty()){
            msg = "Configuration Warning: The <Handler> " +
                    "element that declares " + this.getClass().getName() +
                    " did not provide 1 or more <wcsHost> " +
                    "child elements to limit the WCS services that " +
                    "may be accessed. This not recomended.";
            log.warn(msg);
        }
        else {
            for (Object o : hosts) {
                hostElem = (Element) o;
                String host = hostElem.getTextTrim();
                url = new URL(host);
                log.debug(Util.urlInfo(url));
                uri = new URI(host);
                log.debug(Util.uriInfo(uri));
                log.info("Adding " + url + " to allowed hosts list.");
                trustedHosts.add(host);
            }
        }
    }

    private boolean isTrustedHost(String url){
        for(String trustedHost : trustedHosts){
            if(url.startsWith(trustedHost))
                return true;
        }
        return false;
    }


    private void ingestPrefix(Element config) throws Exception {
        if (config != null) {
            String msg;
            Element e = config.getChild("prefix");
            if (e != null)
                _prefix = e.getTextTrim();

            if (_prefix.equals("/")) {
                msg = "Bad Configuration. The <Handler> " +
                        "element that declares " + this.getClass().getName() +
                        " MUST provide 1 <prefix>  " +
                        "child element whose value may not be equal to \"/\"";
                log.error(msg);
                throw new Exception(msg);
            }
            if (!_prefix.endsWith("/"))
                _prefix += "/";

            if (_prefix.startsWith("/"))
                _prefix = _prefix.substring(1);
        }
        log.info("Using prefix=" + _prefix);
    }

    String stripPrefix(String dataSource){
        if(_prefix.startsWith("/")) {
            if (!dataSource.startsWith("/"))
                dataSource = "/" + dataSource;
        }
        else {
            while(dataSource.startsWith("/") && dataSource.length()>1)
                dataSource = dataSource.substring(1);
        }
        if(dataSource.startsWith(_prefix))
            return dataSource.substring(_prefix.length(),dataSource.length());

        return dataSource;
    }

}
