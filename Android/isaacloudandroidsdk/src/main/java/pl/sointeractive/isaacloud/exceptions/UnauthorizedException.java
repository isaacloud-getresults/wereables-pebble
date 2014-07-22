package pl.sointeractive.isaacloud.exceptions;

public class UnauthorizedException extends IsaaCloudConnectionException {

    private static final long serialVersionUID = 1L;
    String message;

    public UnauthorizedException() {
        message = "The request requires user authentication.";
    }

    public UnauthorizedException(String internalMessage, int internalCode) {
        message = "The request requires user authentication." + "\n"
                + "Internal message: " + internalMessage + "\n"
                + "Internal code: " + internalCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
