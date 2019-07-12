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

package org.kantega.respiro.api;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 *
 */
public interface RespiroExecutorService extends ExecutorService {

    <T> Collection<? extends Callable<T>> callables(Collection<? extends Callable<T>> tasks);

    <T> Callable<T> callable(Callable<? extends T> callable);

    Runnable runnable(Runnable runnable);

}
