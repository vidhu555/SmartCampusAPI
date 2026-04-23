package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable e) {
        LOGGER.severe("Unhandled exception: " + e.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(500, "Internal Server Error",
                        "An unexpected error occurred. Please contact the system administrator."))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}