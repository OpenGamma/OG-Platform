/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link DefaultComputationTargetTypeProvider} class.
 */
@Test(groups = TestGroup.UNIT)
public class DefaultComputationTargetTypeProviderTest {

  public void testGetSimpleTypes() {
    final DefaultComputationTargetTypeProvider provider = new DefaultComputationTargetTypeProvider();
    final Set<ComputationTargetType> types = new HashSet<ComputationTargetType>(provider.getSimpleTypes());
    assertEquals(types, ImmutableSet.<ComputationTargetType>of(ComputationTargetType.PORTFOLIO, ComputationTargetType.PORTFOLIO_NODE, ComputationTargetType.POSITION, ComputationTargetType.TRADE,
        ComputationTargetType.SECURITY, ComputationTargetType.PRIMITIVE, ComputationTargetType.CURRENCY, ComputationTargetType.UNORDERED_CURRENCY_PAIR,
        ComputationTargetType.CREDIT_CURVE_IDENTIFIER));
  }

  public void testGetAdditionalTypes() {
    final DefaultComputationTargetTypeProvider provider = new DefaultComputationTargetTypeProvider();
    final Set<ComputationTargetType> types = new HashSet<ComputationTargetType>(provider.getAdditionalTypes());
    assertEquals(types, ImmutableSet.<ComputationTargetType>of(ComputationTargetType.POSITION_OR_TRADE, ComputationTargetType.LEGACY_PRIMITIVE));
  }

  public void testGetAllTypes() {
    final DefaultComputationTargetTypeProvider provider = new DefaultComputationTargetTypeProvider();
    final Set<ComputationTargetType> types = new HashSet<ComputationTargetType>(provider.getAllTypes());
    assertEquals(types, ImmutableSet.<ComputationTargetType>of(ComputationTargetType.PORTFOLIO, ComputationTargetType.PORTFOLIO_NODE, ComputationTargetType.POSITION, ComputationTargetType.TRADE,
        ComputationTargetType.SECURITY, ComputationTargetType.PRIMITIVE, ComputationTargetType.CURRENCY, ComputationTargetType.UNORDERED_CURRENCY_PAIR, ComputationTargetType.POSITION_OR_TRADE,
        ComputationTargetType.LEGACY_PRIMITIVE, ComputationTargetType.CREDIT_CURVE_IDENTIFIER));
  }

}
