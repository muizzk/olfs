package opendap.bes.singleProcess;

import opendap.bes.dap2Responders.BesApi;
import opendap.bes.BESManager;
import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;
import java.io.OutputStream;

public class HelloWorld {
    private Document configDoc;
    private org.slf4j.Logger log;

    public static final String DDS = "dds";
    public static final String DAS = "das";
    public static final String DDX = "ddx";
    public static final String XML_ERRORS = "xml";

    public static final String XDAP_ACCEPT_CONTEXT = "xdap_accept";
    public static final String DEFAULT_XDAP_ACCEPT = "2.0";

    
    public static void main(String args[]) {
	String datasource = "fnoc1.nc";
	String ce = "time";
	String xdap_accept = "3.2";

	Element besManagerElement = new Element("BESManager");
	BESManager besManager = new BESManager();
	
	BesApi app = new BesApi();

	if (true == app.isConfigured())
	    {
		System.out.println("BES Configured");
	    }
	else
	    {
		System.out.println("BES not Configured");
	    }

	try {
	   Document xmlDDS = app.getDDSRequest(datasource,ce,xdap_accept);
	}
	catch (Exception BadConfigurationException) {
	    System.out.println("Bad Configuration Error");
	}

	System.out.println("Hello World");
    }
}