package pl.sointeractive.isaacloud.connection;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Class represents the http request sent to the desired url.
 *
 * @author Mateusz Renes
 */
public class HttpRequest {

    private String url;
    private String method;
    private HttpRequestHeaders headers;
    private HttpRequestBody body;

    private HttpRequest(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers;
        this.body = builder.body;
    }

    public HttpRequestHeaders getHeaders() {
        return headers;
    }

    public HashMap<String, String> getHeadersHashMap() {
        return headers.getHeaders();
    }

    public HttpRequestBody getBody() {
        return body;
    }

    public byte[] getBodyBytes() {
        return body.getBodyBytes();
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public static class Builder {
        private String url;
        private String method;
        private HttpRequestHeaders headers;
        private HttpRequestBody body;

        public Builder() {
            url = null;
            method = null;
            headers = new HttpRequestHeaders();
            body = new HttpRequestBody();
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setBody(HttpRequestBody body) {
            this.body = body;
        }

        public void setBody(JSONObject json) {
            body.setBodyText(json);
        }

        public void setBody(String bodyText) {
            this.body = new HttpRequestBody(bodyText);
        }

        public void setHeaders(HttpRequestHeaders headers) {
            this.headers = headers;
        }

        public void addHeader(String headerName, String headerValue) {
            headers.addHeader(headerName, headerValue);
        }

        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }
}
