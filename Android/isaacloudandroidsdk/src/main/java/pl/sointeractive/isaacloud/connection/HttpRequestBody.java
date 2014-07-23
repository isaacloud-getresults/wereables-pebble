package pl.sointeractive.isaacloud.connection;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class HttpRequestBody {

    private String bodyText;

    public HttpRequestBody() {

    }

    public HttpRequestBody(String bodyText) {
        this.setBodyText(bodyText);
    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    public void setBodyText(JSONObject json) {
        this.bodyText = json.toString();
    }

    public byte[] getBodyBytes() {
        byte[] bodyBytes = null;
        try {
            bodyBytes = bodyText.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return bodyBytes;
    }
}
