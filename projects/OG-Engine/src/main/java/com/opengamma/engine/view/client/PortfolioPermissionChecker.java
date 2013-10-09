/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import static com.opengamma.engine.view.permission.PortfolioPermission.ALLOW;
import static com.opengamma.engine.view.permission.PortfolioPermission.DENY;
import static com.opengamma.engine.view.permission.PortfolioPermission.PARTIAL;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.engine.view.permission.PortfolioPermission;
import com.opengamma.util.ArgumentChecker;

/**
 * Responsible for scanning a portfolio and, based on whether
 * a node's children are accessible, determines the permissions
 * for each individual node.
 */
public class PortfolioPermissionChecker {

  /**
   * The permissions determined for each node in the portfolio. Note
   * that not all nodes may have an entry. Any not can be assumed
   * to be denied access.
   */
  private final Map<PortfolioNode, PortfolioPermission> _permissions;

  /**
   * Constructor for the permission checker taking the portfolio to
   * check and the checker for each individual node.
   *
   * @param portfolio the portfolio to check, not null
   * @param nodeChecker the checker for each node in the portfolio, not null
   */
  public PortfolioPermissionChecker(Portfolio portfolio, NodeChecker nodeChecker) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    ArgumentChecker.notNull(nodeChecker, "nodeChecker");
    _permissions = checkNodes(portfolio.getRootNode(), nodeChecker);
  }

  /**
   * Recursively check each node in the portfolio.
   *
   * @param node the node to check
   * @param nodeChecker the checker for the node
   * @return map of nodes with permissions
   */
  private Map<PortfolioNode, PortfolioPermission> checkNodes(PortfolioNode node,
                                                             NodeChecker nodeChecker) {

    // TODO This should probably use PortfolioNodeTraverser for doing the depth-first traversal of the portfolio


    if (nodeChecker.check(node) == DENY) {
      return ImmutableMap.of(node, DENY);
    } else {
      List<PortfolioNode> children = node.getChildNodes();
      ImmutableMap.Builder<PortfolioNode, PortfolioPermission> builder = ImmutableMap.builder();
      // Result for node of interest is dependent on whether the children are accessible
      // so keep track of whether we are denied access to any
      boolean allAllowed = true;

      for (PortfolioNode child : children) {
        Map<PortfolioNode, PortfolioPermission> result = checkNodes(child, nodeChecker);
        builder.putAll(result);
        allAllowed = allAllowed && result.get(child) == ALLOW;
      }

      builder.put(node, allAllowed ? ALLOW : PARTIAL);
      return builder.build();
    }
  }

  /**
   * Returns the permissions for the specified node.
   *
   * @param node the node to check
   * @return the permissions for the node
   */
  public PortfolioPermission permissionCheck(PortfolioNode node) {
    return _permissions.containsKey(node) ? _permissions.get(node) : DENY;
  }
}
