/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.util.ArgumentChecker;

/**
 * A traverser that runs in parallel using a number of threads. The ordering is non-deterministic.
 */
public class ParallelPortfolioNodeTraverser extends PortfolioNodeTraverser {

  private final ExecutorService _executorService;

  /**
   * Creates a traverser.
   * 
   * @param callback the callback to invoke, not null
   * @param executorService the executor service for parallel resolutions
   */
  public ParallelPortfolioNodeTraverser(final PortfolioNodeTraversalCallback callback, final ExecutorService executorService) {
    super(callback);
    ArgumentChecker.notNull(executorService, "executorService");
    _executorService = executorService;
  }

  protected ExecutorService getExecutorService() {
    return _executorService;
  }

  private final class Context {

    private final AtomicInteger _count = new AtomicInteger();
    private final Queue<Runnable> _work = new ConcurrentLinkedQueue<Runnable>();

    private final class NodeTraverser implements Runnable {

      private final NodeTraverser _parent;
      private final PortfolioNode _node;
      private final AtomicInteger _count = new AtomicInteger();
      private volatile boolean _secondPass;

      public NodeTraverser(final PortfolioNode node, final NodeTraverser parent) {
        _node = node;
        _parent = parent;
      }

      @Override
      public void run() {
        getCallback().preOrderOperation(_node);
        final List<PortfolioNode> childNodes = _node.getChildNodes();
        final List<Position> positions = _node.getPositions();
        _count.addAndGet(childNodes.size() + positions.size());
        for (final Position position : positions) {
          submit(new Runnable() {
            @Override
            public void run() {
              try {
                getCallback().preOrderOperation(position);
              } finally {
                childDone();
              }
            }
          });
        }
        for (PortfolioNode node : childNodes) {
          submit(new NodeTraverser(node, this));
        }
      }

      public void childDone() {
        if (_count.decrementAndGet() == 0) {
          if (_secondPass) {
            try {
              getCallback().postOrderOperation(_node);
            } finally {
              if (_parent != null) {
                _parent.childDone();
              }
            }
          } else {
            _secondPass = true;
            final List<Position> positions = _node.getPositions();
            if (positions.isEmpty()) {
              try {
                getCallback().postOrderOperation(_node);
              } finally {
                if (_parent != null) {
                  _parent.childDone();
                }
              }
            } else {
              _count.addAndGet(positions.size());
              for (final Position position : positions) {
                submit(new Runnable() {
                  @Override
                  public void run() {
                    try {
                      getCallback().postOrderOperation(position);
                    } finally {
                      childDone();
                    }
                  }
                });
              }
            }
          }
        }
      }

    }

    private void submit(final Runnable runnable) {
      _count.incrementAndGet();
      if (_work.isEmpty()) {
        synchronized (this) {
          if (_work.isEmpty()) {
            _work.add(runnable);
            notify();
            // Don't submit a job - there will be the caller to waitForCompletion
            return;
          } else {
            _work.add(runnable);
          }
        }
      } else {
        _work.add(runnable);
      }
      getExecutorService().submit(new Runnable() {
        @Override
        public void run() {
          // The original thread might have raced ahead so there might not be work for us in the queue
          final Runnable underlying = _work.poll();
          if (underlying != null) {
            underlying.run();
            if (_count.decrementAndGet() == 0) {
              // This was the last piece of work
              synchronized (Context.this) {
                Context.this.notify();
              }
            }
          }
        }
      });
    }

    public void waitForCompletion() {
      try {
        do {
          Runnable work = _work.poll();
          while (work != null) {
            work.run();
            if (_count.decrementAndGet() == 0) {
              // This was the last piece of work - we're done
              return;
            }
            work = _work.poll();
          }
          // Nothing in the queue but the background threads are busy
          synchronized (this) {
            if (_count.get() == 0) {
              // We're done
              return;
            }
            if (_work.isEmpty()) {
              wait();
            }
          }
        } while (true);
      } catch (InterruptedException e) {
        throw new OpenGammaRuntimeException("interrupted", e);
      }
    }

  }

  /**
   * Traverse the nodes notifying using the callback.
   * 
   * @param portfolioNode the node to start from, null does nothing
   */
  public void traverse(PortfolioNode portfolioNode) {
    if (portfolioNode == null) {
      return;
    }
    final Context context = new Context();
    context.submit(context.new NodeTraverser(portfolioNode, null));
    context.waitForCompletion();
  }

}
