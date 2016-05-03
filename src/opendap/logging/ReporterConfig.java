package opendap.logging;

import opendap.coreServlet.ServletUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class encapsulates a the configuration for the anonymized log API.
 *
 * The configuration is designed with a tri-state switch:
 *
 * If the HyraxBaseUrl element has no content then the system has not been configured. (state 0)
 * If the HyraxBaseUrl element has anything that does not parse as a URL in it then no log or ping
 * access will be enabled e.g. do nothing. (state 1)
 * If the HyraxBaseUrl element has a URL as its content then the log reporter will be enabled. (state 2)
 *
 * If log Reporting is enabled then the ping service will be enabled, and additionally if the
 * logReporting attribute is set to some form of "yes" or "true" then the log reporting feature will be enabled.
 *
 *
 *
 * The content of configuration (in XML form):
 *
 *
 <pre>
 // Must Configure - this is what will ship.
 <HyraxMetrics>
 <HyraxBaseUrl logReporting=”no”></HyraxBaseUrl>
 <updateIntervalDays>5</updateIntervalDays>
 </HyraxMetrics>

 // Do nothing
 <HyraxMetrics>
 <HyraxBaseUrl logReporting=”no”>no</HyraxBaseUrl>
 <updateIntervalDays>5</updateIntervalDays>
 </HyraxMetrics>

 // Ping only
 <HyraxMetrics>
 <HyraxBaseUrl logReporting=”no” >
 http://test.opendap.org/opendap
 </HyraxBaseUrl>
 <updateIntervalDays>5</updateIntervalDays>
 </HyraxMetrics>


 // Best Metrics Collection
 <HyraxMetrics>
 <HyraxBaseUrl logReporting=”yes” >
 http://test.opendap.org/opendap
 </HyraxBaseUrl>
 <updateIntervalDays>5</updateIntervalDays>
 </HyraxMetrics>
 </pre>

 */
public class ReporterConfig implements ServletContextListener {



    public static final long DEFAULT_UPDATE_INTERVAL = 5;

    private static AtomicBoolean _logReportingEnabled = new AtomicBoolean(false);

    private static URL _hyraxBaseUrl =  null;

    private static AtomicLong _updateIntervalDays = new AtomicLong(DEFAULT_UPDATE_INTERVAL);

    private static AtomicBoolean _configured =  new AtomicBoolean(false);
    private static AtomicBoolean _enabled =  new AtomicBoolean(false);


    private static ReentrantLock lock = new ReentrantLock();

    private static AtomicBoolean _initialized = new AtomicBoolean(false);


    public static String status() {
        StringBuilder sb = new StringBuilder();

        sb.append("[getHyraxBaseUrl()=").append(getHyraxBaseUrl()==null?null:getHyraxBaseUrl().toString()).append("]");
        sb.append("[isConfigured()=").append(isConfigured()).append("]");
        sb.append("[isReportingEnabled()=").append(isReportingEnabled()).append("]");
        sb.append("[isLogReportingEnabled()=").append(isLogReportingEnabled()).append("]");
        sb.append("[getUpdateIntervalDays()=").append(getUpdateIntervalDays()).append("]");
        return sb.toString();
    }

    public static URL getHyraxBaseUrl() {
        Logger log = LoggerFactory.getLogger(ReporterConfig.class);
        URL retVal = null;

        lock.lock();
        try {
            if (_hyraxBaseUrl != null){
                try {
                    retVal = new URL(_hyraxBaseUrl.getProtocol(), _hyraxBaseUrl.getHost(), _hyraxBaseUrl.getPort(), _hyraxBaseUrl.getFile());
                } catch (MalformedURLException e) {
                    log.error("getHyraxBaseUrl(): OUCH! Unable to build a URL from a URL! This shouldn't happen.");
                }
            }
            return retVal;

        }
        finally {
            lock.unlock();
        }
    }

    /**
     * @return True if a configuration has been processed
     */
    public static boolean isConfigured(){
        return _configured.get();
    }


    /**
     * @return True if basic reporting is enabled. This means that when the server starts up it will register with the
     * OPeNDAP metrics collection service and that the metrics collection service will come looking to see if the
     * server is up.
     */
    public static boolean isReportingEnabled(){
        return _enabled.get();
    }

    /**
     *
     * @return Returns true is the configuration has indicated that the retrieval of anonymous log information by the
     * authorized server will be allowed. If <tt>isReporting()</tt> returns false then no log reporting should be
     * allowed.
     */
    public static boolean isLogReportingEnabled(){
        return _logReportingEnabled.get();
    }

    /**
     *
     * @return The interval (in days) at which the metrics collection service should ping and (if enabled) collect the
     * logs.
     */
    public static long getUpdateIntervalDays(){
        return _updateIntervalDays.get();
    }


    /**
     * This is a run once method that will process the passed <tt>HyraxMetrics</tt> configuration element and configure
     * the state of the permissible Hyrax Metrics collection for a particular instance of the OLFS.
     *
     * @param config The <tt>HyraxMetrics</tt> configuration element (usually found in the olfs.xml file)
     */
    public static void processConfig(Element config)  {
        Logger log = LoggerFactory.getLogger(ReporterConfig.class);

        lock.lock();
        try {

            // Only one pass through the init() call if the thing is configured at all.
            if(_initialized.get()) {
                log.error("processConfig(): This method has already been called and the ReporterConfiguration processed. Doing NOTHING.");
                return;
            }

            if(config==null){
                log.error("processConfig(): The passed configuration Element was null! Expected an HyraxMetrics node. No Actions taken");
                return;
            }

            Element updateIntervalDaysElement = config.getChild("updateIntervalDays");
            if(updateIntervalDaysElement!=null){
                String intervalString = updateIntervalDaysElement.getTextTrim();
                if(!intervalString.isEmpty()){
                    try {
                        _updateIntervalDays.set(Long.parseLong(intervalString));
                    }
                    catch (NumberFormatException e){
                        _updateIntervalDays.set(DEFAULT_UPDATE_INTERVAL);
                        log.error("processConfig(): Unable to parse the value of the 'updateIntervalDays' element as a long. " +
                                "Setting update interval to default value of {} days.",_updateIntervalDays.get());
                    }
                }

            }

            Element hyraxBaseUrlElement = config.getChild("HyraxBaseUrl");
            if(hyraxBaseUrlElement!=null) {
                String hyraxBaseUrl = hyraxBaseUrlElement.getTextTrim();
                if(hyraxBaseUrl.isEmpty()){
                    _hyraxBaseUrl = null;
                    _configured.set(false);
                    log.error("processConfig(): The HyraxMetrics component of the olfs.xml file has NOT been configured!");
                }
                else {
                    _configured.set(true);
                    log.info("processConfig(): The HyraxMetrics component of the olfs.xml file has been configured.");
                    try {
                        URL url = new URL(hyraxBaseUrl);
                        _hyraxBaseUrl = url;
                        _enabled.set(true);
                        log.info("processConfig(): The logReporting 'ping' activity has been ENABLED.");
                    } catch (MalformedURLException e) {
                        // If we are here it's not a valid URL and that means that LogReporting is off.
                        _hyraxBaseUrl =  null;
                        _enabled.set(false);
                    }
                }

                String s = hyraxBaseUrlElement.getAttributeValue("logReporting");

                if(s!=null){
                    if(s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("y")){
                        log.info("processConfig(): The logReporting attribute was set to '{}', logReporting is ENABLED.",s);
                        _logReportingEnabled.set(true);
                    }
                    else {
                        log.info("processConfig(): The logReporting attribute was set to '{}', logReporting is DISABLED.",s);
                        _logReportingEnabled.set(false);
                    }

                }
                else {
                    log.error("processConfig(): The HyraxBaseUrl element of the HyraxMetrics element in the olfs.xml file is " +
                            "MISSING the 'logReporting' attribute. Disabling logReporting (settng to 'no')");
                    _logReportingEnabled.set(false);

                }

            }
            else {
                _hyraxBaseUrl = null;
                _configured.set(false);
                log.error("processConfig(): The HyraxMetrics component is MISSING the HyraxBaseUrl element. all logReporting activities are DISABLED!");
            }

            _initialized.set(true);

        }
        finally {
            lock.unlock();
        }

    }



    public void contextInitialized(ServletContextEvent arg0) {
        ServletContext context = arg0.getServletContext();
        Logger log = LoggerFactory.getLogger(this.getClass());
        Document configDoc;
        String filename = ServletUtil.getConfigPath(context) + "olfs.xml";

        log.debug("Loading Configuration File: " + filename);

        try {
            configDoc = opendap.xml.Util.getDocument(filename);
            log.debug("loadConfiguration(): Configuration loaded and parsed.");
            Element root = configDoc.getRootElement();
            if(root==null){
                String msg = "The configuration document \""+ filename+ "\" appears to be empty :(";
                log.error(msg);
                return;
            }
            Element config = root.getChild("HyraxMetrics");
            processConfig(config);

        } catch (IOException | JDOMException e) {
            String msg = "Caught "+e.getClass().getName()+"  when attempting to access \""+ filename+ "\" :( Message: "+e.getMessage();
            log.error(msg);
        }

        log.info("loadConfiguration(): ReporterConfig.status() {}",status());

        return;
    }

    public void contextDestroyed(ServletContextEvent arg0) {
        return;
    }




}
