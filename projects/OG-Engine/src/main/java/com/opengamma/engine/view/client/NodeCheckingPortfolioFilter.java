/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import static com.opengamma.engine.view.permission.PortfolioPermission.ALLOW;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Optional;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.engine.view.permission.PortfolioPermission;
import com.opengamma.id.UniqueId;

/**
 * Portfolio filter that checks each node and its children to determine
 * whether it should remain in the filtered set. The principles for the
 * filtering are:
 *
 *         P-root
 *         - P-child1
 *           - P-child11
 *         - P-child2
 *           - P-child21
 *           - P-child22
 *         - P-child3
 *           - P-child31
 *
 * If we are permissioned for the whole portfolio, do no filtering.
 * If we are permissioned for whole branch e.g. just P-child2 in diagram
 * then take that node as the new root. If we are permissioned for discrete
 * branches then we need to create artificial parents to fill out the graph
 * If we are doing any filtering then aggregates become meaningless (as
 * they will include results from missing nodes) so they need to be replaced
 * with the artificial nodes.
 * Finally, strip off any partially-applicable roots which only have a
 * single child as they add clutter without much value
 */
public class NodeCheckingPortfolioFilter implements PortfolioFilter {

  /**
   * Generates a unique id for the portfolios. As the ids are never
   * actually used (for database lookups etc) we can just use an int.
   */
  private static int s_portfolioId;
  /**
   * Generates a unique id for the portfolio nodes. As the ids are never
   * actually used (for database lookups etc) we can just use an int.
   */
  private static int s_portfolioNodeId;

  /**
   * The node checker which performs a permission check on an individual
   * node disregarding its parents and children.
   */
  private final NodeChecker _nodeChecker;

  /**
   * Constructs the filter with the individual node checker.
   *
   * @param nodeChecker the node checker
   */
  public NodeCheckingPortfolioFilter(NodeChecker nodeChecker) {
    _nodeChecker = nodeChecker;
  }

  @Override
  public Portfolio generateRestrictedPortfolio(Portfolio portfolio) {

    PortfolioPermissionChecker checker = new PortfolioPermissionChecker(portfolio, _nodeChecker);
    PortfolioNode rootNode = portfolio.getRootNode();

    Optional<? extends PortfolioNode> newRoot = buildRestrictedRootNode(checker, rootNode);

    if (newRoot.isPresent()) {
      PortfolioNode node = newRoot.get();
      return node.equals(rootNode) ? portfolio : createPortfolioForNode(trimParents(node));
    } else {
      return new SimplePortfolio("Access Denied");
    }
  }

  /**
   * Recursively remove parents whilst there is only a single child.
   *
   * @param node the node to be trimmed
   * @return the trimmed node
   */
  private PortfolioNode trimParents(PortfolioNode node) {
    return node.getChildNodes().size() == 1 ?
        trimParents(node.getChildNodes().get(0)) :
        node;
  }

  private Portfolio createPortfolioForNode(PortfolioNode node) {

    return new SimplePortfolio(
        UniqueId.of("RESTRICTED_PORTFOLIO", "PF_" + s_portfolioId++),
        node.getName(),
        new SimplePortfolioNode(node));
  }

  /**
   * Recursively copy or remove nodes depending on whether they are
   * accessible or not.
   *
   * @param checker the checker to use for each node
   * @param node the node tree to copy
   * @return an optional node tree, empty if there are no permissions, else
   * populated with the copied node tree
   */
  private Optional<? extends PortfolioNode> buildRestrictedRootNode(PortfolioPermissionChecker checker,
                                                                    PortfolioNode node) {

    switch(checker.permissionCheck(node)) {
      case ALLOW:
        return Optional.of(node);
      case DENY:
        return Optional.absent();
      default:

        SimplePortfolioNode newRoot =
            new SimplePortfolioNode(UniqueId.of("RESTRICTED_NODE", "PN_" + s_portfolioNodeId++),
                                    node.getName() + " [restricted]");
        newRoot.addPositions(node.getPositions());

        for (Map.Entry<PortfolioNode, PortfolioPermission> entry : getAccessibleChildNodes(node, checker).entrySet()) {
          PortfolioNode childNode = entry.getValue() == ALLOW ?
              entry.getKey() :
              buildRestrictedRootNode(checker, entry.getKey()).get();
          newRoot.addChildNode(childNode);
        }

        return Optional.of(newRoot);
    }
  }

  private Map<PortfolioNode, PortfolioPermission> getAccessibleChildNodes(PortfolioNode rootNode, PortfolioPermissionChecker checker) {

    Map<PortfolioNode, PortfolioPermission> eligible = new LinkedHashMap<>();
    for (PortfolioNode node : rootNode.getChildNodes()) {
      PortfolioPermission permission = checker.permissionCheck(node);
      if (permission != PortfolioPermission.DENY) {
        eligible.put(node, permission);
      }
    }
    return eligible;
  }

}
