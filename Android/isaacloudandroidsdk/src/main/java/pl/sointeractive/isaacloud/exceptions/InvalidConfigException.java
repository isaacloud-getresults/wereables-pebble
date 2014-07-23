package pl.sointeractive.isaacloud.exceptions;

public class InvalidConfigException extends Exception {

    private static final long serialVersionUID = 1L;
    String config;

    public InvalidConfigException(String missingConfig) {
        this.config = missingConfig;
    }

    @Override
    public String getMessage() {
        return "<" + config + "> config not found.";
    }
}
