package opendap.webstart;

import opendap.bes.BesXmlAPI;
import org.jdom.Element;
import org.slf4j.Logger;

import java.io.*;
import java.util.Scanner;

public class IdvViewerRequestHandler extends JwsHandler {

    private Logger log;
    private String resourcesDir;
    private Element config;

    private String _serviceId = "idv";
    private String _jnlpFileName = _serviceId+".jnlp";

    public void init(Element config, String resourcesDirectory) {

        log = org.slf4j.LoggerFactory.getLogger(getClass());

        resourcesDir = resourcesDirectory;
        this.config = config;
        _jnlpFileName = resourcesDirectory+"/"+ _serviceId+".jnlp";

        File f = new File(_jnlpFileName);

        if(!f.exists()){
            log.error("Missing JNLP file: "+_jnlpFileName);
        }

    }



    public boolean datasetCanBeViewed(String serviceId, String query) {
        log.debug("Checking request. serviceId:"+serviceId+"   query: "+query);
        if(_serviceId.equalsIgnoreCase(serviceId))
            return true;
        else
            return false;
    }

    public String getViewerLinkHtml(String context, String datasetURI) {

        return "<a href='" + context + "/webstart/idv.jnlp?url=" + datasetURI + "'>IDV</a>";
    }


    public String getJnlpForDataset(String query) {

        String queryStart = "dataset=";

        String datasetUrl = "";
        if(query.startsWith(queryStart)){
            datasetUrl = query.substring(queryStart.length(),query.length());
        }


        String  jnlp = "";

        try{
           jnlp= readFileAsString(_jnlpFileName);
        }
        catch (IOException e) {
            log.error("Unable to retrieve JNLP file: "+_jnlpFileName);
        }

        log.debug("Got JNLP:\n"+jnlp);



        jnlp = jnlp.replace("{datasetUrl}",datasetUrl);

        log.debug("Tweaked JNLP:\n"+jnlp);


        return jnlp;


    }



}