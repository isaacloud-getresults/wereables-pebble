package pl.sointeractive.isaacloud.exceptions;

public class InvalidQueryParameterException extends
        IsaaCloudConnectionException {

    private static final long serialVersionUID = 1L;
    String parameter;

    public InvalidQueryParameterException(String invalidParameter) {
        this.parameter = invalidParameter;
    }

    @Override
    public String getMessage() {
        return "<" + parameter + "> query parameter not recognized";
    }
}
