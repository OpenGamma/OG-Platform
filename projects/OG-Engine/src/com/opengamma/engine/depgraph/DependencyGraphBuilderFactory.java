/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Constructs {@link DependencyGraphBuider} instances with common parameters. All dependency graph builders
 * created by a single factory will share the same additional thread allowance.
 */
public class DependencyGraphBuilderFactory {

  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraphBuilderFactory.class);

  private static final Executor s_executor = Executors.newCachedThreadPool(new ThreadFactory() {

    private final AtomicInteger _nextJobThreadId = new AtomicInteger();

    @Override
    public Thread newThread(final Runnable r) {
      final Thread t = new Thread(r) {
        @Override
        public void run() {
          s_logger.info("Starting background thread {}", this);
          super.run();
          s_logger.info("Finished background thread {}", this);
        }
      };
      t.setDaemon(true);
      t.setName(DependencyGraphBuilder.class.getSimpleName() + "-" + _nextJobThreadId.incrementAndGet());
      return t;
    }

  });

  private int _maxAdditionalThreadsPerBuilder = DependencyGraphBuilder.getDefaultMaxAdditionalThreads();
  private int _maxAdditionalThreads = DependencyGraphBuilder.getDefaultMaxAdditionalThreads();
  private final Executor _executor = createExecutor();

  public DependencyGraphBuilderFactory() {
  }

  public void setMaxAdditionalThreadsPerBuilder(final int maxAdditionalThreadsPerBuilder) {
    _maxAdditionalThreadsPerBuilder = maxAdditionalThreadsPerBuilder;
  }

  public int getMaxAdditionalThreadsPerBuilder() {
    return _maxAdditionalThreadsPerBuilder;
  }

  public void setMaxAdditionalThreads(final int maxAdditionalThreads) {
    _maxAdditionalThreads = maxAdditionalThreads;
  }

  public int getMaxAdditionalThreads() {
    return _maxAdditionalThreads;
  }

  public DependencyGraphBuilder newInstance() {
    final DependencyGraphBuilder builder = new DependencyGraphBuilder(getExecutor());
    configureBuilder(builder);
    return builder;
  }

  protected void configureBuilder(final DependencyGraphBuilder builder) {
    builder.setMaxAdditionalThreads(getMaxAdditionalThreadsPerBuilder());
  }

  protected Executor createExecutor() {
    // Wrap calls to the underlying executor so that all threads are pooled but a pool is not created for each factory
    return new Executor() {

      private final AtomicInteger _threads = new AtomicInteger();
      private final Queue<Runnable> _commands = new ConcurrentLinkedQueue<Runnable>();

      private Runnable wrap(final Runnable command) {
        return new Runnable() {
          @Override
          public void run() {
            try {
              s_logger.debug("Starting job execution");
              command.run();
            } finally {
              s_logger.debug("Job execution complete");
              threadExit();
              s_logger.debug("Thread exit complete");
            }
          }
        };
      }

      private void executeImpl(final Runnable command) {
        getDefaultExecutor().execute(wrap(command));
      }

      private void threadExit() {
        int threads = _threads.decrementAndGet();
        while (threads < getMaxAdditionalThreads()) {
          final Runnable command = _commands.poll();
          if (command == null) {
            if (s_logger.isDebugEnabled()) {
              s_logger.debug("No pending commands to run - {}", threads);
            }
            return;
          }
          if (s_logger.isDebugEnabled()) {
            s_logger.debug("Thread capacity available - {}", threads);
          }
          threads = _threads.incrementAndGet();
          if (threads <= getMaxAdditionalThreads()) {
            if (s_logger.isDebugEnabled()) {
              s_logger.debug("Thread capacity {} acquired for execution", threads);
            }
            executeImpl(command);
            return;
          }
          if (s_logger.isDebugEnabled()) {
            s_logger.debug("Too many threads {} - requeuing job", threads);
          }
          _commands.add(command);
          threads = _threads.decrementAndGet();
        }
      }

      @Override
      public void execute(final Runnable command) {
        final int threads = _threads.incrementAndGet();
        if (threads <= getMaxAdditionalThreads()) {
          if (s_logger.isDebugEnabled()) {
            s_logger.debug("Direct execution - {} threads", threads);
          }
          executeImpl(command);
          return;
        }
        // Already started too many jobs
        if (s_logger.isDebugEnabled()) {
          s_logger.debug("Too many threads {} - queuing job", threads);
        }
        _commands.add(command);
        threadExit();
      }
    };
  }

  protected static Executor getDefaultExecutor() {
    return s_executor;
  }

  protected Executor getExecutor() {
    return _executor;
  }

}
