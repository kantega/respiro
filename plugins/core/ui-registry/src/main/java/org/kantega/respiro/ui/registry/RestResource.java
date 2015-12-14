/*
 * Copyright 2015 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kantega.respiro.ui.registry;

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
