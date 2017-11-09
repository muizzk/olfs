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

package opendap.bes;

import opendap.bes.dap2Responders.BesApi;
import opendap.namespaces.BES;
import org.jdom.Element;

import java.io.IOException;
import java.util.Date;

public class PathInfo {

    public static final String PATH_INFO = "PathInfo";
    public static final String PATH = "path";
    public static final String VALID_PATH = "validPath";
    public static final String IS_DATA = "isData";
    public static final String IS_DIR = "isDir";
    public static final String IS_ACCESSIBLE = "access";
    public static final String IS_FILE = "isFile";
    public static final String LMT = "lastModified";
    public static final String SIZE = "size";
    public static final String REMAINDER = "remainder";


    protected Element _pathInfoElement;
    protected Element _validPathElement;
    protected Element _remainderElement;

    protected String _path;
    protected String _validPath;
    protected String _remainder;

    protected boolean _isDir;
    protected boolean _isFile;
    protected boolean _isData;
    protected boolean _canAccess;
    protected Date _lmt;
    protected long _size;


    protected PathInfo(){

    }

    public PathInfo(Element piElement) throws IOException {
        this();

        String s;

        _pathInfoElement = (Element) piElement.clone();

        _path = _pathInfoElement.getAttributeValue(PATH);
        if(_path==null)
            throw new IOException("BES returned invalid response the "+ BesApi.SHOW_PATH_INFO+" command. " +
                    "Missing the '"+PATH+"' element in the response.");

        _validPathElement = _pathInfoElement.getChild(VALID_PATH, BES.BES_NS);
        if(_validPathElement==null)
            throw new IOException("BES returned invalid response the "+ BesApi.SHOW_PATH_INFO+" command. " +
                    "Missing the '"+VALID_PATH+"' element in the response.");

        _validPath = _validPathElement.getTextTrim();

        s = _validPathElement.getAttributeValue(IS_DIR);
        if(s==null)
            throw new IOException("BES returned invalid response the "+ BesApi.SHOW_PATH_INFO+" command. " +
                    "Missing the '"+IS_DIR+"' attribute of the '"+VALID_PATH+"' element in the response.");
        _isDir = Boolean.parseBoolean(s);

        s = _validPathElement.getAttributeValue(IS_FILE);
        if(s==null)
            throw new IOException("BES returned invalid response the "+ BesApi.SHOW_PATH_INFO+" command. " +
                    "Missing the '"+IS_FILE+"' attribute of the '"+VALID_PATH+"' element in the response.");
        _isFile = Boolean.parseBoolean(s);

        s = _validPathElement.getAttributeValue(IS_ACCESSIBLE);
        if(s==null)
            throw new IOException("BES returned invalid response the "+ BesApi.SHOW_PATH_INFO+" command. " +
                    "Missing the '"+IS_ACCESSIBLE+"' attribute of the '"+VALID_PATH+" 'element in the response.");
        _canAccess = Boolean.parseBoolean(s);

        s = _validPathElement.getAttributeValue(IS_DATA);
        if(s==null)
            throw new IOException("BES returned invalid response the "+ BesApi.SHOW_PATH_INFO+" command. " +
                    "Missing the '"+IS_DATA+"' attribute of the '"+VALID_PATH+" 'element in the response.");
        _isData = Boolean.parseBoolean(s);


        s = _validPathElement.getAttributeValue(LMT);
        if(s==null)
            throw new IOException("BES returned invalid response the "+ BesApi.SHOW_PATH_INFO+" command. " +
                    "Missing the "+LMT+" attribute of the '"+VALID_PATH+" 'element in the response.");
        long secondsSinceEpoch = Long.parseLong(s);
        _lmt = new Date(secondsSinceEpoch);

        s = _validPathElement.getAttributeValue(SIZE);
        if(s==null)
            throw new IOException("BES returned invalid response the "+ BesApi.SHOW_PATH_INFO+" command. " +
                    "Missing the "+SIZE+" attribute of the '"+VALID_PATH+" 'element in the response.");
        _size = Long.parseLong(s);

        _remainderElement = _pathInfoElement.getChild(REMAINDER, BES.BES_NS);
        if(_remainderElement==null)
            throw new IOException("BES returned invalid response the "+ BesApi.SHOW_PATH_INFO+" command. " +
                    "Missing the "+REMAINDER+" element in the response.");
        _remainder = _remainderElement.getTextTrim();

    }

    public Element getPathInfoElement(){
        return (Element) _pathInfoElement.clone();
    }
    public boolean isDir() { return _isDir; }
    public boolean isFile(){ return _isFile; }
    public boolean isData(){ return _isData; }
    public String validPath() { return _validPath; }
    public String path() { return _path; }
    public String remainder() { return _remainder; }
    public long size(){ return _size; }
    public Date lastModified(){ return _lmt; }
    public boolean isAccessible(){ return _canAccess; }

    @Override
    public String toString(){
        String step = "    ";
        String indent = step;
        StringBuilder sb = new StringBuilder("\"").append(getClass().getSimpleName()).append("\": {\n");
        sb.append(indent).append("\"path\": \"").append(path()).append("\",\n");
        sb.append(indent).append("\"validPath\": \"").append(validPath()).append("\",\n");
        sb.append(indent).append("\"isDir\": ").append(isDir()).append(",\n");
        sb.append(indent).append("\"isFile\": ").append(isFile()).append(",\n");
        sb.append(indent).append("\"isData\": ").append(isData()).append(",\n");
        sb.append(indent).append("\"size\": ").append(size()).append(",\n");
        sb.append(indent).append("\"lastModified\": ").append(lastModified().getTime()).append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    public static String getCacheKey(String path){
        String cacheKey = "{\""+Thread.currentThread().getName()+"\": { \"PathInfo\": \""+path+"\"}}";
        return cacheKey;
    }

    public String getCacheKey(){
        return getCacheKey(_path);
    }

    public boolean isDirectoryRequest(){
        String r = remainder();
        return  isDir() && (r.isEmpty() || r.equals("contents.html") || r.equals("catalog.html") || r.equals("/"));
    }

}
