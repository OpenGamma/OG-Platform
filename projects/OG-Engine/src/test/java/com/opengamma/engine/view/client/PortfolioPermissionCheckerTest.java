/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import static com.opengamma.engine.view.permission.PortfolioPermission.ALLOW;
import static com.opengamma.engine.view.permission.PortfolioPermission.DENY;
import static com.opengamma.engine.view.permission.PortfolioPermission.PARTIAL;
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
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.engine.view.permission.PortfolioPermission;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class PortfolioPermissionCheckerTest {

  @Test
  public void testNonExistentNodeIsDenied() {

    SimplePortfolioNode node = new SimplePortfolioNode("not in the portfolio");
    PortfolioPermissionChecker checker = new PortfolioPermissionChecker(new SimplePortfolio("test"), createAllowNodeChecker());
    assertThat(checker.permissionCheck(node), is(DENY));
  }

  @Test
  public void testSingleNodeWithAllow() {

    SimplePortfolioNode node = new SimplePortfolioNode();

    PortfolioPermissionChecker checker = new PortfolioPermissionChecker(new SimplePortfolio("test", node),
                                                                        createAllowNodeChecker());

    assertThat(checker.permissionCheck(node), is(ALLOW));
  }

  @Test
  public void testSingleNodeWithDeny() {

    SimplePortfolioNode node = new SimplePortfolioNode();

    PortfolioPermissionChecker checker = new PortfolioPermissionChecker(new SimplePortfolio("test", node),
                                                                        createDenyNodeChecker());

    assertThat(checker.permissionCheck(node), is(DENY));
  }

  @Test
  public void testAllChildrenAllowMeansParentAllows() {

    SimplePortfolioNode root =
        nodeTree(1,
             nodeTree(2),
             nodeTree(3),
             nodeTree(4));

    Map<Integer, PortfolioPermission> permissions = ImmutableMap.of(
        1, ALLOW,
        2, ALLOW,
        3, ALLOW,
        4, ALLOW);

    PortfolioPermissionChecker checker = new PortfolioPermissionChecker(new SimplePortfolio("test", root),
                                                                        createMappedNodeChecker(permissions));

    assertThat(checker.permissionCheck(root), is(ALLOW));
  }

  @Test
  public void testAllChildrenDenyMeansParentPartials() {

    SimplePortfolioNode root =
        nodeTree(1,
                 nodeTree(2),
                 nodeTree(3),
                 nodeTree(4));

    Map<Integer, PortfolioPermission> permissions = ImmutableMap.of(
        1, ALLOW,
        2, DENY,
        3, DENY,
        4, DENY);

    PortfolioPermissionChecker checker = new PortfolioPermissionChecker(new SimplePortfolio("test", root),
                                                                        createMappedNodeChecker(permissions));

    assertThat(checker.permissionCheck(root), is(PARTIAL));
  }

  @Test
  public void testChildrenAllowAndDenyMeansParentPartials() {

    SimplePortfolioNode root =
        nodeTree(1,
                 nodeTree(2),
                 nodeTree(3),
                 nodeTree(4));

    Map<Integer, PortfolioPermission> permissions = ImmutableMap.of(
        1, ALLOW,
        2, ALLOW,
        3, DENY,
        4, ALLOW);

    PortfolioPermissionChecker checker = new PortfolioPermissionChecker(new SimplePortfolio("test", root),
                                                                        createMappedNodeChecker(permissions));

    assertThat(checker.permissionCheck(root), is(PARTIAL));

  }

  @Test
  public void testParentsDenialOverridesChildren() {

    SimplePortfolioNode root =
        nodeTree(1,
                 nodeTree(2),
                 nodeTree(3),
                 nodeTree(4));

    Map<Integer, PortfolioPermission> permissions = ImmutableMap.of(
        1, DENY,
        2, ALLOW,
        3, ALLOW,
        4, ALLOW);

    PortfolioPermissionChecker checker = new PortfolioPermissionChecker(new SimplePortfolio("test", root),
                                                                        createMappedNodeChecker(permissions));

    assertThat(checker.permissionCheck(root), is(DENY));
  }

  @Test
  public void testMultilevelTree() {

    SimplePortfolioNode root =
        nodeTree(1,
           nodeTree(2,
              nodeTree(5),
              nodeTree(6)),
           nodeTree(3,
              nodeTree(7)),
           nodeTree(4,
              nodeTree(8),
              nodeTree(9)));

    // When we add child nodes, it's possible they are copied,
    // so we can't just pull these vars out as we construct
    // the tree
    List<PortfolioNode> childNodes = root.getChildNodes();
    PortfolioNode node2 = childNodes.get(0);
    PortfolioNode node3 = childNodes.get(1);
    PortfolioNode node4 = childNodes.get(2);

    Map< Integer, PortfolioPermission> permissions =
        ImmutableMap.<Integer, PortfolioPermission>builder()
            .put(1, ALLOW)
            .put(2, ALLOW)
            .put(3, DENY)
            .put(4, ALLOW)
            .put(5, DENY)
            .put(6, ALLOW)
            .put(7, ALLOW)
            .put(8, ALLOW)
            .put(9, ALLOW)
        .build();

    PortfolioPermissionChecker checker = new PortfolioPermissionChecker(new SimplePortfolio("test", root),
                                                                        createMappedNodeChecker(permissions));

    assertThat(checker.permissionCheck(root), is(PARTIAL));
    assertThat(checker.permissionCheck(node2), is(PARTIAL));
    assertThat(checker.permissionCheck(node3), is(DENY));
    assertThat(checker.permissionCheck(node4), is(ALLOW));
  }

}
