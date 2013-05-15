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
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.util.tuple.IntObjectPair;
import com.opengamma.util.tuple.Pair;

/**
 * Fudge message builder for {@link DependencyGraph}
 */
@FudgeBuilderFor(DependencyGraph.class)
public class DependencyGraphFudgeBuilder implements FudgeBuilder<DependencyGraph> {

  private static final String CALCULATION_CONFIGURATION_NAME_FIELD = "calculationConfigurationName";
  private static final String NODE_FIELD = "dependencyNode";
  private static final String EDGE_FIELD = "edge";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, DependencyGraph depGraph) {
    MutableFudgeMsg msg = serializer.newMessage();
    msg.add(CALCULATION_CONFIGURATION_NAME_FIELD, null, depGraph.getCalculationConfigurationName());
    
    Map<DependencyNode, Integer> nodeToId = new HashMap<DependencyNode, Integer>();
    Set<IntObjectPair<Integer>> edges = new HashSet<IntObjectPair<Integer>>();
    for (DependencyNode node : depGraph.getDependencyNodes()) {
      processEdges(node, edges, nodeToId, serializer, msg);
    }
    for (Pair<Integer, Integer> edge : edges) {
      msg.add(EDGE_FIELD, edge.getFirst());
      msg.add(EDGE_FIELD, edge.getSecond());
    }
    return msg;
  }

  private void processEdges(DependencyNode node, Set<IntObjectPair<Integer>> edges, Map<DependencyNode, Integer> nodeToId, FudgeSerializer serializer, MutableFudgeMsg msg) {
    int nodeId = getNodeId(node, nodeToId, serializer, msg);
    for (DependencyNode inputNode : node.getInputNodes()) {
      int inputNodeId = getNodeId(inputNode, nodeToId, serializer, msg);
      edges.add(IntObjectPair.of(inputNodeId, (Integer) nodeId));
    }
  }

  private int getNodeId(DependencyNode node, Map<DependencyNode, Integer> nodeToId, FudgeSerializer serializer, MutableFudgeMsg msg) {
    Integer id = nodeToId.get(node);
    if (id == null) {
      id = nodeToId.size();
      nodeToId.put(node, id);
      serializer.addToMessage(msg, NODE_FIELD, null, node);
    }
    return id;
  }
  
  @Override
  public DependencyGraph buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    String calcConfigName = msg.getString(CALCULATION_CONFIGURATION_NAME_FIELD);
    List<DependencyNode> nodes = new ArrayList<DependencyNode>();
    for (FudgeField nodeField : msg.getAllByName(NODE_FIELD)) {
      DependencyNode node = deserializer.fieldValueToObject(DependencyNode.class, nodeField);
      nodes.add(node);
    }
    List<FudgeField> edgeConnections = msg.getAllByName(EDGE_FIELD);
    for (int i = 0; i < edgeConnections.size(); i += 2) {
      int from = deserializer.fieldValueToObject(Integer.class, edgeConnections.get(i));
      int to = deserializer.fieldValueToObject(Integer.class, edgeConnections.get(i + 1));
      DependencyNode inputNode = nodes.get(from);
      DependencyNode dependentNode = nodes.get(to);
      dependentNode.addInputNode(inputNode);
    }
    
    DependencyGraph graph = new DependencyGraph(calcConfigName);
    for (DependencyNode node : nodes) {
      graph.addDependencyNode(node);
    }
    return graph;
  } 
  
}
