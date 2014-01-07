/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PoolExecutor;

/**
 * A traverser that runs in parallel using a number of threads. The ordering is non-deterministic.
 */
public class ParallelPortfolioNodeTraverser extends PortfolioNodeTraverser {

  private static final Logger s_logger = LoggerFactory.getLogger(ParallelPortfolioNodeTraverser.class);

  private final PoolExecutor _pool;

  /**
   * Creates a traverser.
   * 
   * @param callback the callback to invoke, not null
   * @param executorService the executor service for parallel resolutions
   */
  public ParallelPortfolioNodeTraverser(final PortfolioNodeTraversalCallback callback, final PoolExecutor executorService) {
    super(callback);
    ArgumentChecker.notNull(executorService, "executorService");
    _pool = executorService;
  }

  protected PoolExecutor.Service<?> createExecutorService() {
    return _pool.createService(null);
  }

  private static final class Context {

    private final PoolExecutor.Service<?> _executorService;
    private final PortfolioNodeTraversalCallback _callback;

    public Context(PoolExecutor.Service<?> executorService, PortfolioNodeTraversalCallback callback) {
      _executorService = executorService;
      _callback = callback;
    }

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
        _callback.preOrderOperation(_node);
        final List<PortfolioNode> childNodes = _node.getChildNodes();
        final List<Position> positions = _node.getPositions();
        _count.addAndGet(childNodes.size() + positions.size());
        for (final Position position : positions) {
          submit(new Runnable() {
            @Override
            public void run() {
              try {
                _callback.preOrderOperation(_node, position);
              } catch (Exception e) {
                s_logger.warn("Failed preOrderOperation", e);
              } finally {
                childDone();
              }
            }
          });
        }
        for (final PortfolioNode node : childNodes) {
          submit(new NodeTraverser(node, this));
        }
      }

      public void childDone() {
        if (_count.decrementAndGet() == 0) {
          if (_secondPass) {
            try {
              _callback.postOrderOperation(_node);
            } catch (Exception e) {
              s_logger.warn("Failed preOrderOperation", e);
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
                _callback.postOrderOperation(_node);
              } catch (Exception e) {
                s_logger.warn("Failed postOrderOperation", e);
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
                      _callback.postOrderOperation(_node, position);
                    } catch (Exception e) {
                      s_logger.warn("Failed postOrderOperation", e);
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
      _executorService.execute(runnable);
    }

    public void waitForCompletion() {
      try {
        _executorService.join();
      } catch (InterruptedException e) {
        s_logger.info("Interrupted waiting for completion");
        throw new OpenGammaRuntimeException("interrupted", e);
      }
    }

  }

  /**
   * Traverse the nodes notifying using the callback.
   * 
   * @param portfolioNode the node to start from, null does nothing
   */
  @Override
  public void traverse(final PortfolioNode portfolioNode) {
    if (portfolioNode == null) {
      return;
    }
    final Context context = new Context(createExecutorService(), getCallback());
    context.submit(context.new NodeTraverser(portfolioNode, null));
    context.waitForCompletion();
  }

}
