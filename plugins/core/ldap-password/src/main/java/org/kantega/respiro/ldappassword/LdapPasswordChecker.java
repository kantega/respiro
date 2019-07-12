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

package org.kantega.respiro.ldappassword;

import org.kantega.respiro.security.AuthenticationResult;
import org.kantega.respiro.security.PasswordChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.*;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.*;

/**
 *
 */
public class LdapPasswordChecker implements PasswordChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordChecker.class);

    private final String ldapUrl;
    private final String ldapRealm;
    private final String userSearchBase;
    private final String groupSearchBase;

    public LdapPasswordChecker(String ldapUrl, String ldapRealm, String userSearchBase, String groupSearchBase) {
        this.ldapUrl = ldapUrl;
        this.ldapRealm = ldapRealm;
        this.userSearchBase = userSearchBase;
        this.groupSearchBase = groupSearchBase;
    }

    public AuthenticationResult checkPassword(String username, String password) {
        if (username == null || username.isEmpty()) {
            return AuthenticationResult.UNAUTHENTICATED;
        }
        if (password == null || password.isEmpty()) {
            return AuthenticationResult.UNAUTHENTICATED;
        }
        Hashtable<String, Object> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);

        env.put(Context.SECURITY_PRINCIPAL, username + "@" + ldapRealm);
        env.put(Context.SECURITY_CREDENTIALS, password);

        DirContext context = null;

        try {
            context = new InitialDirContext(env);
            String userDistName = lookupUser(username, context);
            Set<String> groups = findGroups(userDistName, context);
            LOGGER.info(String.format("User %s authenticated. Member of %s", username, Arrays.toString(groups.toArray())));
            return new AuthenticationResult(true, username, groups);
        } catch (AuthenticationException e) {

            LOGGER.info(String.format("User %s could not be authenticated (%s)", username, e.getMessage()));
            return AuthenticationResult.UNAUTHENTICATED;
        } catch (NamingException e) {
            throw new RuntimeException("Cannot communicate with LDAP", e);
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException e) {
                    // Ignore
                }
            }
        }
    }

    private String lookupUser(String username, DirContext context) throws NamingException {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningAttributes(new String[0]);

        String filter = String.format("(samaccountname=%s)", username);
        NamingEnumeration<SearchResult> nameEnum = context.search(userSearchBase, filter, controls);
        while(nameEnum.hasMoreElements()){
            SearchResult res = nameEnum.nextElement();
            return res.getNameInNamespace();

        }
        throw new IllegalArgumentException("User not found " + username);
    }

    private Set<String> findGroups(String userDistName, DirContext context) throws NamingException {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningAttributes(new String[]{"cn"});

        String filter = String.format("(member=%s)", userDistName);
        NamingEnumeration<SearchResult> nameEnum = context.search(groupSearchBase, filter, controls);
        Set<String> result = new HashSet<>();
        while(nameEnum.hasMoreElements()){
            SearchResult res = nameEnum.nextElement();
            result.add(res.getAttributes().get("cn").get().toString());
        }
        return result;
    }
}
