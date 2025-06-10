package africa.payaza.routing.security.exception;

import org.springframework.dao.EmptyResultDataAccessException;

public class InvalidTenantIdentifierException extends RuntimeException {

    public InvalidTenantIdentifierException(final String message) {
        super(message);
    }

    public InvalidTenantIdentifierException(String message, EmptyResultDataAccessException e) {
        super(message, e);
    }
}
