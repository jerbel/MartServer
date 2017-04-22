package org.occiware.mart.servlet.impl;

import org.occiware.mart.server.exception.ModelValidatorException;
import org.occiware.mart.server.exception.ParseOCCIException;
import org.occiware.mart.server.facade.AbstractOCCIRequest;
import org.occiware.mart.server.facade.OCCIRequest;
import org.occiware.mart.server.facade.OCCIResponse;
import org.occiware.mart.server.parser.ContentData;
import org.occiware.mart.server.parser.HeaderPojo;
import org.occiware.mart.server.utils.Constants;
import org.occiware.mart.server.utils.Utils;
import org.occiware.mart.servlet.utils.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by cgourdin on 11/04/2017.
 *
 */
public class OCCIServletInputParser extends AbstractOCCIRequest implements OCCIRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OCCIServletInputParser.class);

    private HeaderPojo headers;
    private HttpServletRequest request;
    private String requestPath;
    private Map<String, String> requestParameters;

    private boolean onMixinTagLocation = false;

    private boolean onEntityLocation = false;


    /**
     * Define if this is a collection query.
     */
    private boolean collectionQuery;
    private boolean onBoundedLocation = false;
    private boolean onCategoryLocation = false;

    /**
     * Define if the path is an interface query /-/.
     */
    private boolean interfQuery;

    /**
     * Define if the query is an action query.
     */
    private boolean actionInvocationQuery;
    /**
     * Define if there is no content datas, the query is defined with a path (for Get and Delete methods).
     */
    private boolean datasOnlyOnPath;

    public OCCIServletInputParser(OCCIResponse response, String contentType, String username, HttpServletRequest req, HeaderPojo headers, Map<String, String> requestParameters) {
        super(response, contentType, req.getPathInfo(), username);
        this.headers = headers;
        this.request = req;

        this.requestPath = req.getPathInfo();
        if (!requestPath.startsWith("/")) {
            requestPath = "/" + requestPath;
        }
        if (!requestPath.endsWith("/")) {
            requestPath = requestPath + "/";
        }
        this.requestParameters = requestParameters;
    }

    /**
     * Build the data objects for usage in PUT, GET etc. when call findEntity etc.
     */
    @Override
    public void parseInput() throws ParseOCCIException {

        String content = null;
        // For all media type that have content occi build like json, xml, text plain, yml etc..
        if (request == null) {
            throw new ParseOCCIException("No request to parse.");
        }
        // Parse the path
        parsePath();

        if (!interfQuery) {
            // Parse the content body if any.
            switch (contentType) {
                case Constants.MEDIA_TYPE_JSON:
                case Constants.MEDIA_TYPE_JSON_OCCI:
                case Constants.MEDIA_TYPE_TEXT_PLAIN:
                    InputStream in = null;
                    LOGGER.info("Parsing input uploaded datas...");
                    try {
                        in = request.getInputStream();
                        if (in != null) {
                            // throw new ParseOCCIException("The input has no content delivered.");
                            content = Utils.convertInputStreamToString(in);
                            // for Object occiRequest to be fully completed.
                            getInputParser().parseInputToDatas(content);
                        }
                    } catch (IOException ex) {
                        throw new ParseOCCIException("The server cant read the content input --> " + ex.getMessage());
                    } finally {
                        Utils.closeQuietly(in);
                    }
                    break;
                case Constants.MEDIA_TYPE_TEXT_OCCI:
                    // For all media type that have header definition only, known for now is text/occi.
                    if (headers != null && !headers.getHeaderMap().isEmpty()) {
                        // for object occiRequest to be fully completed, the parameter is Map<String, List<String>> encapsulated on MultivaluedMap.)
                        getInputParser().parseInputToDatas(headers);
                    }
                    break;
                default:
                    throw new ParseOCCIException("Cannot parse for " + contentType + " cause: unknown parser");
            }
        }


    }

    /**
     * Parse the path to a data object. a path may have /category/myresource/
     */
    private void  parsePath() {
        // This section is important for Get query and Delete query.

        // Detect if this is an interface request.
        if (requestPath.equals("/.well-known/org/ogf/occi/-/") || requestPath.endsWith("/-/")) {
            interfQuery = true;
            return;
        }

        // Detect if this is an action invocation request
        if (requestParameters != null && requestParameters.get("action") != null) {
            actionInvocationQuery = true;
            return;
        }

        // Detect if this path is on an existing entity path.
        if (this.isEntityLocation(requestPath)) {
            onEntityLocation = true;
            return;
        }

        // Detect if this path is on an existing mixin tag definition location.
        if (this.isMixinTagLocation(requestPath)) {
            onMixinTagLocation = true;
            return;
        }

        // Detect if the path is on a category (mixin, kind) like /myconnector/compute/ or /ipnetwork/ or on known path parent (bounded path).
        collectionQuery = true;
        if (this.isCategoryLocation(requestPath)) {
            onCategoryLocation = true;
        } else {
            onBoundedLocation = true;
        }

    }

    public HeaderPojo getHeaders() {
        return headers;
    }

    public void setHeaders(HeaderPojo headers) {
        this.headers = headers;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }


    public boolean isCollectionQuery() {
        return collectionQuery;
    }

    public void setCollectionQuery(boolean collectionQuery) {
        this.collectionQuery = collectionQuery;
    }

    public boolean isInterfQuery() {
        return interfQuery;
    }

    public void setInterfQuery(boolean interfQuery) {
        this.interfQuery = interfQuery;
    }

    public boolean isActionInvocationQuery() {
        return actionInvocationQuery;
    }

    public void setActionInvocationQuery(boolean actionInvocationQuery) {
        this.actionInvocationQuery = actionInvocationQuery;
    }

    public boolean isDatasOnlyOnPath() {
        return datasOnlyOnPath;
    }

    public void setDatasOnlyOnPath(boolean datasOnlyOnPath) {
        this.datasOnlyOnPath = datasOnlyOnPath;
    }
}
