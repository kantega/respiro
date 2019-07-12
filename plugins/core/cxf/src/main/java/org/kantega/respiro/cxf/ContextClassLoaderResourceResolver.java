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

package org.kantega.respiro.cxf;

import org.apache.cxf.Bus;
import org.apache.cxf.resource.ResourceManager;
import org.apache.cxf.resource.ResourceResolver;

import java.io.InputStream;
import java.net.URL;

/**
 *
 */
public class ContextClassLoaderResourceResolver implements ResourceResolver{

    public ContextClassLoaderResourceResolver(Bus bus) {
        bus.getExtension(ResourceManager.class).addResourceResolver(this);
    }

    public <T> T resolve(String resourceName, Class<T> resourceType) {
        if (resourceName == null || resourceType == null) {
            return null;
        }
        if(!resourceType.isAssignableFrom(URL.class)) {
            return null;
        }
        ClassLoader classLoader = CxfPlugin.pluginClassLoader.get();
        if(classLoader == null) {
            return null;
        }
        URL url = classLoader.getResource(resourceName);
        if (resourceType.isInstance(url)) {
            return resourceType.cast(url);
        }
        return null;
    }

    public InputStream getAsStream(String name) {
        ClassLoader classLoader = CxfPlugin.pluginClassLoader.get();
        if(classLoader == null) {
            return null;
        }
        return classLoader.getResourceAsStream(name);
    }
}
