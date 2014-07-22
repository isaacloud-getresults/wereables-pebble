package pl.sointeractive.isaacloud.connection;

import java.util.HashMap;

public class HttpRequestHeaders {

    private HashMap<String, String> headers;

    public HttpRequestHeaders() {
        headers = new HashMap<String, String>();
    }

    public HttpRequestHeaders(HashMap<String, String> headers) {
        this.setHeaders(headers);
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }

    public void addHeader(String headerName, String headerValue) {
        headers.put(headerName, headerValue);
    }


}
