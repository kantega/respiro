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

package org.kantega.respiro.hello;

import org.kantega.respiro.api.RespiroExecutorService;
import org.kantega.respiro.hello.ws.greet_1_0.Greeting;
import org.kantega.respiro.hello.ws.hello_1.FaultElement;
import org.kantega.respiro.hello.ws.hello_1_0.Hello;
import org.kantega.respiro.hello.ws.hello_1_0.MyFault;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.xml.ws.WebServiceContext;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 */
@RolesAllowed("innholdsprodusent")
public class HelloService implements Hello{

    private Greeting grt;
    private UserProfileService upfs;
    private RespiroExecutorService executorService;

    @Resource
    private WebServiceContext context;

    public HelloService(Greeting grt, UserProfileService upfs, RespiroExecutorService executorService) {
        this.grt = grt;
        this.upfs = upfs;
        this.executorService = executorService;
    }

    public HelloService() {
        // Required by JAX-WS
    }

    public String greet(String receiver, String lang) throws MyFault{

        if( lang == null)
            lang = "en";

        if( lang.length() > 2) {
            FaultElement fe = new FaultElement();
            fe.setMessage("Invalid language '"+ lang+"'");
            throw new MyFault(fe.getMessage(), fe);
        }


        try {
            String username = context.getUserPrincipal().getName();

            Future<UserProfile> profileFuture = executorService.submit(() -> upfs.findUserProfile(username));

            UserProfile userProfile = profileFuture.get();

            return String.format("%s, %s! (called by %s)", grt.greet(lang), receiver, userProfile.getFullName());

        } catch (InterruptedException e) {
            throw  new RuntimeException(e);
        } catch (ExecutionException e) {
            throw  new RuntimeException(e.getCause());
        }


    }
}
