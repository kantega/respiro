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

package org.kantega.respiro.collector.jaxrs;

import org.kantega.respiro.collector.DoNotCollect;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import java.lang.reflect.Field;

/**
 *
 */
public class CollectingFeature implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if (!resourceInfo.getResourceClass().isAnnotationPresent(DoNotCollect.class)
                && ! metricsDisabled(resourceInfo.getResourceClass())) {
            context.register(ContainerCollectingFilter.class);
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
