package org.kantega.respiro.ui.resources;

import java.security.Principal;

/**
 *
 */
public class UserProfile {


    private final String username;

    public UserProfile(Principal userPrincipal) {
        this.username = userPrincipal.getName();
    }

    public String getUsername() {
        return username;
    }
}
