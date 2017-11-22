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

package opendap.coreServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NoMatchingResponder extends HttpResponder {

    public NoMatchingResponder(){
        super();
    }

    /*
    @Override
    public ResourceInfo getResourceInfo(String resourceName) throws Exception {
        throw new Exception("The "+getClass().getName()+" is semantically a symbol for 'we looked and no " +
                "handler matched', and thus an instance may never be invoked to do stuff :(");
    }
    */
    /*
    @Override
    public long getLastModified(HttpServletRequest request) throws Exception {
        throw new Exception("The "+getClass().getName()+" is semantically a symbol for 'we looked and no " +
                "handler matched', and thus an instance may never be invoked to do stuff :(");
    }
    */

    @Override
    public void respondToHttpGetRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        throw new Exception("The "+getClass().getName()+" is semantically a symbol for 'we looked and no " +
                "handler matched', and thus an instance may never be invoked to do stuff :(");
    }
}
