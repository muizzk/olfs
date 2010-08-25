package opendap.semantics.IRISail;

import net.sf.saxon.s9api.SaxonApiException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: ndp
 * Date: Aug 5, 2010
 * Time: 12:05:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class RdfImporter {

    private Logger log;


    private HashSet<String> downService;
    private Vector<String>  imports;


    public RdfImporter() {
        log = org.slf4j.LoggerFactory.getLogger(this.getClass());
        downService = new HashSet<String>();
        imports = new Vector<String>();
    }

    public void reset() {
        downService.clear();
        imports.clear();
    }

    /**
     * ****************************************
     * Update repository
     */
    public boolean importReferencedRdfDocs(IRISailRepository repository, Vector<String> notImport) {

        boolean repositoryChanged = false;

        Vector<String> rdfDocList = new Vector<String>();

        findNeededRDFDocuments(repository, rdfDocList, notImport);

        while (!rdfDocList.isEmpty()) {
            repositoryChanged = true;

            addNeededRDFDocuments(repository, rdfDocList);

            findNeededRDFDocuments(repository, rdfDocList, notImport);
        }

        return repositoryChanged;
    }


    /**
     * Find all rdfcache:RDFDocuments that are referenced by existing documents in the repository.
     *
     * @param repository
     * @param rdfDocs
     */
    private void findNeededRDFDocuments(IRISailRepository repository, Vector<String> rdfDocs, Vector<String> noImports) {
        TupleQueryResult result = null;
        List<String> bindingNames;
        RepositoryConnection con = null;
        if (noImports != null) {
            imports.addAll(noImports);
        }
        try {
            con = repository.getConnection();

            String queryString = "(SELECT doc "
                    + "FROM {doc} rdf:type {rdfcache:StartingPoint} "
                    + "union "
                    + "SELECT doc "
                    + "FROM {tp} rdf:type {rdfcache:StartingPoint}; rdfcache:dependsOn {doc}) "
                    + "MINUS "
                    + "SELECT doc "
                    + "FROM CONTEXT rdfcache:cachecontext {doc} rdfcache:last_modified {lastmod} "
                    + "USING NAMESPACE "
                    + "rdfcache = <" + RepositoryUtility.rdfCacheNamespace + ">";

            log.debug("Query for NeededRDFDocuments: " + queryString);

            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SERQL,
                    queryString);

            result = tupleQuery.evaluate();

            while (result.hasNext()) {
                BindingSet bindingSet = result.next();

                Value firstValue = bindingSet.getValue("doc");
                String doc = firstValue.stringValue();

                if (!rdfDocs.contains(doc) && !imports.contains(doc)
                        && !downService.contains(doc)
                        && doc.startsWith("http://")) {
                    rdfDocs.add(doc);

                    log.debug("Adding to rdfDocs: " + doc);
                }
            }

        } catch (QueryEvaluationException e) {
            log.error("Caught an QueryEvaluationException! Msg: "
                    + e.getMessage());

        } catch (RepositoryException e) {
            log.error("Caught RepositoryException! Msg: " + e.getMessage());
        } catch (MalformedQueryException e) {
            log.error("Caught MalformedQueryException! Msg: " + e.getMessage());
        }

        finally {
            if (result != null) {
                try {
                    result.close();
                } catch (QueryEvaluationException e) {
                    log.error("Caught a QueryEvaluationException! Msg: "
                            + e.getMessage());
                }
            }

            try {
                con.close();
            } catch (RepositoryException e) {
                log.error("Caught a RepositoryException! in findNeededRDFDocuments() Msg: "
                        + e.getMessage());
            }
        }

        log.info("Number of needed files identified:  "
                + rdfDocs.size());

    }


    /**
     * Add the each of the RDF documents whose URL's are in the passed Vector to the Repository.
     *
     * @param repository
     * @param rdfDocs
     */
    private void addNeededRDFDocuments(IRISailRepository repository, Vector<String> rdfDocs) {
        URI uriaddress;
        long inferStartTime, inferEndTime;
        inferStartTime = new Date().getTime();

        String importURL = null;
        RepositoryConnection con = null;
        int notimport = 0;
        String contentType = "";


        try {
            con = repository.getConnection();

            log.debug("rdfDocs.size=" + rdfDocs.size());
            notimport = 0;
            while (!rdfDocs.isEmpty()) {
                importURL = rdfDocs.remove(0).toString();

                log.debug("Checking import URL: " + importURL);

                if (downService.contains(importURL)) {
                    log.error("Previous server error, Skipping " + importURL);
                }
                else {

                    URL myurl = new URL(importURL);


                    int rsCode;
                    HttpURLConnection hc = (HttpURLConnection) myurl.openConnection();
                    log.debug("Connected to import URL: " + importURL);

                    rsCode = hc.getResponseCode();
                    contentType = hc.getContentType();
                    InputStream importIS = hc.getInputStream();

                    hc.disconnect();

                    log.debug("Got HTTP status code: " + rsCode);
                    log.debug("Got Content Type:     " + contentType);

                    if (rsCode == -1) {
                        log.error("Unable to get an HTTP status code for resource "
                                + importURL + " WILL NOT IMPORT!");
                        downService.add(importURL);

                    } else if (rsCode != 200) {
                        log.error("Error!  HTTP status code " + rsCode + " Skipping importURL " + importURL);
                        downService.add(importURL);
                    } else {

                        log.debug("Import URL appears valid ( " + importURL + " )");
                        //@todo make this a more robust

                        if (importURL.endsWith(".owl") || importURL.endsWith(".rdf")) {

                            uriaddress = new URIImpl(importURL);

                            URL url;

                            url = new URL(importURL);

                            log.info("Importing URL " + url);
                            con.add(url, importURL, RDFFormat.RDFXML,
                                    (Resource) uriaddress);
                            repository.setLTMODContext(importURL, con); // set last modified
                            // time of the context
                            repository.setContentTypeContext(importURL, contentType, con); //

                            log.info("Finished importing URL " + url);
                            imports.add(importURL);


                        } else if (importURL.endsWith(".xsd")) {

                            uriaddress = new URIImpl(importURL);

                            ByteArrayInputStream inStream;
                            log.info("Transforming URL " + importURL);
                            inStream = new ByteArrayInputStream(repository
                                    .transformXSD(importURL).toByteArray());
                            log.info("Finished transforming URL " + importURL);
                            log.debug("Importing URL " + importURL);
                            con.add(inStream, importURL, RDFFormat.RDFXML,
                                    (Resource) uriaddress);
                            repository.setLTMODContext(importURL, con); // set last modified
                            // time for the context
                            repository.setContentTypeContext(importURL, contentType, con); //
                            log.debug("Finished importing URL " + importURL);
                            imports.add(importURL);


                        } else if (importURL.endsWith("+psdef/")) {

                            uriaddress = new URIImpl(importURL);

                            ByteArrayInputStream inStream;
                            log.info("Transforming RDFa " + importURL);

                            inStream = new ByteArrayInputStream(repository.transformRDFa(importURL).toByteArray());

                            log.info("Finished transforming RDFa " + importURL);
                            log.debug("Importing RDFa " + importURL);
                            con.add(inStream, importURL, RDFFormat.RDFXML,
                                    (Resource) uriaddress);

                            repository.setLTMODContext(importURL, con); // set last modified
                            // time for the context
                            repository.setContentTypeContext(importURL, contentType, con); //
                            log.debug("Finished importing URL " + importURL);\
                            imports.add(importURL);


                        } else {

                            //urlc.setRequestProperty("Accept",
                            //                "application/rdf+xml,application/xml,text/xml,*/*");
                            // urlc.setRequestProperty("Accept",
                            // "application/rdf+xml, application/xml;
                            // q=0.9,text/xml; q=0.9, */*; q=0.2");

                            try {
                                InputStream inStream = hc.getInputStream();

                                uriaddress = new URIImpl(importURL);
                                if ((contentType != null) &&
                                        (contentType.equalsIgnoreCase("text/xml") ||
                                                contentType.equalsIgnoreCase("application/xml") ||
                                                contentType.equalsIgnoreCase("application/rdf+xml"))
                                        ) {
                                    con.add(inStream, importURL, RDFFormat.RDFXML, (Resource) uriaddress);
                                    repository.setLTMODContext(importURL, con);
                                    log.info("Imported non owl/xsd = " + importURL);
                                    imports.add(importURL);

                                } else {
                                    log.warn("SKIPPING Import URL '" + importURL + " It does not appear to reference a " +
                                            "document that I know how to process.");
                                    notimport++;

                                }
                            } catch (IOException e) {
                                log.error("Caught an IOException! in urlc.getInputStream() Msg: "
                                        + e.getMessage());

                            }

                            log.info("Imported non owl/xsd = " + importURL);
                            log.info("Total non owl/xsd Nr = " + notimport);
                        }
                    }
                } // while (!rdfDocs.isEmpty()
            }
        } catch (Exception e) {
            log.error("addNeededRDFDocuments(): Caught "+e.getClass().getName()+" Message: " + e.getMessage());
        } finally {
            try {
                if (importURL != null && !imports.contains(importURL))
                    downService.add(importURL); //skip this file

                if (con != null)
                    con.close();
            } catch (RepositoryException e) {
                log.error("Caught an RepositoryException! in addNeededRDFDocuments() Msg: "
                        + e.getMessage());
            }
            inferEndTime = new Date().getTime();
            double inferTime = (inferEndTime - inferStartTime) / 1000.0;
            log.debug("Import takes " + inferTime + " seconds");
        }
    }


}
