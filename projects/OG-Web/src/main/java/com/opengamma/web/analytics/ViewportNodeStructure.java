/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ final class ViewportNodeStructure {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewportNodeStructure.class);

  private final AnalyticsNode _rootNode;
  private final Map<Integer, List<String>> _rowToPath = Maps.newHashMap();

  /* package */ ViewportNodeStructure(AnalyticsNode root, TargetLookup targetLookup) {
    Set<List<String>>  expandedNodes = new HashSet<>();
    _rootNode = createNode(root, targetLookup, expandedNodes);
  }

  /* package */ ViewportNodeStructure(AnalyticsNode root, TargetLookup targetLookup, Set<List<String>> expandedNodes) {
    _rootNode = createNode(root, targetLookup, expandedNodes);
  }

  /* package */ AnalyticsNode createNode(AnalyticsNode root, TargetLookup targetLookup, Set<List<String>> expandedNodes) {
    ArgumentChecker.notNull(targetLookup, "targetLookup");
    // root can be null if a view only contains primitives and doesn't have a portfolio
    if (root == null) {
      return null;
    } else {
      return createNode(root, Collections.<String>emptyList(), targetLookup, expandedNodes);
    }
  }

  private AnalyticsNode createNode(AnalyticsNode gridStructureNode,
                                   List<String> parentPath,
                                   TargetLookup targetLookup,
                                   Set<List<String>> expandedNodes) {
    List<String> path = Lists.newArrayList(parentPath);
    path.add(targetLookup.getRow(gridStructureNode.getStartRow()).getName());
    boolean expanded = expandedNodes.contains(path);
    if (expanded) {
      s_logger.debug("Building expanded node {}", path);
    }
    List<AnalyticsNode> viewportStructureChildNodes = Lists.newArrayList();
    for (AnalyticsNode gridStructureChildNode : gridStructureNode.getChildren()) {
      AnalyticsNode viewportStructureChildNode = createNode(gridStructureChildNode, path, targetLookup, expandedNodes);
      viewportStructureChildNodes.add(viewportStructureChildNode);
    }
    _rowToPath.put(gridStructureNode.getStartRow(), Collections.unmodifiableList(path));

    return new AnalyticsNode(gridStructureNode.getStartRow(),
                             gridStructureNode.getEndRow(),
                             viewportStructureChildNodes,
                             !expanded);
  }

  /* package */ List<String> getPathForRow(int rowIndex) {
    return _rowToPath.get(rowIndex);
  }

  /* package */ AnalyticsNode getRootNode() {
    return _rootNode;
  }
}
