package pl.sointeractive.isaacloud;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.exceptions.InvalidConfigException;
import pl.sointeractive.isaacloud.exceptions.InvalidQueryParameterException;
import pl.sointeractive.isaacloud.exceptions.InvalidRequestMethodException;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public class Isaacloud extends Connector {

    private final static String TAG = Isaacloud.class.getSimpleName();

    /**
     * Constructor for default com.isaacloud.
     *
     * @param config - map that contains clientID:clientSecret
     * @throws InvalidConfigException
     */
    public Isaacloud(Map<String, String> config) throws InvalidConfigException {
        super("https://api.isaacloud.com", "https://oauth.isaacloud.com", "v1",
                config);
    }

    /**
     * Call to api methods.
     *
     * @param uri        path to resource
     * @param method     REST method type
     * @param parameters query parameters
     * @param body       body json object
     * @return api json response
     * @throws java.io.IOException          connection problem
     * @throws IsaacloudConnectionException response had an error code
     */
    @Deprecated
    public HttpResponse api(String uri, String method,
                            Map<String, Object> parameters, JSONObject body)
            throws IOException, IsaaCloudConnectionException {
        return this.callService(uri, method, parameters, body.toString());
    }

    /**
     * @param uri resource we want to access
     * @return object representing the resource
     */
    public Caller path(String uri) {
        return new Caller(uri);
    }

    /**
     * Push new event into Queue.
     *
     * @param subjectId   subject Id, id of resource
     * @param subjectType GROUP or USER
     * @param priority    PRIORITY_LOW, PRIORITY_MEDIUM, PRIORITY_NORMAL, PRIORITY_HIGH,
     *                    PRIORITY_CRITICAL, PRIORITY_BLOCKER
     * @param sourceId    transaction source id
     * @param type        NORMAL, GROUP, DEBUG, FORCED
     * @param body        event body
     * @return response with the created event.
     * @throws java.io.IOException          connection problem
     * @throws JSONException
     * @throws IsaacloudConnectionException response had an error code
     */
    public HttpResponse event(long subjectId, String subjectType,
                              String priority, long sourceId, String type, JSONObject body)
            throws IOException, IsaaCloudConnectionException, JSONException {

        // compose event
        JSONObject obj = new JSONObject();
        if (body != null)
            obj.put("body", body);
        if (priority != null)
            obj.put("priority", priority);

        obj.put("sourceId", sourceId);
        obj.put("subjectId", subjectId);

        if (subjectType != null)
            obj.put("subjectType", subjectType);

        if (type != null)
            obj.put("type", type);

        // Send event into queue
        return callService("/queues/events", Connector.POST,
                new HashMap<String, Object>(), obj.toString());

    }

    /**
     * Get base api url.
     *
     * @return url to api
     */
    public String getApiUrl() {
        return this.baseUrl;
    }

    /**
     * Get base oauth url.
     *
     * @return oauth url
     */
    public String getOauthUrl() {
        return oauthUrl;
    }

    /**
     * Get base version.
     *
     * @return api version
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Get token.
     *
     * @return token as string
     * @throws java.io.IOException          connection error
     * @throws IsaacloudConnectionException response had an error code
     */
    public String getToken() throws IOException, IsaaCloudConnectionException {
        return getAuthentication();
    }

    /**
     * Class for building the request.
     */
    public class Caller {

        /**
         * Path to resource.
         */
        protected String path;

        /**
         * Parameters.
         */
        protected Map<String, Object> parameters = new HashMap<String, Object>();

        /**
         * Simple constructor.
         *
         * @param _path path to resource.
         */
        public Caller(String _path) {
            this.path = _path;
        }

        /**
         * narrows the result set to contain only json fields, which are in the
         * list of the method
         *
         * @param fields list of field names
         * @return updated Caller object
         */
        public Caller withFields(String... fields) {
            parameters.put("fields", Arrays.asList(fields));
            return this;
        }

        /**
         * Declares exactly which fields in custom fields should be shown.
         *
         * @param customs list of field names
         * @return updated Caller object
         */
        public Caller withCustoms(String... customs) {
            parameters.put("customs", Arrays.asList(customs));
            return this;
        }

        /**
         * Declares the number limit of the returned objects.
         *
         * @param limit The maximum number of objects returned.
         * @return
         */
        public Caller withLimit(int limit) {
            parameters.put("limit", limit);
            return this;
        }

        /**
         * Returns only the the resources with groups' ids in the list.
         *
         * @param groups list of groups' ids
         * @return updated Caller object
         */
        public Caller withGroups(Long... groups) {
            parameters.put("groups", Arrays.asList(groups));
            return this;
        }

        /**
         * Returns only the the resources with ids' ids in the list.
         *
         * @param ids list of ids
         * @return updated Caller object
         */
        public Caller withIds(Long... ids) {
            parameters.put("ids", Arrays.asList(ids));
            return this;
        }

        /**
         * Returns only the the resources with groups' ids in the list.
         *
         * @param segments list of segments' ids
         * @return updated Caller object
         */
        public Caller withSegments(Long... segments) {
            parameters.put("segments", Arrays.asList(segments));
            return this;
        }

        /**
         * Limits the number and defines the offset for the results, works only
         * with list resources.
         *
         * @param limit  limit of returned results
         * @param offset starting point for the returned results
         * @return updated Caller object
         */
        public Caller withPaginator(Long limit, Long offset) {
            parameters.put("limit", limit);
            parameters.put("offset", offset);
            return this;
        }

        /**
         * Declares the order in which results in list resources should be
         * returned
         *
         * @param order list of tuples in form of (fieldName, ASC or DESC)
         * @return updated Caller object
         */
        public Caller withOrder(Map<String, String> order) {
            parameters.put("order", order);
            return this;
        }

        /**
         * Performs a search with concrete field values.
         *
         * @param query list of tuples in form of (fieldName,fieldValue)
         * @return updated Caller object
         */
        public Caller withQuery(Map<String, Object> query) {
            parameters.put("query", query);
            return this;
        }

        /**
         * Returns only the resources created between certain dates given as
         * milliseconds. In case one of the parameters is None, the limit is not
         * set.
         *
         * @param from starting date as millis
         * @param to   end date as millis
         * @return updated Caller object
         */
        public Caller withCreatedAt(Long from, Long to) {
            if (from != null)
                parameters.put("fromc", from);
            if (to != null)
                parameters.put("toc", to);
            return this;
        }

        /**
         * Returns only the resources last updated between certain dates given
         * as milliseconds. In case one of the parameters is None, the limit is
         * not set.
         *
         * @param from starting date as millis
         * @param to   end date as millis
         * @return updated Caller object
         */
        public Caller withUpdatedAt(Long from, Long to) {
            if (from != null)
                parameters.put("fromu", from);
            if (to != null)
                parameters.put("tou", to);
            return this;
        }

        /**
         * Declares whether custom fields should be returned
         *
         * @return updated Caller object
         */
        public Caller withCustom() {
            parameters.put("custom", true);
            return this;
        }

        /**
         * Add custom query parameters to request.
         *
         * @param params map with queries
         * @return updated Caller object
         */
        public Caller withQueryParameters(Map<String, Object> params) {
            parameters.putAll(params);
            return this;
        }

        /**
         * Call to api get methods.
         *
         * @return api json response
         * @throws InvalidRequestMethodException
         * @throws InvalidQueryParameterException
         */
        public HttpResponse get() throws IOException,
                IsaaCloudConnectionException {
            Log.d(TAG, "GET request for: " + path);
            return callService(path, Connector.GET, parameters, null);
        }

        /**
         * Call to api put methods.
         *
         * @return api json response
         */
        public HttpResponse put(JSONObject body) throws IOException,
                IsaaCloudConnectionException {
            Log.d(TAG, "PUT request for: " + path);
            return callService(path, Connector.PUT, parameters, body.toString());
        }

        /**
         * Call to api post methods.
         *
         * @return api json response
         */
        public HttpResponse post(JSONObject body) throws IOException,
                IsaaCloudConnectionException {
            Log.d(TAG, "POST request for: " + path);
            return callService(path, Connector.POST, parameters,
                    body.toString());
        }

        /**
         * Call to api delete methods.
         *
         * @return api json response
         */
        public HttpResponse delete() throws IOException,
                IsaaCloudConnectionException {
            Log.d(TAG, "DELETE request for: " + path);
            return callService(path, Connector.DELETE, parameters, null);
        }

        /**
         * Call to api patch methods.
         *
         * @return api json response
         */
        public HttpResponse patch(JSONObject body) throws IOException,
                IsaaCloudConnectionException {
            Log.d(TAG, "PATCH request for: " + path);
            return callService(path, Connector.PATCH, parameters,
                    body.toString());
        }
    }

}