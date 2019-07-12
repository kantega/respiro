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

import org.simplericity.serberuhs.DefaultKerberosSubjectFactory;
import org.simplericity.serberuhs.KerberosSubjectFactory;
import org.simplericity.serberuhs.SpNego;
import org.simplericity.serberuhs.SpNegoResult;
import org.simplericity.serberuhs.filter.KerberosFilterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.Principal;
import java.util.Optional;

class KerberosFilter implements Filter {
    private final Logger logger = LoggerFactory.getLogger(KerberosFilter.class);
    private final KerberosSubjectFactory factory;
    private final Configuration configuration;
    private final ActiveDirectoryDAO activeDirectoryDAO;
    static final String AUTORIZED_PRINCIPAL_SESSION_ATTRIBUTE = KerberosFilter.class.getName() + "_AUTHORIZED_PRINCIPAL";
    static final String COMMON_NAME_SESSION_ATTRIBUTE = KerberosFilter.class.getName() + "_COMMON_NAME";

    KerberosFilter(Configuration configuration, ActiveDirectoryDAO activeDirectoryDAO) {
        this.configuration = configuration;
        this.activeDirectoryDAO = activeDirectoryDAO;
        factory = new DefaultKerberosSubjectFactory();
        factory.setConfiguration(configuration);
    }


    @Override
    public final void init(FilterConfig filterConfig) throws ServletException {
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) request;
        final HttpServletResponse res = (HttpServletResponse) response;

        if (configuration.isEnabled()) {
            if (!isUserLoggedIn(req)) {
                SpNego spNego = new SpNego();

                spNego.negotiate(factory.getSubject(), req, res);

                if (spNego.getResult() == SpNegoResult.AUTHORIZED) {
                    handleSuccessfulAuthorization(spNego.getAuthorizedPrincipal(), req, res, chain);
                } else if (spNego.getResult() == SpNegoResult.MISSING_AUTHORIZATION_HEADER) {
                    handleMissingAuthorizationHeader(req, res);
                } else {
                    logger.error("Failed to authorize user " + spNego.getResult() + ":" + (spNego.getException() == null ? "" : spNego.getException().getMessage()));
                    handleUnsuccessfulAutorization(req, res);
                }
            } else {
                authenticatedDispatch(req, res, chain);
            }

        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isUserLoggedIn(HttpServletRequest req){
        return !activeDirectoryDAO.shouldReload(UserInfo.from(req, Optional.empty()));
    }

    private void dispatchToFallback(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        if (configuration.getFallbackLoginPath() != null) {

            RequestDispatcher dispatcher = req.getRequestDispatcher(configuration.getFallbackLoginPath());

            dispatcher.forward(req, res);

        } else
            res.setStatus(401);
    }

    private void handleMissingAuthorizationHeader(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        dispatchToFallback(req, res);
    }

    private void handleUnsuccessfulAutorization(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

        dispatchToFallback(req, res);
    }

    private void handleSuccessfulAuthorization(String authorizedPrincipal, HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {

        exposePrincipalSessionAttribute(authorizedPrincipal, req);
        UserInfo userInfo = UserInfo.from(req, Optional.of(activeDirectoryDAO));
        exposeCommonNameSessionAttribute(userInfo, req);

        authenticatedDispatch(req, res, chain);
    }

    private void authenticatedDispatch(final HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        final UserInfo userInfo = UserInfo.from(req, Optional.of(activeDirectoryDAO));

        chain.doFilter(new HttpServletRequestWrapper(req) {
            @Override
            public String getRemoteUser() {
                return userInfo.getPrincipal();
            }

            @Override
            public boolean isUserInRole(String role) {
                return userInfo.getGroups().contains(role);
            }

            @Override
            public Principal getUserPrincipal() {
                return this::getRemoteUser;
            }
        }, res);
    }


    private void exposePrincipalSessionAttribute(String authorizedPrincipal, HttpServletRequest req) {
        req.getSession().setAttribute(AUTORIZED_PRINCIPAL_SESSION_ATTRIBUTE, authorizedPrincipal);

    }

    private void exposeCommonNameSessionAttribute(UserInfo userInfo, HttpServletRequest req) {
        req.getSession().setAttribute(COMMON_NAME_SESSION_ATTRIBUTE, userInfo.getCommonName());
    }


    @Override
    public void destroy() {

    }


    static class Configuration implements KerberosFilterConfiguration {

        private final boolean enabled;
        private final Path keytabFile;
        private final String principal;
        private final String password;

        Configuration(boolean enabled, Path keytabFile, String principal, String password) {
            this.enabled = enabled;
            this.keytabFile = keytabFile;
            this.principal = principal;
            this.password = password;
        }

        @Override
        public String getFallbackLoginPath() {
            return null;
        } // does not support login

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public File getKeytabFile() {
            return keytabFile.toFile();
        }

        @Override
        public String getPrincipal() {
            return principal;
        }

        @Override
        public String getPassword() {
            return password;
        }
    }
}