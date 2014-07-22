package pl.sointeractive.isaacloud.connection;

import android.util.Log;

public class HttpToken {

    private String tokenType;
    private int tokenTimeToLive;
    private String accessToken;
    private long updateTime;

    public HttpToken() {
        tokenType = "";
        tokenTimeToLive = 0;
        accessToken = "";
        updateTime = 0;
    }

    public HttpToken(String tokenType, String accessToken, int tokenTimeToLive, long updateTime) {
        this.setTokenType(tokenType);
        this.setAccessToken(accessToken);
        this.setTokenTimeToLive(tokenTimeToLive);
        this.setUpdateTime(updateTime);
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }


    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public int getTokenTimeToLive() {
        return tokenTimeToLive;
    }

    public void setTokenTimeToLive(int tokenTimeToLive) {
        this.tokenTimeToLive = tokenTimeToLive;
    }

    public String getAuthorizationHeader() {
        String result = tokenType + " " + accessToken;
        Log.d("HttpToken", "getAuthorizationHeader() returns: " + result);
        return result;
    }


}
