/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

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
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.view.cache.CacheSelectHint;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;

/**
 * Abstract description of an execution plan. A plan can be created from a set of {@link GraphFragment}
 * objects for persistence into a cache to avoid the build cost. A cached plan can be used to
 * construct a set of objects equivalent to the originals and start the execution for a different
 * executor.
 */
/* package */abstract class ExecutionPlan {
  
  private static final Logger s_logger = LoggerFactory.getLogger(ExecutionPlan.class);

  private static final class SingleFragment extends ExecutionPlan {

    private final Collection<DependencyNode> _nodes;
    private final CacheSelectHint _cacheSelectHint;

    public SingleFragment(final Collection<DependencyNode> nodes, final CacheSelectHint cacheSelectHint) {
      s_logger.info("Creating {} for {} nodes", this, nodes.size());
      _nodes = nodes;
      _cacheSelectHint = cacheSelectHint;
    }

    @Override
    public Future<DependencyGraph> run(final GraphFragmentContext context, final GraphExecutorStatisticsGatherer statistics) {
      s_logger.info("Running {}", this);
      final CompleteGraphFragment fragment = new CompleteGraphFragment(context, statistics, _nodes);
      context.allocateFragmentMap(1);
      fragment.setCacheSelectHint(_cacheSelectHint);
      fragment.execute();
      return fragment.getFuture();
    }

  }
  
  private static final class MultipleFragment extends ExecutionPlan {

    private static final class FragmentDescriptor {

      private final Collection<DependencyNode> _nodes;
      private final CacheSelectHint _cacheSelectHint;
      private final int[] _inputs;
      private final int[] _outputs;
      private final int[] _tail;

      private FragmentDescriptor(final GraphFragment<?, ?> fragment) {
        _nodes = fragment.getNodes();
        _cacheSelectHint = fragment.getCacheSelectHint();
        int[] a;
        int i;
        if (fragment.getInputFragments().isEmpty()) {
          _inputs = null;
        } else {
          a = new int[fragment.getInputFragments().size()];
          i = 0;
          for (GraphFragment<?, ?> input : fragment.getInputFragments()) {
            a[i++] = input.getIdentifier();
          }
          _inputs = a;
        }
        a = new int[fragment.getOutputFragments().size()];
        i = 0;
        for (GraphFragment<?, ?> output : fragment.getOutputFragments()) {
          a[i++] = output.getIdentifier();
        }
        if ((i == 1) && (a[0] == 1)) {
          _outputs = null;
        } else {
          _outputs = a;
        }
        final Collection<? extends GraphFragment<?, ?>> tails = fragment.getTail();
        if (tails != null) {
          a = new int[tails.size()];
          i = 0;
          for (GraphFragment<?, ?> tail : tails) {
            a[i++] = tail.getIdentifier();
          }
          _tail = a;
        } else {
          _tail = null;
        }
      }

      public Collection<DependencyNode> getNodes() {
        return _nodes;
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

    }

    private final Map<Integer, FragmentDescriptor> _fragments = new HashMap<Integer, FragmentDescriptor>();

    public MultipleFragment(final GraphFragment<?, ?> root) {
      s_logger.info("Creating {}", this);
      process(root.getInputFragments());
    }

    private void process(final Collection<? extends GraphFragment<?, ?>> fragments) {
      for (GraphFragment<?, ?> fragment : fragments) {
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
      for (Map.Entry<Integer, FragmentDescriptor> descriptor : _fragments.entrySet()) {
        final GraphFragment fragment = new GraphFragment(context, descriptor.getValue().getNodes());
        fragment.setCacheSelectHint(descriptor.getValue().getCacheSelectHint());
        fragments.put(descriptor.getKey(), fragment);
      }
      final List<GraphFragment> runnables = new LinkedList<GraphFragment>();
      final GraphFragment.Root root = new GraphFragment.Root(context, statistics);
      for (Map.Entry<Integer, FragmentDescriptor> entry : _fragments.entrySet()) {
        final GraphFragment fragment = fragments.get(entry.getKey());
        final FragmentDescriptor descriptor = _fragments.get(entry.getKey());
        if (descriptor.getInputs() == null) {
          runnables.add(fragment);
        } else {
          for (Integer i : descriptor.getInputs()) {
            fragment.getInputFragments().add(fragments.get(i));
          }
          fragment.initBlockCount();
        }
        if (descriptor.getOutputs() == null) {
          fragment.getOutputFragments().add(root);
          root.getInputFragments().add(fragment);
        } else {
          for (Integer i : descriptor.getOutputs()) {
            fragment.getOutputFragments().add(fragments.get(i));
          }
        }
        if (descriptor.getTail() != null) {
          final List<GraphFragment> tail = new ArrayList<GraphFragment>(descriptor.getTail().length);
          for (Integer i : descriptor.getTail()) {
            tail.add(fragments.get(i));
          }
          fragment.setTail(tail);
        }
      }
      root.initBlockCount();
      for (GraphFragment runnable : runnables) {
        runnable.execute();
      }
      return root.getFuture();
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

}
