package ci.transit;

import io.quarkus.logging.Log;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) {
    Log.info("METHOD : " + requestContext.getMethod());
    Log.info("URL : " + requestContext.getUriInfo().getAbsolutePath());
    Log.info("PATH PARAMS : " + requestContext.getUriInfo().getPathParameters());
    Log.info("QUERY PARAMS : " + requestContext.getUriInfo().getQueryParameters());
  }

  @Override
  public void filter(ContainerRequestContext requestContext,
                     ContainerResponseContext responseContext) {
    Log.info("RESPONSE STATUS : " + responseContext.getStatus());
  }
}
