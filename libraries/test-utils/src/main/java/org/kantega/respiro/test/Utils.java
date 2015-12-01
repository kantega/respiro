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

package org.kantega.respiro.test;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;

/**
 * Created by helaar on 17.11.2015.
 */
public class Utils {

    public static File getBasedir() {
        return new File(URLDecoder.decode(Utils.class.getResource("/").getFile())).getParentFile().getParentFile();
    }
    public static String getReststopPort() {
        return getPort("/reststopPort.txt");
    }
    public static String getH2Port() {
        return getPort("/h2Port.txt");
    }

    private static String getPort(String path) {
        try {
            return new String(Files.readAllBytes(new File(URLDecoder.decode(Utils.class.getResource(path).getFile())).toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void waitForFile(String path, long timeoutMsec) {
        long timeout = System.currentTimeMillis()+timeoutMsec;
        File waitFor = new File(getBasedir(),path);
        while(System.currentTimeMillis() < timeout && !waitFor.exists())
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                //
            }
    }
}
