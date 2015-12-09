package org.kantega.respiro.ui.resources;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class RestResource {
    private final String path;

    private List<RestMethod> methods = new ArrayList<RestMethod>();

    private static Set<Class> rsMethods = new HashSet<>();
    static {
        rsMethods.add(HEAD.class);
        rsMethods.add(GET.class);
        rsMethods.add(POST.class);
        rsMethods.add(DELETE.class);
        rsMethods.add(OPTIONS.class);
        rsMethods.add(PUT.class);

    }

    public RestResource(Class clazz) {
        Path path = (Path) clazz.getAnnotation(Path.class);
        this.path = path.value();
        for (Method method : clazz.getMethods()) {
            for (Annotation annotation : method.getAnnotations()) {
                if(rsMethods.contains(annotation.annotationType())) {
                    methods.add(new RestMethod(method, annotation));
                }
            }
        }

    }

    public String getPath() {
        return path;
    }

    public List<RestMethod> getMethods() {
        return methods;
    }
}
