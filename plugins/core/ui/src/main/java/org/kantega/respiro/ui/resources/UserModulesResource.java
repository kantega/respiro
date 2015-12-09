package org.kantega.respiro.ui.resources;

import org.kantega.respiro.ui.UiModule;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Path("respiro/modules")
public class UserModulesResource {

    // Disable metrics for this resource
    public static boolean METRICS = false;

    private final Collection<UiModule> uiModules;

    public UserModulesResource(Collection<UiModule> uiModules) {
        this.uiModules = uiModules;
    }



    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ModuleJson> get() {
        return uiModules.stream().map( m -> toJson(m)).collect(Collectors.toList());
    }

    private ModuleJson toJson(UiModule m) {
        return new ModuleJson(m);
    }


}
