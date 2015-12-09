package org.kantega.respiro.ui.resources;

import org.kantega.respiro.ui.UiModule;

/**
 *
 */
public class ModuleJson {
    private String src;
    public ModuleJson(UiModule m) {
        src = m.getSrc();
    }


    public String getSrc() {
        return src;
    }
}
