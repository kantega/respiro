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

package org.kantega.respiro.collector;

/**
 *
 */
public interface ExchangeMessage {

    enum Type {REQUEST, RESPONSE}

    String getPayload();

    String getAddress();

    String getMethod();

    String getHeaders();

    default ResponseStatus getResponseStatus() {
        if(getResponseCode() != null) {
            if (getResponseCode().startsWith("2")) {
                return ResponseStatus.SUCCESS;
            } else if (getResponseCode().startsWith("5")) {
                return ResponseStatus.ERROR;
            }
        }
        return ResponseStatus.UNDETERMINED;
    }

    String getResponseCode();

    Type getType();

    String getProtocol();

    enum ResponseStatus {SUCCESS, WARNING, ERROR, UNDETERMINED}
}
