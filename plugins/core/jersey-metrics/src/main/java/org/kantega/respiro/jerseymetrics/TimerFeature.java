/*
 * Copyright 2019 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kantega.respiro.jerseymetrics;

import javax.ws.rs.Path;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

/**
 *
 */
@Provider
public class TimerFeature implements DynamicFeature {


    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {

        Method resourceMethod = resourceInfo.getResourceMethod();
        Class<?> resourceClass = resourceInfo.getResourceClass();
        if(metricsDisabled(resourceClass)) {
            return;
        }
        Path methodPath = resourceMethod.getAnnotation(Path.class);
        Path classPath  = resourceClass.getAnnotation(Path.class);

        Path path = methodPath != null ? methodPath : classPath;
        if(path != null) {
            UriBuilder builder = methodPath != null
                    ? UriBuilder.fromResource(resourceClass).path(resourceClass, resourceMethod.getName())
                    : UriBuilder.fromResource(resourceClass);

            String template = builder.toTemplate();
            context.register(new TimerBeforeFilter(template));
            context.register(TimerAfterFilter.class);
        }
    }

    private boolean metricsDisabled(Class<?> resourceClass) {
        try {
            Field metrics = resourceClass.getDeclaredField("METRICS");
            return Boolean.FALSE.equals(metrics.get(null));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
    }
}
