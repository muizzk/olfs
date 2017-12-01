/*
 * /////////////////////////////////////////////////////////////////////////////
 * // This file is part of the "Hyrax Data Server" project.
 * //
 * //
 * // Copyright (c) 2017 OPeNDAP, Inc.
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

import opendap.bes.BESError;
import opendap.bes.BadConfigurationException;
import opendap.bes.BesDapDispatcher;
import opendap.bes.PathInfo;
import opendap.bes.dap2Responders.BesApi;
import opendap.coreServlet.ReqInfo;
import opendap.ppt.PPTException;
import org.jdom.Element;
import org.jdom.JDOMException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class GatewayPathInfo extends PathInfo {
    BesGatewayApi _besApi;

    public GatewayPathInfo(){
        super();
    }

    public GatewayPathInfo(Element pathInfoElement) throws IOException {
        super(pathInfoElement);
    }


    public GatewayPathInfo(String urlPath, BesGatewayApi besApi) throws JDOMException, BadConfigurationException, PPTException, BESError, IOException {
        this();
        _besApi = besApi;
        // FIXME! MAKE SURE THIS WORKS! MAY NEED TO POPULATE OTHER VALUES!
        _path = urlPath;
        if(_path.contains("."))
            _path =  _besApi.getBesDataSourceID(_path, BesGatewayApi.stripDotSuffixPattern);


        ////////////////////////////////////////////////////////////////////////////////////////
        // THIS IS A HACK TO MAKE THIS WORK WITHOUT A GATEWAY SPECIFIC showPathInfo BES command.
        String localhost = "http://localhost:8080/opendap";
        if(_path.startsWith(localhost))
            _path = _path.substring(localhost.length());
        ////////////////////////////////////////////////////////////////////////////////////////

        _remainder = "";
        if(urlPath.contains("."))
            _remainder = urlPath.substring(urlPath.indexOf('.'));

        PathInfo pathInfo = _besApi.getGatewayPathInfo(_path);

        _isFile = pathInfo.isFile();
        _isData = pathInfo.isData();
        _isDir = pathInfo.isDir();
        _canAccess = pathInfo.isAccessible();
        _lmt = pathInfo.lastModified();
    }


    public void setRemainder(String r){
        _remainder = r;
    }

}
