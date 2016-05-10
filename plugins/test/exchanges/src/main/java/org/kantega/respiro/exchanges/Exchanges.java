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
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public class Exchanges {
    private Deque<ExchangeInfo> exchangeLog = new ConcurrentLinkedDeque<>();

    private AtomicLong counter = new AtomicLong();

    private static Exchanges instance;

    public Exchanges() {
        instance = this;
    }

    public static Exchanges getInstance() {
        return instance;
    }

    public void addExchange(ExchangeInfo message) {
        while(exchangeLog.size() >= 50) {
            exchangeLog.removeLast();
        }
        exchangeLog.addFirst(message);
    }

    public ArrayList<ExchangeInfo> getExchangeLog() {
        return new ArrayList<>(exchangeLog);
    }

    public void clear() {
        exchangeLog.clear();
    }
}
