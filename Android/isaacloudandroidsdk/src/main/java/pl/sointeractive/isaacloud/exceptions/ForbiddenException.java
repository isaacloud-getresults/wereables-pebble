package pl.sointeractive.isaacloud.exceptions;

public class ForbiddenException extends IsaaCloudConnectionException {

    private static final long serialVersionUID = 1L;
    String message;

    public ForbiddenException() {
        message = "The request was a valid request, but the server is refusing to respond to it.";
    }

    public ForbiddenException(String internalMessage, int internalCode) {
        message = "The request was a valid request, but the server is refusing to respond to it."
                + "\n"
                + "Internal message: "
                + internalMessage
                + "\n"
                + "Internal code: " + internalCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
