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

<h1 align="left">Hyrax Access Metrics Configuration Is Required.</h1>
<hr size="1" noshade="noshade"/>
<p>Greetings,</p>

<p>If you're reading this chances are you have just fired up your brand new Hyrax server.</p>
<p>You'll need to take a moment to finish the basic configuration of the server before we can get to the data.</p>
<p>If you do nothing this page will never go away. :)</p>

<h2>Explanation</h2>
<p><span style="font-weight: bold;">OMG! DO SOMETHING! EXPLAIN IT!</span></p>
<p>
    In order to better direct our efforts when improving Hyrax and it's overall performance it's important for us to
    have a clear picture about the actual ways in which the servers are being used.</p>
<p> Some of the questions we would like to answer are:</p>
<ul>
    <li><p>Are the servers seeing lots of small requests?</p></li>
    <li><p>Large requests?</p></li>
    <li><p>What output formats are most popular?</p></li>
    <li><p>Which software clients are used to access tye service?</p></li>
    <li><p>Can the server be modified to be better support the ways in
        which popular clients utilize the service?</p></li>
    <li><p>Can some clients be improved to better utilize the service?</p></li>
    <li><p>Do clients check constrained metadata responses?</p></li>
</ul>

<p>If you would like to help improve Hyrax please consider fully enabling the HyraxMetrics!</p>

<h2>Configuration Instructions</h2>
<p>
    </p>

<div class="para">
    <ol>
        <li><a href="http://docs.opendap.org/index.php/Hyrax_-_OLFS_Configuration#OLFS_Configuration_Location">Locate your <tt>olfs.xml</tt> file.</a></li>
        <li>Edit the <tt>olfs.xml</tt> file and locate the XML element <tt>HyraxMetrics</tt></li>
        <li>Locate the child element <tt>HyraxBaseUrl</tt> and notice that the element has no content:
            <pre class="small">
&lt;HyraxMetrics&gt;
    <span style="font-weight: bold">&lt;HyraxBaseUrl logReporting=&#8221;no&#8221;&gt;&lt;/HyraxBaseUrl&gt;</span>
    &lt;updateIntervalDays&gt;5&lt;/updateIntervalDays&gt;
&lt;/HyraxMetrics&gt;
            </pre>
            By configuring this element you will stop this from being the "one" page.<br/>
            If for some reason the <tt>olfs.xml</tt> does not contain the <tt>HyraxMetrics</tt> element then try
            getting the OLFS to perform a fresh install:<br/>
            <ol>
                <li>Locate your and backup your current configuration directory.
                    <blockquote><tt>tar -cvzf olfs_config.tgz /etc/olfs</tt></blockquote>
                </li>
                <li>Remove the current configuration directory.
                    <blockquote><tt>sudo rm -r /etc/olfs</tt></blockquote>
                </li>
                <li>Recreate the configuration directory.
                    <blockquote>
                        <tt>sudo mkdir /etc/olfs</tt><br/>
                        <tt>chown yer_user /etc/olfs</tt>
                    </blockquote>
                </li>
                <li>Restart Tomcat.</li>
                <li>Start back at the top of this section.</li>
            </ol>
            <br/>
            The next three sections detail your configuration options. <br/><br/>
        </li>

        <li><span style="font-weight: bold">Metrics Disabled</span>
            <div>
                Setting the value of <code>HyraxBaseUrl</code> to "<tt>no</tt>" (or really, to anything other than a
                valid URL) will disable all Hyrax metrics collection.
            <div class="small">
            <pre>
&lt;!-- Hyrax Metrics: Disabled --&gt;
&lt;HyraxMetrics&gt;
    <span style="font-weight: bold">&lt;HyraxBaseUrl logReporting=&#8221;no&#8221;&gt;no&lt;/HyraxBaseUrl&gt;</span>
    &lt;updateIntervalDays&gt;5&lt;/updateIntervalDays&gt;
&lt;/HyraxMetrics&gt;
            </pre>
                </div>

            </div>
        </li>
        <li><span style="font-weight: bold">Metrics "Ping" Only</span>
            <div>
                Setting the value of <tt>HyraxBaseUrl</tt> to the publicly accessible domain name or IP
                address of your server instructs your server to register with OPeNDAP and will enable OPeNDAP
                Inc. servers to find your server's publicly available Hyrax interface to see that the Hyrax instance is running.
                This will help us to get a rough estimate of the numbers of active instances of Hyrax operating in the field.
                <div class="small">
        <pre>
&lt;!-- Hyrax Metrics: "Ping" Only --&gt;
&lt;HyraxMetrics&gt;
    <span style="font-weight: bold">&lt;HyraxBaseUrl logReporting=&#8221;no&#8221;&gt;http://yourserver.org/opendap/&lt;/HyraxBaseUrl&gt;</span>
    &lt;updateIntervalDays&gt;5&lt;/updateIntervalDays&gt;
&lt;/HyraxMetrics&gt;
        </pre>
            </div>
            </div>
        </li>
        <li><span style="font-weight: bold">Best Metrics Collection</span>
            <div>
                If you wish for your server to participate in the OPeNDAP Inc.'s access pattern analysis project then
                simply do everything for the "Ping Only" option above, plus change the value of the
                <tt>logReporting</tt> attribute of the <code>HyraxBaseUrl</code> element
                so that it's value is "true" or "yes". If this is set and value of the <code>HyraxBaseUrl</code> element
                is your server's publicly accessible domain name or IP then the OPeNDAP metrics collection client will
                collect metrics data from the server at the interval defined in the <code>updateIntervalDays</code> element.
                <div class="small">
        <pre>
&lt;!-- Hyrax Metrics: Best Metrics Collection --&gt;
&lt;HyraxMetrics&gt;
    <span style="font-weight: bold">&lt;HyraxBaseUrl logReporting=&#8221;yes&#8221;&gt;http://yourserver.org/opendap/&lt;/HyraxBaseUrl&gt;
    &lt;updateIntervalDays&gt;5&lt;/updateIntervalDays&gt;</span>
&lt;/HyraxMetrics&gt;
        </pre>
            </div>
                </div>
        </li>
        <li>Save the <span style="font-family: monospace;">olfs.xml</span> file.</li>
        <li>Restart the Tomcat.</li>



    </ol>

</div>

<h2>Privacy</h2>
<div class="small">
The Hyrax Metrics log reporting system shares only the following information for each request received by Hyrax:
    <dl>
        <dt><span class="small_bold">User-Agent</span></dt>
        <dd>
            The value of the HTTP "User-Agent" header from the client request. This will help us to associate request
            patterns with the software program that issued them.
        </dd>

        <dt><span class="small_bold">Session ID</span></dt>
        <dd>
            The session identifier, if available. Although the DAP protocol is stateless, we know that many clients
            utilize a number of transactions when accessing a dataset. Session identifiers will help us to see what
            a particular client may do over the course of a data access activity made up of numerous
            individual requests.
        </dd>

        <dt><span class="small_bold">Request Time</span></dt>
        <dd>Time request was received</dd>

        <dt><span class="small_bold">Duration</span></dt>
        <dd>Time request took to complete</dd>

        <dt><span class="small_bold">HTTP Status</span></dt>
        <dd>The HTTP return status of the request.</dd>

        <dt><span class="small_bold">Request Number</span></dt>
        <dd>The request number from a hit counter in Hyrax.</dd>

        <dt><span class="small_bold">HTTP Verb</span></dt>
        <dd>THe HTTP command, typically GET or POST</dd>

        <dt><span class="small_bold">Resource ID</span></dt>
        <dd>The path part of the URL that identifies the resource being accessed.</dd>

        <dt><span class="small_bold">Query String</span></dt>
        <dd>
            Everything after the question mark in the request URL. This is where the various projection,
            selection (filtering), and server side function clauses will appear.
        </dd>

    </dl>
</div>


<h2>Thanks for using our software!</h2>

<p>We hope we hope you find this software useful, and we welcome
    your questions and comments. </p>

<p>To Contact Us:</p>

<p> Technical Support: <a href="mailto:support@opendap.org">support@opendap.org</a></p>

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
