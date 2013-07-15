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

import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.InterpolatedCurveSpecification;
import com.opengamma.financial.analytics.fudgemsg.AnalyticsTestBase;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class CurveSpecificationBuildersTest extends AnalyticsTestBase {
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

  @Test
  public void testCurveSpecification() {
    final CurveSpecification specification = new CurveSpecification(LocalDate.of(2013, 1, 1), "NAME", NODES);
    assertEquals(specification, cycleObject(CurveSpecification.class, specification));
  }

  @Test
  public void testInterpolatedCurveSpecification() {
    final InterpolatedCurveSpecification specification = new InterpolatedCurveSpecification(LocalDate.of(2013, 1, 1), "NAME", NODES, "A", "B", "C");
    assertEquals(specification, cycleObject(InterpolatedCurveSpecification.class, specification));
  }
}
