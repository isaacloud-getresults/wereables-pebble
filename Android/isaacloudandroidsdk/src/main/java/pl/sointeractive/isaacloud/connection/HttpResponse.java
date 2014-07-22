package pl.sointeractive.isaacloud.connection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpResponse {

    private boolean isValid;
    private int responseCode;
    private String responseString;
    private String method;


    private HttpResponse(Builder builder) {
        this.isValid = builder.isValid;
        this.responseCode = builder.responseCode;
        this.responseString = builder.responseString;
        this.method = builder.method;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public String toString() {
        String s = "";
        s += "HttpResponse" + "\n";
        s += "Method: " + method.toString() + "\n";
        s += "ResponseCode: " + responseCode + "\n";
        s += "isValid: " + isValid;
        if (isValid) {
            s += "\n" + "JSON: " + responseString;
        }
        return s;
    }

    public String getResponseString() {
        return responseString;
    }

    public JSONObject getJSONObject() {
        JSONObject json = null;
        try {
            json = new JSONObject(responseString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public JSONArray getJSONArray() {
        JSONArray json = null;
        try {
            json = new JSONArray(responseString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public boolean isValid() {
        return isValid;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public static class Builder {
        private boolean isValid;
        private int responseCode;
        private String responseString;
        private String method;

        public void setIsValid(boolean isValid) {
            this.isValid = isValid;
        }

        public void setResponseCode(int responseCode) {
            this.responseCode = responseCode;
        }

        public void setResponseString(String responseString) {
            this.responseString = responseString;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public HttpResponse build() {
            return new HttpResponse(this);
        }

    }

}
