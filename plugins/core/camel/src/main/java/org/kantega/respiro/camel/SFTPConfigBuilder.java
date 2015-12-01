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

package org.kantega.respiro.camel;

/**
 * Created by helaar on 11.11.2015.
 */
public class SFTPConfigBuilder {



    public static Builder sftp(String knownHostsFile, String host, int port, String remotePath, String localWorkDir) {
        return new Builder(knownHostsFile, host, port, remotePath, localWorkDir);
    }

    public enum YesNo {yes, no};
    public enum LogLevel {INFO, WARN};

    public static class Builder {


        private final String knownHostsFile;
        private final String host;
        private final int port;
        private final String remotePath;
        private String username;
        private String password;
        private String localDir;
        private boolean shouldDelete = true;
        private YesNo strictHostKeyChecking = YesNo.no;
        private LogLevel jschLoggingLevel = LogLevel.WARN;

        public Builder(String knownHostsFile, String host, int port, String remotePath, String localWorkDir) {

            this.localDir = localWorkDir;
            this.host = host;
            this.port = port;
            this.remotePath = remotePath;
            this.knownHostsFile = knownHostsFile;
        }

        public String build() {
            StringBuilder builder = new StringBuilder();

            builder.append("sftp:");
            if (username != null && username.length() > 0)
                builder.append(username).append("@");
            builder.append(host).append(":").append(port).append(remotePath);
            builder.append("?knownHostsFile=").append(knownHostsFile);
            builder.append("&localWorkDirectory=").append(localDir);
            builder.append("&delete=").append(this.shouldDelete);
            builder.append("&strictHostKeyChecking=").append(strictHostKeyChecking);
            builder.append("&jschLoggingLevel=").append(jschLoggingLevel);
            if (password != null && password.length() > 0)
                builder.append("&password=").append(password);

            return builder.toString();
        }

        public Builder auth(String username, String password) {
            this.username = username.trim();
            this.password = password.trim();
            return this;
        }

        public Builder delete(boolean shouldDelete) {
            this.shouldDelete = shouldDelete;
            return this;
        }

        public Builder strictHostKeyChecking(YesNo checkValue) {
            this.strictHostKeyChecking = checkValue;
            return this;
        }

        public Builder jschLoggingLevel(LogLevel level) {
            this.jschLoggingLevel = level;
            return this;
        }
    }


}
