package ci.transit.system.transport.server.impl.utilities;

import java.util.stream.Collectors;

import ci.transit.system.transport.server.impl.dto.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(" ; "));
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(new ErrorResponse("VALIDATION_ERROR", message))
            .build();
    }
}
