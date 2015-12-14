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

package org.kantega.respiro.ui.plugins;


import org.kantega.reststop.classloaderutils.PluginClassLoader;

/**
 *
 */
public class PluginJson {
    private final String groupId;
    private String artifactId;
    private final String version;

    public PluginJson(PluginClassLoader classLoader) {
        this.artifactId = classLoader.getPluginInfo().getArtifactId();
        this.groupId = classLoader.getPluginInfo().getGroupId();
        this.version = classLoader.getPluginInfo().getVersion();
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getVersion() {
        return version;
    }
}
