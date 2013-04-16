/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.math.BigDecimal;
import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ComputationTargetResolverUtils} class.
 */
@Test(groups = TestGroup.UNIT)
public class ComputationTargetResolverUtilsTest {

  private static final SimplePortfolioNode NODE = new SimplePortfolioNode(UniqueId.of("A", "B"), "Name");
  private static final Position POSITION = new SimplePosition(UniqueId.of("Test", "1"), new BigDecimal(1), ExternalIdBundle.EMPTY);

  public void testCreateResolvedTarget() {
    // No re-write
    ComputationTarget target = ComputationTargetResolverUtils.createResolvedTarget(
        new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, NODE.getUniqueId()).containing(ComputationTargetType.POSITION, POSITION.getUniqueId()), POSITION);
    assertEquals(target.getType(), ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.of(SimplePosition.class)));
    assertEquals(target.getContextIdentifiers(), Arrays.asList(NODE.getUniqueId()));
    assertSame(target.getValue(), POSITION);
    // Rewrite to remove the union type
    target = ComputationTargetResolverUtils.createResolvedTarget(new ComputationTargetSpecification(ComputationTargetType.POSITION.or(ComputationTargetType.TRADE), POSITION.getUniqueId()), POSITION);
    assertEquals(target.getType(), ComputationTargetType.of(SimplePosition.class));
    assertEquals(target.getContextIdentifiers(), null);
    assertSame(target.getValue(), POSITION);
    // Rewrite the nested type
    target = ComputationTargetResolverUtils.createResolvedTarget(
        new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, NODE.getUniqueId()).containing(ComputationTargetType.POSITION.or(ComputationTargetType.TRADE),
            POSITION.getUniqueId()), POSITION);
    assertEquals(target.getType(), ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.of(SimplePosition.class)));
    assertEquals(target.getContextIdentifiers(), Arrays.asList(NODE.getUniqueId()));
    assertSame(target.getValue(), POSITION);
    target = ComputationTargetResolverUtils.createResolvedTarget(
        new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, NODE.getUniqueId()).containing(ComputationTargetType.PORTFOLIO_NODE, NODE.getUniqueId()).containing(
            ComputationTargetType.POSITION.or(ComputationTargetType.TRADE), POSITION.getUniqueId()), POSITION);
    assertEquals(target.getType(), ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.PORTFOLIO_NODE).containing(ComputationTargetType.of(SimplePosition.class)));
    assertEquals(target.getContextIdentifiers(), Arrays.asList(NODE.getUniqueId(), NODE.getUniqueId()));
    assertSame(target.getValue(), POSITION);
  }

}
