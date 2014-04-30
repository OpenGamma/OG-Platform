/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Set;
import java.util.TreeSet;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.curve.AbstractCurveSpecification;
import com.opengamma.financial.analytics.curve.ConstantCurveSpecification;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.InterpolatedCurveSpecification;
import com.opengamma.financial.analytics.curve.SpreadCurveSpecification;
import com.opengamma.financial.analytics.fudgemsg.AnalyticsTestBase;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Tests all curve specification Fudge builders.
 */
@Test(groups = TestGroup.UNIT)
public class CurveSpecificationBuildersTest extends AnalyticsTestBase {
  /** A set of nodes */
  private static final Set<CurveNodeWithIdentifier> NODES = new TreeSet<>();

  static {
    NODES.add(new CurveNodeWithIdentifier(new CreditSpreadNode("X", Tenor.DAY), ExternalId.of("Test", "A"), "X", DataFieldType.OUTRIGHT));
    NODES.add(new CurveNodeWithIdentifier(new CreditSpreadNode("Y", Tenor.ONE_MONTH), ExternalId.of("Test", "B"), "Y", DataFieldType.POINTS));
    NODES.add(new CurveNodeWithIdentifier(new CreditSpreadNode("Z", Tenor.ONE_YEAR), ExternalId.of("Test", "C"), "Z", DataFieldType.OUTRIGHT));
    NODES.add(new CurveNodeWithIdentifier(new CashNode(Tenor.DAY, Tenor.ONE_WEEK, ExternalId.of("Test convention", "convention1"), "Test"),
        ExternalId.of("Test", "D"), "A", DataFieldType.OUTRIGHT));
    NODES.add(new CurveNodeWithIdentifier(new CashNode(Tenor.TWO_DAYS, Tenor.TWO_WEEKS, ExternalId.of("Test convention", "convention2"), "Test"),
        ExternalId.of("Test", "E"), "B", DataFieldType.POINTS));
    NODES.add(new CurveNodeWithIdentifier(new CashNode(Tenor.TWO_DAYS, Tenor.THREE_WEEKS, ExternalId.of("Test convention", "convention3"), "Test"),
        ExternalId.of("Test", "F"), "C", DataFieldType.OUTRIGHT));
  }

  /**
   * Tests cycling of curve specifications.
   */
  @Test
  public void testCurveSpecification() {
    final CurveSpecification specification = new CurveSpecification(LocalDate.of(2013, 1, 1), "NAME", NODES);
    assertEquals(specification, cycleObject(CurveSpecification.class, specification));
  }

  /**
   * Tests cycling of interpolated curve specifications
   */
  @Test
  public void testInterpolatedCurveSpecification() {
    final InterpolatedCurveSpecification specification = new InterpolatedCurveSpecification(LocalDate.of(2013, 1, 1), "NAME", NODES, "A", "B", "C");
    assertEquals(specification, cycleObject(InterpolatedCurveSpecification.class, specification));
  }

  /**
   * Tests cycling of constant curve specifications
   */
  @Test
  public void testConstantCurveSpecification() {
    ConstantCurveSpecification specification = new ConstantCurveSpecification(LocalDate.of(2013, 1, 1), "NAME", ExternalSchemes.activFeedTickerSecurityId("A"), MarketDataRequirementNames.ALL);
    assertEquals(specification, cycleObject(ConstantCurveSpecification.class, specification));
    specification = new ConstantCurveSpecification(LocalDate.of(2013, 1, 1), "NAME", ExternalSchemes.activFeedTickerSecurityId("A"), MarketDataRequirementNames.MARKET_VALUE);
    assertEquals(specification, cycleObject(ConstantCurveSpecification.class, specification));
    final ConstantCurveSpecification other = new ConstantCurveSpecification(LocalDate.of(2013, 1, 1), "NAME", ExternalSchemes.activFeedTickerSecurityId("A"), null);
    assertEquals(specification, cycleObject(ConstantCurveSpecification.class, other));
  }

  /**
   * Tests cycling of spread curve specifications
   */
  @Test
  public void testSpreadCurveSpecification() {
    final LocalDate date = LocalDate.of(2013, 1, 1);
    final AbstractCurveSpecification constant = new ConstantCurveSpecification(date, "C", ExternalSchemes.bloombergTickerSecurityId("A"), null);
    final AbstractCurveSpecification interpolated = new InterpolatedCurveSpecification(date, "R", NODES, "B", "F", "G");
    final SpreadCurveSpecification spread1 = new SpreadCurveSpecification(date, "I", constant, interpolated, "+");
    assertEquals(spread1, cycleObject(SpreadCurveSpecification.class, spread1));
    final SpreadCurveSpecification spread2 = new SpreadCurveSpecification(date, "D", spread1, interpolated, "-");
    assertEquals(spread2, cycleObject(SpreadCurveSpecification.class, spread2));
  }
}
