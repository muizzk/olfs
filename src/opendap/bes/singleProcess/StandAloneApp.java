package opendap.bes.singleProcess;

import opendap.bes.dap2Responders.BesApi;
import opendap.bes.BESManager;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public class StandAloneApp {
    private Document configDoc;
    private org.slf4j.Logger log;

    public static final String DDS = "dds";
    public static final String DAS = "das";
    public static final String DDX = "ddx";
    public static final String XML_ERRORS = "xml";

    public static final String XDAP_ACCEPT_CONTEXT = "xdap_accept";
    public static final String DEFAULT_XDAP_ACCEPT = "2.0";

    public static final int bufSize = 8192;

    static {
        //System.loadLibrary("myjni");
        System.loadLibrary("bes_dispatch");
        System.loadLibrary("bes_standalone");
    }

    private native void sayHello();
    private native void write(ByteBuffer request, ByteBuffer result);
    private native double size(ByteBuffer buffer);

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
    private  Element getBesConfig() {
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


    private  void configBesManager() throws Exception {
        BESManager besManager = new BESManager();
        besManager.init(getBesConfig());
    }

    /**
     *  * @param args
     */
    public static void main(String args[]) throws Exception {
        String datasource = "fnoc1.nc";
        String ce = "time";
        String xdap_accept = "3.2";

        ByteBuffer requestBuf = ByteBuffer.allocateDirect(bufSize);
        ByteBuffer responseBuf = ByteBuffer.allocateDirect(bufSize);

       opendap.bes.singleProcess.StandAloneApp hw = new opendap.bes.singleProcess.StandAloneApp();

        hw.sayHello();

        hw.configBesManager();

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

            CharBuffer cb = requestBuf.asCharBuffer();
            //cb.append(xmlo.outputString(xmlDDS));

            double nSize = hw.size(requestBuf);
            System.out.println("BufferSize:"+nSize);

            hw.write(requestBuf,responseBuf);

            System.out.println("Nothing burning...");

            for (int i = 100; i > 0; i--) {
                System.out.println("DB[" + i + "]= " + responseBuf.get(i));
            }

        } catch (Exception e) {
            System.err.println("Caught "+e.getClass().getName()+" message: "+e.getMessage());
        }

    }
}