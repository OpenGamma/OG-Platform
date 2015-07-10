/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.FixedDateInterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.fudgemsg.AnalyticsTestBase;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CurveDefinitionBuildersTest extends AnalyticsTestBase {

  /**
   * Tests the construction of curve definitions.
   */
  @Test
  public void testCurveDefinition() {
    final Set<CurveNode> nodes = new TreeSet<>();
    nodes.add(new CreditSpreadNode("X", Tenor.DAY));
    nodes.add(new CreditSpreadNode("X", Tenor.EIGHT_YEARS));
    nodes.add(new CreditSpreadNode("Y", Tenor.ONE_MONTH));
    CurveDefinition definition = new CurveDefinition("NAME", nodes);
    assertEquals(definition, cycleObject(CurveDefinition.class, definition));
    definition = new CurveDefinition("NAME", nodes);
    definition.setUniqueId(UniqueId.of("test", "id"));
    assertEquals(definition, cycleObject(CurveDefinition.class, definition));
  }

  /**
   * Tests the construction of all interpolated curve definitions.
   */
  @Test
  public void testInterpolatedCurveDefinitions() {
    final Set<CurveNode> nodes = new TreeSet<>();
    final String curveNodeIdMapperName = "Id mapper";
    final CashNode cash1w = new CashNode(Tenor.ONE_DAY, Tenor.ONE_WEEK, ExternalId.of("Test", "1W Cash"), curveNodeIdMapperName);
    final CashNode cash1m = new CashNode(Tenor.ONE_DAY, Tenor.ONE_MONTH, ExternalId.of("Test", "1m Cash"), curveNodeIdMapperName);
    final CashNode cash6m = new CashNode(Tenor.ONE_DAY, Tenor.SIX_MONTHS, ExternalId.of("Test", "6m Cash"), curveNodeIdMapperName);
    final CashNode cash12m = new CashNode(Tenor.ONE_DAY, Tenor.TWELVE_MONTHS, ExternalId.of("Test", "12m Cash"), curveNodeIdMapperName);
    final SwapNode swap2y = new SwapNode(Tenor.ONE_DAY, Tenor.TWO_YEARS, ExternalId.of("Test", "Pay leg 2y"), ExternalId.of("Test", "Receive leg 2y"), curveNodeIdMapperName);
    final SwapNode swap5y = new SwapNode(Tenor.ONE_DAY, Tenor.FIVE_YEARS, ExternalId.of("Test", "Pay leg 5y"), ExternalId.of("Test", "Receive leg 5y"), curveNodeIdMapperName);
    final SwapNode swap10y = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, ExternalId.of("Test", "Pay leg 10y"), ExternalId.of("Test", "Receive leg 10y"), curveNodeIdMapperName);
    nodes.add(cash1w);
    nodes.add(cash1m);
    nodes.add(cash6m);
    nodes.add(cash12m);
    nodes.add(swap2y);
    nodes.add(swap5y);
    nodes.add(swap10y);
    final String interpolatorName = "interpolator name";
    final String leftExtrapolatorName = "left extrapolator name";
    final String rightExtrapolatorName = "right extrapolator name";
    final String curveName = "NAME";
    InterpolatedCurveDefinition definition = new InterpolatedCurveDefinition(curveName, nodes, interpolatorName);
    definition.setUniqueId(UniqueId.of("test", "id1"));
    assertEquals(definition, cycleObject(InterpolatedCurveDefinition.class, definition));
    definition = new InterpolatedCurveDefinition(curveName, nodes, interpolatorName, rightExtrapolatorName);
    definition.setUniqueId(UniqueId.of("test", "id2"));
    assertEquals(definition, cycleObject(InterpolatedCurveDefinition.class, definition));
    definition = new InterpolatedCurveDefinition(curveName, nodes, interpolatorName, rightExtrapolatorName, leftExtrapolatorName);
    definition.setUniqueId(UniqueId.of("test", "id3"));
    assertEquals(definition, cycleObject(InterpolatedCurveDefinition.class, definition));
    final List<LocalDate> fixedDates = Arrays.asList(LocalDate.of(2013, 10, 1), LocalDate.of(2013, 11, 1), LocalDate.of(2014, 1, 1));
    FixedDateInterpolatedCurveDefinition fixedDateDefinition = new FixedDateInterpolatedCurveDefinition(curveName, nodes, interpolatorName, fixedDates);
    assertEquals(fixedDateDefinition, cycleObject(FixedDateInterpolatedCurveDefinition.class, fixedDateDefinition));
    fixedDateDefinition = new FixedDateInterpolatedCurveDefinition(curveName, nodes, interpolatorName, rightExtrapolatorName, fixedDates);
    assertEquals(fixedDateDefinition, cycleObject(FixedDateInterpolatedCurveDefinition.class, fixedDateDefinition));
    fixedDateDefinition = new FixedDateInterpolatedCurveDefinition(curveName, nodes, interpolatorName, rightExtrapolatorName, leftExtrapolatorName, fixedDates);
    assertEquals(fixedDateDefinition, cycleObject(FixedDateInterpolatedCurveDefinition.class, fixedDateDefinition));
  }

}
