package ci.transit.system.transport.server.impl.utilities;

import jakarta.ws.rs.core.Response;

/**
 * Cette classe represente une erreur fonctionnelle de l'API.
 *
 * @author Transit
 *
 */
public class ApiException extends RuntimeException {

    private final Response.Status status;
    private final String code;

    public ApiException(Response.Status status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public Response.Status getStatus() { return status; }
    public String getCode() { return code; }

    public static ApiException notFound(String message) {
        return new ApiException(Response.Status.NOT_FOUND, "NOT_FOUND", message);
    }

    public static ApiException badRequest(String message) {
        return new ApiException(Response.Status.BAD_REQUEST, "BAD_REQUEST", message);
    }

    public static ApiException forbidden(String message) {
        return new ApiException(Response.Status.FORBIDDEN, "FORBIDDEN", message);
    }
}
