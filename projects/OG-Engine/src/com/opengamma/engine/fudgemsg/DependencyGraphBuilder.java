/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.util.tuple.Pair;

/**
 * Fudge message builder for {@link DependencyGraph}
 */
@FudgeBuilderFor(DependencyGraph.class)
public class DependencyGraphBuilder implements FudgeBuilder<DependencyGraph> {

  private static final String CALCULATION_CONFIGURATION_NAME_FIELD = "calculationConfigurationName";
  private static final String NODE_FIELD = "dependencyNode";
  private static final String EDGE_FIELD = "edge";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, DependencyGraph depGraph) {
    MutableFudgeMsg msg = context.newMessage();
    msg.add(CALCULATION_CONFIGURATION_NAME_FIELD, null, depGraph.getCalculationConfigurationName());
    
    Map<DependencyNode, Integer> nodeToId = new HashMap<DependencyNode, Integer>();
    Set<Pair<Integer, Integer>> edges = new HashSet<Pair<Integer, Integer>>();
    for (DependencyNode node : depGraph.getDependencyNodes()) {
      processEdges(node, edges, nodeToId, context, msg);
    }
    for (Pair<Integer, Integer> edge : edges) {
      context.addToMessage(msg, EDGE_FIELD, null, edge);
    }
    return msg;
  }

  private void processEdges(DependencyNode node, Set<Pair<Integer, Integer>> edges, Map<DependencyNode, Integer> nodeToId, FudgeSerializationContext context, MutableFudgeMsg msg) {
    int nodeId = getNodeId(node, nodeToId, context, msg);
    for (DependencyNode inputNode : node.getInputNodes()) {
      int inputNodeId = getNodeId(inputNode, nodeToId, context, msg);
      edges.add(Pair.<Integer, Integer>of(inputNodeId, nodeId));
    }
  }

  private int getNodeId(DependencyNode node, Map<DependencyNode, Integer> nodeToId, FudgeSerializationContext context, MutableFudgeMsg msg) {
    Integer id = nodeToId.get(node);
    if (id == null) {
      id = nodeToId.size();
      nodeToId.put(node, id);
      context.addToMessage(msg, NODE_FIELD, null, node);
    }
    return id;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public DependencyGraph buildObject(FudgeDeserializationContext context, FudgeMsg msg) {
    String calcConfigName = msg.getString(CALCULATION_CONFIGURATION_NAME_FIELD);
    List<DependencyNode> nodes = new ArrayList<DependencyNode>();
    for (FudgeField nodeField : msg.getAllByName(NODE_FIELD)) {
      DependencyNode node = context.fieldValueToObject(DependencyNode.class, nodeField);
      nodes.add(node);
    }
    for (FudgeField edgeField : msg.getAllByName(EDGE_FIELD)) {
      Pair<Integer, Integer> edge = context.fieldValueToObject(Pair.class, edgeField);
      DependencyNode inputNode = nodes.get(edge.getFirst());
      DependencyNode dependentNode = nodes.get(edge.getSecond());
      dependentNode.addInputNode(inputNode);
    }
    
    DependencyGraph graph = new DependencyGraph(calcConfigName);
    for (DependencyNode node : nodes) {
      graph.addDependencyNode(node);
    }
    return graph;
  } 
  
}
