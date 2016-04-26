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

package opendap.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import opendap.coreServlet.ServletUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;


/**
 * User: ndp
 * Date: Feb 6, 2007
 * Time: 3:34:35 PM
 */
public class LogUtil {

    private static boolean isLogInit = false;
    //private static volatile long logID = 0;


    private static String _hyraxAccessLog = "HyraxAccess";


    private static Logger log;
    static{
        System.out.print("+++LogUtil.static - Instantiating Logger ... ");

        try {
            log = org.slf4j.LoggerFactory.getLogger(LogUtil.class);
            System.out.print("Logger instantiated.\n");
        }
        catch(NoClassDefFoundError e) {
            System.err.println("\n\n[ERROR]  +++LogUtil.initLogging() -  Unable to instantiate Logger. java.lang.NoClassDefFoundError: "+e.getMessage()+"  [ERROR]\n");
            throw e;
        }
    }


    private static String getLogbackConfigFileName(String configPath) {


        // set up the log path
        if (!configPath.endsWith("/"))
            configPath += "/";
        String logPath = configPath + "logs";

        File logPathFile = new File(logPath);
        if (!logPathFile.exists()) {
            System.out.println("+++LogUtil.initLogging(): Creating log dir: " + logPath);
            if (!logPathFile.mkdirs()) {
                throw new RuntimeException("Creation of logfile directory failed." + logPath);
            }
        } else {
            System.out.println("+++LogUtil.initLogging(): Found log dir: " + logPath);
        }
        System.out.println("+++LogUtil.initLogging(): Using log dir: " + logPath);


        // read in Log4J/slf4j/logback config file
        System.setProperty("logdir", logPath); // put the location in a well know place.


        String logbackConfig = configPath + "logback-test.xml";
        File f = new File(logbackConfig);
        if (!f.exists()) {
            System.out.println("+++LogUtil.initLogging(): Unable to locate logback configuration: " + logbackConfig);
            logbackConfig = configPath + "logback.xml";
            f = new File(logbackConfig);
            if (!f.exists()) {
                System.out.println("+++LogUtil.initLogging(): Unable to locate logback configuration: " + logbackConfig);
                logbackConfig = null;
            }

        }

        return logbackConfig;






    }

    private static String getLogbackConfigFileName(HttpServlet servlet) {


        ServletContext servletContext = servlet.getServletContext();

        String configPath = ServletUtil.getConfigPath(servlet);

        // set up the log path
        String logPath = configPath + "logs";


        File logPathFile = new File(logPath);
        if (!logPathFile.exists()) {
            if (!logPathFile.mkdirs()) {
                throw new RuntimeException("Creation of logfile directory failed." + logPath);
            }
        }

        // read in Logback config file
        System.setProperty("logdir", logPath); // variable substitution

        String logbackConfig = servletContext.getInitParameter("logbackConfig");
        if (logbackConfig == null) {
            logbackConfig = configPath + "logback-test.xml";
            File f = new File(logbackConfig);
            if (!f.exists()) {
                logbackConfig = configPath + "logback.xml";
                f = new File(logbackConfig);
                if (!f.exists()) {
                    // Try to use the one that shipped with the webapp
                    String defaultLogbackConfig = ServletUtil.getSystemPath(servlet, "WEB-INF/logback.xml");
                    f = new File(defaultLogbackConfig);
                    if (!f.exists())
                        logbackConfig = null;
                    else
                        logbackConfig = defaultLogbackConfig;
                }

            }
        }
        return logbackConfig;

    }

    private static void initializeLoggingFromConfigFile(String logbackConfig){


        try {
            if(logbackConfig != null){
                System.out.println("+++LogUtil.initLogging() - Logback configuration using: "+ logbackConfig);

                LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
                try {
                    JoranConfigurator configurator = new JoranConfigurator();
                    configurator.setContext(lc);
                    // the context was probably already configured by default configuration
                    // rules
                    lc.reset();
                    configurator.doConfigure(logbackConfig);
                } catch (JoranException je) {
                    je.printStackTrace();
                }
                StatusPrinter.printInCaseOfErrorsOrWarnings(lc);

            }
            else {
                System.out.println("+++LogUtil.initLogging() - Logback configuration using logback's " +
                        "default configuration mechanism");
            }

            System.out.println("+++LogUtil.initLogging() - Logback configured.");

        } catch (Exception t) {
            t.printStackTrace();
        }

        System.out.print("+++LogUtil.initLogging() - Insiantiating Logger...");

        try {
            log = org.slf4j.LoggerFactory.getLogger(LogUtil.class);
            log.debug("Testing debug...");
            log.info("Testing info...");
            log.warn("Testing warn...");
            log.error("Testing error...");
        }
        catch(NoClassDefFoundError e) {
            System.out.println("\n\n[ERROR]  +++LogUtil.initLogging() -  Unable to instantiate Logger. java.lang.NoClassDefFoundError: "+e.getMessage()+"  [ERROR]\n");
            throw e;
        }





    }

    /**
     * Initialize logging for the web application context in which the given
     * servlet is running. Two types of logging are supported:
     * <p/>
     * 1) Regular logging using the SLF4J API.
     * 2) Performance logging which can write Apache common logging format logs,
     * use the LogUtil.logServerStartup(String) method.
     * <p/>
     * The log directory is determined by the servlet containers content
     * directory. The configuration of logging is controlled by the log4j.xml
     * file.
     *
     * @param servlet - the servlet.
     */
    public static void initLogging(HttpServlet servlet) {


        // Initialize logging if not already done.
        if (isLogInit)
            return;

        System.out.println("+++LogUtil.initLogging() - BEGIN");

        try {
            String logbackConfig = getLogbackConfigFileName(servlet);
            initializeLoggingFromConfigFile(logbackConfig);
        } catch (Exception t) {
            t.printStackTrace();
        }


        System.out.println("+++LogUtil.initLogging() - END");

        isLogInit = true;


    }


    /**
     * Initialize logging for the web application context in which the given
     * servlet is running. Two types of logging are supported:
     * <p/>
     * 1) Regular logging using the SLF4J API.
     * 2) Performance logging which can write Apache common logging format logs,
     * use the LogUtil.logServerStartup(String) method.
     * <p/>
     * The log directory is determined by the servlet containers content
     * directory. The configuration of logging is controlled by the log4j.xml
     * file.
     *
     * @param configPath - the path to the log4j.xml file
     */
    public static void initLogging(String configPath) {

        // Initialize logging if not already done.
        if (isLogInit)
            return;

        System.out.println("+++LogUtil.initLogging() - path='"+configPath+"'");

        try {
            String logbackConfig = getLogbackConfigFileName(configPath);
            initializeLoggingFromConfigFile(logbackConfig);
        } catch (Exception t) {
            t.printStackTrace();
        }

        System.out.println("+++LogUtil.initLogging() - END");

        isLogInit = true;
    }


    /**
     * Initialize logging for the web application context in which the given
     * servlet is running. Two types of logging are supported:
     * <p/>
     * 1) Regular logging using the SLF4J API.
     * 2) Performance logging which can write Apache common logging format logs,
     * use the LogUtil.logServerStartup(String) method.
     * <p/>
     * The log directory is determined by the servlet containers content
     * directory. The configuration of logging is controlled by the log4j.xml
     * file.
     *
     */
    public static void initLogging() {

        String path = System.getProperty("user.dir")+"/";

        initLogging(path);
    }



    /**
     * Gather current thread information for inclusion in regular logging
     * messages. Call this method only for non-request servlet activities, e.g.,
     * during the init() or destroy().
     * <p/>
     * Use the SLF4J API to log a regular logging messages.
     * <p/>
     * This method gathers the following information:
     * 1) "ID" - an identifier for the current thread; and
     * 2) "startTime" - the system time in millis when this method is called.
     * <p/>
     * The appearance of the regular log messages are controlled in the
     * log4j.xml configuration file.
     * @param source The source id of who started the logging. Typically an init()
     * method.
     *
     */
    public static void logServerStartup(String source) {
        // Setup context.
        synchronized (LogUtil.class) {
            MDC.put("ID", "Server Startup");
            MDC.put("SOURCE", source);
        }
        MDC.put("startTime", System.currentTimeMillis() + "");
        log.info("Logging started.");
    }


    /**
     * Gather current thread information for inclusion in regular logging
     * messages. Call this method only for non-request servlet activities, e.g.,
     * during the init() or destroy().
     * <p/>
     * Use the SLF4J API to log a regular logging messages.
     * <p/>
     * This method gathers the following information:
     * 1) "ID" - an identifier for the current thread; and
     * 2) "startTime" - the system time in millis when this method is called.
     * <p/>
     * The appearance of the regular log messages are controlled in the
     * log4j.xml configuration file.
     * @param source The source id of who started the logging. Typically an init()
     * method.
     *
     */
    public static void logServerShutdown(String source) {
        // Setup context.
        synchronized (LogUtil.class) {
            MDC.put("ID", "Server Startup");
            MDC.put("SOURCE", source);
        }
        MDC.put("startTime", System.currentTimeMillis() + "");
        log.info("Logging started.");
    }

    /**
     * Collects information at the begining of a server access and caches it in a logging object that
     * keeps separate collections for each thread (and therefore each request).
     * @param req
     * @param reqId
     */
    public static void hyraxAccessStart(HttpServletRequest req, String reqId){
        logServerAccessStart(req, reqId);
    }



    public static void hyraxAccessEnd(int status){

        logServerAccessEnd(status);

        //############################################################################################
        // THIS IS THE SPOT WHERE THE ACCESS LOG ENTRY IS WRITTEN!!
        // Doesn't matter what we write to the access_log because the access log formatter ignores
        // it in lieu of the stuff in MDC. All that matters is that we write something.
        Logger hyrax_access_log = org.slf4j.LoggerFactory.getLogger(_hyraxAccessLog);
        hyrax_access_log.info("");

    }


    /**
     * Gather information from the given HttpServletRequest for inclusion in both
     * regular logging messages and THREDDS access log messages. Call this method
     * at start of each doXXX() method (e.g., doGet(), doPut()) in any servlet
     * you implement.
     * This method and sets metadata information about the completed request and stores it in
     * the logging systems MDC object which associates the values with the current thread/request.
     *
     * <p/>
     * Use the SLF4J API to log a regular logging messages. Use the
     * logServerAccess() method to log a THREDDS access log message.
     * <p/>
     * This method gathers the following information:
     * 1) "ID" - an identifier for the current thread;
     * 2) "host" - the remote host (IP address or host name);
     * 3) "userid" - the reqID of the remote user;
     * 4) "startTime" - the system time in millis when this request is started (i.e., when this method is called); and
     * 5) "request" - The HTTP request, e.g., "GET /index.html HTTP/1.1".
     * <p/>
     * The appearance of the regular log messages and the THREDDS access log
     * messages are controlled in the log4j.xml configuration file. For the log
     * messages to look like an Apache server "common" log message, use the
     * following log4j pattern:
     * <p/>
     * "%X{host} %X{ident} %X{userid} [%d{dd/MMM/yyyy:HH:mm:ss}] %X{request} %m%n"
     *
     * @param req     the current HttpServletRequest.
     * @param reqID  The unique ID for this request. Typically a hit counter is adequate..
     */
    private static void logServerAccessStart(HttpServletRequest req, String reqID) {

        HttpSession session = req.getSession(false);


        MDC.put("ID", reqID);
        MDC.put("SOURCE", req.getMethod());
        MDC.put("host", req.getRemoteHost());
        MDC.put("ident", (session == null) ? "-" : session.getId());
        MDC.put("userid", req.getRemoteUser() == null ? "-" : req.getRemoteUser() );
        MDC.put("startTime", System.currentTimeMillis() + "");

        String userAgent = req.getHeader("User-Agent");
        MDC.put("UserAgent",  userAgent==null?"-":userAgent);

        String resourceID =  req.getRequestURI();
        MDC.put("resourceID",resourceID);

        String query = req.getQueryString();
        query = (query == null) ? "" : query;
        MDC.put("query", query);

        log.info("REQUEST START - Remote host: " + req.getRemoteHost() + " - RequestedResource: '" + resourceID + "'  QueryString: '" + query +"'");

    }




    /**
     * Computes and sets metadata information about the completed request and stores it in
     * the logging systems MDC object which associates the values with the current thread.
     *
     * @param httpStatus        - the result code for this request.
     */
    private static void logServerAccessEnd(int httpStatus) {

        long endTime = System.currentTimeMillis();

        MDC.put("http_status", Integer.toString(httpStatus));

        String sTime = MDC.get("startTime");
        long startTime = Long.valueOf(sTime);
        long duration = endTime - startTime;

        MDC.put("duration", Long.toString(duration)+" ms");

        log.info("REQUEST COMPLETE - http_status: " + httpStatus + " duration: "+ duration + " ms");
    }


}
