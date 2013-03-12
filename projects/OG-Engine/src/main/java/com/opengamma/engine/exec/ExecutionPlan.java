/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.engine.cache.CacheSelectHint;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.exec.ExecutionPlanCache.DependencyNodeKey;
import com.opengamma.engine.exec.stats.GraphExecutorStatisticsGatherer;

/**
 * Abstract description of an execution plan. A plan can be created from a set of {@link GraphFragment} objects for persistence into a cache to avoid the build cost. A cached plan can be used to
 * construct a set of objects equivalent to the originals and start the execution for a different executor.
 */
/* package */abstract class ExecutionPlan implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final Logger s_logger = LoggerFactory.getLogger(ExecutionPlan.class);

  private static Collection<DependencyNode> mapNodes(final Collection<DependencyNode> from, final Map<DependencyNodeKey, DependencyNode> to) {
    final Collection<DependencyNode> nodes = new ArrayList<DependencyNode>(from.size());
    for (final DependencyNode node : from) {
      final DependencyNode mapped = to.get(new DependencyNodeKey(node));
      if (mapped == null) {
        s_logger.warn("Node {} not mapped", node);
      } else if (mapped == node) {
        s_logger.debug("Using identity mapping");
        return from;
      } else {
        nodes.add(mapped);
      }
    }
    return nodes;
  }

  private static Collection<DependencyNode> mapNodeKeys(final Collection<DependencyNodeKey> from, final Map<DependencyNodeKey, DependencyNode> to) {
    final Collection<DependencyNode> nodes = new ArrayList<DependencyNode>(from.size());
    for (final DependencyNodeKey node : from) {
      final DependencyNode mapped = to.get(node);
      if (mapped == null) {
        s_logger.warn("Node {} not mapped", node);
      } else {
        nodes.add(mapped);
      }
    }
    return nodes;
  }

  private static final class SingleFragment extends ExecutionPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    private final transient Collection<DependencyNode> _nodes;
    private final CacheSelectHint _cacheSelectHint;

    public SingleFragment(final Collection<DependencyNode> nodes, final CacheSelectHint cacheSelectHint) {
      _nodes = nodes;
      _cacheSelectHint = cacheSelectHint;
    }

    @Override
    public Future<DependencyGraph> run(final GraphFragmentContext context, final GraphExecutorStatisticsGatherer statistics) {
      s_logger.info("Running {}", this);
      final CompleteGraphFragment fragment = new CompleteGraphFragment(context, statistics, _nodes);
      context.allocateFragmentMap(1);
      fragment.setCacheSelectHint(_cacheSelectHint);
      fragment.execute(context);
      return fragment.getFuture();
    }

    @Override
    public SingleFragment withNodes(final Map<DependencyNodeKey, DependencyNode> nodes) {
      // The supplied nodes are the content of the fragment
      return new SingleFragment(nodes.values(), _cacheSelectHint);
    }

    /* package */@Override
    boolean isTestEqual(final ExecutionPlan other) {
      if (other instanceof SingleFragment) {
        return _cacheSelectHint.isPrivate() == ((SingleFragment) other)._cacheSelectHint.isPrivate();
      }
      return false;
    }

  }

  private static final class MultipleFragment extends ExecutionPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final class FragmentDescriptor implements Serializable {

      private static final long serialVersionUID = 1L;

      private final transient Collection<DependencyNode> _nodes;
      private volatile Collection<DependencyNodeKey> _nodeKeys;
      private final CacheSelectHint _cacheSelectHint;
      private final int[] _inputs;
      private final int[] _outputs;
      private final int[] _tail;

      private FragmentDescriptor(final GraphFragment<?> fragment) {
        _nodes = fragment.getNodes();
        _cacheSelectHint = fragment.getCacheSelectHint();
        int[] a;
        int i;
        if (fragment.getInputFragments().isEmpty()) {
          _inputs = null;
        } else {
          a = new int[fragment.getInputFragments().size()];
          i = 0;
          for (final GraphFragment<?> input : fragment.getInputFragments()) {
            a[i++] = input.getIdentifier();
          }
          _inputs = a;
        }
        a = new int[fragment.getOutputFragments().size()];
        i = 0;
        for (final GraphFragment<?> output : fragment.getOutputFragments()) {
          a[i++] = output.getIdentifier();
        }
        if ((i == 1) && (a[0] == 1)) {
          _outputs = null;
        } else {
          _outputs = a;
        }
        final Collection<? extends GraphFragment<?>> tails = fragment.getTail();
        if (tails != null) {
          a = new int[tails.size()];
          i = 0;
          for (final GraphFragment<?> tail : tails) {
            a[i++] = tail.getIdentifier();
          }
          _tail = a;
        } else {
          _tail = null;
        }
      }

      private FragmentDescriptor(final FragmentDescriptor copyFrom, final Map<DependencyNodeKey, DependencyNode> withNodes) {
        if (copyFrom.getNodes() != null) {
          _nodes = mapNodes(copyFrom.getNodes(), withNodes);
        } else {
          _nodes = mapNodeKeys(copyFrom.getNodeKeys(), withNodes);
        }
        _cacheSelectHint = copyFrom.getCacheSelectHint();
        _inputs = copyFrom.getInputs();
        _outputs = copyFrom.getOutputs();
        _tail = copyFrom.getTail();
      }

      public Collection<DependencyNode> getNodes() {
        return _nodes;
      }

      public Collection<DependencyNodeKey> getNodeKeys() {
        return _nodeKeys;
      }

      public CacheSelectHint getCacheSelectHint() {
        return _cacheSelectHint;
      }

      public int[] getInputs() {
        return _inputs;
      }

      public int[] getOutputs() {
        return _outputs;
      }

      public int[] getTail() {
        return _tail;
      }

      /**
       * Populates the node keys structure.
       */
      private void writeObject(final java.io.ObjectOutputStream out) throws IOException {
        if (_nodeKeys == null) {
          final Collection<DependencyNodeKey> keys = new ArrayList<DependencyNodeKey>(_nodes.size());
          for (final DependencyNode node : _nodes) {
            keys.add(new DependencyNodeKey(node));
          }
          _nodeKeys = keys;
        }
        out.defaultWriteObject();
      }

    }

    private final Map<Integer, FragmentDescriptor> _fragments;

    public MultipleFragment(final GraphFragment<?> root) {
      this(new HashMap<Integer, FragmentDescriptor>());
      s_logger.info("Creating {}", this);
      process(root.getInputFragments());
    }

    private MultipleFragment(final Map<Integer, FragmentDescriptor> fragments) {
      _fragments = fragments;
    }

    private void process(final Collection<? extends GraphFragment<?>> fragments) {
      for (final GraphFragment<?> fragment : fragments) {
        if (_fragments.get(fragment.getIdentifier()) == null) {
          _fragments.put(fragment.getIdentifier(), new FragmentDescriptor(fragment));
          process(fragment.getInputFragments());
        }
      }
    }

    @SuppressWarnings({"unchecked" })
    @Override
    public Future<DependencyGraph> run(final GraphFragmentContext context, final GraphExecutorStatisticsGatherer statistics) {
      s_logger.info("Running {} for {} fragments", this, _fragments.size());
      context.allocateFragmentMap(_fragments.size());
      final Map<Integer, GraphFragment> fragments = Maps.newHashMapWithExpectedSize(_fragments.size());
      for (final Map.Entry<Integer, FragmentDescriptor> descriptor : _fragments.entrySet()) {
        final GraphFragment fragment = new GraphFragment(context, descriptor.getValue().getNodes());
        fragment.setCacheSelectHint(descriptor.getValue().getCacheSelectHint());
        fragments.put(descriptor.getKey(), fragment);
      }
      final List<GraphFragment> runnables = new LinkedList<GraphFragment>();
      final GraphFragment.Root root = new GraphFragment.Root(context, statistics);
      for (final Map.Entry<Integer, FragmentDescriptor> entry : _fragments.entrySet()) {
        final GraphFragment fragment = fragments.get(entry.getKey());
        final FragmentDescriptor descriptor = _fragments.get(entry.getKey());
        if (descriptor.getInputs() == null) {
          runnables.add(fragment);
        } else {
          for (final Integer i : descriptor.getInputs()) {
            fragment.getInputFragments().add(fragments.get(i));
          }
          fragment.initBlockCount();
        }
        if (descriptor.getOutputs() == null) {
          fragment.getOutputFragments().add(root);
          root.getInputFragments().add(fragment);
        } else {
          for (final Integer i : descriptor.getOutputs()) {
            fragment.getOutputFragments().add(fragments.get(i));
          }
        }
        if (descriptor.getTail() != null) {
          final List<GraphFragment> tail = new ArrayList<GraphFragment>(descriptor.getTail().length);
          for (final Integer i : descriptor.getTail()) {
            tail.add(fragments.get(i));
          }
          fragment.setTail(tail);
        }
      }
      root.initBlockCount();
      for (final GraphFragment runnable : runnables) {
        runnable.execute(context);
      }
      return root.getFuture();
    }

    @Override
    public MultipleFragment withNodes(final Map<DependencyNodeKey, DependencyNode> nodes) {
      final Map<Integer, FragmentDescriptor> fragments = new HashMap<Integer, FragmentDescriptor>(_fragments);
      for (final Map.Entry<Integer, FragmentDescriptor> fragment : fragments.entrySet()) {
        fragment.setValue(new FragmentDescriptor(fragment.getValue(), nodes));
      }
      return new MultipleFragment(fragments);
    }

    /* package */@Override
    boolean isTestEqual(final ExecutionPlan other) {
      if (other instanceof MultipleFragment) {
        return _fragments.size() == ((MultipleFragment) other)._fragments.size();
      }
      return false;
    }

  }

  /**
   * Creates an execution plan for fragment tree.
   * 
   * @param root the root of the fragment tree
   */
  public static ExecutionPlan of(final MutableGraphFragment.Root root) {
    return new MultipleFragment(root);
  }

  public static ExecutionPlan of(final CompleteGraphFragment fragment) {
    return new SingleFragment(fragment.getNodes(), fragment.getCacheSelectHint());
  }

  /**
   * Constructs appropriate objects and starts the execution.
   */
  public abstract Future<DependencyGraph> run(final GraphFragmentContext context, final GraphExecutorStatisticsGatherer statistics);

  public abstract ExecutionPlan withNodes(final Map<DependencyNodeKey, DependencyNode> nodes);

  /**
   * Tests equality between two plans; this is sufficient for the unit tests, it is not really a valid comparison check.
   */
  /* package */abstract boolean isTestEqual(ExecutionPlan other);

}
