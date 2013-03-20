/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Comparator;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class ObligorRedCodeAggregatorTest {

  public static final ObligorRedCodeAggregator AGGREGATOR = new ObligorRedCodeAggregator();

  @Test
  public void testNameIsDefined() {
    assertEquals(AGGREGATOR.getName(), "RED Codes");
  }

  @Test
  public void testNoRequiredEntries() {
    assertEquals(AGGREGATOR.getRequiredEntries().isEmpty(), true);
  }

  @Test
  public void testSimpleStringComparisonIsUsed() {
    assertEquals(AGGREGATOR.compare("39FF64", "6H27C2") < 0, true);
    assertEquals(AGGREGATOR.compare("6H27C2", "6H27C2"), 0);
    assertEquals(AGGREGATOR.compare("6H27C2", "39FF64") > 0, true);
  }

  @Test
  public void testPositionComparatorUsesRedCodes() {

    Position p1 = new SimplePosition(BigDecimal.ONE, ExternalSchemes.redCode("39FF64"));
    Position p2 = new SimplePosition(BigDecimal.ONE, ExternalSchemes.redCode("6H27C2"));

    Comparator<Position> comparator = AGGREGATOR.getPositionComparator();

    assertEquals(comparator.compare(p1, p2) < 0, true);
    assertEquals(comparator.compare(p1, p1), 0);
    assertEquals(comparator.compare(p2, p1) > 0, true);
  }

  @Test
  public void testPositionSecurityWithoutRedCodeIsIgnored() {

    Position posn = new SimplePosition(BigDecimal.ONE, ExternalSchemes.bloombergBuidSecurityId("123456"));
    assertEquals(AGGREGATOR.classifyPosition(posn), "N/A");
  }

  @Test
  public void testPositionSecurityWithRedCodeIsUsed() {

    Position p1 = new SimplePosition(BigDecimal.ONE, ExternalSchemes.redCode("39FF64"));
    Position p2 = new SimplePosition(BigDecimal.ONE, ExternalSchemes.redCode("6H27C2"));
    assertEquals(AGGREGATOR.classifyPosition(p1), "39FF64");
    assertEquals(AGGREGATOR.classifyPosition(p2), "6H27C2");
  }

}