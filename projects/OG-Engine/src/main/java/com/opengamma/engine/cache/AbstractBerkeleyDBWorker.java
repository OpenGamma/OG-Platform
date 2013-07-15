/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;

/**
 * A worker thread that will perform all Berkeley I/O operations. Berkeley DB doesn't like its threads getting interrupted so we have to isolate it from the OpenGamma calculation and worker threads
 * that use interrupts to cancel running jobs.
 */
public class AbstractBerkeleyDBWorker implements Runnable {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractBerkeleyDBWorker.class);

  /**
   * Requests that this worker will satisfy.
   */
  public abstract static class Request {

    private boolean _done;

    protected final synchronized void waitFor() {
      try {
        while (!_done) {
          wait();
        }
      } catch (InterruptedException e) {
        throw new OpenGammaRuntimeException("Interrupted", e);
      }
    }

    protected final synchronized void signal() {
      _done = true;
      notify();
    }

    protected abstract void runInTransaction(AbstractBerkeleyDBWorker worker);

  }

  /**
   * A request to stop this worker.
   */
  public static final class PoisonRequest extends Request {

    @Override
    protected void runInTransaction(AbstractBerkeleyDBWorker worker) {
      worker.poison(this);
    }

  }

  private final Environment _environment;
  private final BlockingQueue<Request> _requests;
  private Transaction _transaction;
  private PoisonRequest _poisoned;

  public AbstractBerkeleyDBWorker(final Environment environment, final BlockingQueue<Request> requests) {
    _environment = environment;
    _requests = requests;
  }

  private void poison(final PoisonRequest poison) {
    _poisoned = poison;
  }

  protected Transaction getTransaction() {
    return _transaction;
  }

  @Override
  public void run() {
    s_logger.info("Worker started");
    _poisoned = null;
    do {
      Request req = null;
      try {
        req = _requests.take();
        if (req == null) {
          s_logger.info("Worker poisoned");
          return;
        }
        boolean rollback = true;
        _transaction = (_environment != null) ? _environment.beginTransaction(null, null) : null;
        try {
          do {
            req.runInTransaction(this);
            req.signal();
            req = _requests.poll();
          } while (req != null);
          rollback = false;
        } finally {
          if (_transaction != null) {
            if (rollback) {
              s_logger.error("Rolling back transaction");
              _transaction.abort();
            } else {
              _transaction.commit();
            }
            _transaction = null;
          }
        }
      } catch (Throwable t) {
        s_logger.error("Caught exception", t);
      } finally {
        if (req != null) {
          req.signal();
        }
      }
    } while (_poisoned == null);
    // If there are other workers, put the poison back in the queue for them
    _requests.add(_poisoned);
  }

}
