package org.kantega.respiro.ui.resources;


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
