package pl.sointeractive.isaacloud.exceptions;

public class IsaaCloudConnectionException extends Exception {

    private static final long serialVersionUID = 1L;
    private String message;
    private String internalMessage;
    private int internalCode;

    public IsaaCloudConnectionException() {
        message = "Request call to IsaaCloud failed.";
    }

    public IsaaCloudConnectionException(String internalMessage, int internalCode) {
        this.setInternalMessage(internalMessage);
        this.setInternalCode(internalCode);
        this.message = "Request call to IsaaCloud failed." + "\n"
                + "Internal message: " + internalMessage + "\n"
                + "Internal code: " + internalCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getInternalMessage() {
        return internalMessage;
    }

    public void setInternalMessage(String internalMessage) {
        this.internalMessage = internalMessage;
    }

    public int getInternalCode() {
        return internalCode;
    }

    public void setInternalCode(int internalCode) {
        this.internalCode = internalCode;
    }
}
