package mg.msys.gutenber_ws.webservice.shared.exception;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
