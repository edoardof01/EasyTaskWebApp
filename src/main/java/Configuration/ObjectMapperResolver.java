package Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ObjectMapperResolver implements ContextResolver<ObjectMapper> {
    private final ObjectMapper objectMapper;

    public ObjectMapperResolver() {
        objectMapper = ObjectMapperProvider.createObjectMapper();
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }
}
