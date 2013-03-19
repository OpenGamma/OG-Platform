/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.comparison;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PositionSetComparisonTest extends AbstractTest {

  private static final Logger s_logger = LoggerFactory.getLogger(PositionSetComparisonTest.class);

  private Set<Position> createPositionSetA() {
    final Set<Position> set = new HashSet<Position>();
    Security security;
    set.add(createPosition("A1", 10, createRawSecurity("1", 42), "P1", "P2", null, null));
    security = createSwaptionSecurity("3", true, Currency.USD, ExternalId.of("Underlying", "3"));
    set.add(createPosition("A3", 30, security, "P1", "P2", createTrade(20, security, "T1", "T2"), createTrade(10, security, "T1", "T2")));
    security = createFRASecurity("4", Currency.USD, 42d, ExternalId.of("Underlying", "4"));
    set.add(createPosition("A4", 40, security, "P1", "P2", createTrade(30, security, null, null), createTrade(10, security, null, null)));
    security = createRawSecurity("5", 42);
    security = createRawSecurity("6", 42);
    set.add(createPosition("A6", 60, security, null, null, createTrade(40, security, null, null), createTrade(20, security, null, null)));
    return set;
  }

  private Set<Position> createPositionSetB() {
    final Set<Position> set = new HashSet<Position>();
    Security security;
    set.add(createPosition("B2", 20, createEquityOptionSecurity("2", OptionType.CALL, 42d, ExternalId.of("Underlying", "2")), null, null, null, null));
    security = createSwaptionSecurity("3", true, Currency.USD, ExternalId.of("Underlying", "3"));
    set.add(createPosition("B3", 30, security, "P1", "P2", createTrade(20, security, "T1", "T2"), createTrade(10, security, "T1", "T2")));
    security = createRawSecurity("5", 42);
    set.add(createPosition("B5", 50, security, null, null, createTrade(30, security, "T1", "T2"), createTrade(20, security, "T1", "T2")));
    security = createRawSecurity("6", 42);
    set.add(createPosition("B6", 60, security, null, null, createTrade(40, security, null, null), createTrade(20, security, null, null)));
    return set;
  }

  private static void expect(final Collection<Position> positions, final Collection<String> positionIdentifiers) {
    final HashSet<String> found = new HashSet<String>(positionIdentifiers);
    for (Position position : positions) {
      if (!found.remove(position.getUniqueId().getValue())) {
        throw new AssertionError("Found " + position + ", not listed in " + positionIdentifiers);
      }
    }
    if (!found.isEmpty()) {
      throw new AssertionError("Expected positions " + positionIdentifiers + " missing");
    }
  }

  private static void assertACompareB(final PositionSetComparison result) {
    s_logger.debug("A.B = {}", result);
    // Check the identical positions
    expect(result.getIdentical(), Arrays.asList("A3", "A6"));
    // Check those present in A but not B
    expect(result.getOnlyInFirst(), Arrays.asList("A1", "A4"));
    // Check those present in B but not A
    expect(result.getOnlyInSecond(), Arrays.asList("B2", "B5"));
    // Check those that have "changed" - none
    assertTrue(result.getChanged().isEmpty());
  }

  public void testIterable() {
    final PositionSetComparator comparator = new PositionSetComparator(OpenGammaFudgeContext.getInstance());
    assertACompareB(comparator.compare((Iterable<Position>) createPositionSetA(), (Iterable<Position>) createPositionSetB()));
  }

  public void testACompareB() {
    final PositionSetComparator comparator = new PositionSetComparator(OpenGammaFudgeContext.getInstance());
    assertACompareB(comparator.compare(createPositionSetA(), createPositionSetB()));
  }

  public void testCompareEmpty() {
    final Set<Position> a = createPositionSetA();
    final PositionSetComparator comparator = new PositionSetComparator(OpenGammaFudgeContext.getInstance());
    final PositionSetComparison result = comparator.compare(a, Collections.<Position>emptySet());
    s_logger.debug("A.e = {}", result);
    assertFalse(result.isEqual());
    assertTrue(result.getChanged().isEmpty());
    assertEquals(new HashSet<Position>(result.getOnlyInFirst()), a);
    assertTrue(result.getOnlyInSecond().isEmpty());
    assertTrue(result.getIdentical().isEmpty());
  }

  public void testEqualIdentity() {
    final Set<Position> a = createPositionSetA();
    final PositionSetComparator comparator = new PositionSetComparator(OpenGammaFudgeContext.getInstance());
    final PositionSetComparison result = comparator.compare(a, a);
    s_logger.debug("A.A = {}", result);
    assertNotNull(result);
    assertTrue(result.isEqual());
    assertTrue(result.getChanged().isEmpty());
    assertTrue(result.getOnlyInFirst().isEmpty());
    assertTrue(result.getOnlyInSecond().isEmpty());
    assertEquals(new HashSet<Position>(result.getIdentical()), a);
  }

  public void testEqualObject() {
    final Set<Position> a1 = createPositionSetA();
    final Set<Position> a2 = createPositionSetA();
    final PositionSetComparator comparator = new PositionSetComparator(OpenGammaFudgeContext.getInstance());
    final PositionSetComparison result = comparator.compare(a1, a2);
    s_logger.debug("A.A' = {}", result);
    assertNotNull(result);
    assertTrue(result.isEqual());
    assertTrue(result.getChanged().isEmpty());
    assertTrue(result.getOnlyInFirst().isEmpty());
    assertTrue(result.getOnlyInSecond().isEmpty());
    assertEquals(result.getIdentical().size(), a1.size());
  }

  public void testEqualsEmpty() {
    final PositionSetComparator comparator = new PositionSetComparator(OpenGammaFudgeContext.getInstance());
    final PositionSetComparison result = comparator.compare(Collections.<Position>emptySet(), Collections.<Position>emptySet());
    s_logger.debug("e.e = {}", result);
    assertNotNull(result);
    assertTrue(result.isEqual());
    assertTrue(result.getChanged().isEmpty());
    assertTrue(result.getOnlyInFirst().isEmpty());
    assertTrue(result.getOnlyInSecond().isEmpty());
    assertTrue(result.getIdentical().isEmpty());
  }

  public void testCompareIgnoreAttributes() {
    final Set<Position> setA = new HashSet<Position>();
    final Security security = createRawSecurity("1", 42);
    setA.add(createPosition("A1", 10, security, "P1", "P2", null, null));
    setA.add(createPosition("A2", 20, security, "P1", "P2", createTrade(10, security, "T1", "T2"), createTrade(10, security, "T3", "T4")));
    setA.add(createPosition("A3", 20, security, null, null, createTrade(10, security, "T1", "T2"), createTrade(10, security, "T3", "T4")));
    final Set<Position> setB = new HashSet<Position>();
    setB.add(createPosition("B1", 10, security, null, null, null, null));
    setB.add(createPosition("B2", 20, security, "P5", "P6", createTrade(10, security, "T3", "T1"), createTrade(10, security, null, null)));
    setB.add(createPosition("B3", 20, security, null, null, createTrade(10, security, "T1", "T2"), createTrade(10, security, "T3", "T4")));
    setB.add(createPosition("B4", 20, security, "P1", "P2", createTrade(10, security, "T3", "T1"), createTrade(10, security, null, null)));
    final PositionSetComparator comparator = new PositionSetComparator(OpenGammaFudgeContext.getInstance());
    PositionSetComparison result = comparator.compare(setA, setB);
    expect(result.getIdentical(), Arrays.asList("A3"));
    comparator.setIgnorePositionAttributes(true);
    result = comparator.compare(setA, setB);
    expect(result.getIdentical(), Arrays.asList("A1", "A2", "A3"));
    result = comparator.compare(setB, setA);
    expect(result.getIdentical(), Arrays.asList("B1", "B3"));
    comparator.setIgnorePositionAttributes(false);
    comparator.setIgnoreTradeAttributes(true);
    result = comparator.compare(setA, setB);
    expect(result.getIdentical(), Arrays.asList("A2", "A3"));
    result = comparator.compare(setB, setA);
    expect(result.getIdentical(), Arrays.asList("B3", "B4"));
    comparator.setIgnorePositionAttributes(true);
    result = comparator.compare(setA, setB);
    expect(result.getIdentical(), Arrays.asList("A1", "A2", "A3"));
    result = comparator.compare(setB, setA);
    expect(result.getIdentical(), Arrays.asList("B1", "B2", "B3", "B4"));
  }

}
