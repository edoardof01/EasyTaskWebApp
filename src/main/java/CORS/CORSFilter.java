package CORS;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
public class CORSFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "http://localhost:4200"); // Consenti Angular
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); // Metodi consentiti
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization"); // Intestazioni consentite
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true"); // Se necessario

        // Se la richiesta è OPTIONS, imposta lo stato su 200
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            responseContext.setStatus(200);
        }
    }
}


