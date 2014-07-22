package pl.sointeractive.isaacloud;

import android.util.Base64;
import android.util.Log;

import com.google.common.base.Joiner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.connection.HttpToken;
import pl.sointeractive.isaacloud.exceptions.BadRequestException;
import pl.sointeractive.isaacloud.exceptions.ElementNotFoundException;
import pl.sointeractive.isaacloud.exceptions.FatalServerErrorException;
import pl.sointeractive.isaacloud.exceptions.ForbiddenException;
import pl.sointeractive.isaacloud.exceptions.InvalidConfigException;
import pl.sointeractive.isaacloud.exceptions.InvalidQueryParameterException;
import pl.sointeractive.isaacloud.exceptions.InvalidRequestMethodException;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;
import pl.sointeractive.isaacloud.exceptions.LimitReachedException;
import pl.sointeractive.isaacloud.exceptions.UnauthorizedException;

/**
 * Connector class for the Android SDK.
 *
 * @author Mateusz Renes
 */
public class Connector {

    // query parameters
    public static final String FIELDS = "fields";
    public static final String GROUPS = "groups";
    public static final String LIMIT = "limit";
    public static final String OFFSET = "offset";
    public static final String ORDER = "order";
    public static final String SEGMENTS = "segments";
    // request methods
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    public static final String PATCH = "PATCH";
    private static final String TAG = "Connector";
    private static final String[] availableQueryParameters = {"fields",
            "groups", "limit", "offset", "order", "segments"};
    private static final String[] availableRequestMethods = {"POST", "GET",
            "PUT", "DELETE", "PATCH"};
    private static HttpToken httpToken;
    protected String baseUrl;
    protected String version;
    protected String oauthUrl;
    private String instanceId;
    private String appSecret;
    private SSLContext sslContext;
    private boolean hasValidCertificate;

    /**
     * Base constructor.
     *
     * @param baseUrl  The base URL address of the API.
     * @param oauthUrl The OAuth URL of the API. Used to generate access token.
     * @param version  Version of the API.
     * @param config   Configuration parameters. Requires "gamificationId" and
     *                 "appSecret" keys and their respective values.
     * @throws InvalidConfigException Thrown when "gamificationId" or "appSecret" are not found in
     *                                the parameters.
     */
    public Connector(String baseUrl, String oauthUrl, String version,
                     Map<String, String> config) throws InvalidConfigException {
        this.baseUrl = baseUrl;
        this.oauthUrl = oauthUrl;
        this.setVersion(version);
        httpToken = new HttpToken();
        // check config
        if (config.containsKey("instanceId")) {
            this.instanceId = config.get("instanceId");
        } else {
            throw new InvalidConfigException("instanceId");
        }
        if (config.containsKey("appSecret")) {
            this.appSecret = config.get("appSecret");
        } else {
            throw new InvalidConfigException("appSecret");
        }
        // set valid certificate to false
        hasValidCertificate = false;
    }

    /**
     * Checks the validity of the token.
     *
     * @return
     */
    public static boolean isTokenValid() {
        long currentTime = new Date().getTime();
        if (currentTime > httpToken.getUpdateTime()
                + httpToken.getTokenTimeToLive() * 1000) {
            return false;
        } else
            return true;
    }

    private void initializeSSLContext() {
        // certificate handling
        try {
            // Load trusted IsaaCloud certificate
            Certificate ca;
            ca = SSLCertificateFactory.getCertificate(Config.PORT, Config.HOST);
            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance(tmfAlgorithm);
            tmf.init(keyStore);
            // Create an SSLContext that uses our TrustManager
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the authentication token.
     *
     * @return Authentication header in format: <token_type> <access_token>
     * @throws JSONException             Thrown when an error occurs during JSON operations
     * @throws java.io.IOException       Thrown when an error occurs during IO operations
     * @throws UnauthorizedException
     * @throws BadRequestException
     * @throws FatalServerErrorException
     * @throws ElementNotFoundException
     * @throws LimitReachedException
     */
    public String getAuthentication() throws IsaaCloudConnectionException,
            IOException {
        if (!isTokenValid()) {
            getAccessTokenData();
        }
        Log.d(TAG, httpToken.getAuthorizationHeader());
        return httpToken.getAuthorizationHeader();
    }

    public void getAccessTokenData() throws IsaaCloudConnectionException,
            IOException {
        // generate credentials
        String base64EncodedCredentials = null;
        base64EncodedCredentials = Base64.encodeToString(
                (instanceId + ":" + appSecret).getBytes("US-ASCII"),
                Base64.DEFAULT);
        String auth = "Basic " + base64EncodedCredentials;
        // setup connection
        URL url = new URL(this.oauthUrl + "/token");
        HttpsURLConnection connection = (HttpsURLConnection) url
                .openConnection();
        connection.setRequestMethod(Connector.POST);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setConnectTimeout(Config.TIMEOUT);
        connection.setReadTimeout(Config.TIMEOUT);
        // set socket
        connection.setSSLSocketFactory(sslContext.getSocketFactory());
        // setup headers
        connection.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        connection.setRequestProperty("Authorization", auth);
        // set body
        OutputStream os = new BufferedOutputStream(connection.getOutputStream());
        os.write("grant_type=client_credentials".getBytes("UTF-8"));
        os.flush();
        os.close();
        // connect
        connection.connect();
        // check response code
        int responseCode = connection.getResponseCode();
        Log.d(TAG, "Response code: " + responseCode);
        // handle errors
        try {
            handleErrors(connection, responseCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // get result string
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
        String resultString = reader.readLine();
        // build response
        HttpResponse.Builder responseBuilder = new HttpResponse.Builder();
        responseBuilder.setMethod(Connector.POST);
        responseBuilder.setResponseCode(responseCode);
        if (resultString != null) {
            responseBuilder.setResponseString(resultString);
            responseBuilder.setIsValid(true);
        } else {
            responseBuilder.setIsValid(false);
        }
        HttpResponse response = responseBuilder.build();
        // disconnect
        connection.disconnect();
        // update token time
        long currentTime = new Date().getTime();
        httpToken.setUpdateTime(currentTime);
        // save token data
        try {
            httpToken.setTokenTimeToLive(response.getJSONObject().getInt(
                    "expires_in"));
            httpToken.setAccessToken(response.getJSONObject().getString(
                    "access_token"));
            httpToken.setTokenType(response.getJSONObject().getString(
                    "token_type"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prepares the url with parameters.
     *
     * @param wholeUri   path to be enhanced
     * @param parameters parameters to be added
     * @return new path
     */
    protected String prepareUrl(String wholeUri, Map<String, Object> parameters) {
        String regex = "\\{[a-zA-Z0-9,]+\\}";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(wholeUri);

        while (matcher.find()) {

            String id = matcher.group();
            String tmp = id.replace("{", "");
            tmp = tmp.replace("}", "");

            if (parameters.containsKey(tmp)) {
                String rep = parameters.get(tmp).toString();
                wholeUri = wholeUri.replace(id, rep);
                parameters.remove(tmp);
            }

        }

        String and = "?";

        for (Entry<String, Object> entry : parameters.entrySet()) {

            if (entry.getValue() != null) {

                wholeUri = wholeUri + and + entry.getKey() + "=";
                Object vals = entry.getValue();

                if (List.class.isAssignableFrom(vals.getClass())) {
                    wholeUri = wholeUri + Joiner.on(",").join((List) vals);
                } else if (Map.class.isAssignableFrom(vals.getClass())) {
                    wholeUri = wholeUri
                            + Joiner.on(",").withKeyValueSeparator(":")
                            .join((Map) vals);
                } else {
                    wholeUri = wholeUri + vals;
                }
                and = "&";
            }

        }

        return wholeUri;
    }

    /**
     * Call required service from the API. For the future implementation of the
     * wrapper: catch the SocketTimeoutException and MalformedURLException when
     * using this method in order to gain more control over the exception
     * handling process
     * <p/>
     * Caution: In case an IOException is NOT thrown, but the http response code
     * is still pointing at an error, an adequate information is stored in the
     * returned HttpResponse.
     *
     * @param uri        Uri of the method. Used together with the base Uri of the API
     *                   to get the whole address.
     * @param methodName Name of the method (GET, POST, PUT, DELETE).
     * @param parameters Url parameters to add to the uri (like limit or fields).
     * @param body       Request body.
     * @return Request response in form of a HttpResponse class.
     * @throws java.io.IOException            Thrown when an error occurs during IO operations
     * @throws InvalidQueryParameterException Thrown when an invalid query parameter is detected in the
     *                                        request call.
     * @throws InvalidRequestMethodException  Thrown when an unrecognized request method is used.
     * @throws BadRequestException            Thrown when the request syntax is somehow malformed and
     *                                        cannot be processed by the server.
     * @throws UnauthorizedException          Thrown when the user lacks authorization to access resources.
     * @throws LimitReachedException          Thrown when the user has reached the limit of the specified
     *                                        resource.
     * @throws ElementNotFoundException       Thrown when the no resource is found under the specified URI.
     * @throws FatalServerErrorException      Thrown when an unexpected server error occurs.
     */
    public HttpResponse callService(String uri, String methodName,
                                    Map<String, Object> parameters, String body)
            throws InvalidQueryParameterException,
            InvalidRequestMethodException, IsaaCloudConnectionException,
            IOException {
        // check for valid ceritificate
        Log.d(TAG, "Check for certificate");
        if (!hasValidCertificate) {
            Log.d(TAG, "No valid certificate found, loading new certificate");
            initializeSSLContext();
            hasValidCertificate = true;
        }
        // generate uri
        String targetUri = prepareUrl(this.baseUrl + "/" + this.version + uri,
                parameters);
        Log.d(TAG, "targetUri: " + targetUri);
        // setup connection
        URL url = new URL(targetUri);
        HttpsURLConnection connection = (HttpsURLConnection) url
                .openConnection();
        if (methodName.equals(Connector.GET)
                || methodName.equals(Connector.DELETE)) {
            connection.setDoOutput(false);
        } else {
            connection.setDoOutput(true);
        }
        connection.setDoInput(true);
        // set method
        if (isValidRequestMethod(methodName)) {
            connection.setRequestMethod(methodName);
        } else {
            throw new InvalidRequestMethodException(methodName);
        }
        connection.setConnectTimeout(Config.TIMEOUT);
        connection.setReadTimeout(Config.TIMEOUT);
        // set socket
        connection.setSSLSocketFactory(sslContext.getSocketFactory());
        // setup headers
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", getAuthentication());
        // setup body (optional)
        if (body != null) {
            OutputStream os = new BufferedOutputStream(
                    connection.getOutputStream());
            os.write(body.getBytes("UTF-8"));
            os.flush();
            os.close();
        }
        // connect
        connection.connect();
        // check response code
        int responseCode = connection.getResponseCode();
        Log.d(TAG, "Response code: " + responseCode);
        // handle errors
        try {
            handleErrors(connection, responseCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // get result string
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
        String resultString = reader.readLine();
        Log.d(TAG, "Result String: " + resultString);
        // disconnect
        connection.disconnect();
        // prepare response builder
        HttpResponse.Builder responseBuilder = new HttpResponse.Builder();
        // build response
        responseBuilder.setResponseCode(responseCode);
        responseBuilder.setMethod(methodName);
        if (resultString != null) {
            responseBuilder.setResponseString(resultString);
            responseBuilder.setIsValid(true);
        } else {
            responseBuilder.setIsValid(false);
        }
        HttpResponse response = responseBuilder.build();
        // return response
        return response;

    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isValidQueryParameter(String key) {
        for (int i = 0; i < availableQueryParameters.length - 1; i++) {
            if (key.equals(availableQueryParameters[i]))
                return true;
        }
        return false;
    }

    public boolean isValidRequestMethod(String key) {
        for (int i = 0; i < availableRequestMethods.length - 1; i++) {
            if (key.equals(availableRequestMethods[i]))
                return true;
        }
        return false;
    }

    private void handleErrors(HttpsURLConnection connection, int responseCode)
            throws IsaaCloudConnectionException, JSONException {
        if (responseCode >= 400) {
            JSONObject json = getErrorJSON(connection);
            String errorMessage = json.getString("message");
            int errorCode = json.getInt("code");
            switch (responseCode) {
                case 400:
                    throw new BadRequestException(errorMessage, errorCode);
                case 401:
                    throw new UnauthorizedException(errorMessage, errorCode);
                case 402:
                    throw new LimitReachedException(errorMessage, errorCode);
                case 403:
                    throw new ForbiddenException();
                case 404:
                    throw new ElementNotFoundException(errorMessage, errorCode);
                case 500:
                    throw new FatalServerErrorException(errorMessage, errorCode);
                default:
                    break;
            }
        }
    }

    private JSONObject getErrorJSON(HttpsURLConnection connection) {
        JSONObject errorJSON = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getErrorStream()));
            String errorMessage = reader.readLine();
            Log.d(TAG, "Error message: " + errorMessage);
            errorJSON = new JSONObject(errorMessage);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return errorJSON;
    }

}
