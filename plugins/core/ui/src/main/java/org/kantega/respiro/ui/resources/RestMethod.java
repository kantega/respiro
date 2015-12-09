package org.kantega.respiro.ui.resources;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 *
 */
public class RestMethod {

    private final String pathTemplate;
    private final String method;

    public RestMethod(Method resourceMethod, Annotation methodAnnotation) {

        Path methodPath = resourceMethod.getAnnotation(Path.class);
        Class<?> resourceClass = resourceMethod.getDeclaringClass();
        Path classPath  = resourceClass.getAnnotation(Path.class);

        Path path = methodPath != null ? methodPath : classPath;

        UriBuilder builder = methodPath != null
                ? UriBuilder.fromResource(resourceClass).path(resourceClass, resourceMethod.getName())
                : UriBuilder.fromResource(resourceClass);

        this.pathTemplate = builder.toTemplate();

        this.method = methodAnnotation.annotationType().getAnnotation(HttpMethod.class).value();
    }

    public String getMethod() {
        return method;
    }

    public String getPathTemplate() {
        return pathTemplate;
    }
}
