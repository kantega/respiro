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

package org.kantega.respiro.exchanges;

import org.kantega.respiro.collector.ExchangeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 */
public class Exchanges {
    private List<ExchangeInfo> exchangeLog = new CopyOnWriteArrayList<>();

    private static Exchanges instance;

    public Exchanges() {
        instance = this;
    }

    public static Exchanges getInstance() {
        return instance;
    }

    public void addExchange(ExchangeInfo message) {
        while(exchangeLog.size() > 50) {
            exchangeLog.remove(0);
        }
        exchangeLog.add(0, message);
    }

    public ArrayList<ExchangeInfo> getExchangeLog() {
        return new ArrayList<>(exchangeLog);
    }

    public void clear() {
        exchangeLog.clear();
    }
}
