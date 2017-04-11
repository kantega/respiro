package org.kantega.respiro.api;

import org.kantega.reststop.api.Plugin;

import java.util.Collection;

/**
 */
@Plugin
public class RespiroApiPlugin {

    public RespiroApiPlugin(Collection<Initializer> initializers) {
        
        initializers.forEach(Initializer::initialize);
    }
}
