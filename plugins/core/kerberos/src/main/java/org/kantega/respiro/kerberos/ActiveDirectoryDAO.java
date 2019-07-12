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

package org.kantega.respiro.kerberos;

import javax.naming.*;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class ActiveDirectoryDAO {

    private final ActiveDirectoryConnection connection;
    private final String realm;
    private final String userSearchBase;
    private final String searchAttribute;
    public final long userInfoMaxAgeSecs;


    public ActiveDirectoryDAO(String ldapUrl, String username, String password, String realm, String userSearchBase, String searchAttribute, long userInfoMaxAgeSecs) {

        this.realm = realm;
        this.userSearchBase = userSearchBase;
        this.searchAttribute = searchAttribute;
        this.userInfoMaxAgeSecs = userInfoMaxAgeSecs;

        connection = new ActiveDirectoryConnection(ldapUrl, username, password, realm);
    }



    public Set<String> findGroups(String userDistName) {
        try (ActiveDirectoryDirContext activeDirectory = connection.newContext()){

            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(new String[]{"cn"});

            // Find user from kerberos principal (ActiveDirectory)
            String principal = userDistName.replaceAll("@"+realm, "");
            String userFilter = String.format("(%s=%s)",searchAttribute, principal);
            NamingEnumeration<SearchResult> userResult = activeDirectory.search(userSearchBase, userFilter, controls);
            if(!userResult.hasMoreElements())
                throw new javax.ws.rs.NotFoundException(userFilter);
            SearchResult user = userResult.nextElement();

            // Find user group membership (ActiveDirectory)
            Set<String> result = new HashSet<>();
            addNestedGroups(result, activeDirectory, controls,  user.getNameInNamespace());
            return result;
        } catch (NamingException snfe) {
            throw new javax.ws.rs.ServiceUnavailableException();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void addNestedGroups(Set<String> groups, ActiveDirectoryDirContext activeDirectory, SearchControls controls, String nameInNamespace) throws NamingException {
        String filter = String.format("(member:1.2.840.113556.1.4.1941:=%s)", nameInNamespace);
        NamingEnumeration<SearchResult> nameEnum = activeDirectory.search(userSearchBase, filter, controls);
        while (nameEnum.hasMoreElements()) {
            SearchResult res = nameEnum.nextElement();
            String groupName = res.getAttributes().get("cn").get().toString();
            if(!groups.contains(groupName)) {
                groups.add(groupName);
                //addNestedGroups(groups,activeDirectory,controls, res.getNameInNamespace());
            }
        }

    }

    public String getCommonName(String principal){
        try (ActiveDirectoryDirContext activeDirectory = connection.newContext()){

            String attributes[] = {"cn"};
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(attributes);

            String filter = String.format("(%s=%s)", searchAttribute, principal.substring(0,principal.indexOf("@")));
            NamingEnumeration<SearchResult> nameEnum = activeDirectory.search(userSearchBase, filter, controls);
            if (nameEnum.hasMoreElements()) {
                SearchResult res = nameEnum.nextElement();
                return res.getAttributes().get(attributes[0]).get().toString();
            }else{
                //Alle brukere skal ha registrert Common Name under cn-attributtet.
                return "John Doe";
            }
        } catch (ServiceUnavailableException | CommunicationException snfe) {
            throw new javax.ws.rs.ServiceUnavailableException();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public boolean shouldReload(UserInfo userInfo) {
        return userInfo == null ||
            userInfo.getAgeInSeconds() > userInfoMaxAgeSecs;
    }

    private static class ActiveDirectoryConnection {

        private final Hashtable<String, Object> env = new Hashtable<>();

        ActiveDirectoryConnection(String ldapUrl, String username, String password, String ldapRealm) {

            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ldapUrl);

            env.put(Context.SECURITY_PRINCIPAL, username + "@" + ldapRealm);
            env.put(Context.SECURITY_CREDENTIALS, password);


        }

        ActiveDirectoryDirContext newContext() throws NamingException {
            return new ActiveDirectoryDirContext(env);
        }

    }

    private static class ActiveDirectoryDirContext extends InitialDirContext implements AutoCloseable {

        ActiveDirectoryDirContext(Hashtable<?, ?> environment) throws NamingException {
            super(environment);
        }
    }

}
