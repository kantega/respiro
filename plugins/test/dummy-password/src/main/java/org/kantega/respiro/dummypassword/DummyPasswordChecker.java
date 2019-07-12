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

package org.kantega.respiro.dummypassword;

import org.kantega.respiro.security.AuthenticationResult;
import org.kantega.respiro.security.PasswordChecker;

import java.io.*;
import java.util.*;

/**
 *
 */
public class DummyPasswordChecker implements PasswordChecker {

    private final String basedir = System.getProperty("reststopPluginDir");

    private final Map<String, UserInfo> users;

    public DummyPasswordChecker() {
        try {
            File usersFile = new File(basedir, "src/test/security/users");
            users = new HashMap<>();

            BufferedReader reader = new BufferedReader(new FileReader(usersFile));
            String line;
            while((line = reader.readLine()) != null) {
                String tokens[] = line.split(":");
                if( tokens.length <2 && tokens.length >3 )
                    throw new RuntimeException("Invalid user configuration: " + line);

                String username = tokens[0].trim();
                String password = tokens[1].trim();
                String groups[] = tokens.length == 3 ? tokens[2].replaceAll("\\s", "").split(",") : new String[]{};

                users.put(username, new UserInfo(password, new HashSet<>(Arrays.asList(groups))));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public AuthenticationResult checkPassword(String username, String password) {

        UserInfo info = users.get(username);

        if (info != null && info.getPassword().equals(password))
            return new AuthenticationResult(true, username, info.getGroups());
        else
            return AuthenticationResult.UNAUTHENTICATED;
    }


    private class UserInfo {
        private final String password;
        private final Set<String> groups;


        private UserInfo(String password, Set<String> groups) {
            this.password = password;
            this.groups = groups;
        }

        public String getPassword() {
            return password;
        }

        public Set<String> getGroups() {
            return groups;
        }
    }
}
