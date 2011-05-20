/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.connector;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import com.opengamma.util.tuple.Pair;

/**
 * Creates {@link ExecutorService} instances that limit the number of threads they create to a "per-client" threshold
 * and a "per-system" threshold. If there are reasons for limiting the number of threads the process should start the
 * system threshold should be used. If the service is shared by a number of clients, and a level of "fairness" is
 * required then a client threshold will prevent any one from saturating the system with requests.
 */
public final class ClientExecutor {

  private static final Logger s_logger = LoggerFactory.getLogger(ClientExecutor.class);

  private final int _maxThreadsPerClient;
  private final int _maxThreads;
  private final Executor _executor = Executors.newCachedThreadPool(new CustomizableThreadFactory("Dispatch-"));
  private final Queue<Pair<PerClientExecutor, Runnable>> _commands = new LinkedList<Pair<PerClientExecutor, Runnable>>();

  private int _activeThreads;

  /* package */ClientExecutor(final int maxThreadsPerClient, final int maxThreads) {
    _maxThreadsPerClient = maxThreadsPerClient;
    _maxThreads = maxThreads;
  }

  public int getMaxThreadsPerClient() {
    return _maxThreadsPerClient;
  }

  public int getMaxThreads() {
    return _maxThreads;
  }

  private class PerClientExecutor extends AbstractExecutorService {

    private final Queue<Runnable> _clientCommands = new LinkedList<Runnable>();
    private int _activeClientThreads;
    private int _activeCommands;
    private int _waitingTermination;
    private volatile boolean _poisoned;

    private class RunFirst implements Runnable {

      private final Runnable _command;

      public RunFirst(final Runnable command) {
        _command = command;
      }

      @Override
      public void run() {
        try {
          s_logger.debug("Running command for client {}", PerClientExecutor.this);
          _command.run();
        } catch (Throwable t) {
          s_logger.error("Exception thrown by command", t);
        }
        synchronized (ClientExecutor.this) {
          synchronized (PerClientExecutor.this) {
            if ((--_activeCommands == 0) && (_waitingTermination > 0)) {
              PerClientExecutor.this.notifyAll();
            }
            if (_clientCommands.isEmpty()) {
              _activeClientThreads--;
              respawnOrDecrementGlobalThreadCount();
            } else {
              s_logger.debug("Running queued commands for client {}", PerClientExecutor.this);
              _executor.execute(new RunRemaining());
            }
          }
        }
      }

    }

    // Caller must hold both monitors
    private void respawnOrDecrementGlobalThreadCount() {
      Pair<PerClientExecutor, Runnable> command = _commands.poll();
      while (command != null) {
        synchronized (command.getFirst()) {
          if (command.getFirst()._activeClientThreads < getMaxThreadsPerClient()) {
            // Transfer the execution slot to the other client
            s_logger.debug("Releasing thread to client {}", command.getFirst());
            command.getFirst()._activeClientThreads++;
            _executor.execute(command.getFirst().new RunFirst(command.getSecond()));
            return;
          } else {
            // There is at least one thread running for the client, so put in its queue
            s_logger.debug("Passing command from global queue to queue for client {}", command.getFirst());
            command.getFirst()._clientCommands.add(command.getSecond());
            command = _commands.poll();
          }
        }
      }
      // No global tasks left
      s_logger.debug("No commands left in global queue");
      _activeThreads--;
    }

    private class RunRemaining implements Runnable {

      @Override
      public void run() {
        Runnable command = null;
        do {
          synchronized (ClientExecutor.this) {
            synchronized (PerClientExecutor.this) {
              if (command != null) {
                // Don't do this on the first time round the loop
                if ((--_activeCommands == 0) && (_waitingTermination > 0)) {
                  PerClientExecutor.this.notifyAll();
                }
              }
              command = _clientCommands.poll();
              if (command == null) {
                s_logger.debug("No more deferred commands for client {}", PerClientExecutor.this);
                _activeClientThreads--;
                respawnOrDecrementGlobalThreadCount();
                return;
              }
            }
          }
          try {
            s_logger.debug("Running deferred command for client {}", PerClientExecutor.this);
            command.run();
          } catch (Throwable t) {
            s_logger.error("Exception thrown by command", t);
          }
        } while (true);
      }

    }

    @Override
    public void execute(final Runnable command) {
      if (_poisoned) {
        s_logger.warn("Executor has been shutdown");
        return;
      }
      s_logger.debug("Executing command for {}", this);
      synchronized (ClientExecutor.this) {
        synchronized (this) {
          _activeCommands++;
          if (_activeClientThreads < getMaxThreadsPerClient()) {
            _activeClientThreads++;
            if (_activeThreads < getMaxThreads()) {
              _activeThreads++;
              _executor.execute(new RunFirst(command));
            } else {
              _activeClientThreads--;
              s_logger.debug("Total thread limit exceeded, queuing command for {}", this);
              _commands.add(Pair.of(this, command));
            }
          } else {
            s_logger.debug("Client thread limit exceeded, queuing command for {}", this);
            _clientCommands.add(command);
          }
          s_logger.debug("Active client threads {}, total active threads {}", _activeClientThreads, _activeThreads);
        }
      }
    }

    @Override
    public synchronized boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
      _waitingTermination++;
      try {
        timeout = unit.toMillis(timeout);
        long tWaitUntil = System.currentTimeMillis() + timeout;
        while (((_activeClientThreads > 0) || !_clientCommands.isEmpty()) && (System.currentTimeMillis() < tWaitUntil)) {
          wait(timeout);
        }
        return (_activeClientThreads == 0) && _clientCommands.isEmpty();
      } finally {
        _waitingTermination--;
      }
    }

    @Override
    public boolean isShutdown() {
      return _poisoned;
    }

    @Override
    public boolean isTerminated() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void shutdown() {
      _poisoned = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
      throw new UnsupportedOperationException();
    }

  }

  /* package */ExecutorService createClientExecutor() {
    return new PerClientExecutor();
  }

}
