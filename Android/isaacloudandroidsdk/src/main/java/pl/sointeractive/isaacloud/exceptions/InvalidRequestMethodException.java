package pl.sointeractive.isaacloud.exceptions;

public class InvalidRequestMethodException extends IsaaCloudConnectionException {

    private static final long serialVersionUID = 1L;
    String requestMethod;

    public InvalidRequestMethodException(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    @Override
    public String getMessage() {
        return "<" + requestMethod + "> query parameter not recognized";
    }
}
