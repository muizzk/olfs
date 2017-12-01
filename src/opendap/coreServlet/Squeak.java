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

package opendap.coreServlet;


import java.io.IOException;
import java.util.Date;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import opendap.bes.BESError;
import opendap.bes.BESThreddsDispatchHandler;
import opendap.bes.BadConfigurationException;
import opendap.bes.BesDapDispatcher;
import opendap.bes.DirectoryDispatchHandler;
import opendap.bes.FileDispatchHandler;
import opendap.bes.PathInfo;
import opendap.bes.VersionDispatchHandler;
import opendap.bes.dap2Responders.BesApi;
import opendap.logging.LogUtil;
import opendap.logging.Procedure;
import opendap.logging.Timer;
import opendap.ncml.NcmlDatasetDispatcher;
import opendap.ppt.PPTException;
import opendap.threddsHandler.StaticCatalogDispatch;
import opendap.gateway.GatewayPathInfo;

/**
 * This servlet provides the dispatching for all OPeNDAP requests.
 * <p/>
 * <p>This server will respond to both HTTP GET and POST requests.
 * activities are handled by ordered collections of DispatchHandlers.
 * <p/>
 * <p/>
 * <p>This server is designed so that the dispatch activities are handled by
 * ordered collections of DispatchHandlers are identified at run time through
 * the olfs.xml configuration file. The olfs.xml file is identified in the
 * servlets web.xml file. The olfs.xml file is typically located in
 * $CATALINE_HOME/content/opendap. <p/>
 * 
 * <p>The web.xml file used to configure this servlet must contain servlet parameters identifying
 * the location of the olfs.xml file.</p>
 * <p/>
 * <p/>
 * <p/>
 */
public class Squeak extends DispatchServlet {

    private Logger _log;

    //protected Vector<DispatchHandler> _httpGetDispatchHandlers;
    // protected Vector<DispatchHandler> _httpPostDispatchHandlers;


    private VersionDispatchHandler _versionHandler;
    private NcmlDatasetDispatcher _ncmlHandler;
    private StaticCatalogDispatch _staticThreddsCatalogHandler;
    private opendap.gateway.DispatchHandler _gatewayHandler;
    private BesDapDispatcher _dapHandler;
    private DirectoryDispatchHandler _directoryHandler;
    private BESThreddsDispatchHandler _besThreddsHandler;
    private FileDispatchHandler _fileHandler;

    public Squeak(){
        /*
        _httpGetDispatchHandlers = new Vector<>();
        _httpPostDispatchHandlers = new Vector<>();

        _versionHandler =  new VersionDispatchHandler();
        _httpGetDispatchHandlers.add(_versionHandler);

        _ncmlHandler = new NcmlDatasetDispatcher();
        _httpGetDispatchHandlers.add(_ncmlHandler);

        _staticThreddsCatalogHandler = new StaticCatalogDispatch();
        _httpGetDispatchHandlers.add(_staticThreddsCatalogHandler);

        _gatewayHandler = new opendap.gateway.DispatchHandler();
        _httpGetDispatchHandlers.add(_gatewayHandler);

        _dapHandler = new BesDapDispatcher();
        _httpGetDispatchHandlers.add(_dapHandler);

        _directoryHandler = new DirectoryDispatchHandler();
        _httpGetDispatchHandlers.add(_directoryHandler);

        _besThreddsHandler = new BESThreddsDispatchHandler();
        _httpGetDispatchHandlers.add(_besThreddsHandler);

        _fileHandler = new FileDispatchHandler();
        _httpGetDispatchHandlers.add(_fileHandler);
        */

    }

    private void sortHandlers() throws ServletException {
        for(DispatchHandler dh : httpGetDispatchHandlers){
            _log.debug("Processing instance of {}",dh.getClass().getName());
            if(dh instanceof opendap.bes.VersionDispatchHandler) {
                _versionHandler = (opendap.bes.VersionDispatchHandler) dh;
                _log.debug("Set _versionHandler - Instance of {}",_versionHandler.getClass().getName());
            }
            else if(dh instanceof opendap.ncml.NcmlDatasetDispatcher) {
                _ncmlHandler = (opendap.ncml.NcmlDatasetDispatcher) dh;
                _log.debug("Set _ncmlHandler - Instance of {}",_ncmlHandler.getClass().getName());
            }
            else if(dh instanceof opendap.threddsHandler.StaticCatalogDispatch) {
                _staticThreddsCatalogHandler = (opendap.threddsHandler.StaticCatalogDispatch) dh;
                _log.debug("Set _staticThreddsCatalogHandler - Instance of {}",_staticThreddsCatalogHandler.getClass().getName());
            }
            else if(dh instanceof opendap.gateway.DispatchHandler) {
                _gatewayHandler = (opendap.gateway.DispatchHandler) dh;
                _log.debug("Set _gatewayHandler - Instance of {}",_gatewayHandler.getClass().getName());
            }
            else if(dh instanceof BesDapDispatcher) {
                _dapHandler = (BesDapDispatcher) dh;
                _log.debug("Set _dapHandler - Instance of {}",_dapHandler.getClass().getName());
            }
            else if(dh instanceof opendap.bes.DirectoryDispatchHandler) {
                _directoryHandler = (opendap.bes.DirectoryDispatchHandler) dh;
                _log.debug("Set _directoryHandler - Instance of {}",_directoryHandler.getClass().getName());
            }
            else if(dh instanceof opendap.bes.BESThreddsDispatchHandler) {
                _besThreddsHandler = (opendap.bes.BESThreddsDispatchHandler) dh;
                _log.debug("Set _besThreddsHandler - Instance of {}",_besThreddsHandler.getClass().getName());
            }
            else if(dh instanceof opendap.bes.FileDispatchHandler) {
                _fileHandler = (opendap.bes.FileDispatchHandler) dh;
                _log.debug("Set _fileHandler - Instance of {}",_fileHandler.getClass().getName());
            }
            else {
                throw new ServletException("Unknown DispatchHandler type: " + dh.getClass().getName());
            }

        }
    }


    /**
     *
     * ************************************************************************
     * Initializes the servlet. The init() method (at this time) basically
     * sets up the object opendap.util.Debug from the debuggery flags in the
     * servlet InitParameters. The Debug object can be referenced (with
     * impunity) from any of the dods code...
     *
     * @throws javax.servlet.ServletException
     */
    @Override
    public void init() throws ServletException {
        _log = LoggerFactory.getLogger(getClass());
        super.init();
        sortHandlers();
    }


    public static PathInfo besGetPathInfo(String resourcePath)
            throws JDOMException, BadConfigurationException, PPTException, BESError, IOException {

        Logger log = LoggerFactory.getLogger(Squeak.class);
        PathInfo besPathInfo;
        String cacheKey = PathInfo.getCacheKey(resourcePath);
        besPathInfo = (PathInfo) RequestCache.get(cacheKey);
        if(besPathInfo!=null) {
            log.debug("getBesPathInfo() - RequestCache has PathInfo for key {}",cacheKey);
            return besPathInfo;
        }
        BesApi besApi = new BesApi();
        besPathInfo = besApi.getBesPathInfo(resourcePath);
        RequestCache.put(cacheKey,besPathInfo);
        log.debug("getBesPathInfo() - Adding PathInfo for key {} to RequestCache",cacheKey);

        return besPathInfo;
    }



    /**
     * ***********************************************************************
     * Handles incoming requests from clients. Parses the request and determines
     * what kind of OPeNDAP response the cleint is requesting. If the request is
     * understood, then the appropriate handler method is called, otherwise
     * an error is returned to the client.
     * <p/>
     * This method is the entry point for <code>OLFS</code>. It uses
     * the methods <code>processOpendapURL</code> to extract the OPeNDAP URL
     * information from the incoming client request. This OPeNDAP URL information
     * is cached and made accessible through get and set methods.
     * <p/>
     * After  <code>processOpendapURL</code> is called <code>loadIniFile()</code>
     * is called to load configuration information from a .ini file,
     * <p/>
     * If the standard behaviour of the servlet (extracting the OPeNDAP URL
     * information from the client request, or loading the .ini file) then
     * you should overload <code>processOpendapURL</code> and <code>loadIniFile()
     * </code>. <b> We don't recommend overloading <code>doGet()</code> beacuse
     * the logic contained there may change in our core and cause your server
     * to behave unpredictably when future releases are installed.</b>
     *
     * @param request  The client's <code> HttpServletRequest</code> request
     *                 object.
     * @param response The server's <code> HttpServletResponse</code> response
     *                 object.
     * @see ReqInfo
     */
    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) {

        String relativeUrl = ReqInfo.getLocalUrl(request);
        int request_status = HttpServletResponse.SC_OK;
        try {
            Procedure timedProcedure = Timer.start();
            RequestCache.openThreadCache();
            try {
                if(LicenseManager.isExpired(request)){
                    LicenseManager.sendLicenseExpiredPage(request,response);
                    return;
                }
                int reqno = reqNumber.incrementAndGet();
                LogUtil.logServerAccessStart(request, "HyraxAccess", "HTTP-GET", Long.toString(reqno));
                _log.debug(Util.getMemoryReport());
                _log.debug(ServletUtil.showRequest(request, reqno));
                //log.debug(AwsUtil.probeRequest(this, request));

                if(redirectForServiceOnlyRequest(request,response))
                    return;

                _log.debug("Requested relative URL: '" + relativeUrl +
                        "' suffix: '" + ReqInfo.getRequestSuffix(request) +
                        "' CE: '" + ReqInfo.getConstraintExpression(request) + "'");

                if (Debug.isSet("probeRequest"))
                    _log.debug(ServletUtil.probeRequest(this, request));

                gdhParams gdhp = new gdhParams();
                gdhp.request = request;
                getDispatchHandler(gdhp);
                if (gdhp.dispatchHandler != null) {
                    _log.debug("Request being handled by: " + gdhp.dispatchHandler.getClass().getName());
                    gdhp.dispatchHandler.handleRequest(request, gdhp.pathInfo, response);
                } else {
                    //send404(request,response);
                    throw  new OPeNDAPException(HttpServletResponse.SC_NOT_FOUND, "Failed to locate resource: "+relativeUrl);
                }
            }
            finally {
                Timer.stop(timedProcedure);
            }
        }
        catch (Throwable t) {
            try {
                request_status = OPeNDAPException.anyExceptionHandler(t, this, response);
            }
            catch(Throwable t2) {
                try {
                    _log.error("\n########################################################\n" +
                            "Request processing failed.\n" +
                            "Normal Exception handling failed.\n" +
                            "This is the last error log attempt for this request.\n" +
                            "########################################################\n", t2);
                }
                catch(Throwable t3){
                    // It's boned now.. Leave it be.
                }
            }
        }
        finally {
            LogUtil.logServerAccessEnd(request_status, "HyraxAccess");
            RequestCache.closeThreadCache();
            _log.info("doGet(): Response completed.\n");
        }
        _log.info("doGet() - Timing Report: \n{}", Timer.report());
        Timer.reset();
    }
    //**************************************************************************


    private boolean redirectForServiceOnlyRequest(HttpServletRequest req,
                                                  HttpServletResponse res)
            throws IOException {
        // log.debug(ServletUtil.probeRequest(this, req));
        if (ReqInfo.isServiceOnlyRequest(req)) {
            String reqURI = req.getRequestURI();
            String newURI = reqURI+"/";
            res.sendRedirect(Scrub.urlContent(newURI));
            _log.debug("Sent redirectForServiceOnlyRequest to map the servlet " +
                    "context to a URL that ends in a '/' character!");
            return true;
        }
        return false;
    }


    /**
     * @param request  .
     * @param response .
     */
    @Override
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response) {

        String relativeUrl = ReqInfo.getLocalUrl(request);
        int request_status = HttpServletResponse.SC_OK;
        try {
            try {
                RequestCache.openThreadCache();
                int reqno = reqNumber.incrementAndGet();
                LogUtil.logServerAccessStart(request, "HyraxAccess", "HTTP-POST", Long.toString(reqno));
                _log.debug(ServletUtil.showRequest(request, reqno));
                _log.debug("Requested relative URL: '" + relativeUrl +
                        "' suffix: '" + ReqInfo.getRequestSuffix(request) +
                        "' CE: '" + ReqInfo.getConstraintExpression(request) + "'");

                if (Debug.isSet("probeRequest"))
                    _log.debug(ServletUtil.probeRequest(this, request));

                gdhParams gdhp = new gdhParams();
                gdhp.request = request;
                getDispatchHandler(gdhp);
                if (gdhp.dispatchHandler != null) {
                    _log.debug("Request being handled by: " + gdhp.dispatchHandler.getClass().getName());
                    gdhp.dispatchHandler.handleRequest(request, gdhp.pathInfo, response);
                } else {
                    //send404(request,response);
                    throw  new OPeNDAPException(HttpServletResponse.SC_NOT_FOUND, "Failed to locate resource: "+relativeUrl);
                }
            }
            finally {
                _log.info("doPost(): Response completed.\n");
            }
        } catch (Throwable t) {
            try {
                request_status = OPeNDAPException.anyExceptionHandler(t, this, response);
            }
            catch(Throwable t2) {
                try {
                    _log.error("BAD THINGS HAPPENED!", t2);
                }
                catch(Throwable t3){
                    // It's boned now.. Leave it be.
                }
            }
        }
        finally{
            LogUtil.logServerAccessEnd(request_status, "HyraxAccess");
            RequestCache.closeThreadCache();
        }
    }



    /**
     * Gets the last modified date of the requested resource. Because the data handler is really
     * the only entity capable of determining the last modified date the job is passed  through to it.
     *
     * @param req The current request
     * @return Returns the time the HttpServletRequest object was last modified, in milliseconds
     *         since midnight January 1, 1970 GMT
     */
    @Override
    protected long getLastModified(HttpServletRequest req) {
        RequestCache.openThreadCache();
        long reqno = reqNumber.incrementAndGet();
        LogUtil.logServerAccessStart(req, "HyraxAccess", "LAST-MOD", Long.toString(reqno));

        long lmt = new Date().getTime();
        Procedure timedProcedure = Timer.start();
        try {
            if (ReqInfo.isServiceOnlyRequest(req)) {
                return lmt;
            }
            if (!LicenseManager.isExpired(req) && !ReqInfo.isServiceOnlyRequest(req)) {
                String relativeUrl = ReqInfo.getLocalUrl(req);

                gdhParams gdhp = new gdhParams();
                gdhp.request = req;
                getDispatchHandler(gdhp);
                if (gdhp.dispatchHandler != null) {
                    _log.debug("Request being handled by: " + gdhp.dispatchHandler.getClass().getName());
                    lmt = gdhp.dispatchHandler.getLastModified(gdhp.pathInfo);
                } else {
                    //send404(request,response);
                    throw  new OPeNDAPException(HttpServletResponse.SC_NOT_FOUND, "Failed to locate resource: "+relativeUrl);
                }
            }
        } catch (Exception e) {
            _log.error("getLastModifiedTime() - Caught " + e.getClass().getName() + " msg: " + e.getMessage());
        } finally {
            LogUtil.logServerAccessEnd(HttpServletResponse.SC_OK, "HyraxAccess");
            Timer.stop(timedProcedure);
        }
        return lmt;
    }


    @Override
    public void destroy() {

        LogUtil.logServerShutdown("destroy()");

        super.destroy();
    }

    class gdhParams {
        HttpServletRequest request;
        PathInfo pathInfo;
        DispatchHandler dispatchHandler;
    }

    private void getDispatchHandler(gdhParams gdhP)
            throws Exception {
         getDispatchHandler_besInfo(gdhP);
    }



    /**
     * Returns the DispatchHandler that is to process the request. The logic to
     * deterimine which DH to return is relies on asking the BES for an anaylsis of
     * the URL path in the context of it's catalog. THis information allows this
     * code to asses the request and select teh appropriate handler.
     *
     * @param gdhP The request we are looking to handle
     * @return The IsoDispatchHandler that can handle the request, null if no
     *         handler claims the request.
     * @throws Exception For bad behaviour.
     */
    private void
    getDispatchHandler_besInfo(gdhParams gdhP) throws Exception {

        // Check cache for object, yada yada
        String dispatchHandlerKey = getClass().getName()+"getDispatchHandler()";
        gdhParams cachedResult  = (gdhParams)RequestCache.get(dispatchHandlerKey);
        if(cachedResult!=null) {
            gdhP.dispatchHandler = cachedResult.dispatchHandler;
            gdhP.pathInfo = cachedResult.pathInfo;
            return;
        }

        String relativeUrl = ReqInfo.getLocalUrl(gdhP.request);
        // DispatchHandler dispatchHandler = null;

        if (relativeUrl.startsWith("/gateway")){
            // Send to gateway.DispatchHandler
            gdhP.dispatchHandler = _gatewayHandler;
            // And since Gateway only uses the BES to service remote resources
            // rather than files on the BES filesystem the PathInfo object
            // has to have a special implementation to support this difference.
            // So we don't need to bother getting/setting the PathInfo yet
            // gdhP.pathInfo = new GatewayPathInfo(relativeUrl, _gatewayHandler.getBesGatewayApi());
            gdhP.pathInfo = new GatewayPathInfo(relativeUrl,_gatewayHandler.getBesGatewayApi());
        }
        else if (_ncmlHandler.requestCanBeHandled(gdhP.request,null)){
            gdhP.dispatchHandler = _ncmlHandler;
        }
        else if(relativeUrl.startsWith("/thredds")){
            // Send to StaticCatalogDispatch
            gdhP.dispatchHandler = _staticThreddsCatalogHandler;
        }
        else {
            PathInfo besPathInfo = besGetPathInfo(relativeUrl); // This handles the use of the RequestCache
            String remainder = besPathInfo.remainder();
            String validPath = besPathInfo.validPath();
            gdhP.pathInfo = besPathInfo;

            if(remainder.isEmpty()){
                // This is the easy part, no remainder. It's a simple file or directory in the BES.
                if(besPathInfo.isFile()){
                    // Send to FileDispatchHandler
                    gdhP.dispatchHandler = _fileHandler;
                }
                else if(besPathInfo.isDir()){
                    // Send to directory dispatcher.
                    gdhP.dispatchHandler =  _directoryHandler;
                }
            }
            else {
                // So there's a remainder. Now the fun begins.
                _log.debug("remainder: {}", remainder);

                if(remainder.equalsIgnoreCase("version") && validPath.equals("/")){
                    // VersionDispatch Handler
                    gdhP.dispatchHandler = _versionHandler;
                }
                else if(remainder.endsWith("contents.html") ||
                        remainder.endsWith("catalog.html") ||
                        remainder.endsWith("/"))  {
                    gdhP.dispatchHandler =  _directoryHandler;
                }
                else if( remainder.endsWith("catalog.xml")){
                    // Send to BESThreddsDispatchHandler
                    gdhP.dispatchHandler =  _besThreddsHandler;
                }
                else if( remainder.equals(FileDispatchHandler.FILE_SUFFIX)){
                    // It's a DAP4 file request.
                    gdhP.dispatchHandler =  _fileHandler;
                }
                else {
                    // Looks like DAP request - send to BesDapDispatcher
                    gdhP.dispatchHandler =  _dapHandler;
                }

            }
        }

        RequestCache.put(dispatchHandlerKey,gdhP);


    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // THis next bit was an attempt to perform dispatch using only regex. There are issues....
    //
    //


/*
    String DAP2_REQUEST_REGEX = "^.*\\.(dds|das|dods|ascii|html|rdf|info|csv|tiff|jp2|json|iso|rubric)$";
    String DAP4_REQUEST_REGEX = "^.*\\.(file|dsr(\\.(xml|html)?)|dmr(\\.(xml|html|json|rdf|iso|rubric))?|dap(\\.(json|csv|tiff|jp2|nc|nc4|xml)?))$";
    String DAP_REQUEST_MATCH_REGEX = "(" + DAP2_REQUEST_REGEX + "|" + DAP4_REQUEST_REGEX + ")";
    Pattern DapRequestPattern = Pattern.compile(DAP_REQUEST_MATCH_REGEX);
    private boolean isDapRequest(String path){
        return DapRequestPattern.matcher(path).matches();
    }

    private DispatchHandler
    getDispatchHandler_regex(HttpServletRequest request)
            throws Exception {

        String dispatchHandlerKey = getClass().getName()+"getDispatchHandler()";
        DispatchHandler cachedDispatchHandler  = (DispatchHandler)RequestCache.get(dispatchHandlerKey);
        if(cachedDispatchHandler!=null) {
            return cachedDispatchHandler;
        }

        DispatchHandler dispatchHandler = null;

        String relativeUrl = ReqInfo.getLocalUrl(request);

        if(relativeUrl.equals("version")){
            // Send to StaticCatalogDispatch
            dispatchHandler = _versionHandler;
        }
        if(relativeUrl.startsWith("thredds")){
            // Send to StaticCatalogDispatch
            dispatchHandler = _staticThreddsCatalogHandler;
        }
        else if (relativeUrl.startsWith("gateway")){
                // Send to gateway.DispatchHandler
                dispatchHandler = _gatewayHandler;
        }
        else if (relativeUrl.endsWith("/contents.html") ||
                relativeUrl.endsWith("/catalog.html") ||
                relativeUrl.endsWith("/")) {
            dispatchHandler = _directoryHandler;
        } else if (relativeUrl.endsWith("/catalog.xml")) {
            // Send to BESThreddsDispatchHandler
            dispatchHandler = _besThreddsHandler;
        }
        else if (relativeUrl.endsWith(".ncml")) {
            // Send to BESThreddsDispatchHandler
            dispatchHandler = _ncmlHandler;
        }
        else if(isDapRequest(relativeUrl)){
            dispatchHandler = _dapHandler;
        }
        else {
            PathInfo besPathInfo = besGetPathInfo(request);
            String remainder = besPathInfo.remainder();
            String validPath = besPathInfo.validPath();

            if (remainder.isEmpty()) {
                // This is the easy part, no remainder. It's a simple file or directory in the BES.
                if (besPathInfo.isFile()) {
                    if (besPathInfo.isData()) {
                        if (validPath.toLowerCase().endsWith(".ncml")) {
                            // Send to NcmlDatasetDispatcher  because NcML hack...
                            dispatchHandler = _ncmlHandler;
                        } else {
                            // Send to BesDapDispatcher because data visibility is controlled there.
                            dispatchHandler = _dapHandler;
                        }
                    } else {
                        // Send to FileDispatchHandler
                        dispatchHandler = _fileHandler;
                    }
                } else if (besPathInfo.isDir()) {
                    // Send to directory dispatcher.
                    dispatchHandler = _directoryHandler;
                }
            } else {
                // So there's a remainder. Now the fun begins.
                _log.debug("remainder: {}", remainder);

                    // Looks like DAP request - send to BesDapDispatcher
                    dispatchHandler = _dapHandler;
            }
        }

        if(dispatchHandler!=null)
            RequestCache.put(dispatchHandlerKey,dispatchHandler);

        return dispatchHandler;
    }
*/




}
