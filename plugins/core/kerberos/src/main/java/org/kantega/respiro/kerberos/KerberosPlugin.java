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

import org.kantega.reststop.api.Config;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;
import org.kantega.reststop.servlet.api.ServletBuilder;

import javax.servlet.Filter;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.kantega.reststop.servlet.api.FilterPhase.AUTHENTICATION;

@Plugin
public class KerberosPlugin {

    @Export
    private final Filter kerberosFilter;

    @Export
    private final Filter userInfoServlet;

    public KerberosPlugin(@Config(defaultValue = "true") boolean kerberosEnabled,
                          @Config String kerberosKeytabFileName,
                          @Config String kerberosPrincipal,
                          @Config String kerberosPassword,
                          @Config(defaultValue = "300") long ldapRenewUserInfoSecs,
                          @Config String kerberosLdapUserSearchBase,
                          @Config String ldapUrl,
                          @Config String ldapUser,
                          @Config String ldapPassword,
                          @Config String ldapRealm,
                          @Config(defaultValue = "sAMAccountName") String ldapPrincipalSearchAttribute,
                          ServletBuilder servletBuilder) {

        final Path kerberosKeytabFile = Paths.get(kerberosKeytabFileName).normalize();

        final KerberosFilter.Configuration config =
            new KerberosFilter.Configuration(kerberosEnabled, kerberosKeytabFile, kerberosPrincipal, kerberosPassword);

        final ActiveDirectoryDAO adConnection = new ActiveDirectoryDAO(ldapUrl, ldapUser, ldapPassword, ldapRealm,
            kerberosLdapUserSearchBase, ldapPrincipalSearchAttribute, ldapRenewUserInfoSecs);

        kerberosFilter = servletBuilder.filter(new KerberosFilter(config, adConnection), AUTHENTICATION, "/*");
        userInfoServlet = servletBuilder.servlet( new UserInfoServlet(adConnection),"/user");
    }
}
