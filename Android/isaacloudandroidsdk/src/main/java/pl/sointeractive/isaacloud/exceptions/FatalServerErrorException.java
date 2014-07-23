package pl.sointeractive.isaacloud.exceptions;

public class FatalServerErrorException extends IsaaCloudConnectionException {

    private static final long serialVersionUID = 1L;
    String message;

    public FatalServerErrorException() {
        message = "The server encountered an unexpected condition which prevented it from fulfilling the request.";
    }

    public FatalServerErrorException(String internalMessage, int internalCode) {
        message = "The server encountered an unexpected condition which prevented it from fulfilling the request."
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
