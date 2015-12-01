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

package org.kantega.respiro.api;

import javax.xml.ws.Service;

/**
 * Created by helaar on 16.10.2015.
 */
public interface ServiceBuilder {
    <P> Build<P> service(Class<? extends Service> service, Class<P> port);

    interface Build<P> {
        Build<P> username(String username);
        Build<P> password(String password);
        Build<P> endpointAddress(String endpointAddress);
        Build<P> receiveTimeoutMs(long timeoutMs);
        Build<P> connectTimeoutMs(long timeoutMs);

        P build();
    }
}
