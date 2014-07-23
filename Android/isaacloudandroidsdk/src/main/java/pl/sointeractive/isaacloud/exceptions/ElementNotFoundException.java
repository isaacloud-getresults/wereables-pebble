package pl.sointeractive.isaacloud.exceptions;

public class ElementNotFoundException extends IsaaCloudConnectionException {

    private static final long serialVersionUID = 1L;
    String message;

    public ElementNotFoundException() {
        message = "The server has not found anything matching the Request-URI.";
    }

    public ElementNotFoundException(String internalMessage, int internalCode) {
        message = "The server has not found anything matching the Request-URI."
                + "\n" + "Internal message: " + internalMessage + "\n"
                + "Internal code: " + internalCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
