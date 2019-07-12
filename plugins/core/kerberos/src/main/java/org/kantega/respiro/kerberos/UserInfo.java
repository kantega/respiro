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

import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

class UserInfo {
    private final String principal;
    private final String commonName;
    private Set<String> groups = new HashSet<>();
    private final Instant created = Instant.now();

    UserInfo(String principal, Set<String> groups, String commonName) {
        this.principal = principal;
        this.commonName = commonName;

        setGroups(groups);
    }

    public void setGroups(Set<String> groups) {
        if (groups != null)
            this.groups = groups;
        else
            this.groups = new HashSet<>();
    }

    public Set<String> getGroups() {
        return groups;
    }

    public String getPrincipal() {
        return principal;
    }

    public long getAgeInSeconds() {
        return (Instant.now().toEpochMilli() - created.toEpochMilli())/1000L;
    }

    public String getCommonName() {
        return commonName;
    }

    public String toJson() {
        StringBuilder builder = new StringBuilder();
        builder.append("[{\"principal\":\"").append(principal).append(",\"roles\":[");
        String comma = "";
        for (String g : groups) {
            builder.append(comma).append("\"").append(g).append("\"");
            comma = ",";
        }
        builder.append("]}]");

        return builder.toString();
    }

    public static UserInfo from(HttpServletRequest request, Optional<ActiveDirectoryDAO> maybeAd){

        UserInfo userInfo = (UserInfo) request.getSession(true).getAttribute(UserInfo.class.getName());


        if( maybeAd.isPresent() && maybeAd.get().shouldReload(userInfo) ) {
            String principal = request.getSession().getAttribute(KerberosFilter.AUTORIZED_PRINCIPAL_SESSION_ATTRIBUTE).toString();

            userInfo = new UserInfo(principal, maybeAd.get().findGroups(principal), maybeAd.get().getCommonName(principal));
            userInfo.to(request);

            LoggerFactory.getLogger(UserInfo.class)
                .info("User {} loaded, member of {}",userInfo.commonName, Arrays.toString(userInfo.getGroups().toArray()));
        }

        return userInfo;
    }

    public void to(HttpServletRequest request) {
        request.getSession(true).setAttribute(UserInfo.class.getName(), this);
    }


}
