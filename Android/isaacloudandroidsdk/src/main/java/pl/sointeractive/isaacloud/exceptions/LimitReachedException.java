package pl.sointeractive.isaacloud.exceptions;

public class LimitReachedException extends IsaaCloudConnectionException {

    private static final long serialVersionUID = 1L;
    String message;

    public LimitReachedException() {
        message = "The limit for the accessed resource has been reached.";
    }

    public LimitReachedException(String internalMessage, int internalCode) {
        message = "The limit for the accessed resource has been reached."
                + "\n" + "Internal message: " + internalMessage + "\n"
                + "Internal code: " + internalCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
