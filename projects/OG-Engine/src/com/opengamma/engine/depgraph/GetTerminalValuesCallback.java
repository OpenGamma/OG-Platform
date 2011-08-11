/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.depgraph.DependencyGraphBuilder.GraphBuildingContext;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Handles callback notifications of terminal values so populate a graph set.
 */
/* package */class GetTerminalValuesCallback implements ResolvedValueCallback {

  private static final Logger s_logger = LoggerFactory.getLogger(GetTerminalValuesCallback.class);

  private final Map<ValueSpecification, DependencyNode> _spec2Node = new HashMap<ValueSpecification, DependencyNode>();
  private final Map<ParameterizedFunction, Map<ComputationTarget, List<DependencyNode>>> _func2target2nodes =
      new HashMap<ParameterizedFunction, Map<ComputationTarget, List<DependencyNode>>>();
  private final Set<DependencyNode> _graphNodes;
  private final Map<ValueRequirement, ValueSpecification> _resolvedValues;
  private final ResolutionFailureVisitor _failureVisitor;

  public GetTerminalValuesCallback(final Set<DependencyNode> graphNodes, final Map<ValueRequirement, ValueSpecification> resolvedValues, final ResolutionFailureVisitor failureVisitor) {
    ArgumentChecker.notNull(graphNodes, "graphNodes");
    ArgumentChecker.notNull(resolvedValues, "resolvedValues");
    _graphNodes = graphNodes;
    _resolvedValues = resolvedValues;
    _failureVisitor = failureVisitor;
  }

  @Override
  public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
    s_logger.error("Couldn't resolve {}", value);
    if (failure != null) {
      if (_failureVisitor != null) {
        failure.accept(_failureVisitor);
      }
      context.exception(new UnsatisfiableDependencyGraphException(failure));
    } else {
      s_logger.warn("No failure state for {}", value);
      context.exception(new UnsatisfiableDependencyGraphException(value));
    }
  }

  // Caller must already hold the monitor
  private List<DependencyNode> getOrCreateNodes(final ParameterizedFunction function, final ComputationTarget target) {
    Map<ComputationTarget, List<DependencyNode>> target2nodes = _func2target2nodes.get(function);
    if (target2nodes == null) {
      target2nodes = new HashMap<ComputationTarget, List<DependencyNode>>();
      _func2target2nodes.put(function, target2nodes);
    }
    List<DependencyNode> nodes = target2nodes.get(target);
    if (nodes == null) {
      nodes = new ArrayList<DependencyNode>();
      target2nodes.put(target, nodes);
    }
    return nodes;
  }

  private static boolean mismatchUnionImpl(final Set<ValueSpecification> as, final Set<ValueSpecification> bs) {
    for (ValueSpecification a : as) {
      if (bs.contains(a)) {
        // Exact match
        continue;
      }
      for (ValueSpecification b : bs) {
        if (a.getValueName() == b.getValueName()) {
          // Match the name, but other data wasn't exact so reject
          return true;
        }
      }
    }
    return false;
  }

  private static boolean mismatchUnion(final Set<ValueSpecification> as, final Set<ValueSpecification> bs) {
    return mismatchUnionImpl(as, bs) || mismatchUnionImpl(bs, as);
  }

  // Caller must already hold the monitor
  private DependencyNode getOrCreateNode(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue resolvedValue) {
    s_logger.debug("Resolved {} to {}", valueRequirement, resolvedValue.getValueSpecification());
    DependencyNode useExisting = _spec2Node.get(resolvedValue.getValueSpecification());
    if (useExisting != null) {
      s_logger.debug("Existing production of {} found in graph set", resolvedValue);
      return useExisting;
    }
    final List<DependencyNode> nodes = getOrCreateNodes(resolvedValue.getFunction(), resolvedValue.getComputationTarget());
    final DependencyNode node;
    // [PLAT-346] Here is a good spot to tackle PLAT-346; what do we merge into a single node, and which outputs
    // do we discard if there are multiple functions that can produce them.
    for (DependencyNode existingNode : nodes) {
      if (mismatchUnion(existingNode.getOutputValues(), resolvedValue.getFunctionOutputs())) {
        s_logger.debug("Can't reuse {} for {}", existingNode, resolvedValue);
      } else {
        s_logger.debug("Reusing {} for {}", existingNode, resolvedValue);
        useExisting = existingNode;
        break;
      }
    }
    if (useExisting != null) {
      node = useExisting;
    } else {
      node = new DependencyNode(resolvedValue.getComputationTarget());
      node.setFunction(resolvedValue.getFunction());
      nodes.add(node);
    }
    for (ValueSpecification output : resolvedValue.getFunctionOutputs()) {
      node.addOutputValue(output);
    }
    for (final ValueSpecification input : resolvedValue.getFunctionInputs()) {
      node.addInputValue(input);
      DependencyNode inputNode = _spec2Node.get(input);
      if (inputNode != null) {
        s_logger.debug("Found node {} for input {}", inputNode, input);
        node.addInputNode(inputNode);
      } else {
        s_logger.debug("Finding node productions for {}", input);
        final Map<ResolveTask, ResolvedValueProducer> resolver = context.getTasksProducing(input);
        if (!resolver.isEmpty()) {
          for (Map.Entry<ResolveTask, ResolvedValueProducer> resolvedEntry : resolver.entrySet()) {
            resolvedEntry.getValue().addCallback(context, new ResolvedValueCallback() {

              @Override
              public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
                // This shouldn't happen; if the value we're considering was produced once then the producer should be able to
                // produce it again.
                s_logger.error("Failed production for {} ({})", input, value);
                assert false;
              }

              @Override
              public void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
                synchronized (GetTerminalValuesCallback.this) {
                  final DependencyNode inputNode = getOrCreateNode(context, valueRequirement, resolvedValue);
                  node.addInputNode(inputNode);
                }
              }

              @Override
              public String toString() {
                return "node productions for " + input;
              }

            });
            // Only the values are ref-counted
            resolvedEntry.getValue().release(context);
          }
        } else {
          s_logger.warn("No registered node production for {}", input);
        }
      }
    }
    s_logger.debug("Adding {} to graph set", node);
    _spec2Node.put(resolvedValue.getValueSpecification(), node);
    _graphNodes.add(node);
    return node;
  }

  @Override
  public synchronized void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
    s_logger.info("Resolved {} to {}", valueRequirement, resolvedValue.getValueSpecification());
    getOrCreateNode(context, valueRequirement, resolvedValue);
    _resolvedValues.put(valueRequirement, resolvedValue.getValueSpecification());
    context.close(pump);
  }

  @Override
  public String toString() {
    return "TerminalValueCallback";
  }

};
