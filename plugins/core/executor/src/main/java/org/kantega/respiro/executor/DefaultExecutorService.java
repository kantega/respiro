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

package org.kantega.respiro.executor;

import org.kantega.respiro.api.NettyExecutorService;
import org.kantega.respiro.collector.Collector;
import org.kantega.respiro.collector.ExchangeInfo;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 *
 */
public class DefaultExecutorService implements NettyExecutorService {
    private final ExecutorService wrapped;

    public DefaultExecutorService(ExecutorService wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean isShutdown() {
        return wrapped.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return wrapped.isTerminated();
    }

    @Override
    public void shutdown() {
        wrapped.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return wrapped.shutdownNow();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return wrapped.awaitTermination(timeout, unit);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return wrapped.invokeAll(restoringCallables(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return wrapped.invokeAll(restoringCallables(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return wrapped.invokeAny(restoringCallables(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return wrapped.invokeAny(restoringCallables(tasks), timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return wrapped.submit(restoringCallable(task));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return wrapped.submit(restoringRunnable(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return wrapped.submit(restoringRunnable(task), result);
    }

    @Override
    public void execute(Runnable command) {
        wrapped.execute(restoringRunnable(command));
    }

    private Runnable restoringRunnable(Runnable runnable) {

        ExchangeInfo exchangeInfo = assertCurrentExchangeInfo();

        return () -> {
            try {
                Collector.restoreCollectionContext(exchangeInfo);
                runnable.run();
            } finally {
                Collector.clearCollectionContext();
            }
        };
    }

    private <T> Callable<T> restoringCallable(Callable<? extends T> callable) {

        ExchangeInfo exchangeInfo = assertCurrentExchangeInfo();

        return () -> {
            try {
                Collector.restoreCollectionContext(exchangeInfo);
                return callable.call();
            } finally {
                Collector.clearCollectionContext();
            }
        };
    }

    private ExchangeInfo assertCurrentExchangeInfo() {
        return Collector.getCurrent()
                .orElseThrow(() ->
                        new IllegalStateException("NettyExecutorService cannot be used outside a SOAP / REST thread request context"));

    }

    private <T> Collection<? extends Callable<T>> restoringCallables(Collection<? extends Callable<T>> tasks) {
        return tasks.stream().map(this::restoringCallable).collect(Collectors.toList());
    }

    @Override
    public <T> Callable<T> callable(Callable<? extends T> callable) {
        return restoringCallable(callable);
    }

    @Override
    public <T> Collection<? extends Callable<T>> callables(Collection<? extends Callable<T>> tasks) {
        return restoringCallables(tasks);
    }

    @Override
    public Runnable runnable(Runnable runnable) {
        return runnable;
    }
}
