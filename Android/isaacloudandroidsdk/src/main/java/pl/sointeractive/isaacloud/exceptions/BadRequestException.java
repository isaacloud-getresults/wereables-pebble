package pl.sointeractive.isaacloud.exceptions;

public class BadRequestException extends IsaaCloudConnectionException {

    private static final long serialVersionUID = 1L;
    String message;

    public BadRequestException() {
        message = "The request could not be understood by the server due to malformed syntax.";
    }

    public BadRequestException(String internalMessage, int internalCode) {
        message = "The request could not be understood by the server due to malformed syntax."
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
