package opendap.bes.singleProcess;

import opendap.bes.dap2Responders.BesApi;
import opendap.bes.BESManager;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;

import javax.servlet.ServletException;
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

    /**
     * Makes a the default BES configuration prcedurally.
     *
     * <BESManager>
     * <BES>
     * <prefix>/</prefix>
     * <host>localhost</host>
     * <port>10022</port>
     * <maxResponseSize>0</maxResponseSize>
     * <ClientPool maximum="200" maxCmds="2000" />
     * </BES>
     * </BESManager>
     **/
    private static Element getBesConfig() {
        Element bes = new Element("BES");

        Element prefix = new Element("prefix");
        prefix.setText("/");
        bes.addContent(prefix);

        Element host = new Element("host");
        host.setText("localhost");
        bes.addContent(host);

        Element port = new Element("port");
        port.setText("10022");
        bes.addContent(port);

        Element maxResponseSize = new Element("maxResponseSize");
        maxResponseSize.setText("0");
        bes.addContent(maxResponseSize);

        Element clientPool = new Element("ClientPool");
        clientPool.setAttribute("maximum", "200");
        clientPool.setAttribute("maxCmds", "2000");
        bes.addContent(clientPool);

        Element besManager = new Element("BESManager");
        besManager.addContent(bes);
        return besManager;
    }


    private static void configBesManager() throws Exception {
        BESManager besManager = new BESManager();
        besManager.init(getBesConfig());
    }

    /**
     * @param args
     */
    public static void main(String args[]) throws Exception {
        String datasource = "fnoc1.nc";
        String ce = "time";
        String xdap_accept = "3.2";

        configBesManager();

        BesApi app = new BesApi();

        if (true == app.isConfigured()) {
            System.out.println("BES is Configured!");
        }
        else {
            System.out.println("FAILED to configure BESManager");
        }

        try {
            Document xmlDDS = app.getDDSRequest(datasource, ce, xdap_accept);

            XMLOutputter xmlo = new XMLOutputter(Format.getPrettyFormat());

            xmlo.output(xmlDDS,System.out);

        } catch (Exception BadConfigurationException) {
            System.out.println("Bad Configuration Error");
        }

        System.out.println("Hello World");
    }
}