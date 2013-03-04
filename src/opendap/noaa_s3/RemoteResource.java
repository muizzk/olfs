/*
 * /////////////////////////////////////////////////////////////////////////////
 * // This file is part of the "OPeNDAP 4 Data Server (aka Hyrax)" project.
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
 * // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * //
 * // You can contact OPeNDAP, Inc. at PO Box 112, Saunderstown, RI. 02874-0112.
 * /////////////////////////////////////////////////////////////////////////////
 */

package opendap.noaa_s3;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: ndp
 * Date: 2/28/13
 * Time: 11:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class RemoteResource {

    Logger log;
    private String _resourceUrl;
    private long _lastModified;
    private String _contentType;

    private Header _responseHeaders[];


    public RemoteResource() {
        log = LoggerFactory.getLogger(this.getClass());
        _resourceUrl = null;
        _lastModified = 0;
        _responseHeaders = null;
    }

    public RemoteResource(String url) {
        log = LoggerFactory.getLogger(this.getClass());
        _resourceUrl = url;
        _lastModified = 0;
        _responseHeaders = null;
    }


    public void setResourceUrl(String url){
        if(_resourceUrl==null || !_resourceUrl.equals(url)){
            _resourceUrl  = url;
            _responseHeaders = null;
        }
    }

    public void clearResponseHeaders(){
        _responseHeaders = null;
    }


    public void setLastModifiedTime(long lmt){
        _lastModified = lmt;

    }

    /**
     *
     * @return
     * @throws IOException
     * @throws JDOMException
     */
    public InputStream getResourceAsStream() throws IOException  {


        log.debug("getResourceAsStream() - Retrieving content from " + _resourceUrl);

        // Go get the HEAD for the catalog:
        HttpClient httpClient = new HttpClient();
        GetMethod getRequest = new GetMethod(_resourceUrl);

        int statusCode = httpClient.executeMethod(getRequest);

        if (statusCode != HttpStatus.SC_OK) {
            log.error("Unable to GET remote resource: " + _resourceUrl);
            _lastModified = -1;
            _responseHeaders = null;
            log.error("getResourceAsStream() - Unable to GET the resource: {} HTTP status: {}",_resourceUrl,statusCode);
            throw new IOException("RemoteResource.getResourceAsStream() - Failed to retrieve resource. HTTP status: " + statusCode);
        }
        _responseHeaders = getRequest.getResponseHeaders();

        return getRequest.getResponseBodyAsStream();
    }





    /**
     *
     * @return
     * @throws IOException
     * @throws JDOMException
     */
    public void updateResponseHeaders()  {


        log.debug("updateResponseHeaders() - Retrieving HTTP HEAD for '{}'",  _resourceUrl);

        // Go get the HEAD for the catalog:
        HttpClient httpClient = new HttpClient();
        HeadMethod headReq = new HeadMethod(_resourceUrl);

        try {
        int statusCode = httpClient.executeMethod(headReq);

            if (statusCode != HttpStatus.SC_OK) {
                log.error("Unable to HEAD s3 object: " + _resourceUrl);
            }
            else {

                _responseHeaders = headReq.getResponseHeaders();


            }

        } catch (Exception e) {
            log.error("Unable to HEAD the s3 resource: {} Error Msg: {}",_resourceUrl,e.getMessage());
        }
    }




    public String getContentType(){

        if(_contentType == null){
            _contentType = getHeaderValue("content-type");
        }
        return _contentType;
    }


    public long getLastModified() {


        log.debug("getLastModified() - BEGIN");

        if (_lastModified == 0) {
            String lmt_string = getHeaderValue("last-modified");

            if (lmt_string == null)
                _lastModified = -1;

            else {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
                    Date d = format.parse(lmt_string);
                    _lastModified = d.getTime();
                } catch (ParseException e) {
                    _lastModified = -1;
                }
            }
        }
        log.debug("getLastModified() - END ({})", _lastModified);

        return _lastModified;
    }


    public String getHeaderValue(String hdrName){

        if(_responseHeaders==null)
            updateResponseHeaders();

        if(_responseHeaders==null)
           return null;

        for(Header hdr : _responseHeaders){
            String name = hdr.getName();
            if(name.equalsIgnoreCase(hdrName)){
                return hdr.getValue();
            }
        }
        return null;
    }



}
