/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.fudgemsg.AnalyticsTestBase;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CurveNodeBuildersTest extends AnalyticsTestBase {

  @Test
  public void testCreditCurveNodeBuilder() {
    final CreditSpreadNode node = new CreditSpreadNode("TEST", Tenor.EIGHT_MONTHS);
    assertEquals(node, cycleObject(CreditSpreadNode.class, node));
  }

  @Test
  public void testCashNodeBuilder() {
    final CashNode node = new CashNode(Tenor.ONE_DAY, Tenor.ONE_YEAR, ExternalId.of("convention", "name"), "TEST");
    assertEquals(node, cycleObject(CashNode.class, node));
  }

  @Test
  public void testFRANodeBuilder() {
    final FRANode node = new FRANode(Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, ExternalId.of("convention", "name"), "TEST");
    assertEquals(node, cycleObject(FRANode.class, node));
  }

  @Test
  public void testRateFutureNodeBuilder() {
    final RateFutureNode node = new RateFutureNode(2, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.ONE_MONTH, ExternalId.of("convention", "future"), ExternalId.of("convention", "underlying"), "TEST");
    assertEquals(node, cycleObject(RateFutureNode.class, node));
  }

  @Test
  public void testCurveNodeWithIdentifiers() {
    final CreditSpreadNode node = new CreditSpreadNode("TEST", Tenor.EIGHT_YEARS);
    final CurveNodeWithIdentifier nodeWithId = new CurveNodeWithIdentifier(node, ExternalSchemes.bloombergTickerSecurityId("AAA"));
    assertEquals(nodeWithId, cycleObject(CurveNodeWithIdentifier.class, nodeWithId));
  }

}
