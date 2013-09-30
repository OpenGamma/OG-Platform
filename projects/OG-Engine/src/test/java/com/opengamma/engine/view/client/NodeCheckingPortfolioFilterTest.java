/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import static com.opengamma.engine.view.permission.PortfolioPermission.ALLOW;
import static com.opengamma.engine.view.permission.PortfolioPermission.DENY;
import static com.opengamma.engine.view.client.PortfolioPermissionTestUtils.createAllowNodeChecker;
import static com.opengamma.engine.view.client.PortfolioPermissionTestUtils.createDenyNodeChecker;
import static com.opengamma.engine.view.client.PortfolioPermissionTestUtils.createMappedNodeChecker;
import static com.opengamma.engine.view.client.PortfolioPermissionTestUtils.nodeTree;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.engine.view.permission.PortfolioPermission;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class NodeCheckingPortfolioFilterTest {

  @Test
  public void testSingleNodePortfolioIsUnalteredWhenAllNodesAreAllowed() {

    SimplePortfolioNode root = nodeTree(1);
    Portfolio pf = new SimplePortfolio("node-1", root);

    Portfolio portfolio = new NodeCheckingPortfolioFilter(createAllowNodeChecker()).generateRestrictedPortfolio(pf);
    assertThat(portfolio == pf, is(true));
  }

  @Test
  public void testMultiNodePortfolioIsUnalteredWhenAllNodesAreAllowed() {

    SimplePortfolioNode root = nodeTree(1, nodeTree(2, nodeTree(4)), nodeTree(3));
    Portfolio pf = new SimplePortfolio("node-1", root);

    Portfolio portfolio = new NodeCheckingPortfolioFilter(createAllowNodeChecker()).generateRestrictedPortfolio(pf);
    assertThat(portfolio == pf, is(true));
  }

  @Test
  public void testEmptyPortfolioIsReturnedWhenAllNodesAreDenied() {

    SimplePortfolioNode root = nodeTree(1, nodeTree(2, nodeTree(4)), nodeTree(3));
    Portfolio pf = new SimplePortfolio("node-1", root);

    Portfolio portfolio = new NodeCheckingPortfolioFilter(createDenyNodeChecker()).generateRestrictedPortfolio(pf);

    assertThat(portfolio.getRootNode().size(), is(0));
    assertThat(portfolio.getName(), is("Access Denied"));
  }

  @Test
  public void testInaccessibleNodesAreRemoved() {

    /*
       1---
      / \  \
     2   3  4

    2 is allowed
    3 is denied
    4 is allowed
    Portfolio should just contain 1, 2 & 4 (1 is a restricted version though)

    */
    SimplePortfolioNode root = nodeTree(1, nodeTree(2), nodeTree(3), nodeTree(4));
    Portfolio pf = new SimplePortfolio("node-1", root);

    Map<Integer, PortfolioPermission> permissions = ImmutableMap.of(
        1, ALLOW,
        2, ALLOW,
        3, DENY,
        4, ALLOW);

    Portfolio portfolio = new NodeCheckingPortfolioFilter(createMappedNodeChecker(permissions)).generateRestrictedPortfolio(pf);

    assertThat(portfolio.getName(), is("node-1 [restricted]"));



    PortfolioNode rootNode = portfolio.getRootNode();
    assertThat(rootNode.getName(), is("node-1 [restricted]"));
    List<PortfolioNode> children = rootNode.getChildNodes();
    assertThat(children.size(), is(2));
    assertThat(children.get(0).getName(), is("node-2"));
    assertThat(children.get(1).getName(), is("node-4"));
  }

  @Test
  public void testUnnecessaryParentIsRemoved() {

    /*
       1---
      / \  \
     2   3  4

    2 is allowed
    3 is denied
    4 is denied
    Portfolio should just contain 2

    */
    SimplePortfolioNode root = nodeTree(1, nodeTree(2), nodeTree(3), nodeTree(4));
    Portfolio pf = new SimplePortfolio("node-1", root);

    Map<Integer, PortfolioPermission> permissions = ImmutableMap.of(
        1, ALLOW,
        2, ALLOW,
        3, DENY,
        4, DENY);

    Portfolio portfolio = new NodeCheckingPortfolioFilter(createMappedNodeChecker(permissions)).generateRestrictedPortfolio(pf);

    assertThat(portfolio.getName(), is("node-2"));
    PortfolioNode rootNode = portfolio.getRootNode();
    assertThat(rootNode.getName(), is("node-2"));
    assertThat(rootNode.getChildNodes().size(), is(0));
  }

  @Test
  public void testMultipleUnnecessaryParentsAreRemoved() {

    /*
       1---
      / \  \
     2   3  4
    /
   5---
   |   \
   6   7

    2 is allowed
    3 is denied
    4 is denied
    5 is allowed
    6 is allowed
    7 is allowed
    Portfolio should just contain 5, 6, 7

    */
    SimplePortfolioNode root =
        nodeTree(1,
                 nodeTree(2,
                          nodeTree(5,
                                   nodeTree(6),
                                   nodeTree(7))),
                 nodeTree(3),
                 nodeTree(4));
    Portfolio pf = new SimplePortfolio("node-1", root);

    Map<Integer, PortfolioPermission> permissions =
        ImmutableMap.<Integer, PortfolioPermission>builder()
            .put(1, ALLOW)
            .put(2, ALLOW)
            .put(3, DENY)
            .put(4, DENY)
            .put(5, ALLOW)
            .put(6, ALLOW)
            .put(7, ALLOW)
            .build();

    Portfolio portfolio = new NodeCheckingPortfolioFilter(createMappedNodeChecker(permissions)).generateRestrictedPortfolio(pf);

    assertThat(portfolio.getName(), is("node-5"));
    PortfolioNode rootNode = portfolio.getRootNode();
    assertThat(rootNode.getName(), is("node-5"));
    List<PortfolioNode> children = rootNode.getChildNodes();
    assertThat(children.size(), is(2));
    assertThat(children.get(0).getName(), is("node-6"));
    assertThat(children.get(1).getName(), is("node-7"));
  }


  @Test
  public void testDeepTreeIsProcessedCorrectly() {

    /*
        1---------
       /          \
      2---        3---
     /    \       / \  \
    4-     5     6  7  8---
    | \   / \   / \  \  \  \
    9 10 11 12 13 14 15 16 17

    2 is allowed
    3 is allowed
    4 is allowed
    5 is allowed
    6 is allowed
    7 is denied
    8 is allowed
    9 is allowed
    10 is allowed
    11 is denied
    12 is allowed
    13 is allowed
    14 is allowed
    15 is denied
    16 is denied
    17 is allowed

    Portfolio should contain 1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 17

    */
    SimplePortfolioNode root =
        nodeTree(1,
                 nodeTree(2,
                          nodeTree(4,
                                   nodeTree(9),
                                   nodeTree(10)),
                          nodeTree(5,
                                   nodeTree(11),
                                   nodeTree(12))),
                 nodeTree(3,
                          nodeTree(6,
                                   nodeTree(13),
                                   nodeTree(14)),
                          nodeTree(7,
                                   nodeTree(15)),
                          nodeTree(8,
                                   nodeTree(16),
                                   nodeTree(17))));

    Portfolio pf = new SimplePortfolio("node-1", root);

    Map<Integer, PortfolioPermission> permissions =
        ImmutableMap.<Integer, PortfolioPermission> builder()
          .put(1, ALLOW)
          .put(2, ALLOW)
          .put(3, ALLOW)
          .put(4, ALLOW)
          .put(5, ALLOW)
          .put(6, ALLOW)
          .put(7, DENY)
          .put(8, ALLOW)
          .put(9, ALLOW)
          .put(10, ALLOW)
          .put(11, DENY)
          .put(12, ALLOW)
          .put(13, ALLOW)
          .put(14, ALLOW)
          .put(15, DENY)
          .put(16, DENY)
          .build();

    Portfolio portfolio = new NodeCheckingPortfolioFilter(createMappedNodeChecker(permissions)).generateRestrictedPortfolio(pf);

    assertThat(portfolio.getName(), is("node-1 [restricted]"));
    PortfolioNode rootNode = portfolio.getRootNode();
    assertThat(rootNode.getName(), is("node-1 [restricted]"));
    List<PortfolioNode> n1children = rootNode.getChildNodes();
    assertThat(n1children.size(), is(2));

    PortfolioNode n2 = n1children.get(0);
    assertThat(n2.getName(), is("node-2 [restricted]"));
    PortfolioNode n3 = n1children.get(1);
    assertThat(n3.getName(), is("node-3 [restricted]"));

  }

}
