<%--
  ~ /////////////////////////////////////////////////////////////////////////////
  ~ // This file is part of the "Hyrax Data Server" project.
  ~ //
  ~ //
  ~ // Copyright (c) 2013 OPeNDAP, Inc.
  ~ // Author: Nathan David Potter  <ndp@opendap.org>
  ~ //
  ~ // This library is free software; you can redistribute it and/or
  ~ // modify it under the terms of the GNU Lesser General Public
  ~ // License as published by the Free Software Foundation; either
  ~ // version 2.1 of the License, or (at your option) any later version.
  ~ //
  ~ // This library is distributed in the hope that it will be useful,
  ~ // but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  ~ // Lesser General Public License for more details.
  ~ //
  ~ // You should have received a copy of the GNU Lesser General Public
  ~ // License along with this library; if not, write to the Free Software
  ~ // Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
  ~ //
  ~ // You can contact OPeNDAP, Inc. at PO Box 112, Saunderstown, RI. 02874-0112.
  ~ /////////////////////////////////////////////////////////////////////////////
  --%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page session="false" %>
<html>
<% String contextPath = request.getContextPath(); %>
<head>
    <title>OPeNDAP Hyrax</title>
    <link rel='stylesheet' href='<%= contextPath %>/docs/css/contents.css' type='text/css'/>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
</head>

<body>
<a href="http://www.opendap.org"><img src="<%= contextPath %>/docs/images/logo.gif" alt="Logo" width="206" height="93"
                                      border="0"/></a>

<h1 align="left">Hyrax - Access Metrics Configuration Is Required</h1>
<hr size="1" noshade="noshade"/>
<p>Greetings,</p>

<p>If you're reading this chances are you have just fired up your brand new Hyrax server.</p>
<p>You'll need to take a moment to finish the basic configuration of the server before we can get to the data.</p>
<h2>Explanation</h2>
<p>OMG DO SOMETHING! EXPLAIN IT!</p>
<p>In order to better direct our efforts in improving Hyrax and it's overall performance it's importnt for us to </p>
<h2>Configuration Instructions</h2>
<p>
If you do nothing this page will never go away. :)
<ul>
    <li><p><a href="http://docs.opendap.org/index.php/Hyrax_-_OLFS_Configuration#OLFS_Configuration_Location">Locate your <tt>olfs.xml</tt> file.</a></p></li>
    <li><p>Edit the <tt>olfs.xml</tt> file and locate the XML element <tt>HyraxMetrics</tt>.</p></li>
    <li><p>Locate the child element <tt>HyraxBaseUrl</tt>.<br />
        Notice that it has no content:</p>
        <pre>
// Must Configure  (This is what ships)
&lt;HyraxMetrics&gt;
    &lt;HyraxBaseUrl logReporting=&#8221;no&#8221;&gt;&lt;/HyraxBaseUrl&gt;
    &lt;updateIntervalDays&gt;5&lt;/updateIntervalDays&gt;
&lt;/HyraxMetrics&gt;
        </pre>
    </li>
    <li><p>
        Setting the value of <code>HyraxBaseUrl</code> to no (or, to anything other than a
        valid URL) will disable all Hyrax access metrics.
        </p>
        <pre>
// Do nothing
&lt;HyraxMetrics&gt;
    &lt;HyraxBaseUrl logReporting=&#8221;no&#8221;&gt;no&lt;/HyraxBaseUrl&gt;
    &lt;updateIntervalDays&gt;5&lt;/updateIntervalDays&gt;
&lt;/HyraxMetrics&gt;
        </pre>
    </li>
    <li><p>
        Setting the value of <tt>HyraxBaseUrl</tt> to the publicly accessible domain name or IP
        address of your server instructs your server to register with OPeNDAP and will enable OPeNDAP
        Inc. servers check the publicly available Hyrax interface to see that the Hyrax instance is running.
        </p>
        <pre>
// Ping only
&lt;HyraxMetrics&gt;
    &lt;HyraxBaseUrl logReporting=&#8221;no&#8221;&gt;http://yourserver.org/opendap/&lt;/HyraxBaseUrl&gt;
    &lt;updateIntervalDays&gt;5&lt;/updateIntervalDays&gt;
&lt;/HyraxMetrics&gt;
        </pre>
    </li>
    <li>
        <p>If you wish of your server to participate in the OPeNDAP Inc.'s access pattern analysis project then
        simply change the value of the <tt>logReporting</tt> attribute of the <code>HyraxBaseUrl</code> element
        so that it's value is "true" or "yes". If this is set and value of the <code>HyraxBaseUrl</code> element
        is your server publicly accessible domain name or IP then the OPeNDAP servers will collect metrics data
        from the server at the interval defined in the <code>updateIntervalDays</code> element.
        </p>
        <pre>
// Best Metrics Collection
&lt;HyraxMetrics&gt;
    &lt;HyraxBaseUrl logReporting=&#8221;yes&#8221;&gt;http://test.opendap.org/opendap&lt;/HyraxBaseUrl&gt;
    &lt;updateIntervalDays&gt;5&lt;/updateIntervalDays&gt;
&lt;/HyraxMetrics&gt;
        </pre>
    </li>
    <li><p>Save the file.</p></li>
    <li><p>Restart the Tomcat.</p></li>



</ul>
</p>

<h3>Thanks for using our software!</h3>

<p>We hope we hope you find this software useful, and we welcome
    your questions and comments. </p>

<p>To Contact Us:</p>

<p> Technical Support: <a href="mailto:support@opendap.org">support@opendap.org</a></p>

<p>&nbsp;</p>

<p>Hyrax Java Development: </p>
<blockquote>
    <p><strong>OLFS</strong>: ndp &lt;AT&gt; opendap &lt;DOT&gt; org </p>
</blockquote>
<p>Hyrax C++ Development: </p>
<blockquote>
    <p><strong>BES</strong>: jgallagher &lt;AT&gt; opendap &lt;DOT&gt; edu </p>

    <p><strong>Libdap</strong>: jgallagher &lt;AT&gt; opendap &lt;DOT&gt; org </p>
</blockquote>
<p>&nbsp;</p>
<br/>

<h1>Sponsorship</h1>

<p> OPeNDAP Hyrax development is sponsored by:</p>
<blockquote>
    <blockquote>
        <p><a href="http://www.nsf.gov"><img src="<%= contextPath %>/docs/images/nsf-logo.png" alt="NSF" width="95" height="95" border="0"
                                             align="middle" longdesc="http://www.nsf.gov"/></a> <span class="style8"><a
                href="http://www.nsf.gov">National Science Foundation</a></span></p>

        <p><a href="http://www.nasa.gov"><img src="<%= contextPath %>/docs/images/nasa-logo.jpg" alt="NASA" width="97" height="80" border="0"
                                              align="middle" longdesc="http://www.nasa.gov"/><span class="style8">National Aeronautics and Space Administration</span></a>
        </p>

        <p><a href="http://www.noaa.gov"><img src="<%= contextPath %>/docs/images/noaa-logo.jpg" alt="NOAA" width="90" height="90" border="0"
                                              align="middle" longdesc="http://www.noaa.gov"/></a> <span
                class="style8"><a href="http://www.noaa.gov">National Oceanic and Atmospheric Administration</a></span>
        </p>
    </blockquote>
</blockquote>
<hr size="1" noshade="noshade"/>
<h3>&nbsp;</h3>
<blockquote>
    <blockquote>
        <p>&nbsp;</p>

        <p>&nbsp;</p>
    </blockquote>
</blockquote>
</body>
</html>
