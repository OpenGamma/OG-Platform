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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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

  private final GraphBuildingContext _context;
  private final Map<ValueSpecification, DependencyNode> _spec2Node = new HashMap<ValueSpecification, DependencyNode>();
  private final Map<ParameterizedFunction, Map<ComputationTargetSpecification, Set<DependencyNode>>> _func2target2nodes =
      new HashMap<ParameterizedFunction, Map<ComputationTargetSpecification, Set<DependencyNode>>>();
  private final Collection<DependencyNode> _graphNodes = new ArrayList<DependencyNode>();
  private final Map<ValueRequirement, ValueSpecification> _resolvedValues = new HashMap<ValueRequirement, ValueSpecification>();
  private final BlockingQueue<Pair<ValueRequirement, ResolvedValue>> _resolved = new LinkedBlockingQueue<Pair<ValueRequirement, ResolvedValue>>();
  private final AtomicReference<Thread> _singleton = new AtomicReference<Thread>();
  private ResolutionFailureVisitor<?> _failureVisitor;

  public GetTerminalValuesCallback(final ResolutionFailureVisitor<?> failureVisitor, final GraphBuildingContext context) {
    _failureVisitor = failureVisitor;
    _context = context;
  }

  public void setResolutionFailureVisitor(final ResolutionFailureVisitor<?> failureVisitor) {
    _failureVisitor = failureVisitor;
  }

  @Override
  public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
    s_logger.error("Couldn't resolve {}", value);
    if (failure != null) {
      final ResolutionFailure failureImpl = failure.assertValueRequirement(value);
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
    nextA: for (ValueSpecification a : as) { //CSIGNORE
      if (bs.contains(a)) {
        // Exact match
        continue;
      }
      final String aName = a.getValueName();
      final ValueProperties aProperties = a.getProperties();
      boolean mismatch = false;
      for (ValueSpecification b : bs) {
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

  private DependencyNode getOrCreateNode(final ResolvedValue resolvedValue, final Set<ValueSpecification> downstream, final DependencyNode node,
      final boolean newNode) {
    Set<ValueSpecification> downstreamCopy = null;
    for (final ValueSpecification input : resolvedValue.getFunctionInputs()) {
      DependencyNode inputNode;
      node.addInputValue(input);
      inputNode = _spec2Node.get(input);
      if (inputNode != null) {
        s_logger.debug("Found node {} for input {}", inputNode, input);
        node.addInputNode(inputNode);
      } else {
        s_logger.debug("Finding node production for {}", input);
        final ResolvedValue inputValue = _context.getProduction(input);
        if (inputValue != null) {
          if (downstreamCopy == null) {
            downstreamCopy = new HashSet<ValueSpecification>(downstream);
            downstreamCopy.add(resolvedValue.getValueSpecification());
            s_logger.debug("Downstream = {}", downstreamCopy);
          }
          inputNode = getOrCreateNode(inputValue, downstreamCopy);
          if (inputNode != null) {
            node.addInputNode(inputNode);
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
      for (ValueSpecification valueSpecification : resolvedValue.getFunctionOutputs()) {
        DependencyNode existing = _spec2Node.get(valueSpecification);
        if (existing == null) {
          _spec2Node.put(valueSpecification, node);
        } else {
          // Simplest to keep the existing one (otherwise have to reconnect dependent nodes in the graph)
          node.removeOutputValue(valueSpecification);
        }
      }
      _graphNodes.add(node);
    }
    return node;
  }

  private DependencyNode getOrCreateNode(final ResolvedValue resolvedValue, final Set<ValueSpecification> downstream, final DependencyNode existingNode,
      final Set<DependencyNode> nodes) {
    if (existingNode != null) {
      return getOrCreateNode(resolvedValue, downstream, existingNode, false);
    } else {
      final DependencyNode newNode = new DependencyNode(resolvedValue.getValueSpecification().getTargetSpecification());
      newNode.setFunction(resolvedValue.getFunction());
      newNode.addOutputValues(resolvedValue.getFunctionOutputs());
      return getOrCreateNode(resolvedValue, downstream, newNode, true);
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
    for (DependencyNode node : nodes) {
      final Set<ValueSpecification> outputValues = node.getOutputValues();
      if (mismatchUnion(outputValues, resolvedValue.getFunctionOutputs())) {
        s_logger.debug("Can't reuse {} for {}", node, resolvedValue);
      } else {
        s_logger.debug("Reusing {} for {}", node, resolvedValue);
        // Update the output values for the node with the union. The input values will be dealt with by the caller.
        List<ValueSpecification> replacements = null;
        for (ValueSpecification output : resolvedValue.getFunctionOutputs()) {
          if (outputValues.contains(output)) {
            // Exact match found
            continue;
          }
          final String outputName = output.getValueName();
          final ValueProperties outputProperties = output.getProperties();
          for (ValueSpecification outputValue : outputValues) {
            if (outputName == outputValue.getValueName()) {
              if (outputValue.getProperties().isSatisfiedBy(outputProperties)) {
                // Found match
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
        if (replacements != null) {
          final Iterator<ValueSpecification> replacement = replacements.iterator();
          while (replacement.hasNext()) {
            final ValueSpecification oldValue = replacement.next();
            final ValueSpecification newValue = replacement.next();
            node.replaceOutputValue(oldValue, newValue);
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

  private void completeGraphBuild() {
    while (!_resolved.isEmpty() && _singleton.compareAndSet(null, Thread.currentThread())) {
      synchronized (this) {
        Pair<ValueRequirement, ResolvedValue> resolved = _resolved.poll();
        while (resolved != null) {
          final DependencyNode node = getOrCreateNode(resolved.getSecond(), Collections.<ValueSpecification>emptySet());
          if (node != null) {
            assert node.getOutputValues().contains(resolved.getSecond().getValueSpecification());
            _resolvedValues.put(resolved.getFirst(), resolved.getSecond().getValueSpecification());
          } else {
            s_logger.error("Resolved {} to {} but couldn't create one or more dependency node", resolved.getFirst(), resolved.getSecond().getValueSpecification());
          }
          resolved = _resolved.poll();
        }
      }
      _singleton.set(null);
    }
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
    _resolved.add(Pair.of(valueRequirement, resolvedValue));
    completeGraphBuild();
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
    return new HashMap<ValueRequirement, ValueSpecification>(_resolvedValues);
  }

  protected synchronized void discardIntermediateState() {
    s_logger.debug("Discarding func2target2nodes state");
    _func2target2nodes.clear();
    s_logger.debug("Discarding spec2Node state");
    _spec2Node.clear();
  }

}
