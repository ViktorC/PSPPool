/*
 * Copyright 2017 Viktor Csomor
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
package net.viktorc.pp4j.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import net.viktorc.pp4j.api.JavaProcessExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sub-class of {@link ProcessPoolExecutor} that implements the {@link JavaProcessExecutorService} interface. It manages Java processes
 * to implement a multiprocessing mechanism with the same API as that of the standard Java thread pools. It communicates with the processes
 * via their standard streams exchanging serialized and encoded Java objects. It sends tasks to the processes for execution and it receives
 * the results or exceptions thrown in return.
 *
 * @author Viktor Csomor
 */
public class JavaProcessPoolExecutor extends ProcessPoolExecutor implements JavaProcessExecutorService {

  private static final Logger LOGGER = LoggerFactory.getLogger(JavaProcessPoolExecutor.class);

  /**
   * Constructs a Java process pool executor using the specified parameters.
   *
   * @param processManagerFactory The java process manager factory.
   * @param minPoolSize The minimum size of the process pool.
   * @param maxPoolSize The maximum size of the process pool.
   * @param reserveSize The number of available processes to keep in the pool.
   * @param threadKeepAliveTime The number of milliseconds of idleness after which threads are terminated if the sizes of the thread pools
   * exceed their core sizes.
   * @param <T> The type of the startup task.
   * @throws InterruptedException If the thread is interrupted while it is waiting for the core threads to start up.
   * @throws IllegalArgumentException If <code>options</code> is <code>null</code>, the minimum pool size is less than 0, or the maximum
   * pool size is less than the minimum pool size or 1, or the reserve size is less than 0 or greater than the maximum pool size.
   */
  public <T extends Runnable & Serializable> JavaProcessPoolExecutor(JavaProcessManagerFactory<T> processManagerFactory, int minPoolSize,
      int maxPoolSize, int reserveSize, long threadKeepAliveTime) throws InterruptedException {
    super(processManagerFactory, minPoolSize, maxPoolSize, reserveSize, threadKeepAliveTime);
  }

  /**
   * Constructs a Java process pool executor using the specified parameters.
   *
   * @param processManagerFactory The java process manager factory.
   * @param minPoolSize The minimum size of the process pool.
   * @param maxPoolSize The maximum size of the process pool.
   * @param reserveSize The number of available processes to keep in the pool.
   * @param <T> The type of the startup task.
   * @throws InterruptedException If the thread is interrupted while it is waiting for the core threads to start up.
   * @throws IllegalArgumentException If <code>options</code> is <code>null</code>, the minimum pool size is less than 0, or the maximum
   * pool size is less than the minimum pool size or 1, or the reserve size is less than 0 or greater than the maximum pool size.
   */
  public <T extends Runnable & Serializable> JavaProcessPoolExecutor(JavaProcessManagerFactory<T> processManagerFactory, int minPoolSize,
      int maxPoolSize, int reserveSize) throws InterruptedException {
    super(processManagerFactory, minPoolSize, maxPoolSize, reserveSize);
  }

  /**
   * An implementation of {@link JavaProcessExecutorService#invokeAll(Collection, long, TimeUnit)} with an additional parameter that allows
   * the method to signal to its caller whether it timed out.
   *
   * @param tasks The tasks to execute.
   * @param timeout The duration of time to wait for the execution of the tasks at most.
   * @param unit The unit of the wait time duration.
   * @param timedOut An atomic boolean that the method sets according to whether it timed out or not.
   * @param <T> The return type of the tasks.
   * @return A list of futures corresponding to the provided task. If the method times out, these tasks are all cancelled.
   * @throws InterruptedException If the executing thread is interrupted.
   */
  private <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit, AtomicBoolean timedOut)
      throws InterruptedException {
    if (tasks.isEmpty()) {
      throw new IllegalArgumentException("List of tasks cannot be empty");
    }
    timedOut.set(false);
    List<Future<T>> futures = new ArrayList<>();
    for (Callable<T> task : tasks) {
      futures.add(submit(task));
    }
    long waitTimeNs = unit.toNanos(timeout);
    for (int i = 0; i < futures.size(); i++) {
      Future<T> future = futures.get(i);
      long start = System.nanoTime();
      try {
        if (!future.isDone()) {
          future.get(waitTimeNs, TimeUnit.NANOSECONDS);
        }
      } catch (ExecutionException | CancellationException e) {
        LOGGER.warn(e.getMessage(), e);
      } catch (TimeoutException e) {
        timedOut.set(true);
        for (int j = i; j < futures.size(); j++) {
          Future<T> future1 = futures.get(j);
          if (!future1.isDone()) {
            futures.get(j).cancel(true);
          }
        }
        break;
      } finally {
        waitTimeNs -= (System.nanoTime() - start);
      }
    }
    return futures;
  }

  @Override
  public void execute(Runnable command) {
    Future<?> future = submit(command);
    try {
      future.get();
    } catch (ExecutionException e) {
      throw new UncheckedExecutionException(e);
    } catch (InterruptedException e) {
      future.cancel(true);
      Thread.currentThread().interrupt();
      throw new UncheckedExecutionException(e);
    }
  }

  @Override
  public <T> Future<T> submit(Callable<T> task, boolean terminateProcessAfterwards) {
    if (task == null) {
      throw new IllegalArgumentException("Task cannot be null");
    }
    try {
      return new CastFuture<>(submit(new JavaSubmission<>(new CastCallable<>((Callable<T> & Serializable) task)),
          terminateProcessAfterwards));
    } catch (Exception e) {
      throw new RejectedExecutionException(e);
    }
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result, boolean terminateProcessAfterwards) {
    return new CastFuture<>(submit((Callable<T> & Serializable) () -> {
      task.run();
      return result;
    }, terminateProcessAfterwards));
  }

  @Override
  public Future<?> submit(Runnable task, boolean terminateProcessAfterwards) {
    return submit(task, null, terminateProcessAfterwards);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
    return invokeAll(tasks, timeout, unit, new AtomicBoolean());
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
    return invokeAll(tasks, Long.MAX_VALUE, TimeUnit.DAYS);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    AtomicBoolean timedOut = new AtomicBoolean();
    for (Future<T> future : invokeAll(tasks, timeout, unit, timedOut)) {
      try {
        return future.get();
      } catch (ExecutionException | CancellationException e) {
        LOGGER.warn(e.getMessage(), e);
      }
    }
    if (timedOut.get()) {
      throw new TimeoutException("Timed out before any task could successfully complete");
    }
    throw new ExecutionException(new Exception("No task completed successfully"));
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
    try {
      return invokeAny(tasks, Long.MAX_VALUE, TimeUnit.DAYS);
    } catch (TimeoutException e) {
      throw new ExecutionException(e);
    }
  }

  @Override
  public List<Runnable> shutdownNow() {
    return super.forceShutdown().stream()
        .filter(s -> s instanceof JavaSubmission)
        .map(s -> ((JavaSubmission<?>) s).getTask())
        .collect(Collectors.toList());
  }

  /**
   * A wrapper class implementing the {@link Callable} interface for casting a serializable <code>Callable</code> instance with a not
   * explicitly serializable return type into a serializable instance with an explicitly serializable return type.
   *
   * @param <T> The serializable return type.
   * @param <S> The serializable <code>Callable</code> implementation with a not explicitly serializable return type.
   * @author Viktor Csomor
   */
  private static class CastCallable<T extends Serializable, S extends Callable<? super T> & Serializable>
      implements Callable<T>, Serializable {

    private final Callable<T> callable;

    /**
     * Constructs a serializable <code>Callable</code> instance with a serializable return type based on the specified serializable
     * <code>Callable</code> instance with a not explicitly serializable return type.
     *
     * @param callable The <code>Callable</code> to cast.
     */
    CastCallable(S callable) {
      this.callable = (Callable<T> & Serializable) callable;
    }

    @Override
    public T call() throws Exception {
      return callable.call();
    }

  }

  /**
   * An implementation of the {@link Future} interface for wrapping a <code>Future</code> instance into one with a return type that is a
   * sub-type of that of the wrapped instance.
   *
   * @param <T> The return type of the original <code>Future</code> instance.
   * @param <S> A subtype of <code>T</code>, the return type of the cast <code>Future</code> instance.
   * @author Viktor Csomor
   */
  private static class CastFuture<T, S extends T> implements Future<T> {

    private final Future<S> origFuture;

    /**
     * Constructs the wrapper object for the specified <code>Future</code> instance.
     *
     * @param origFuture The <code>Future</code> instance to cast.
     */
    CastFuture(Future<S> origFuture) {
      this.origFuture = origFuture;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return origFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
      return origFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
      return origFuture.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
      return origFuture.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return origFuture.get(timeout, unit);
    }

  }

  /**
   * An exception thrown if the execution of Java task fails is or interrupted.
   *
   * @author Viktor Csomor
   */
  public static class UncheckedExecutionException extends RuntimeException {

    /**
     * Constructs a wrapper for the specified exception.
     *
     * @param e The source exception.
     */
    private UncheckedExecutionException(Throwable e) {
      super(e);
    }

  }

}