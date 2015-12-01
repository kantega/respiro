/*
 * Copyright 2015 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kantega.respiro.hello;

import javax.ws.rs.client.Client;

/**
 * Created by helaar on 20.10.2015.
 */
public class UserProfileService {

    private final String baseUrl;

    private final Client client;

    public UserProfileService(String baseUrl, Client client) {
        this.baseUrl = baseUrl;
        this.client = client;
    }


    UserProfile findUserProfile(String username){

        return client.target(baseUrl).path("userprofiles")
                .path(username.toUpperCase()).request().get(UserProfile.class);

    }


}
