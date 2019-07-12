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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;

public class UserInfoServlet extends HttpServlet {

    private final ActiveDirectoryDAO activeDirectoryDAO;


    UserInfoServlet(ActiveDirectoryDAO activeDirectoryDAO) {
        this.activeDirectoryDAO = activeDirectoryDAO;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if( req.getSession(false) != null && req.getSession(false).getAttribute(KerberosFilter.AUTORIZED_PRINCIPAL_SESSION_ATTRIBUTE) != null) {
            // kerberos filter has authenticated user...
            resp.getWriter().write(UserInfo.from(req, Optional.of(activeDirectoryDAO)).toJson());
            resp.getWriter().flush();
            resp.setStatus(SC_OK);        }
        else
            resp.setStatus(SC_NOT_FOUND);
    }


}
