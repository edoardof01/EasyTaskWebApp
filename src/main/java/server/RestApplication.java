package server;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class RestApplication extends Application {
    // Puoi lasciare vuota questa classe, è sufficiente per registrare le risorse JAX-RS
}
