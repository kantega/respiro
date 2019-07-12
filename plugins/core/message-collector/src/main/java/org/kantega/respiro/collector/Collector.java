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

package org.kantega.respiro.collector;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 */
public class Collector {

    private static ThreadLocal<ExchangeInfo> current = new ThreadLocal<>();
    private static Collection<CollectionListener> listeners = new CopyOnWriteArrayList<>();

    public static ExchangeInfo newCollectionContext(ExchangeMessage inMessage) {
        ExchangeInfo info = new ExchangeInfo(inMessage);
        current.set(info);
        return info;
    }

    public static Optional<ExchangeInfo> getCurrent() {
        return Optional.ofNullable(current.get());
    }

    public static void restoreCollectionContext(ExchangeInfo current) {
        Collector.current.set(current);
    }

    public static void clearCollectionContext() {
        Collector.current.remove();
    }

    public static void addListener(CollectionListener listener) {
        Collector.listeners.add(listener);
    }
    public static void removeListener(CollectionListener listener) {
        Collector.listeners.remove(listener);
    }

    public static void endCollectionContext() {
        for (CollectionListener listener : listeners) {
            Optional.ofNullable(current.get())
            .ifPresent(listener::collected);
        }
    }
}
