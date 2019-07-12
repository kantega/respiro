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

package org.kantega.respiro.security;

import java.util.Set;

public class AuthenticationResult {
    private final boolean authenticated;
    private final String username;
    private final Set<String> roles;
    public final static AuthenticationResult UNAUTHENTICATED = new AuthenticationResult(false, null, null);


    public AuthenticationResult(boolean authenticated, String username, Set<String> roles) {
        this.authenticated = authenticated;
        this.username = username;
        this.roles = roles;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
