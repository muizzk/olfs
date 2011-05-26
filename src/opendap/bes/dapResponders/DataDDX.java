/////////////////////////////////////////////////////////////////////////////
// This file is part of the "OPeNDAP 4 Data Server (aka Hyrax)" project.
//
//
// Copyright (c) 2011 OPeNDAP, Inc.
// Author: Nathan David Potter  <ndp@opendap.org>
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// You can contact OPeNDAP, Inc. at PO Box 112, Saunderstown, RI. 02874-0112.
/////////////////////////////////////////////////////////////////////////////
package opendap.bes.dapResponders;

import opendap.bes.Version;
import opendap.coreServlet.MimeBoundary;
import opendap.coreServlet.ReqInfo;
import opendap.coreServlet.HttpResponder;
import org.jdom.Document;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;


public class DataDDX extends HttpResponder {



    private Logger log;private BesApi _besApi;

    private static String defaultRegex = ".*\\.dap";


    public DataDDX(String sysPath, BesApi besApi) {
        super(sysPath, null, defaultRegex);
        log = org.slf4j.LoggerFactory.getLogger(this.getClass());
        _besApi = besApi;
    }

    public DataDDX(String sysPath, String pathPrefix, BesApi besApi) {
        super(sysPath, pathPrefix, defaultRegex);
        log = org.slf4j.LoggerFactory.getLogger(this.getClass());
        _besApi = besApi;
    }

    public void respondToHttpGetRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {


        String xmlBase = request.getRequestURL().toString();
        String relativeUrl = ReqInfo.getLocalUrl(request);
        String dataSource = ReqInfo.getBesDataSourceID(relativeUrl);
        String constraintExpression = ReqInfo.getConstraintExpression(request);




        MimeBoundary mb = new MimeBoundary();
        String startID = mb.newContentID();

        log.debug("sendDataDDX() for dataset: " + dataSource+
                "    CE: '" + constraintExpression + "'");


        response.setContentType("Multipart/Related;  "+
                                "type=\"text/xml\";  "+
                                "start=\"<"+startID+">\";  "+
                                "boundary=\""+mb.getBoundary()+"\"");


        Version.setOpendapMimeHeaders(request,response);
        response.setHeader("Content-Description", "dap4_data_ddx");

        // This header indicates to the client that the content of this response
        // is dependant on the HTTP request header XDAP-Accept
        response.setHeader("Vary", "XDAP-Accept");

        // Because the content of this response is dependant on a client provided
        // HTTP header (XDAP-Accept) it is useful to include this Cach-Control
        // header to make caching work correctly...
        response.setHeader("Cache-Control", "public");


        response.setStatus(HttpServletResponse.SC_OK);

        String xdap_accept = request.getHeader("XDAP-Accept");





        OutputStream os = response.getOutputStream();
        ByteArrayOutputStream erros = new ByteArrayOutputStream();

        Document reqDoc = _besApi.getDataDDXRequest(dataSource,
                                                        constraintExpression,
                                                        xdap_accept,
                                                        xmlBase,
                                                        startID,
                                                        mb.getBoundary());

        if(!_besApi.besTransaction(dataSource,reqDoc,os,erros)){
            String msg = new String(erros.toByteArray());
            log.error("sendDAP2Data() encountered a BESError: "+msg);
            os.write(msg.getBytes());

        }


        os.flush();
        log.info("Sent DAP4 Data DDX response.");




    }

}
