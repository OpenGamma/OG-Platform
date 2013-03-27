/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/**
 * Handles callback notifications of terminal values to populate a graph set.
 */
/* package */class GetTerminalValuesCallback implements ResolvedValueCallback {

  private static final Logger s_logger = LoggerFactory.getLogger(GetTerminalValuesCallback.class);

  /**
   * Buffer of resolved value specifications. For any entries in here, all input values have been previously resolved and are in this buffer or the partially constructed graph. Information here gets
   * used to construct dependency graph fragments whenever a terminal item can be resolved.
   */
  private final ConcurrentMap<ValueSpecification, ResolvedValue> _resolvedBuffer = new ConcurrentHashMap<ValueSpecification, ResolvedValue>();

  /**
   * Index into the dependency graph nodes, keyed by their output specifications.
   */
  private final Map<ValueSpecification, DependencyNode> _spec2Node = new ConcurrentHashMap<ValueSpecification, DependencyNode>();

  /**
   * Index into the dependency graph nodes, keyed by their targets and functions.
   */
  private final Map<ParameterizedFunction, Map<ComputationTargetSpecification, Set<DependencyNode>>> _func2target2nodes =
      new HashMap<ParameterizedFunction, Map<ComputationTargetSpecification, Set<DependencyNode>>>();

  /**
   * All dependency graph nodes.
   */
  private final Set<DependencyNode> _graphNodes = new HashSet<DependencyNode>();

  /**
   * Terminal value resolutions, mapping the resolved value specifications back to the originally requested requested value requirements.
   */
  private final Map<ValueSpecification, Collection<ValueRequirement>> _resolvedValues = new HashMap<ValueSpecification, Collection<ValueRequirement>>();

  /**
   * Queue of completed resolutions that have not been processed into the partial graph. Graph construction is single threaded, with this queue holding work items if other thread produce results while
   * one is busy building the graph.
   */
  private final Queue<Pair<ValueRequirement, ResolvedValue>> _resolvedQueue = new LinkedTransferQueue<Pair<ValueRequirement, ResolvedValue>>();

  /**
   * Collection of nodes that are candidates for the targets to be collapsed. Graph construction is single threaded but the cost of running the collapse logic can be high so candidates are buffered
   * here and the collapse is performed outside of the mutex held while the main resolved queue is worked on. The mutex must however be reclaimed in order to action any changes to the dependency
   * graph.
   */
  private final Map<Set<DependencyNode>, Collection<Pair<DependencyNode, Object>>> _collapsing = new HashMap<Set<DependencyNode>, Collection<Pair<DependencyNode, Object>>>();

  /**
   * Queue of nodes that need to be collapsed. Graph construction is single threaded, with this queue holding nodes identified for collapse until the single thread lock can be re-acquired.
   */
  private final Queue<Pair<DependencyNode, DependencyNode>> _collapsedQueue = new LinkedTransferQueue<Pair<DependencyNode, DependencyNode>>();

  /**
   * Mutex for working on the resolved queue. Any thread can add to the queue, only the one that has claimed this mutex can process the elements from it and be sure of exclusive access to the other
   * data structures.
   */
  private final AtomicReference<Thread> _singleton = new AtomicReference<Thread>();

  /**
   * Optional visitor to process failures from the graph build. This can be specified to provide additional error reporting to the user.
   */
  private ResolutionFailureVisitor<?> _failureVisitor;

  /**
   * Optional logic to collapse nodes on mutually compatible targets into a single node.
   */
  private ComputationTargetCollapser _computationTargetCollapser;

  public GetTerminalValuesCallback(final ResolutionFailureVisitor<?> failureVisitor) {
    _failureVisitor = failureVisitor;
  }

  public void setResolutionFailureVisitor(final ResolutionFailureVisitor<?> failureVisitor) {
    _failureVisitor = failureVisitor;
  }

  public void setComputationTargetCollapser(final ComputationTargetCollapser collapser) {
    _computationTargetCollapser = collapser;
  }

  public ResolvedValue getProduction(final ValueSpecification specification) {
    final ResolvedValue value = _resolvedBuffer.get(specification);
    if (value != null) {
      return value;
    }
    final DependencyNode node = _spec2Node.get(specification);
    if (node == null) {
      return null;
    }
    // Can only use the specification if it is consumed by another node; i.e. it has been fully resolved
    // and is not just an advisory used to merge tentative results to give a single node producing multiple
    // outputs.
    synchronized (this) {
      for (final DependencyNode dependent : node.getDependentNodes()) {
        if (dependent.hasInputValue(specification)) {
          return new ResolvedValue(specification, node.getFunction(), node.getInputValuesCopy(), node.getOutputValuesCopy());
        }
      }
    }
    return null;
  }

  public void declareProduction(final ResolvedValue resolvedValue) {
    _resolvedBuffer.put(resolvedValue.getValueSpecification(), resolvedValue);
  }

  @Override
  public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
    s_logger.error("Couldn't resolve {}", value);
    if (failure != null) {
      final ResolutionFailure failureImpl = failure.checkFailure(value);
      if (_failureVisitor != null) {
        failureImpl.accept(_failureVisitor);
      }
      context.exception(new UnsatisfiableDependencyGraphException(failureImpl));
    } else {
      s_logger.warn("No failure state for {}", value);
      context.exception(new UnsatisfiableDependencyGraphException(value));
    }
  }

  private static boolean mismatchUnionImpl(final Set<ValueSpecification> as, final Set<ValueSpecification> bs) {
    nextA: for (final ValueSpecification a : as) { //CSIGNORE
      if (bs.contains(a)) {
        // Exact match
        continue;
      }
      final String aName = a.getValueName();
      final ValueProperties aProperties = a.getProperties();
      boolean mismatch = false;
      for (final ValueSpecification b : bs) {
        if (aName == b.getValueName()) {
          // Match the name; check the constraints
          if (aProperties.isSatisfiedBy(b.getProperties())) {
            continue nextA;
          } else {
            // Mismatch found
            mismatch = true;
          }
        }
      }
      if (mismatch) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tests whether the union of value specifications would be mismatched; that is the two sets can't be composed. Given the intersection of common value names, the properties must be mutually
   * compatible.
   * 
   * @param as the first set of values, not null
   * @param bs the second set of values, not null
   * @return true if the values can't be composed, false if they can
   */
  private static boolean mismatchUnion(final Set<ValueSpecification> as, final Set<ValueSpecification> bs) {
    return mismatchUnionImpl(as, bs) || mismatchUnionImpl(bs, as);
  }

  public synchronized void populateState(final DependencyGraph graph) {
    final Set<DependencyNode> remove = new HashSet<DependencyNode>();
    for (final DependencyNode node : graph.getDependencyNodes()) {
      for (final DependencyNode dependent : node.getDependentNodes()) {
        if (!graph.containsNode(dependent)) {
          // Need to remove "dependent" from the node. We can leave the output values there; they might be used, or will get discarded by discardUnusedOutputs afterwards
          remove.add(dependent);
        }
      }
      _graphNodes.add(node);
      for (final ValueSpecification output : node.getOutputValues()) {
        _spec2Node.put(output, node);
      }
      getOrCreateNodes(node.getFunction(), node.getComputationTarget()).add(node);
    }
    for (final DependencyNode node : remove) {
      node.clearInputs();
    }
    for (final Map.Entry<ValueSpecification, Set<ValueRequirement>> terminal : graph.getTerminalOutputs().entrySet()) {
      _resolvedValues.put(terminal.getKey(), new ArrayList<ValueRequirement>(terminal.getValue()));
    }
  }

  private void collapseNodes() {
    Pair<DependencyNode, DependencyNode> merge = _collapsedQueue.poll();
    while (merge != null) {
      if (_graphNodes.remove(merge.getFirst())) {
        s_logger.debug("Applying collapse {}", merge);
        merge.getFirst().replaceWith(merge.getSecond());
        for (final ValueSpecification output : merge.getFirst().getOutputValues()) {
          final ValueSpecification newOutput = MemoryUtils.instance(new ValueSpecification(output.getValueName(), merge.getSecond().getComputationTarget(), output.getProperties()));
          _spec2Node.remove(output);
          _spec2Node.put(newOutput, merge.getSecond());
          final Collection<ValueRequirement> requirements = _resolvedValues.remove(output);
          if (requirements != null) {
            _resolvedValues.put(newOutput, requirements);
          }
        }
        // TODO: Update _func2target2nodes to remove .getFirst and add .getSecond
        _graphNodes.add(merge.getSecond());
      } else {
        s_logger.debug("Ignoring collapse {}", merge);
      }
      merge = _collapsedQueue.poll();
    }
  }

  private void scheduleCollapsers(final GraphBuildingContext context) {
    if (!_collapsing.isEmpty()) {
      final Iterator<Map.Entry<Set<DependencyNode>, Collection<Pair<DependencyNode, Object>>>> itrCollapsing = _collapsing.entrySet().iterator();
      while (itrCollapsing.hasNext()) {
        final Map.Entry<Set<DependencyNode>, Collection<Pair<DependencyNode, Object>>> collapsing = itrCollapsing.next();
        if (collapsing.getValue().size() > 1) {
          final Iterator<Pair<DependencyNode, Object>> itrNodes = collapsing.getValue().iterator();
          boolean differentGroups = false;
          final Object group = itrNodes.next().getSecond();
          do {
            if (itrNodes.next().getSecond() != group) {
              differentGroups = true;
              break;
            }
          } while (itrNodes.hasNext());
          if (differentGroups) {
            itrCollapsing.remove();
            context.submit(new CollapseNodes(collapsing.getValue()));
          }
        }
      }
    }
  }

  // TODO: Use a mutable object instead of Pair so we don't have to keep reallocating to indicate a group change

  private class CollapseNodes implements ContextRunnable {

    private final Object _group = new Object();
    private final CollapseNodes _parent;
    private final Pair<DependencyNode, Object>[] _nodes;
    private final int _start;
    private int _end;
    private int _endLeft;
    private int _startRight;

    @SuppressWarnings("unchecked")
    public CollapseNodes(final Collection<Pair<DependencyNode, Object>> nodes) {
      _parent = null;
      _nodes = nodes.toArray(new Pair[nodes.size()]);
      _start = 0;
      _end = _nodes.length;
    }

    private CollapseNodes(final CollapseNodes parent, final Pair<DependencyNode, Object>[] nodes, final int start, final int end) {
      _parent = parent;
      _nodes = nodes;
      _start = start;
      _end = end;
    }

    private boolean equals(final ParameterizedFunction a, final ParameterizedFunction b) {
      if (a == b) {
        return true;
      }
      if (a.getFunction() != b.getFunction()) {
        if (!a.getFunction().getFunctionDefinition().getUniqueId().equals(b.getFunction().getFunctionDefinition().getUniqueId())) {
          return false;
        }
      }
      return a.getParameters().equals(b.getParameters());
    }

    private void finish(final GraphBuildingContext context) {
      if (_parent != null) {
        _parent.notifyComplete(context, _start, _end);
      } else {
        synchronized (GetTerminalValuesCallback.this) {
          collapseNodes();
          // Put any unmerged nodes (there will be at least one) back into the set
          if (_end > _start) {
            final Set<DependencyNode> inputs = _nodes[_start].getFirst().getInputNodes();
            Collection<Pair<DependencyNode, Object>> nodes = _collapsing.get(inputs);
            if (nodes == null) {
              nodes = new LinkedList<Pair<DependencyNode, Object>>();
              _collapsing.put(inputs, nodes);
            }
            for (int i = _start; i < _end; i++) {
              nodes.add(Pair.of(_nodes[i].getFirst(), _group));
            }
            scheduleCollapsers(context);
          }
        }
      }
    }

    private void notifyComplete(final GraphBuildingContext context, final int start, final int end) {
      synchronized (this) {
        if (_start == start) {
          // Completion notified for left half of a split
          _endLeft = end;
          if (_startRight == 0) {
            // Right half is still pending
            return;
          }
        } else {
          // Completion notified for right half of a split
          _startRight = start;
          _end = end;
          if (_endLeft == 0) {
            // Left half is still pending
            return;
          }
        }
      }
      if (_endLeft < _startRight) {
        final int l = _end - _startRight;
        System.arraycopy(_nodes, _startRight, _nodes, _endLeft, l);
        _end = _endLeft + l;
        _startRight = _endLeft;
      }
      for (int i = _start; i < _endLeft; i++) {
        final DependencyNode a = _nodes[i].getFirst();
        for (int j = _startRight; j < _end; j++) {
          if (_nodes[i].getSecond() == _nodes[j].getSecond()) {
            // Nodes are from the same group; have already been compared
            continue;
          }
          final DependencyNode b = _nodes[j].getFirst();
          if (equals(a.getFunction(), b.getFunction()) && a.getComputationTarget().getType().equals(b.getComputationTarget().getType())) {
            final ComputationTargetSpecification collapsed = _computationTargetCollapser.collapse(a.getFunction().getFunction(), a.getComputationTarget(), b.getComputationTarget());
            if (collapsed != null) {
              if (collapsed.equals(a.getComputationTarget())) {
                // A and B merged into A
                _nodes[j--] = _nodes[--_end];
                _collapsedQueue.add(Pair.of(b, a));
                s_logger.debug("Merging {} into {}", b, a);
              } else if (collapsed.equals(b.getComputationTarget())) {
                // A and B merged into B
                _nodes[i--] = _nodes[--_endLeft];
                _collapsedQueue.add(Pair.of(a, b));
                s_logger.debug("Merging {} into {}", a, b);
                break;
              } else {
                // A and B merged into new node X
                final DependencyNode x = new DependencyNode(collapsed);
                x.setFunction(a.getFunction());
                _nodes[i--] = _nodes[--_endLeft];
                _nodes[j] = _nodes[_end - 1];
                _nodes[_end - 1] = Pair.of(x, _group);
                _collapsedQueue.add(Pair.of(a, x));
                _collapsedQueue.add(Pair.of(b, x));
                if (s_logger.isDebugEnabled()) {
                  s_logger.debug("Merging {} and {} into new node {}", new Object[] {a, b, x });
                }
                break;
              }
            }
          }
        }
      }
      if (_endLeft < _startRight) {
        final int l = _end - _startRight;
        System.arraycopy(_nodes, _startRight, _nodes, _endLeft, l);
        _end = _endLeft + l;
        _startRight = _endLeft;
      }
      finish(context);
    }

    // ContextRunnable

    @Override
    public boolean tryRun(final GraphBuildingContext context) {
      if (_end - _start >= 4) {
        final int mid = (_start + _end) >> 1;
        context.submit(new CollapseNodes(this, _nodes, _start, mid));
        context.submit(new CollapseNodes(this, _nodes, mid, _end));
      } else {
        for (int i = _start; i < _end - 1; i++) {
          final DependencyNode a = _nodes[i].getFirst();
          for (int j = i + 1; j < _end; j++) {
            if (_nodes[i].getSecond() == _nodes[j].getSecond()) {
              // Nodes are from the same group; have already been compared
              continue;
            }
            final DependencyNode b = _nodes[j].getFirst();
            if (equals(a.getFunction(), b.getFunction()) && a.getComputationTarget().getType().equals(b.getComputationTarget().getType())) {
              final ComputationTargetSpecification collapsed = _computationTargetCollapser.collapse(a.getFunction().getFunction(), a.getComputationTarget(), b.getComputationTarget());
              if (collapsed != null) {
                if (collapsed.equals(a.getComputationTarget())) {
                  // A and B merged into A
                  _nodes[j--] = _nodes[--_end];
                  _collapsedQueue.add(Pair.of(b, a));
                  s_logger.debug("Merging {} into {}", b, a);
                } else if (collapsed.equals(b.getComputationTarget())) {
                  // A and B merged into B
                  _nodes[i--] = _nodes[--_end];
                  _collapsedQueue.add(Pair.of(a, b));
                  s_logger.debug("Merging {} into {}", a, b);
                  break;
                } else {
                  // A and B merged into new node X
                  final DependencyNode x = new DependencyNode(collapsed);
                  x.setFunction(a.getFunction());
                  _nodes[i--] = _nodes[--_end];
                  _nodes[j] = _nodes[_end - 1];
                  _nodes[_end - 1] = Pair.of(x, _group);
                  _collapsedQueue.add(Pair.of(a, x));
                  _collapsedQueue.add(Pair.of(b, x));
                  s_logger.debug("Merging {} and {} into new node {}", new Object[] {a, b, x });
                  break;
                }
              }
            }
          }
        }
        finish(context);
      }
      return true;
    }

  }

  private DependencyNode getOrCreateNode(final ResolvedValue resolvedValue, final Set<ValueSpecification> downstream, DependencyNode node,
      final boolean newNode) {
    Set<ValueSpecification> downstreamCopy = null;
    for (final ValueSpecification input : resolvedValue.getFunctionInputs()) {
      DependencyNode inputNode;
      inputNode = _spec2Node.get(input);
      if (inputNode != null) {
        s_logger.debug("Found node {} for input {}", inputNode, input);
        node.addInputValue(input);
        node.addInputNode(inputNode);
      } else {
        s_logger.debug("Finding node production for {}", input);
        final ResolvedValue inputValue = _resolvedBuffer.get(input);
        if (inputValue != null) {
          if (downstreamCopy == null) {
            downstreamCopy = new HashSet<ValueSpecification>(downstream);
            downstreamCopy.add(resolvedValue.getValueSpecification());
            s_logger.debug("Downstream = {}", downstreamCopy);
          }
          inputNode = getOrCreateNode(inputValue, downstreamCopy);
          if (inputNode != null) {
            node.addInputNode(inputNode);
            node.addInputValue(input);
          } else {
            s_logger.warn("No node production for {}", inputValue);
            return null;
          }
        } else {
          s_logger.warn("No registered production for {}", input);
          return null;
        }
      }
    }
    if (newNode) {
      s_logger.debug("Adding {} to graph set", node);
      // [PLAT-346] Here is a good spot to tackle PLAT-346; which node's outputs do we discard if there are multiple
      // productions for a given value specification?
      for (final ValueSpecification valueSpecification : resolvedValue.getFunctionOutputs()) {
        final DependencyNode existing = _spec2Node.get(valueSpecification);
        if (existing == null) {
          _spec2Node.put(valueSpecification, node);
        } else {
          // Simplest to keep the existing one (otherwise have to reconnect dependent nodes in the graph)
          node.removeOutputValue(valueSpecification);
        }
      }
      if ((_computationTargetCollapser != null) && _computationTargetCollapser.canApplyTo(node.getComputationTarget())) {
        // Test if the targets can be collapsed and another node used here
        final Set<DependencyNode> inputs = node.getInputNodes();
        boolean addToCollapse;
        if (inputs.isEmpty()) {
          addToCollapse = true;
        } else {
          addToCollapse = true;
          for (DependencyNode input : inputs) {
            if (_computationTargetCollapser.canApplyTo(input.getComputationTarget())) {
              // This node will be considered after it's input has been considered
              // TODO: This doesn't happen; either consider this now or put the logic in somewhere to consider this node
              addToCollapse = false;
              break;
            }
          }
        }
        if (addToCollapse) {
          Collection<Pair<DependencyNode, Object>> nodes = _collapsing.get(inputs);
          if (nodes == null) {
            nodes = new LinkedList<Pair<DependencyNode, Object>>();
            _collapsing.put(inputs, nodes);
          }
          nodes.add(Pair.of(node, (Object) node));
        }
      }
      _graphNodes.add(node);
      _resolvedBuffer.remove(resolvedValue.getValueSpecification());
    }
    return node;
  }

  private DependencyNode getOrCreateNode(final ResolvedValue resolvedValue, final Set<ValueSpecification> downstream, final DependencyNode existingNode,
      final Set<DependencyNode> nodes) {
    if (existingNode != null) {
      return getOrCreateNode(resolvedValue, downstream, existingNode, false);
    } else {
      DependencyNode newNode = new DependencyNode(resolvedValue.getValueSpecification().getTargetSpecification());
      newNode.setFunction(resolvedValue.getFunction());
      newNode.addOutputValues(resolvedValue.getFunctionOutputs());
      newNode = getOrCreateNode(resolvedValue, downstream, newNode, true);
      if (newNode != null) {
        nodes.add(newNode);
      }
      return newNode;
    }
  }

  private Set<DependencyNode> getOrCreateNodes(final ParameterizedFunction function, final ComputationTargetSpecification targetSpecification) {
    Map<ComputationTargetSpecification, Set<DependencyNode>> target2nodes = _func2target2nodes.get(function);
    if (target2nodes == null) {
      target2nodes = new HashMap<ComputationTargetSpecification, Set<DependencyNode>>();
      _func2target2nodes.put(function, target2nodes);
    }
    Set<DependencyNode> nodes = target2nodes.get(targetSpecification);
    if (nodes == null) {
      nodes = new HashSet<DependencyNode>();
      target2nodes.put(targetSpecification, nodes);
    }
    return nodes;
  }

  private DependencyNode findExistingNode(final Set<DependencyNode> nodes, final ResolvedValue resolvedValue) {
    for (final DependencyNode node : nodes) {
      final Set<ValueSpecification> outputValues = node.getOutputValues();
      if (mismatchUnion(outputValues, resolvedValue.getFunctionOutputs())) {
        s_logger.debug("Can't reuse {} for {}", node, resolvedValue);
      } else {
        s_logger.debug("Considering {} for {}", node, resolvedValue);
        // Update the output values for the node with the union. The input values will be dealt with by the caller.
        List<ValueSpecification> replacements = null;
        boolean matched = false;
        for (final ValueSpecification output : resolvedValue.getFunctionOutputs()) {
          if (outputValues.contains(output)) {
            // Exact match found
            matched = true;
            continue;
          }
          final String outputName = output.getValueName();
          final ValueProperties outputProperties = output.getProperties();
          for (final ValueSpecification outputValue : outputValues) {
            if (outputName == outputValue.getValueName()) {
              if (outputValue.getProperties().isSatisfiedBy(outputProperties)) {
                // Found match
                matched = true;
                final ValueProperties composedProperties = outputValue.getProperties().compose(outputProperties);
                if (!composedProperties.equals(outputValue.getProperties())) {
                  final ValueSpecification newOutputValue = MemoryUtils
                      .instance(new ValueSpecification(outputValue.getValueName(), outputValue.getTargetSpecification(), composedProperties));
                  s_logger.debug("Replacing {} with {} in reused node", outputValue, newOutputValue);
                  if (replacements == null) {
                    replacements = new ArrayList<ValueSpecification>(outputValues.size() * 2);
                  }
                  replacements.add(outputValue);
                  replacements.add(newOutputValue);
                }
              }
            }
          }
        }
        if (!matched) {
          continue;
        }
        if (replacements != null) {
          final Iterator<ValueSpecification> replacement = replacements.iterator();
          while (replacement.hasNext()) {
            final ValueSpecification oldValue = replacement.next();
            final ValueSpecification newValue = replacement.next();
            final int newConsumers = node.replaceOutputValue(oldValue, newValue);
            DependencyNode n = _spec2Node.remove(oldValue);
            assert n == node;
            n = _spec2Node.get(newValue);
            if (n != null) {
              // Reducing the value has created a collision ...
              if (newConsumers == 0) {
                // Keep the existing one (it's being used, or just an arbitrary choice if neither are used)
                node.removeOutputValue(newValue);
              } else {
                int existingConsumers = 0;
                for (final DependencyNode child : n.getDependentNodes()) {
                  if (child.hasInputValue(newValue)) {
                    existingConsumers++;
                  }
                }
                if (existingConsumers == 0) {
                  // Lose the existing (not being used), keep the new one
                  n.removeOutputValue(newValue);
                  _spec2Node.put(newValue, node);
                } else {
                  if (newConsumers <= existingConsumers) {
                    // Adjust the consumers of the reduced value to use the existing one
                    for (final DependencyNode child : node.getDependentNodes()) {
                      child.replaceInput(newValue, node, n);
                    }
                    node.removeOutputValue(newValue);
                  } else {
                    // Adjust the consumers of the existing value to use the new one
                    for (final DependencyNode child : n.getDependentNodes()) {
                      child.replaceInput(newValue, n, node);
                    }
                    n.removeOutputValue(newValue);
                    _spec2Node.put(newValue, node);
                  }
                }
              }
            } else {
              _spec2Node.put(newValue, node);
            }
          }
        }
        return node;
      }
    }
    return null;
  }

  private DependencyNode getOrCreateNode(final ResolvedValue resolvedValue, final Set<ValueSpecification> downstream) {
    s_logger.debug("Resolved {}", resolvedValue.getValueSpecification());
    if (downstream.contains(resolvedValue.getValueSpecification())) {
      s_logger.debug("Already have downstream production of {} in {}", resolvedValue.getValueSpecification(), downstream);
      return null;
    }
    final DependencyNode existingNode = _spec2Node.get(resolvedValue.getValueSpecification());
    if (existingNode != null) {
      s_logger.debug("Existing production of {} found in graph set", resolvedValue);
      return existingNode;
    }
    final Set<DependencyNode> nodes = getOrCreateNodes(resolvedValue.getFunction(), resolvedValue.getValueSpecification().getTargetSpecification());
    return getOrCreateNode(resolvedValue, downstream, findExistingNode(nodes, resolvedValue), nodes);
  }

  /**
   * Reports a successful resolution of a top level requirement. The production of linked {@link DependencyNode} instances to form the final graph is single threaded. The resolution is added to a
   * queue of successful resolutions. If this is the only (or first) thread to report resolutions then this will work to drain the queue and produce nodes for the graph based on the resolved value
   * cache in the building context. If other threads report resolutions while this is happening they are added to the queue and those threads return immediately.
   */
  @Override
  public void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
    s_logger.info("Resolved {} to {}", valueRequirement, resolvedValue.getValueSpecification());
    if (pump != null) {
      context.close(pump);
    }
    _resolvedQueue.add(Pair.of(valueRequirement, resolvedValue));
    while (!_resolvedQueue.isEmpty() && _singleton.compareAndSet(null, Thread.currentThread())) {
      synchronized (this) {
        Pair<ValueRequirement, ResolvedValue> resolved = _resolvedQueue.poll();
        while (resolved != null) {
          final DependencyNode node = getOrCreateNode(resolved.getSecond(), Collections.<ValueSpecification>emptySet());
          if (node != null) {
            ValueSpecification outputValue = resolved.getSecond().getValueSpecification();
            if (!outputValue.getTargetSpecification().equals(node.getComputationTarget())) {
              outputValue = MemoryUtils.instance(new ValueSpecification(outputValue.getValueName(), node.getComputationTarget(), outputValue.getProperties()));
            }
            assert node.getOutputValues().contains(outputValue);
            Collection<ValueRequirement> requirements = _resolvedValues.get(outputValue);
            if (requirements == null) {
              requirements = new ArrayList<ValueRequirement>();
              _resolvedValues.put(outputValue, requirements);
            }
            requirements.add(resolved.getFirst());
          } else {
            s_logger.error("Resolved {} to {} but couldn't create one or more dependency node", resolved.getFirst(), resolved.getSecond().getValueSpecification());
          }
          resolved = _resolvedQueue.poll();
        }
        collapseNodes();
        scheduleCollapsers(context);
      }
      _singleton.set(null);
    }
  }

  @Override
  public String toString() {
    return "TerminalValueCallback";
  }

  /**
   * Returns the dependency graph nodes built by calls to {@link #resolved}. It is only valid to call this when there are no pending resolutions - that is all calls to {@link #resolved} have returned.
   * A copy of the internal structure is used so that it may be modified by the caller and this callback instance be used to process subsequent resolutions.
   * 
   * @return the dependency graph nodes, not null
   */
  public synchronized Collection<DependencyNode> getGraphNodes() {
    return new ArrayList<DependencyNode>(_graphNodes);
  }

  /**
   * Returns the map of top level requirements requested of the graph builder to the specifications it produced that are in the dependency graph. Failed resolutions are not reported here. It is only
   * valid to call this when there are no pending resolutions - that is all calls to {@link #resolved} have returned. A copy of the internal structure is used so that it may be modified by the caller
   * and this callback instance be used to process subsequent resolutions.
   * 
   * @return the map of resolutions, not null
   */
  public synchronized Map<ValueRequirement, ValueSpecification> getTerminalValues() {
    final Map<ValueRequirement, ValueSpecification> result = new HashMap<ValueRequirement, ValueSpecification>(_resolvedValues.size());
    for (final Map.Entry<ValueSpecification, Collection<ValueRequirement>> resolvedValues : _resolvedValues.entrySet()) {
      for (final ValueRequirement requirement : resolvedValues.getValue()) {
        result.put(requirement, resolvedValues.getKey());
      }
    }
    return result;
  }

  public void reportStateSize() {
    if (!s_logger.isInfoEnabled()) {
      return;
    }
    s_logger.info("Graph = {} nodes for {} terminal outputs", _graphNodes.size(), _resolvedValues.size());
    s_logger.info("Resolved buffer = {}, resolved queue = {}", _resolvedBuffer.size(), _resolvedQueue.size());
  }

}
